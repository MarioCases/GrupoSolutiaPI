package com.example.portalinternogruposolutia;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_GOOGLE = 1001;

    private MaterialButton btnCertificate, btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnCertificate = findViewById(R.id.btnCertificate);
        btnGoogle = findViewById(R.id.btnGoogle);

        btnCertificate.setOnClickListener(v -> onCertificateLogin());
        btnGoogle.setOnClickListener(v -> onGoogleLogin());
    }

    @SuppressWarnings("deprecation")
    private void onGoogleLogin() {
        try {
            Intent intent = AccountManager.newChooseAccountIntent(
                null, null, new String[]{"com.google"}, true, null, null, null, null);
            startActivityForResult(intent, REQUEST_GOOGLE);
        } catch (Exception e) {
            showManualEmailDialog();
        }
    }

    private void showManualEmailDialog() {
        EditText input = new EditText(this);
        input.setHint("correo@ejemplo.com");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT
            | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        new AlertDialog.Builder(this)
            .setTitle("Cuenta de Google")
            .setMessage("No se pudo abrir el selector de cuentas.\nIntroduce tu correo electrónico:")
            .setView(input)
            .setPositiveButton("Aceptar", (d, w) -> {
                String email = input.getText().toString().trim();
                if (email.isEmpty()) email = "usuario@google.com";
                navigateToMain(email);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void onCertificateLogin() {
        btnCertificate.setEnabled(false);
        btnCertificate.setText("Seleccionando certificado...");

        KeyChain.choosePrivateKeyAlias(this, new KeyChainAliasCallback() {
            @Override
            public void alias(@Nullable String alias) {
                btnCertificate.setEnabled(true);
                btnCertificate.setText("Acceder con Certificado Digital");

                if (alias != null) {
                    String cn = extractCn(alias);
                    navigateToMain(cn);
                }
            }
        }, null, null, null, -1, null);
    }

    private String extractCn(String alias) {
        String[] parts = alias.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.regionMatches(true, 0, "CN=", 0, 3)) {
                return trimmed.substring(3).trim();
            }
        }
        return alias;
    }

    private void navigateToMain(String userName) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("role", "admin");
        intent.putExtra("userName", userName);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GOOGLE) {
            if (resultCode == RESULT_OK && data != null) {
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                if (accountName != null && !accountName.isEmpty()) {
                    navigateToMain(accountName);
                } else {
                    Toast.makeText(this, "No se seleccionó ninguna cuenta", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Selección de cuenta cancelada", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
