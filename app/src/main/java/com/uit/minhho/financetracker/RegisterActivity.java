package com.uit.minhho.financetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tên file đã được đổi để phân loại theo mô-đun Auth
        setContentView(R.layout.activity_auth_register);

        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                startActivity(new Intent(this, ModeSelectActivity.class));
                finish();
            });
        }
        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> finish());
        }
    }
}