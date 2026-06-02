package com.example.portalinternogruposolutia;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectFormActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_DELETED = "deleted";
    public static final String MODE_ADD = "add";
    public static final String MODE_EDIT = "edit";

    public static final String RESULT_NAME = "name";
    public static final String RESULT_DESCRIPTION = "description";
    public static final String RESULT_STATUS = "status";
    public static final String RESULT_TECHNOLOGIES = "technologies";
    public static final String RESULT_DEPARTMENT = "department";
    public static final String RESULT_DOCUMENTS = "documents";
    public static final String RESULT_DELETED = "deleted";

    private static final int REQUEST_PICK_DOCUMENT = 300;

    private EditText inputName, inputDescription, inputTechnologies, inputDepartment;
    private RadioGroup statusGroup;
    private MaterialButton btnSave, btnDeleteProject, btnAddDocument;
    private LinearLayout documentsContainer;
    private String mode;
    private int projectId = -1;

    private ArrayList<String> documents = new ArrayList<>();
    private boolean deleted = false;

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
        btnDeleteProject = findViewById(R.id.btnDeleteProject);
        btnAddDocument = findViewById(R.id.btnAddDocument);
        documentsContainer = findViewById(R.id.documentsContainer);

        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = MODE_ADD;

        if (mode.equals(MODE_EDIT)) {
            toolbar.setTitle("Editar Proyecto");
            loadProjectData();
            btnDeleteProject.setVisibility(isAdmin() ? View.VISIBLE : View.GONE);
            btnDeleteProject.setOnClickListener(v -> confirmDeleteProject());
        }

        btnSave.setOnClickListener(v -> saveProject());
        btnAddDocument.setOnClickListener(v -> pickDocument());

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_DOCUMENT && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String fileName = getRealFileName(uri);
            final String safeFileName = (fileName != null) ? fileName : "documento";

            for (String doc : documents) {
                String existingName = doc.split("\\|\\|\\|")[0];
                if (existingName.equals(safeFileName)) {
                    final String docRef = doc;
                    new AlertDialog.Builder(this)
                        .setTitle("Documento duplicado")
                        .setMessage("Ya existe un documento llamado \"" + safeFileName + "\". ¿Deseas reemplazarlo?")
                        .setPositiveButton("Reemplazar", (d, w) -> {
                            documents.remove(docRef);
                            documents.add(safeFileName + "|||" + uri.toString());
                            renderDocuments();
                            Toast.makeText(this, "Documento reemplazado", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
                    return;
                }
            }

            documents.add(safeFileName + "|||" + uri.toString());
            renderDocuments();
            Toast.makeText(this, "Documento añadido: " + safeFileName, Toast.LENGTH_SHORT).show();
        }
    }

    private String getRealFileName(Uri uri) {
        String fileName = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) fileName = cursor.getString(nameIndex);
                }
            } catch (Exception ignored) {}
        }
        if (fileName == null) fileName = uri.getLastPathSegment();
        if (fileName != null && fileName.contains("/")) fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        return fileName;
    }

    private void renderDocuments() {
        documentsContainer.removeAllViews();
        for (int i = 0; i < documents.size(); i++) {
            String doc = documents.get(i);
            String[] parts = doc.split("\\|\\|\\|", 2);
            String name = parts[0];

            View row = LayoutInflater.from(this).inflate(R.layout.item_document, documentsContainer, false);
            TextView label = row.findViewById(R.id.docName);
            View deleteBtn = row.findViewById(R.id.docDelete);

            label.setText(name);

            int idx = i;
            deleteBtn.setOnClickListener(v -> {
                documents.remove(idx);
                renderDocuments();
                Toast.makeText(this, "Documento eliminado: " + name, Toast.LENGTH_SHORT).show();
            });

            documentsContainer.addView(row);
        }

        if (documents.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No hay documentos asociados");
            empty.setTextSize(13f);
            empty.setTextColor(0xFF9CA3AF);
            empty.setPadding(0, 16, 0, 16);
            documentsContainer.addView(empty);
        }
    }

    private void loadProjectData() {
        String name = getIntent().getStringExtra(RESULT_NAME);
        String description = getIntent().getStringExtra(RESULT_DESCRIPTION);
        String status = getIntent().getStringExtra(RESULT_STATUS);
        String technologies = getIntent().getStringExtra(RESULT_TECHNOLOGIES);
        String department = getIntent().getStringExtra(RESULT_DEPARTMENT);
        projectId = getIntent().getIntExtra(EXTRA_PROJECT_ID, -1);

        ArrayList<String> savedDocs = getIntent().getStringArrayListExtra(RESULT_DOCUMENTS);
        if (savedDocs != null) documents = savedDocs;

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

        renderDocuments();
    }

    private void pickDocument() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar documento"), REQUEST_PICK_DOCUMENT);
    }

    private void confirmDeleteProject() {
        new AlertDialog.Builder(this)
            .setTitle("Eliminar proyecto")
            .setMessage("¿Estás seguro de eliminar este proyecto?")
            .setPositiveButton("Eliminar", (d, w) -> {
                deleted = true;
                getIntent().putExtra(RESULT_DELETED, true);
                setResult(RESULT_OK, getIntent());
                finish();
            })
            .setNegativeButton("Cancelar", null)
            .show();
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

        getIntent().putExtra(EXTRA_PROJECT_ID, projectId);
        getIntent().putExtra(RESULT_NAME, name);
        getIntent().putExtra(RESULT_DESCRIPTION, description);
        getIntent().putExtra(RESULT_STATUS, status);
        getIntent().putExtra(RESULT_TECHNOLOGIES, techText);
        getIntent().putExtra(RESULT_DEPARTMENT, department);
        getIntent().putExtra("statusLabel", statusLabel);
        getIntent().putStringArrayListExtra(RESULT_DOCUMENTS, documents);
        getIntent().putExtra(RESULT_DELETED, false);

        setResult(RESULT_OK, getIntent());
        finish();
    }

    private boolean isAdmin() {
        return getIntent().getStringExtra("role") == null || getIntent().getStringExtra("role").equals("admin");
    }
}
