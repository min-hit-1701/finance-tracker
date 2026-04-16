package com.example.finance_tracker;

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

        btnPersonal.setOnClickListener(v ->
                startActivity(new Intent(ModeSelectActivity.this, MainActivity.class))
        );

        btnBusiness.setOnClickListener(v ->
                startActivity(new Intent(ModeSelectActivity.this, BusinessMainActivity.class))
        );
    }
}