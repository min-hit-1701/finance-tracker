package com.uit.minhho.financetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.uit.minhho.financetracker.data.remote.AuthApiClient;

public class RegisterActivity extends AppCompatActivity {
    private final AuthApiClient authApiClient = new AuthApiClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_register);

        EditText etName = findViewById(R.id.etRegisterName);
        EditText etEmail = findViewById(R.id.etRegisterEmail);
        EditText etPassword = findViewById(R.id.etRegisterPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                String fullName = textOf(etName);
                String email = textOf(etEmail);
                String password = textOf(etPassword);

                if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    showPopup("Vui lòng nhập đầy đủ họ tên, email và mật khẩu");
                    return;
                }

                btnRegister.setEnabled(false);
                new Thread(() -> {
                    AuthApiClient.AuthResult result = authApiClient.register(fullName, email, password);
                    runOnUiThread(() -> {
                        if (!isActivityActive()) {
                            return;
                        }
                        btnRegister.setEnabled(true);
                        if (result.success) {
                            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            showPopup(result.message);
                        }
                    });
                }).start();
            });
        }
        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> finish());
        }
    }

    private String textOf(EditText editText) {
        return editText == null || editText.getText() == null
                ? ""
                : editText.getText().toString().trim();
    }

    private void showPopup(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private boolean isActivityActive() {
        return !isFinishing() && !isDestroyed();
    }
}
