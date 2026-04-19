package com.uit.minhho.financetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.uit.minhho.financetracker.fragment.personal.HomeFragment;
import com.uit.minhho.financetracker.fragment.personal.WalletFragment;
import com.uit.minhho.financetracker.fragment.personal.BudgetFragment;
import com.uit.minhho.financetracker.fragment.personal.ReportFragment;
import com.uit.minhho.financetracker.fragment.personal.CategoryFragment;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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