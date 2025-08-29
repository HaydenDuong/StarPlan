package com.example.starplan;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = "FeedbackActivity";
    private static final String SERVER_URL = "http://10.0.2.2:5000"; // For Android emulator, use 10.0.2.2 for localhost

    private Toolbar toolbar;
    private ProgressBar progressBarOverall, pbCircularScore, progressBarFeedbackLoading;
    private TextView tvSelectedResumeName, tvAnalysisSummary;
    private ImageView ivResumeIcon;
    private Button btnCheckImprovement, btnGenerateCoverLetter, btnPreviousFeedback, btnNextFeedback;
    private EditText etCoverLetter;
    private RecyclerView rvCriteriaBreakdown;
    private FeedbackAdapter feedbackAdapter;
    private LinearLayout llDetailedFeedbackContainer; // This will wrap the RecyclerView

    private String selectedResumeFile;
    private int selectedJobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Retrieve data from the previous activity
        selectedResumeFile = getIntent().getStringExtra("selected_resume_file");
        selectedJobId = getIntent().getIntExtra("selected_job_id", -1);

        if (selectedResumeFile == null || selectedJobId == -1) {
            Toast.makeText(this, "Error: Missing resume or job data.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Missing resume file or job ID. Resume: " + selectedResumeFile + ", Job ID: " + selectedJobId);
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        tvSelectedResumeName.setText(selectedResumeFile);
        // Set resume icon based on file type if needed (e.g., PDF, DOCX)
        if (selectedResumeFile.toLowerCase().endsWith(".pdf")) {
            ivResumeIcon.setImageResource(R.drawable.ic_pdf_file_gray);
        }
        // Add more conditions for other file types if necessary

        // Fetch feedback from the backend
        new FetchFeedbackTask().execute();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_feedback);
        progressBarOverall = findViewById(R.id.progress_bar_overall);
        pbCircularScore = findViewById(R.id.pb_circular_score);
        progressBarFeedbackLoading = findViewById(R.id.progress_bar_feedback_loading);
        tvSelectedResumeName = findViewById(R.id.tv_selected_resume_name);
        ivResumeIcon = findViewById(R.id.iv_resume_icon);
        tvAnalysisSummary = findViewById(R.id.tv_analysis_summary);
        btnCheckImprovement = findViewById(R.id.btn_check_improvement);
        rvCriteriaBreakdown = findViewById(R.id.rv_criteria_breakdown);
        // llDetailedFeedbackContainer = findViewById(R.id.ll_detailed_feedback); // Assuming you add this wrapper if needed

        etCoverLetter = findViewById(R.id.et_cover_letter);
        btnGenerateCoverLetter = findViewById(R.id.btn_generate_cover_letter);
        btnPreviousFeedback = findViewById(R.id.btn_previous_feedback);
        btnNextFeedback = findViewById(R.id.btn_next_feedback);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Feedback & Application");
        }
    }

    private void setupRecyclerView() {
        rvCriteriaBreakdown.setLayoutManager(new LinearLayoutManager(this));
        feedbackAdapter = new FeedbackAdapter(this);
        rvCriteriaBreakdown.setAdapter(feedbackAdapter);
        // Initially hide the detailed breakdown
        rvCriteriaBreakdown.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        btnCheckImprovement.setOnClickListener(v -> {
            if (rvCriteriaBreakdown.getVisibility() == View.GONE) {
                rvCriteriaBreakdown.setVisibility(View.VISIBLE);
                btnCheckImprovement.setText("Hide Details");
            } else {
                rvCriteriaBreakdown.setVisibility(View.GONE);
                btnCheckImprovement.setText("Check what you can improve");
            }
        });

        btnGenerateCoverLetter.setOnClickListener(v -> {
            // TODO: Implement cover letter generation logic
            Toast.makeText(this, "Generate Cover Letter clicked (Not implemented)", Toast.LENGTH_SHORT).show();
            etCoverLetter.setText("Generated cover letter placeholder... based on job ID: " + selectedJobId + " and resume: " + selectedResumeFile);
        });

        btnPreviousFeedback.setOnClickListener(v -> onBackPressed());

        btnNextFeedback.setOnClickListener(v -> {
            // TODO: Navigate to the next step (e.g., submission confirmation or review)
            Toast.makeText(this, "Next clicked (Not implemented)", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class FetchFeedbackTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarFeedbackLoading.setVisibility(View.VISIBLE);
            tvAnalysisSummary.setText("Fetching analysis...");
            pbCircularScore.setProgress(0);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            File resumeFile = new File(getFilesDir(), selectedResumeFile);
            // This assumes selectedResumeFile is just the name and it's in getFilesDir()
            // If it's a full path or from a content URI, this part needs adjustment for actual file access.
            // For now, proceeding with the assumption it's a file name accessible via getFilesDir().
            // You might need to copy the file from its URI to app-specific storage first.
            Log.d(TAG, "Attempting to use resume file: " + resumeFile.getAbsolutePath() + " for job ID: " + selectedJobId);

            // Ensure the file exists (placeholder for actual file handling from previous screen)
            if (!resumeFile.exists()) {
                Log.e(TAG, "Resume file does not exist at: " + resumeFile.getAbsolutePath());
                // For testing, let's try to create a dummy file if it doesn't exist
                try {
                    if (resumeFile.createNewFile()) {
                        Log.d(TAG, "Created dummy resume file for testing: " + resumeFile.getAbsolutePath());
                    } else {
                        Log.e(TAG, "Failed to create dummy resume file.");
                        return null; // Stop if file doesn't exist and cannot be created
                    }
                } catch (IOException e) {
                    Log.e(TAG, "IOException while creating dummy file: " + e.getMessage());
                    return null;
                }
            }

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("job_id", String.valueOf(selectedJobId))
                    .addFormDataPart("resume", resumeFile.getName(),
                            RequestBody.create(MediaType.parse("application/octet-stream"), resumeFile)) // Use octet-stream for generic file
                    .build();

            Request request = new Request.Builder()
                    .url(SERVER_URL + "/get_feedback")
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String jsonString = response.body().string();
                    Log.d(TAG, "Received feedback JSON: " + jsonString);
                    return new JSONObject(jsonString); // Return the whole response object
                } else {
                    Log.e(TAG, "Server error: " + response.code() + " " + response.message());
                    if (response.body() != null) Log.e(TAG, "Error body: " + response.body().string());
                }
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error fetching or parsing feedback", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject fullJsonResponse) {
            progressBarFeedbackLoading.setVisibility(View.GONE);
            if (fullJsonResponse != null) {
                try {
                    // Assuming the structure is: {"feedback": { "overall_score": ..., "detailed_feedback": ...}}
                    JSONObject feedbackData = fullJsonResponse.getJSONObject("feedback");

                    JSONObject overallScoreObj = feedbackData.getJSONObject("overall_score");
                    int score = overallScoreObj.getInt("score"); // e.g. 50
                    // Max score for overall_score might be 100 or specified in JSON
                    int maxScore = overallScoreObj.optInt("max_score", 100);
                    String justification = overallScoreObj.getString("justification");

                    pbCircularScore.setMax(maxScore);
                    pbCircularScore.setProgress(score);
                    tvAnalysisSummary.setText(String.format(Locale.getDefault(),
                            "%d%% alignment. %s", (score * 100 / maxScore), justification));

                    // Assuming 'detailed_feedback' contains the criteria for the RecyclerView
                    if (feedbackData.has("detailed_feedback")) {
                        JSONObject detailedFeedback = feedbackData.getJSONObject("detailed_feedback");
                        feedbackAdapter.updateData(detailedFeedback);
                    } else if (feedbackData.has("feedback")) { // Fallback if nested under 'feedback' again
                        JSONObject detailedFeedback = feedbackData.getJSONObject("feedback");
                        feedbackAdapter.updateData(detailedFeedback);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing feedback JSON", e);
                    tvAnalysisSummary.setText("Failed to parse feedback.");
                    Toast.makeText(FeedbackActivity.this, "Error parsing feedback data.", Toast.LENGTH_LONG).show();
                }
            } else {
                tvAnalysisSummary.setText("Failed to get feedback.");
                Toast.makeText(FeedbackActivity.this, "Failed to get feedback. Check server connection or logs.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
