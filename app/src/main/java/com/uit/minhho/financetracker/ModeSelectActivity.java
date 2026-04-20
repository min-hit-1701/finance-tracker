package com.uit.minhho.financetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ModeSelectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_select);

        Button btnPersonal = findViewById(R.id.btnPersonal);
        Button btnBusiness = findViewById(R.id.btnBusiness);

        if (btnPersonal != null) {
            btnPersonal.setOnClickListener(v -> {
                startActivity(new Intent(this, PersonalMainActivity.class));
                finish();
            });
        }
        if (btnBusiness != null) {
            btnBusiness.setOnClickListener(v -> {
                startActivity(new Intent(this, BusinessMainActivity.class));
                finish();
            });
        }
    }
}