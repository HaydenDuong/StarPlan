package com.example.starplan;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.btnSignup).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}


