package com.example.starplan;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ResumeUploadActivity extends AppCompatActivity implements ResumeAdapter.OnItemClickListener {

    private static final String TAG = "ResumeUploadActivity";
    private static final String SERVER_URL = "http://10.0.2.2:5000"; // For Android emulator
    
    private int selectedJobId = -1; // Member variable to store the job ID

    private Toolbar toolbar;
    private LinearLayout llResumeUploadArea;
    private Button btnNext, btnPrevious;
    private RecyclerView rvResumeFiles;
    private ResumeAdapter resumeAdapter;
    private List<String> resumeFileList; // Activity's own list, can be used for state if needed
    private ProgressBar progressBarLoadingList; // Optional: for loading state

    private ActivityResultLauncher<String[]> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_upload);

        // Retrieve the job_id from the intent
        selectedJobId = getIntent().getIntExtra("job_id", -1);
        if (selectedJobId == -1) {
            Log.e(TAG, "Job ID not received from intent. Defaulting to -1.");
            // Optionally, you could finish the activity or show a toast if job_id is crucial
            // Toast.makeText(this, "Error: Job ID not found.", Toast.LENGTH_LONG).show();
            // finish();
            // return; // Early return if job_id is absolutely necessary
        } else {
            Log.d(TAG, "Received job_id: " + selectedJobId);
        }

        // Initialize Views
        toolbar = findViewById(R.id.toolbar_resume_upload);
        llResumeUploadArea = findViewById(R.id.ll_resume_upload_area);
        rvResumeFiles = findViewById(R.id.rv_resume_files);
        btnNext = findViewById(R.id.btn_next_resume);
        btnPrevious = findViewById(R.id.btn_previous_resume);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Apply");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup RecyclerView
        // Initialize with an empty list; adapter will manage its own copy
        this.resumeFileList = new ArrayList<>(); // Activity's copy
        resumeAdapter = new ResumeAdapter(this, new ArrayList<>(), this); // Adapter gets its own initial empty list
        rvResumeFiles.setLayoutManager(new LinearLayoutManager(this));
        rvResumeFiles.setAdapter(resumeAdapter);

        // Setup Click Listeners
        llResumeUploadArea.setOnClickListener(v -> handleUploadAreaClick());
        btnNext.setOnClickListener(v -> handleNextButtonClick());
        btnPrevious.setOnClickListener(v -> handlePreviousButtonClick());

        // File Picker Launcher
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        Toast.makeText(this, "Selected file: " + uri.getPath(), Toast.LENGTH_LONG).show();
                        String fileName = getFileNameFromUri(uri);
                        // Behavior for locally picked files:
                        // You might want to add to the adapter directly or re-fetch/simulate server update
                        // For simplicity, let's assume if user picks a file, we want it in the list shown.
                        // This part might need refinement based on desired UX for local vs server files.
                        List<String> currentAdapterList = resumeAdapter.getCurrentFiles(); // Need to add this method to adapter
                        if (fileName != null && !currentAdapterList.contains(fileName)) {
                            currentAdapterList.add(0, fileName); // Add to top for visibility
                            resumeAdapter.updateData(currentAdapterList); // Update adapter with modified list
                            resumeAdapter.setSelectedPosition(0);
                            rvResumeFiles.scrollToPosition(0);
                        } else if (fileName != null) {
                            int existingPos = currentAdapterList.indexOf(fileName);
                            resumeAdapter.setSelectedPosition(existingPos);
                            rvResumeFiles.scrollToPosition(existingPos);
                        }
                        updateNextButtonState();
                    }
                });

        fetchResumes();
        updateNextButtonState();
    }

    private String getFileNameFromUri(Uri uri) {
        String path = uri.getPath();
        if (path != null) {
            int cut = path.lastIndexOf('/');
            if (cut != -1) {
                return path.substring(cut + 1);
            }
        }
        return "UnnamedFile";
    }

    private void handleUploadAreaClick() {
        filePickerLauncher.launch(new String[]{"application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"});
    }

    @Override
    public void onItemClick(String fileName) {
        Log.d(TAG, "File clicked: " + fileName);
        updateNextButtonState();
    }

    private void fetchResumes() {
        new FetchResumesTask().execute(SERVER_URL + "/list_resumes");
    }

    private void updateNextButtonState() {
        boolean isFileSelected = resumeAdapter.getSelectedFile() != null;
        btnNext.setEnabled(isFileSelected);
        btnNext.setAlpha(isFileSelected ? 1.0f : 0.5f);
    }

    private void handleNextButtonClick() {
        String selectedFile = resumeAdapter.getSelectedFile();
        if (selectedFile != null) {
            Intent intent = new Intent(ResumeUploadActivity.this, FeedbackActivity.class);
            // Pass the selectedJobId and selected_resume_file to FeedbackActivity
            intent.putExtra("selected_job_id", selectedJobId);
            intent.putExtra("selected_resume_file", selectedFile);
            Log.d(TAG, "Starting FeedbackActivity with job_id: " + selectedJobId + " and resume: " + selectedFile);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please select a file first.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePreviousButtonClick() {
        onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private class FetchResumesTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // if (progressBarLoadingList != null) progressBarLoadingList.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<String> doInBackground(String... urls) {
            if (urls.length == 0) return null;
            String urlString = urls[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = null;
            List<String> files = new ArrayList<>(); // This is a new list for each background task

            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                jsonStr = buffer.toString();
                Log.d(TAG, "doInBackground - Received JSON string: " + jsonStr);
                JSONObject jsonResponse = new JSONObject(jsonStr);
                JSONArray jsonArray = jsonResponse.getJSONArray("available_resumes");
                // This log was previously before the loop, now correctly after:
                for (int i = 0; i < jsonArray.length(); i++) {
                    files.add(jsonArray.getString(i));
                }
                Log.d(TAG, "doInBackground - Parsed " + files.size() + " files successfully.");
                return files; // Return the new list populated from JSON

            } catch (IOException e) {
                Log.e(TAG, "Error fetching resumes", e);
                return null;
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON: " + e.getMessage() + " | JSON String: " + jsonStr, e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(List<String> result) { // 'result' is the new list from doInBackground
            // if (progressBarLoadingList != null) progressBarLoadingList.setVisibility(View.GONE);
            Log.d(TAG, "onPostExecute - Received result list size: " + (result != null ? result.size() : "null"));

            if (result != null && !result.isEmpty()) {
                Log.d(TAG, "onPostExecute - Updating adapter with new data.");
                resumeAdapter.updateData(result); // Pass the 'result' list directly to the adapter

                // Optionally, if the Activity needs its own copy of the latest list for other purposes:
                // this.resumeFileList.clear();
                // this.resumeFileList.addAll(result);

            } else if (result != null && result.isEmpty()) {
                Log.d(TAG, "onPostExecute - Received an empty list of resumes.");
                Toast.makeText(ResumeUploadActivity.this, "No resumes found on server.", Toast.LENGTH_LONG).show();
                resumeAdapter.updateData(new ArrayList<>()); // Clear adapter by passing a new empty list
            } else { // result is null (error during doInBackground or empty list from server)
                Log.e(TAG, "onPostExecute - Result is null or problem occurred. Failed to load resumes.");
                Toast.makeText(ResumeUploadActivity.this, "Failed to load resumes. Check Logcat for errors.", Toast.LENGTH_LONG).show();
                resumeAdapter.updateData(new ArrayList<>()); // Clear adapter by passing a new empty list
            }
            updateNextButtonState();
        }
    }
}
