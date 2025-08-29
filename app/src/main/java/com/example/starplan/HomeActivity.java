package com.example.starplan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerJobs;
    private JobAdapter jobAdapter;
    private List<Job> jobList;
    private static final int UPLOAD_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        initViews();
        loadJobData();
        setupBottomNavigation();
        setupClickListeners();
    }

    private void initViews() {
        recyclerJobs = findViewById(R.id.recyclerJobs);
        recyclerJobs.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadJobData() {
        try {
            // Read JSON from assets folder
            InputStream is = getAssets().open("job_listings.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            // Parse JSON
            String jsonString = jsonBuilder.toString();
            Gson gson = new Gson();
            Type jobListType = new TypeToken<List<Job>>(){}.getType();
            jobList = gson.fromJson(jsonString, jobListType);

            // Limit to first 10 jobs for better performance
            if (jobList.size() > 10) {
                jobList = jobList.subList(0, 10);
            }

            // Setup adapter
            jobAdapter = new JobAdapter(this, jobList);
            recyclerJobs.setAdapter(jobAdapter);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading job data", Toast.LENGTH_SHORT).show();
            jobList = new ArrayList<>();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_dashboard) {
                Toast.makeText(this, "Dashboard - Coming Soon", Toast.LENGTH_SHORT).show();
                return false;
            } else if (itemId == R.id.nav_resume) {
                startActivity(new Intent(this, ResumeLibraryActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                Toast.makeText(this, "Profile - Coming Soon", Toast.LENGTH_SHORT).show();
                return false;
            }
            return false;
        });
    }

    private void setupClickListeners() {
        findViewById(R.id.btnUpload).setOnClickListener(v -> uploadResume());
        
        findViewById(R.id.btnCreateNew).setOnClickListener(v -> {
            Intent intent = new Intent(this, ResumeBuilderActivity.class);
            startActivity(intent);
        });
    }

    private void uploadResume() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, UPLOAD_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == UPLOAD_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                handleFileUpload(fileUri);
            }
        }
    }

    private void handleFileUpload(Uri fileUri) {
        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading resume...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Upload file
        ApiService apiService = new ApiService(this);
        apiService.uploadPdf(fileUri, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(String message) {
                progressDialog.dismiss();
                showUploadSuccessDialog(message);
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                showUploadErrorDialog(error);
            }
        });
    }

    private void showUploadSuccessDialog(String message) {
        new AlertDialog.Builder(this)
            .setTitle("Upload Successful!")
            .setMessage(message + "\n\nYour resume has been added to your Resume Library.")
            .setPositiveButton("Go to Resume Library", (dialog, which) -> {
                Intent intent = new Intent(this, ResumeLibraryActivity.class);
                startActivity(intent);
            })
            .setNegativeButton("Stay Here", null)
            .show();
    }

    private void showUploadErrorDialog(String error) {
        new AlertDialog.Builder(this)
            .setTitle("Upload Failed")
            .setMessage(error)
            .setPositiveButton("Try Again", null)
            .show();
    }
}
