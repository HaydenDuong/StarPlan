package com.example.starplan;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Random;

public class JobDetailActivity extends AppCompatActivity {
    private Job currentJob;
    private TabLayout tabLayout;
    private View descriptionContent, companyContent, aiSummaryContent;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_job_detail);

        initViews();
        loadJobData();
        setupTabs();
        setupClickListeners();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tabLayout = findViewById(R.id.tabLayout);
        descriptionContent = findViewById(R.id.descriptionContent);
        companyContent = findViewById(R.id.companyContent);
        aiSummaryContent = findViewById(R.id.aiSummaryContent);
    }

    private void loadJobData() {
        int jobId = getIntent().getIntExtra("job_id", 1);
        
        try {
            // Read JSON from assets
            InputStream is = getAssets().open("job_listings.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            // Parse JSON and find job by ID
            String jsonString = jsonBuilder.toString();
            Gson gson = new Gson();
            Type jobListType = new TypeToken<List<Job>>(){}.getType();
            List<Job> jobs = gson.fromJson(jsonString, jobListType);

            // Find the job with matching ID
            currentJob = jobs.stream()
                    .filter(job -> job.id == jobId)
                    .findFirst()
                    .orElse(jobs.get(0)); // fallback to first job

            populateJobDetails();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading job data", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateJobDetails() {
        if (currentJob == null) return;

        // Header info
        TextView jobTitle = findViewById(R.id.jobTitle);
        TextView companyName = findViewById(R.id.companyName);
        TextView companyInitials = findViewById(R.id.companyInitials);
        TextView location = findViewById(R.id.location);
        TextView salaryRange = findViewById(R.id.salaryRange);
        TextView employmentType = findViewById(R.id.employmentType);
        CardView companyAvatar = findViewById(R.id.companyAvatar);

        jobTitle.setText(currentJob.jobTitle);
        companyName.setText(currentJob.company);
        companyInitials.setText(currentJob.getCompanyInitials());
        location.setText(currentJob.location);
        salaryRange.setText(currentJob.salaryRange);
        employmentType.setText(currentJob.employmentType);

        // Random avatar color
        int[] colors = {
            Color.parseColor("#5E6BFF"),
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#4ECDC4"),
            Color.parseColor("#45B7D1"),
            Color.parseColor("#96CEB4"),
            Color.parseColor("#FECA57")
        };
        companyAvatar.setCardBackgroundColor(colors[random.nextInt(colors.length)]);

        // Skills chips
        ChipGroup skillsGroup = findViewById(R.id.skillsChipGroup);
        skillsGroup.removeAllViews();
        for (String skill : currentJob.skills) {
            Chip chip = new Chip(this);
            chip.setText(skill);
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setChipStrokeColorResource(R.color.brand_blue);
            chip.setChipStrokeWidth(2f);
            chip.setTextColor(Color.parseColor("#5E6BFF"));
            chip.setTextSize(12f);
            skillsGroup.addView(chip);
        }

        // Tab content
        populateTabContent();
    }

    private void populateTabContent() {
        // Description tab
        TextView jobDescription = findViewById(R.id.jobDescription);
        TextView responsibilities = findViewById(R.id.responsibilities);
        TextView qualifications = findViewById(R.id.qualifications);

        jobDescription.setText(currentJob.description);

        // Format responsibilities as bullet points
        StringBuilder responsibilitiesText = new StringBuilder();
        for (String responsibility : currentJob.responsibilities) {
            responsibilitiesText.append("• ").append(responsibility).append("\n");
        }
        responsibilities.setText(responsibilitiesText.toString().trim());

        // Format qualifications as bullet points
        StringBuilder qualificationsText = new StringBuilder();
        for (String qualification : currentJob.qualifications) {
            qualificationsText.append("• ").append(qualification).append("\n");
        }
        qualifications.setText(qualificationsText.toString().trim());

        // Company tab
        TextView companyInfo = findViewById(R.id.companyInfo);
        companyInfo.setText(String.format("%s is a leading company in the industry, offering exciting opportunities for career growth and professional development. Join our dynamic team and be part of innovative projects that make a real impact.", currentJob.company));

        // AI Summary tab
        TextView aiSummary = findViewById(R.id.aiSummary);
        aiSummary.setText(String.format("This %s position at %s offers excellent career advancement opportunities. The role requires %s experience and offers competitive compensation (%s). Based on the job requirements, this position would be ideal for candidates with strong %s skills who are looking to advance their career in a %s environment.",
                currentJob.jobTitle,
                currentJob.company,
                currentJob.experienceLevel.toLowerCase(),
                currentJob.salaryRange,
                currentJob.skills.isEmpty() ? "technical" : currentJob.skills.get(0),
                currentJob.remote.toLowerCase()
        ));
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showTabContent(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Show first tab by default
        showTabContent(0);
    }

    private void showTabContent(int position) {
        descriptionContent.setVisibility(View.GONE);
        companyContent.setVisibility(View.GONE);
        aiSummaryContent.setVisibility(View.GONE);

        switch (position) {
            case 0:
                descriptionContent.setVisibility(View.VISIBLE);
                break;
            case 1:
                companyContent.setVisibility(View.VISIBLE);
                break;
            case 2:
                aiSummaryContent.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.btnApply).setOnClickListener(v -> 
            Toast.makeText(this, "Application submitted successfully!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
