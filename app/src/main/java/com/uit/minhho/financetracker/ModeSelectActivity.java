package com.uit.minhho.financetracker;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class ModeSelectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_select);

        MaterialCardView cardPersonal = findViewById(R.id.cardPersonal);
        MaterialCardView cardBusiness = findViewById(R.id.cardBusiness);

        if (cardPersonal != null) {
            cardPersonal.setOnClickListener(v -> {
                startActivity(new Intent(this, PersonalMainActivity.class));
                finish();
            });
        }
        if (cardBusiness != null) {
            cardBusiness.setOnClickListener(v -> {
                startActivity(new Intent(this, BusinessMainActivity.class));
                finish();
            });
        }
    }
}
