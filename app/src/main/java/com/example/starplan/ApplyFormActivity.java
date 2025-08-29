package com.example.starplan;

import android.content.Intent; // Added import
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ApplyFormActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout tilName, tilEmail, tilContactNo;
    private TextInputEditText etName, etEmail, etContactNo;
    private Button btnNext, btnPrevious;

    //int selectedJobId = getIntent().getIntExtra("selected_job_id", -1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_form);

        // Initialize Views
        toolbar = findViewById(R.id.toolbar_apply_form);
        tilName = findViewById(R.id.til_name);
        etName = findViewById(R.id.et_name);
        tilEmail = findViewById(R.id.til_email);
        etEmail = findViewById(R.id.et_email);
        tilContactNo = findViewById(R.id.til_contact_no);
        etContactNo = findViewById(R.id.et_contact_no);
        btnNext = findViewById(R.id.btn_next_apply);
        btnPrevious = findViewById(R.id.btn_previous_apply);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true); // Ensure title is shown
            getSupportActionBar().setTitle("Apply"); // Explicitly set title if not from manifest
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed()); // Handle back button click

        // Setup Click Listeners
        btnNext.setOnClickListener(v -> handleNextButtonClick());
        btnPrevious.setOnClickListener(v -> handlePreviousButtonClick());
    }

    private void handleNextButtonClick() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String contactNo = etContactNo.getText().toString().trim();

        boolean isValid = true;

        if (name.isEmpty()) {
            tilName.setError("Name is required");
            isValid = false;
        } else {
            tilName.setError(null);
        }

        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        if (contactNo.isEmpty()) {
            tilContactNo.setError("Contact number is required");
            isValid = false;
        } else {
            tilContactNo.setError(null);
        }

        if (isValid) {
            // Proceed to the next step
            Intent intent = new Intent(ApplyFormActivity.this, ResumeUploadActivity.class);
            //intent.putExtra("selected_job_id", selectedJobId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please correct the errors.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePreviousButtonClick() {
        // Navigate back to the previous screen (e.g., JobDetailActivity)
        // This is often the same as onBackPressed()
        onBackPressed();
        Toast.makeText(this, "Navigating to previous screen.", Toast.LENGTH_SHORT).show();
    }

    // It's good practice to override onSupportNavigateUp for toolbar back button
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
