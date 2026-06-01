package com.example.portalinternogruposolutia;

import android.app.AlertDialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portalinternogruposolutia.adapter.ProjectAdapter;
import com.example.portalinternogruposolutia.model.Project;
import com.example.portalinternogruposolutia.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ProjectAdapter adapter;
    private TextView emptyState;
    private TextView resultsCount;
    private EditText searchInput;
    private LinearLayout statusChipsContainer;
    private LinearLayout techChipsContainer;

    private List<Project> allProjects;
    private List<User> users;
    private User currentUser;

    private String currentStatusFilter = "all";
    private String currentTechFilter = "all";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        allProjects = MockData.getProjects();
        users = MockData.getUsers();
        currentUser = users.get(0);

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

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProjectAdapter(allProjects, isReadOnly());
        recycler.setAdapter(adapter);

        setupSearch();
        setupStatusChips();
        setupTechChips();
        updateResultsCount(allProjects.size());
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

    private void applyFilters() {
        List<Project> filtered = allProjects.stream()
            .filter(p -> currentStatusFilter.equals("all") || p.getStatus().equals(currentStatusFilter))
            .filter(p -> currentTechFilter.equals("all") || p.getTechnologies().contains(currentTechFilter))
            .filter(p -> searchQuery.isEmpty() || p.getName().toLowerCase().contains(searchQuery))
            .collect(Collectors.toList());

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
                adapter.updateList(
                    ((ProjectAdapter) recycler.getAdapter()).getItemCount() > 0
                        ? allProjects.stream()
                            .filter(p -> currentStatusFilter.equals("all") || p.getStatus().equals(currentStatusFilter))
                            .filter(p -> currentTechFilter.equals("all") || p.getTechnologies().contains(currentTechFilter))
                            .filter(p -> searchQuery.isEmpty() || p.getName().toLowerCase().contains(searchQuery))
                            .collect(Collectors.toList())
                        : allProjects,
                    isReadOnly()
                );
                dialog.dismiss();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private boolean isReadOnly() {
        return currentUser.getRole().equals("visitor");
    }
}
