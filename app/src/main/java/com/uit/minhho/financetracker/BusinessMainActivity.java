package com.uit.minhho.financetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
        setContentView(R.layout.activity_business_main);

        setSupportActionBar(findViewById(R.id.business_toolbar));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_business);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_business_dashboard) {
                fragment = new DashboardFragment();
            } else if (itemId == R.id.nav_business_management) {
                fragment = new BusinessFragment();
            } else if (itemId == R.id.nav_business_transaction) {
                fragment = new TransactionFragment();
            } else if (itemId == R.id.nav_business_report) {
                fragment = new ReportFragment();
            }

            if (fragment != null) openFragment(fragment);
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_business_dashboard);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.business_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back_business) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            }
            return true;
        } else if (id == R.id.action_logout) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_switch_personal) {
            startActivity(new Intent(this, PersonalMainActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container_business, fragment)
                .commit();
    }
}
