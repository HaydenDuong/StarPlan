package com.example.starplan;

import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
                Toast.makeText(this, "Resume - Coming Soon", Toast.LENGTH_SHORT).show();
                return false;
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
        
        findViewById(R.id.btnCreateNew).setOnClickListener(v -> 
            Toast.makeText(this, "Create New Resume - Coming Soon", Toast.LENGTH_SHORT).show());
    }
}
