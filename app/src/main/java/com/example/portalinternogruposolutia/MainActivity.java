package com.example.portalinternogruposolutia;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portalinternogruposolutia.adapter.ProjectAdapter;
import com.example.portalinternogruposolutia.model.Project;
import com.example.portalinternogruposolutia.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_FORM = 100;
    private static final int PAGE_SIZE = 4;

    private RecyclerView recycler;
    private ProjectAdapter adapter;
    private TextView emptyState, resultsCount, totalPages;
    private EditText searchInput, pageInput;
    private Spinner spinnerStatus, spinnerCategory, spinnerTech;
    private ImageButton btnPrevPage, btnNextPage;
    private View paginationBar;
    private FloatingActionButton fabAdd;

    private List<Project> allProjects;
    private List<User> users;
    private User currentUser;

    private String currentStatusFilter = "all";
    private String currentCategoryFilter = "all";
    private String currentTechFilter = "all";
    private String searchQuery = "";
    private int nextProjectId = 100;

    private List<Project> fullFilteredList = new ArrayList<>();
    private int currentPage = 1;
    private int totalPagesCount = 1;

    private static final Map<String, List<String>> CATEGORIES = new HashMap<>();
    static {
        CATEGORIES.put("Frontend", Arrays.asList("React", "Vue", "Angular", "TypeScript"));
        CATEGORIES.put("Backend", Arrays.asList("Node.js", "Python", ".NET", "Java",
            "Express", "Django", "Flask", "Spring Boot", "GraphQL"));
        CATEGORIES.put("Base de Datos", Arrays.asList("SQL", "SQL Server", "MongoDB",
            "PostgreSQL", "MySQL", "Firebase", "Oracle"));
        CATEGORIES.put("Cloud / Infra", Arrays.asList("AWS", "Azure", "Docker", "Terraform"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        allProjects = MockData.getProjects();
        users = MockData.getUsers();

        String loginRole = getIntent().getStringExtra("role");
        String loginName = getIntent().getStringExtra("userName");
        if (loginRole != null) {
            boolean found = false;
            for (User u : users) {
                if (u.getRole().equals(loginRole)) {
                    currentUser = u;
                    found = true;
                    break;
                }
            }
            if (!found) {
                currentUser = new User(99, loginName != null ? loginName : "Usuario",
                    loginRole, "U", "email@example.com");
                users.add(currentUser);
            }
        } else {
            currentUser = users.get(0);
        }

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
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerTech = findViewById(R.id.spinnerTech);
        fabAdd = findViewById(R.id.fabAdd);
        paginationBar = findViewById(R.id.paginationBar);
        btnPrevPage = findViewById(R.id.btnPrevPage);
        btnNextPage = findViewById(R.id.btnNextPage);
        pageInput = findViewById(R.id.pageInput);
        totalPages = findViewById(R.id.totalPages);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProjectAdapter(new ArrayList<>(), isReadOnly());
        recycler.setAdapter(adapter);

        adapter.setOnProjectClickListener(this::onEditProject);
        adapter.setOnProjectLongClickListener(this::onDeleteProject);
        adapter.setOnDocumentClickListener(this::onManageDocuments);

        setupSearch();
        setupSpinners();
        setupPagination();
        applyFilters();
        updateFabVisibility();

        fabAdd.setOnClickListener(v -> onAddProject());
    }

    private void setupSpinners() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item,
            new String[]{"Todos", "Producción", "En Desarrollo", "Mantenimiento"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                String[] vals = {"all", "production", "development", "maintenance"};
                currentStatusFilter = vals[pos];
                currentPage = 1;
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        List<String> categories = new ArrayList<>();
        categories.add("Todas las tecnologías");
        categories.addAll(CATEGORIES.keySet());

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        rebuildTechSpinner(null);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                currentCategoryFilter = pos == 0 ? "all" : categories.get(pos);
                currentTechFilter = "all";
                currentPage = 1;
                rebuildTechSpinner(currentCategoryFilter.equals("all") ? null : currentCategoryFilter);
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        spinnerTech.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                String selected = (String) p.getItemAtPosition(pos);
                currentTechFilter = selected.equals("Todas") ? "all" : selected;
                currentPage = 1;
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void rebuildTechSpinner(String category) {
        List<String> techs = new ArrayList<>();
        techs.add("Todas");
        if (category != null) {
            List<String> catTechs = CATEGORIES.get(category);
            if (catTechs != null) techs.addAll(catTechs);
        } else {
            CATEGORIES.values().forEach(techs::addAll);
        }

        ArrayAdapter<String> techAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, techs);
        techAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTech.setAdapter(techAdapter);
    }

    private void setupPagination() {
        btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                showPage();
            }
        });

        btnNextPage.setOnClickListener(v -> {
            if (currentPage < totalPagesCount) {
                currentPage++;
                showPage();
            }
        });

        pageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE
                || (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                goToPageInput();
                return true;
            }
            return false;
        });
    }

    private void goToPageInput() {
        String text = pageInput.getText().toString().trim();
        if (text.isEmpty()) return;
        try {
            int page = Integer.parseInt(text);
            if (page >= 1 && page <= totalPagesCount) {
                currentPage = page;
                showPage();
            } else {
                pageInput.setText(String.valueOf(currentPage));
                pageInput.selectAll();
            }
        } catch (NumberFormatException e) {
            pageInput.setText(String.valueOf(currentPage));
            pageInput.selectAll();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FORM && resultCode == RESULT_OK && data != null) {
            boolean deleted = data.getBooleanExtra(ProjectFormActivity.RESULT_DELETED, false);
            int editId = data.getIntExtra(ProjectFormActivity.EXTRA_PROJECT_ID, -1);

            if (deleted && editId >= 0) {
                allProjects.removeIf(p -> p.getId() == editId);
                currentPage = 1;
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

            currentPage = 1;
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
                currentPage = 1;
                applyFilters();
            }
        });
    }

    private List<Project> getFilteredList() {
        return allProjects.stream()
            .filter(p -> currentStatusFilter.equals("all") || p.getStatus().equals(currentStatusFilter))
            .filter(p -> !currentTechFilter.equals("all")
                ? p.getTechnologies().contains(currentTechFilter)
                : currentCategoryFilter.equals("all")
                    || p.getTechnologies().stream().anyMatch(t ->
                        CATEGORIES.getOrDefault(currentCategoryFilter, Arrays.asList()).contains(t)))
            .filter(p -> searchQuery.isEmpty() || p.getName().toLowerCase().contains(searchQuery))
            .collect(Collectors.toList());
    }

    private void applyFilters() {
        fullFilteredList = getFilteredList();
        totalPagesCount = Math.max(1, (int) Math.ceil((double) fullFilteredList.size() / PAGE_SIZE));
        if (currentPage > totalPagesCount) currentPage = totalPagesCount;
        showPage();
    }

    private void showPage() {
        int fromIndex = (currentPage - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, fullFilteredList.size());
        List<Project> page;
        if (fullFilteredList.isEmpty()) {
            page = new ArrayList<>();
        } else {
            page = fullFilteredList.subList(fromIndex, toIndex);
        }

        adapter.updateList(page, isReadOnly());
        boolean empty = fullFilteredList.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        updateResultsCount(fullFilteredList.size());
        updatePaginationControls();
    }

    private void updateResultsCount(int count) {
        String text = count + " resultado" + (count != 1 ? "s" : "");
        resultsCount.setText(text);
    }

    private void updatePaginationControls() {
        if (fullFilteredList.size() > PAGE_SIZE) {
            paginationBar.setVisibility(View.VISIBLE);
            btnPrevPage.setEnabled(currentPage > 1);
            btnPrevPage.setAlpha(currentPage > 1 ? 1f : 0.3f);
            btnNextPage.setEnabled(currentPage < totalPagesCount);
            btnNextPage.setAlpha(currentPage < totalPagesCount ? 1f : 0.3f);
            pageInput.setText(String.valueOf(currentPage));
            totalPages.setText("de " + totalPagesCount);
        } else {
            paginationBar.setVisibility(View.GONE);
        }
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
                currentPage = 1;
                applyFilters();
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
