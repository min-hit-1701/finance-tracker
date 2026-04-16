package com.example.finance_tracker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.finance_tracker.fragment.personal.HomeFragment;
import com.example.finance_tracker.fragment.personal.ReportFragment;
import com.example.finance_tracker.fragment.personal.WalletFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment f = null;

            if (item.getItemId() == R.id.nav_home)
                f = new HomeFragment();
            else if (item.getItemId() == R.id.nav_wallet)
                f = new WalletFragment();
            else if (item.getItemId() == R.id.nav_report)
                f = new ReportFragment();

            if (f != null) loadFragment(f);

            return true;
        });
    }

    void loadFragment(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, f)
                .commit();
    }
}