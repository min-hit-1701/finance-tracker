package com.uit.minhho.financetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.uit.minhho.financetracker.data.remote.AuthApiClient;

public class LoginActivity extends AppCompatActivity {

    private final AuthApiClient authApiClient = new AuthApiClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_login);

        EditText etEmail = findViewById(R.id.etLoginEmail);
        EditText etPassword = findViewById(R.id.etLoginPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                String email = textOf(etEmail);
                String password = textOf(etPassword);

                if (email.isEmpty() || password.isEmpty()) {
                    showPopup("Vui lòng nhập tên người dùng/email và mật khẩu");
                    return;
                }

                btnLogin.setEnabled(false);

                new Thread(() -> {
                    AuthApiClient.AuthResult result = authApiClient.login(email, password);

                    runOnUiThread(() -> {
                        if (!isActivityActive()) {
                            return;
                        }

                        btnLogin.setEnabled(true);

                        if (result.success && result.userId > 0) {
                            saveSession(result);
                            startActivity(new Intent(this, ModeSelectActivity.class));
                            finish();
                        } else if (result.success) {
                            showPopup("Không lấy được thông tin tài khoản. Vui lòng đăng nhập lại");
                        } else {
                            showPopup(result.message);
                        }
                    });
                }).start();
            });
        }

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                startActivity(new Intent(this, RegisterActivity.class));
            });
        }
    }

    private String textOf(EditText editText) {
        return editText == null || editText.getText() == null
                ? ""
                : editText.getText().toString().trim();
    }

    private void saveSession(AuthApiClient.AuthResult result) {
        SharedPreferences preferences = getSharedPreferences("auth_session", MODE_PRIVATE);
        preferences.edit()
                .putInt("userId", result.userId)
                .putString("token", result.token)
                .putString("uid", result.uid)
                .apply();
    }

    private void showPopup(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private boolean isActivityActive() {
        return !isFinishing() && !isDestroyed();
    }
}
