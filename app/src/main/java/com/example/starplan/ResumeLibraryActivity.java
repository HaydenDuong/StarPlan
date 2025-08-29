package com.example.starplan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class ResumeLibraryActivity extends AppCompatActivity {
    private RecyclerView recyclerResumes;
    private ResumeAdapter resumeAdapter;
    private List<Resume> resumeList;

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
        
        // Mock resume data matching your backend's pattern
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
        findViewById(R.id.btnUpload).setOnClickListener(v -> 
            Toast.makeText(this, "Upload Resume - Coming Soon", Toast.LENGTH_SHORT).show());
        
        // Navigate to Resume Builder flow
        findViewById(R.id.btnCreateNew).setOnClickListener(v -> {
            Intent intent = new Intent(this, ResumeBuilderActivity.class);
            startActivity(intent);
        });
    }
}
