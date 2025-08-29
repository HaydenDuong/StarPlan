package com.example.starplan;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class ObjectivesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_objectives);

        initViews();
        setupDropdowns();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupDropdowns() {
        // Industry dropdown
        String[] industries = {"Technology", "Healthcare", "Finance", "Education", "Marketing", "Engineering", "Design", "Sales"};
        AutoCompleteTextView industryDropdown = findViewById(R.id.spinnerIndustry);
        ArrayAdapter<String> industryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, industries);
        industryDropdown.setAdapter(industryAdapter);

        // Level of work dropdown
        String[] levels = {"Entry Level", "Mid Level", "Senior Level", "Executive Level"};
        AutoCompleteTextView levelDropdown = findViewById(R.id.spinnerLevelOfWork);
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, levels);
        levelDropdown.setAdapter(levelAdapter);

        // Company scale dropdown
        String[] scales = {"Startup (1-50)", "Small (51-200)", "Medium (201-1000)", "Large (1000+)"};
        AutoCompleteTextView scaleDropdown = findViewById(R.id.spinnerCompanyScale);
        ArrayAdapter<String> scaleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, scales);
        scaleDropdown.setAdapter(scaleAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
