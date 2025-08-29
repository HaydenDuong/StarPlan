package com.example.starplan;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class ExperienceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_experience);

        initViews();
        setupCurrentJobCheckbox();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupCurrentJobCheckbox() {
        CheckBox currentJobCheckbox = findViewById(R.id.cbCurrentJob);
        EditText toDateField = findViewById(R.id.etToDate);

        currentJobCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                toDateField.setText("Present");
                toDateField.setEnabled(false);
            } else {
                toDateField.setText("");
                toDateField.setEnabled(true);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
