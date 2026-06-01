package com.example.portalinternogruposolutia;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.portalinternogruposolutia.model.Project;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectFormActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_MODE = "mode";
    public static final String MODE_ADD = "add";
    public static final String MODE_EDIT = "edit";

    public static final String RESULT_NAME = "name";
    public static final String RESULT_DESCRIPTION = "description";
    public static final String RESULT_STATUS = "status";
    public static final String RESULT_TECHNOLOGIES = "technologies";
    public static final String RESULT_DEPARTMENT = "department";

    private EditText inputName, inputDescription, inputTechnologies, inputDepartment;
    private RadioGroup statusGroup;
    private MaterialButton btnSave;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_form);

        Toolbar toolbar = findViewById(R.id.toolbarForm);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inputName = findViewById(R.id.formName);
        inputDescription = findViewById(R.id.formDescription);
        inputTechnologies = findViewById(R.id.formTechnologies);
        inputDepartment = findViewById(R.id.formDepartment);
        statusGroup = findViewById(R.id.formStatus);
        btnSave = findViewById(R.id.btnSave);

        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = MODE_ADD;

        if (mode.equals(MODE_EDIT)) {
            toolbar.setTitle("Editar Proyecto");
            loadProjectData();
        }

        btnSave.setOnClickListener(v -> saveProject());

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadProjectData() {
        String name = getIntent().getStringExtra(RESULT_NAME);
        String description = getIntent().getStringExtra(RESULT_DESCRIPTION);
        String status = getIntent().getStringExtra(RESULT_STATUS);
        String technologies = getIntent().getStringExtra(RESULT_TECHNOLOGIES);
        String department = getIntent().getStringExtra(RESULT_DEPARTMENT);

        inputName.setText(name);
        inputDescription.setText(description);
        inputTechnologies.setText(technologies);
        inputDepartment.setText(department);

        if (status != null) {
            switch (status) {
                case "production":  statusGroup.check(R.id.statusProduction); break;
                case "development": statusGroup.check(R.id.statusDevelopment); break;
                case "maintenance": statusGroup.check(R.id.statusMaintenance); break;
            }
        }
    }

    private void saveProject() {
        String name = inputName.getText().toString().trim();
        String description = inputDescription.getText().toString().trim();
        String techText = inputTechnologies.getText().toString().trim();
        String department = inputDepartment.getText().toString().trim();

        if (name.isEmpty()) {
            inputName.setError("El nombre es obligatorio");
            return;
        }

        int selectedId = statusGroup.getCheckedRadioButtonId();
        RadioButton selected = findViewById(selectedId);
        String status = "production";
        String statusLabel = "Producción";

        if (selected != null) {
            int idx = statusGroup.indexOfChild(selected);
            switch (idx) {
                case 0: status = "production";  statusLabel = "Producción"; break;
                case 1: status = "development"; statusLabel = "En Desarrollo"; break;
                case 2: status = "maintenance"; statusLabel = "Mantenimiento"; break;
            }
        }

        getIntent().putExtra(RESULT_NAME, name);
        getIntent().putExtra(RESULT_DESCRIPTION, description);
        getIntent().putExtra(RESULT_STATUS, status);
        getIntent().putExtra(RESULT_TECHNOLOGIES, techText);
        getIntent().putExtra(RESULT_DEPARTMENT, department);
        getIntent().putExtra("statusLabel", statusLabel);

        setResult(RESULT_OK, getIntent());
        finish();
    }
}
