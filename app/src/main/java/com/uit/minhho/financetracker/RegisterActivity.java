package com.uit.minhho.financetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.uit.minhho.financetracker.util.ValidationUtils;
import com.uit.minhho.financetracker.viewmodel.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private EditText etName, etEmail, etPhone, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_register);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (name.isEmpty() || password.isEmpty() || (email.isEmpty() && phone.isEmpty())) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin (Email hoặc Số điện thoại)", Toast.LENGTH_SHORT).show();
                } else if (!email.isEmpty() && !ValidationUtils.isValidEmail(email)) {
                    Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                } else if (!phone.isEmpty() && !ValidationUtils.isValidPhone(phone)) {
                    Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 6) {
                    Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                } else {
                    authViewModel.register(name, email, phone, password);
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay lại màn hình Login
                }
            });
        }
        
        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> finish());
        }
    }
}
