package com.uit.minhho.financetracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.uit.minhho.financetracker.fragment.personal.HomeFragment;
import com.uit.minhho.financetracker.fragment.personal.WalletFragment;
import com.uit.minhho.financetracker.fragment.personal.BudgetFragment;
import com.uit.minhho.financetracker.fragment.personal.ReportFragment;
import com.uit.minhho.financetracker.fragment.personal.CategoryFragment;
import com.uit.minhho.financetracker.util.ChatbotManager;

import java.util.ArrayList;
import java.util.List;

public class PersonalMainActivity extends AppCompatActivity {
    BottomNavigationView bottomNav;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private ChatbotManager chatbotManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_main);

        setSupportActionBar(findViewById(R.id.main_toolbar));

        bottomNav = findViewById(R.id.bottom_nav_personal);
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment f = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) f = new HomeFragment();
            else if (itemId == R.id.nav_wallet) f = new WalletFragment();
            else if (itemId == R.id.nav_budget) f = new BudgetFragment();
            else if (itemId == R.id.nav_report) f = new ReportFragment();
            else if (itemId == R.id.nav_category) f = new CategoryFragment();

            if (f != null) loadFragment(f);
            return true;
        });

        checkAndRequestPermissions();

        // Khởi tạo Chatbot AI
        chatbotManager = new ChatbotManager(this);
        chatbotManager.init();
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.CAMERA
        };

        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Một số tính năng tự động sẽ không hoạt động nếu thiếu quyền", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            Toast.makeText(this, "Đã kích hoạt tính năng tự động nhận diện SMS", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_switch_mode) {
            startActivity(new Intent(this, BusinessMainActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, f).commit();
    }
}
