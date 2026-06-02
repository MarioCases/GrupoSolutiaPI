package com.example.portalinternogruposolutia;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portalinternogruposolutia.adapter.ProjectAdapter;
import com.example.portalinternogruposolutia.model.Project;
import com.example.portalinternogruposolutia.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_FORM = 100;

    private RecyclerView recycler;
    private ProjectAdapter adapter;
    private TextView emptyState;
    private TextView resultsCount;
    private EditText searchInput;
    private LinearLayout statusChipsContainer;
    private LinearLayout techChipsContainer;
    private FloatingActionButton fabAdd;

    private List<Project> allProjects;
    private List<User> users;
    private User currentUser;

    private String currentStatusFilter = "all";
    private String currentTechFilter = "all";
    private String searchQuery = "";
    private int nextProjectId = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        allProjects = MockData.getProjects();
        users = MockData.getUsers();
        currentUser = users.get(0);

        nextProjectId = allProjects.stream().mapToInt(Project::getId).max().orElse(10) + 1;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            updateToolbarSubtitle();
        }

        toolbar.setOnClickListener(v -> showUserSwitchDialog());

        recycler = findViewById(R.id.recyclerProjects);
        emptyState = findViewById(R.id.emptyState);
        resultsCount = findViewById(R.id.resultsCount);
        searchInput = findViewById(R.id.searchInput);
        statusChipsContainer = findViewById(R.id.statusChips);
        techChipsContainer = findViewById(R.id.techChips);
        fabAdd = findViewById(R.id.fabAdd);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProjectAdapter(getFilteredList(), isReadOnly());
        recycler.setAdapter(adapter);

        adapter.setOnProjectClickListener(this::onEditProject);
        adapter.setOnProjectLongClickListener(this::onDeleteProject);
        adapter.setOnDocumentClickListener(this::onManageDocuments);

        setupSearch();
        setupStatusChips();
        setupTechChips();
        updateResultsCount(allProjects.size());
        updateFabVisibility();

        fabAdd.setOnClickListener(v -> onAddProject());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FORM && resultCode == RESULT_OK && data != null) {
            boolean deleted = data.getBooleanExtra(ProjectFormActivity.RESULT_DELETED, false);
            int editId = data.getIntExtra(ProjectFormActivity.EXTRA_PROJECT_ID, -1);

            if (deleted && editId >= 0) {
                allProjects.removeIf(p -> p.getId() == editId);
                updateTechChips();
                applyFilters();
                Toast.makeText(this, "Proyecto eliminado", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = data.getStringExtra(ProjectFormActivity.RESULT_NAME);
            String description = data.getStringExtra(ProjectFormActivity.RESULT_DESCRIPTION);
            String status = data.getStringExtra(ProjectFormActivity.RESULT_STATUS);
            String techText = data.getStringExtra(ProjectFormActivity.RESULT_TECHNOLOGIES);
            String department = data.getStringExtra(ProjectFormActivity.RESULT_DEPARTMENT);
            String statusLabel = data.getStringExtra("statusLabel");
            ArrayList<String> documents = data.getStringArrayListExtra(ProjectFormActivity.RESULT_DOCUMENTS);

            List<String> technologies = techText == null || techText.isEmpty()
                ? new ArrayList<>()
                : Arrays.stream(techText.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (editId >= 0) {
                for (Project p : allProjects) {
                    if (p.getId() == editId) {
                        p.setName(name);
                        p.setDescription(description);
                        p.setStatus(status);
                        p.setStatusLabel(statusLabel);
                        p.setTechnologies(technologies);
                        p.setDepartment(department);
                        p.setDocuments(documents != null ? documents : new ArrayList<>());
                        break;
                    }
                }
            } else {
                Project project = new Project(nextProjectId++, name, description,
                    status, statusLabel, technologies, department);
                if (documents != null) project.setDocuments(documents);
                allProjects.add(project);
            }

            updateTechChips();
            applyFilters();
        }
    }

    private void onAddProject() {
        if (!canEdit()) {
            Toast.makeText(this, "No tienes permiso para añadir proyectos", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ProjectFormActivity.class);
        intent.putExtra(ProjectFormActivity.EXTRA_MODE, ProjectFormActivity.MODE_ADD);
        intent.putExtra("role", currentUser.getRole());
        startActivityForResult(intent, REQUEST_FORM);
    }

    private void onEditProject(Project project) {
        if (isReadOnly()) {
            showProjectDetail(project);
            return;
        }
        Intent intent = new Intent(this, ProjectFormActivity.class);
        intent.putExtra(ProjectFormActivity.EXTRA_MODE, ProjectFormActivity.MODE_EDIT);
        intent.putExtra(ProjectFormActivity.EXTRA_PROJECT_ID, project.getId());
        intent.putExtra("role", currentUser.getRole());
        intent.putExtra(ProjectFormActivity.RESULT_NAME, project.getName());
        intent.putExtra(ProjectFormActivity.RESULT_DESCRIPTION, project.getDescription());
        intent.putExtra(ProjectFormActivity.RESULT_STATUS, project.getStatus());
        intent.putExtra(ProjectFormActivity.RESULT_TECHNOLOGIES,
            String.join(", ", project.getTechnologies()));
        intent.putExtra(ProjectFormActivity.RESULT_DEPARTMENT, project.getDepartment());
        intent.putStringArrayListExtra(ProjectFormActivity.RESULT_DOCUMENTS,
            new ArrayList<>(project.getDocuments()));
        startActivityForResult(intent, REQUEST_FORM);
    }

    private void showProjectDetail(Project p) {
        StringBuilder msg = new StringBuilder();
        msg.append("Estado: ").append(p.getStatusLabel());
        msg.append("\nDescripción: ").append(p.getDescription());
        msg.append("\nTecnologías: ").append(String.join(", ", p.getTechnologies()));
        msg.append("\nDepartamento: ").append(p.getDepartment());
        if (p.getDocumentCount() > 0) {
            msg.append("\n\nDocumentos:");
            for (String doc : p.getDocuments()) {
                msg.append("\n  • ").append(doc.split("\\|\\|\\|")[0]);
            }
        }

        new AlertDialog.Builder(this)
            .setTitle(p.getName())
            .setMessage(msg.toString())
            .setPositiveButton("Cerrar", null)
            .show();
    }

    private boolean onDeleteProject(Project project) {
        if (!isAdmin()) {
            Toast.makeText(this, "Solo el administrador puede eliminar proyectos", Toast.LENGTH_SHORT).show();
            return true;
        }
        return true;
    }

    private void onManageDocuments(Project project) {
        if (isReadOnly()) {
            StringBuilder msg = new StringBuilder("Documentos:");
            if (project.getDocumentCount() == 0) {
                msg = new StringBuilder("Este proyecto no tiene documentos asociados.");
            } else {
                for (String doc : project.getDocuments()) {
                    msg.append("\n  • ").append(doc.split("\\|\\|\\|")[0]);
                }
            }
            new AlertDialog.Builder(this)
                .setTitle("Documentos - " + project.getName())
                .setMessage(msg.toString())
                .setPositiveButton("Cerrar", null)
                .show();
            return;
        }

        onEditProject(project);
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                searchQuery = s.toString().toLowerCase().trim();
                applyFilters();
            }
        });
    }

    private void setupStatusChips() {
        String[][] statuses = {
            {"all", "Todos"},
            {"production", "Producción"},
            {"development", "En Desarrollo"},
            {"maintenance", "Mantenimiento"}
        };

        for (String[] s : statuses) {
            View chip = createChip(s[1], s[0].equals("all"));
            chip.setTag(s[0]);
            chip.setOnClickListener(v -> {
                String val = (String) v.getTag();
                currentStatusFilter = currentStatusFilter.equals(val) ? "all" : val;
                updateChipSelection(statusChipsContainer, currentStatusFilter);
                applyFilters();
            });
            statusChipsContainer.addView(chip);
        }
    }

    private void setupTechChips() {
        techChipsContainer.removeAllViews();
        View allChip = createChip("Todas", true);
        allChip.setTag("all");
        allChip.setOnClickListener(v -> {
            currentTechFilter = "all";
            updateChipSelection(techChipsContainer, "all");
            applyFilters();
        });
        techChipsContainer.addView(allChip);

        for (String tech : MockData.getTechnologies(allProjects)) {
            View chip = createChip(tech, false);
            chip.setTag(tech);
            chip.setOnClickListener(v -> {
                String val = (String) v.getTag();
                currentTechFilter = currentTechFilter.equals(val) ? "all" : val;
                updateChipSelection(techChipsContainer, currentTechFilter);
                applyFilters();
            });
            techChipsContainer.addView(chip);
        }
    }

    private void updateTechChips() {
        currentTechFilter = "all";
        setupTechChips();
    }

    private View createChip(String text, boolean selected) {
        TextView chip = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.item_chip, statusChipsContainer, false);
        chip.setText(text);
        chip.setSelected(selected);
        chip.setBackgroundResource(selected ? R.drawable.bg_chip_filter_active : R.drawable.bg_chip_filter);
        chip.setTextColor(ContextCompat.getColorStateList(this, R.color.chip_text_color));
        return chip;
    }

    private void updateChipSelection(LinearLayout container, String activeValue) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            boolean selected = child.getTag().equals(activeValue);
            child.setSelected(selected);
            child.setBackgroundResource(selected ? R.drawable.bg_chip_filter_active : R.drawable.bg_chip_filter);
        }
    }

    private List<Project> getFilteredList() {
        return allProjects.stream()
            .filter(p -> currentStatusFilter.equals("all") || p.getStatus().equals(currentStatusFilter))
            .filter(p -> currentTechFilter.equals("all") || p.getTechnologies().contains(currentTechFilter))
            .filter(p -> searchQuery.isEmpty() || p.getName().toLowerCase().contains(searchQuery))
            .collect(Collectors.toList());
    }

    private void applyFilters() {
        List<Project> filtered = getFilteredList();
        adapter.updateList(filtered, isReadOnly());
        boolean empty = filtered.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        updateResultsCount(filtered.size());
    }

    private void updateResultsCount(int count) {
        String text = count + " resultado" + (count != 1 ? "s" : "");
        resultsCount.setText(text);
    }

    private void updateToolbarSubtitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(currentUser.getName() + " · " + currentUser.getRoleLabel());
        }
    }

    private void updateFabVisibility() {
        fabAdd.setVisibility(canEdit() ? View.VISIBLE : View.GONE);
    }

    private void showUserSwitchDialog() {
        String[] names = new String[users.size()];
        int selected = 0;

        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            names[i] = u.getName() + " (" + u.getRoleLabel() + ")";
            if (u.getId() == currentUser.getId()) selected = i;
        }

        new AlertDialog.Builder(this)
            .setTitle("Seleccionar usuario")
            .setSingleChoiceItems(names, selected, (dialog, which) -> {
                currentUser = users.get(which);
                updateToolbarSubtitle();
                updateFabVisibility();
                adapter.updateList(getFilteredList(), isReadOnly());
                dialog.dismiss();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private boolean isReadOnly() {
        return currentUser.getRole().equals("visitor");
    }

    private boolean canEdit() {
        return currentUser.getRole().equals("admin") || currentUser.getRole().equals("tech");
    }

    private boolean isAdmin() {
        return currentUser.getRole().equals("admin");
    }
}
