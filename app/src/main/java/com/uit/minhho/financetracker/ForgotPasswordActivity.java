package com.uit.minhho.financetracker;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.uit.minhho.financetracker.data.local.dao.UserDao;
import com.uit.minhho.financetracker.data.local.database.AppDatabase;
import com.uit.minhho.financetracker.receiver.SmsReceiver;
import com.uit.minhho.financetracker.util.ValidationUtils;

import java.util.Random;
import java.util.concurrent.Executors;

public class ForgotPasswordActivity extends AppCompatActivity implements SmsReceiver.SmsListener {

    private LinearLayout layoutStep1, layoutStep2, layoutStep3;
    private EditText etPhoneReset, etOTP, etNewPassword;
    private Button btnSendOTP, btnVerifyOTP, btnResetPassword;
    private TextView tvBackToLogin;

    private String generatedOTP;
    private String currentPhone;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        userDao = AppDatabase.getDatabase(this).userDao();

        initViews();
        setupListeners();
        SmsReceiver.bindListener(this);
    }

    private void initViews() {
        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
        layoutStep3 = findViewById(R.id.layoutStep3);

        etPhoneReset = findViewById(R.id.etPhoneReset);
        etOTP = findViewById(R.id.etOTP);
        etNewPassword = findViewById(R.id.etNewPassword);

        btnSendOTP = findViewById(R.id.btnSendOTP);
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void setupListeners() {
        btnSendOTP.setOnClickListener(v -> sendOTP());
        btnVerifyOTP.setOnClickListener(v -> verifyOTP());
        btnResetPassword.setOnClickListener(v -> resetPassword());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void sendOTP() {
        currentPhone = etPhoneReset.getText().toString().trim();
        if (!ValidationUtils.isValidPhone(currentPhone)) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            if (userDao.getUserByPhone(currentPhone) == null) {
                runOnUiThread(() -> Toast.makeText(this, "Số điện thoại chưa được đăng ký", Toast.LENGTH_SHORT).show());
                return;
            }

            // Simulate sending SMS
            generatedOTP = String.format("%06d", new Random().nextInt(1000000));
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã gửi mã OTP đến " + currentPhone, Toast.LENGTH_SHORT).show();
                layoutStep1.setVisibility(View.GONE);
                layoutStep2.setVisibility(View.VISIBLE);
                
                // Giả lập việc nhận SMS sau 2 giây
                new Handler().postDelayed(() -> {
                    Toast.makeText(this, "[Mô phỏng SMS] Ma xac thuc cua ban la: " + generatedOTP, Toast.LENGTH_LONG).show();
                }, 2000);
            });
        });
    }

    private void verifyOTP() {
        String enteredOTP = etOTP.getText().toString().trim();
        if (enteredOTP.equals(generatedOTP)) {
            layoutStep2.setVisibility(View.GONE);
            layoutStep3.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Mã OTP không chính xác", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            userDao.updatePasswordByPhone(currentPhone, newPassword);
            runOnUiThread(() -> {
                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    public void onSmsReceived(String otp) {
        etOTP.setText(otp);
        verifyOTP();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SmsReceiver.unbindListener();
    }
}
