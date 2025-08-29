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
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResumeLibraryActivity extends AppCompatActivity {
    private RecyclerView recyclerResumes;
    private ResumeAdapter resumeAdapter;
    private List<Resume> resumeList;
    private static final int UPLOAD_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resume_library);

        initViews();
        loadMockResumeData();
        setupBottomNavigation();
        setupClickListeners();
    }

    private void initViews() {
        recyclerResumes = findViewById(R.id.recyclerResumes);
        recyclerResumes.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadMockResumeData() {
        resumeList = new ArrayList<>();
        
        // Load real generated resumes first
        loadGeneratedResumes();
        
        // Then add mock resume data for demo
        resumeList.add(new Resume(
            "1", 
            "Software Engineer Resume", 
            "tailored_resume_job_2_upload.pdf",
            "Last edited 2 days ago",
            "pdf",
            "Software Engineering",
            ""
        ));
        
        resumeList.add(new Resume(
            "2", 
            "Marketing Specialist Resume", 
            "general_resume.pdf",
            "Last edited 5 days ago", 
            "pdf",
            "Marketing",
            ""
        ));
        
        resumeList.add(new Resume(
            "3", 
            "Data Analyst Resume", 
            "data_analyst_resume.pdf",
            "Last edited 1 week ago",
            "pdf", 
            "Data Science",
            ""
        ));

        // Setup adapter
        resumeAdapter = new ResumeAdapter(this, resumeList);
        recyclerResumes.setAdapter(resumeAdapter);
    }

    private void loadGeneratedResumes() {
        // Load resumes from app's internal storage
        File resumesDir = new File(getFilesDir(), "resumes");
        if (resumesDir.exists() && resumesDir.isDirectory()) {
            File[] files = resumesDir.listFiles();
            if (files != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        String displayName = fileName.replace(".docx", "").replace("_", " ");
                        String lastModified = "Generated " + dateFormat.format(new Date(file.lastModified()));
                        String fileType = fileName.endsWith(".pdf") ? "pdf" : "docx";
                        
                        Resume generatedResume = new Resume(
                            "gen_" + file.hashCode(), // Unique ID
                            displayName + " Resume",
                            fileName,
                            lastModified,
                            fileType,
                            "Generated Resume",
                            file.getAbsolutePath() // Store full path for opening
                        );
                        
                        resumeList.add(generatedResume);
                    }
                }
            }
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_resume);
        
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_dashboard) {
                Toast.makeText(this, "Dashboard - Coming Soon", Toast.LENGTH_SHORT).show();
                return false;
            } else if (itemId == R.id.nav_resume) {
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
        
        // Navigate to Resume Builder flow
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
                // Refresh the resume list
                loadMockResumeData();
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
            .setMessage(message + "\n\nYour resume has been added to your library.")
            .setPositiveButton("OK", null)
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
