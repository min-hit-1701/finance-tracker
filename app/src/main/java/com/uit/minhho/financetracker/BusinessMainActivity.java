package com.uit.minhho.financetracker;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.uit.minhho.financetracker.fragment.business.BusinessFragment;
import com.uit.minhho.financetracker.fragment.business.DashboardFragment;
import com.uit.minhho.financetracker.fragment.business.ReportFragment;
import com.uit.minhho.financetracker.fragment.business.TransactionFragment;

public class BusinessMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_business);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_business_dashboard) {
                fragment = new DashboardFragment();
            } else if (itemId == R.id.nav_business_management) {
                fragment = new BusinessFragment();
            } else if (itemId == R.id.nav_business_transaction) {
                fragment = new TransactionFragment();
            } else if (itemId == R.id.nav_business_report) {
                fragment = new ReportFragment();
            } else {
                return false;
            }

            openFragment(fragment);
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_business_dashboard);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_business, fragment)
                .commit();
    }
}
