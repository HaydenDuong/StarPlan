package com.example.starplan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import okhttp3.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ApiService {
    
    private static final String BASE_URL = "http://10.0.2.2:5000"; // Standard emulator mappingo
    // Alternative URLs if this doesn't work:
    // private static final String BASE_URL = "http://192.168.4.22:5000"; // Your computer's IP
    // private static final String BASE_URL = "http://192.168.56.1:5000"; // VirtualBox network
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final MediaType PDF = MediaType.get("application/pdf");
    
    private OkHttpClient client;
    private Context context;
    
    public ApiService(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    }
    
    public interface ApiCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    public interface FileCallback {
        void onSuccess(File file);
        void onError(String error);
    }
    
    /**
     * Generate resume from JSON data
     */
    public void generateResume(String resumeData, FileCallback callback) {
        System.out.println("=== ANDROID API CALL ===");
        System.out.println("URL: " + BASE_URL + "/generate_resume");
        System.out.println("Data: " + resumeData);
        
        RequestBody body = RequestBody.create(resumeData, JSON);
        Request request = new Request.Builder()
            .url(BASE_URL + "/generate_resume")
            .post(body)
            .build();
            
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("=== NETWORK FAILURE ===");
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> callback.onError(getUserFriendlyError("network")));
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        // Generate unique filename with timestamp
                        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                            .format(new java.util.Date());
                        String filename = "generated_resume_" + timestamp + ".pdf";
                        
                        // Save downloaded file
                        File resumeFile = saveResponseToFile(response, filename);
                        runOnUiThread(() -> callback.onSuccess(resumeFile));
                    } catch (Exception e) {
                        runOnUiThread(() -> callback.onError(getUserFriendlyError("save_file")));
                    }
                } else {
                    String errorMsg = response.code() >= 500 ? "server" : "generation";
                    runOnUiThread(() -> callback.onError(getUserFriendlyError(errorMsg)));
                }
                response.close();
            }
        });
    }
    
    /**
     * Upload PDF file
     */
    public void uploadPdf(Uri fileUri, ApiCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                callback.onError(getUserFriendlyError("file_read"));
                return;
            }
            
            // Read file into byte array
            byte[] fileBytes = inputStream.readAllBytes();
            inputStream.close();
            
            RequestBody fileBody = RequestBody.create(fileBytes, PDF);
            MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("resume", "uploaded_resume.pdf", fileBody)
                .build();
                
            Request request = new Request.Builder()
                .url(BASE_URL + "/upload")
                .post(requestBody)
                .build();
                
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> callback.onError(getUserFriendlyError("network")));
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> callback.onSuccess("Resume uploaded successfully!"));
                    } else {
                        String errorType = response.code() == 400 ? "invalid_pdf" : "server";
                        runOnUiThread(() -> callback.onError(getUserFriendlyError(errorType)));
                    }
                    response.close();
                }
            });
            
        } catch (Exception e) {
            callback.onError(getUserFriendlyError("file_read"));
        }
    }
    
    /**
     * Save response to internal storage
     */
    private File saveResponseToFile(Response response, String filename) throws IOException {
        File resumesDir = new File(context.getFilesDir(), "resumes");
        if (!resumesDir.exists()) {
            resumesDir.mkdirs();
        }
        
        File file = new File(resumesDir, filename);
        FileOutputStream fos = new FileOutputStream(file);
        
        InputStream is = response.body().byteStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }
        
        fos.close();
        is.close();
        
        return file;
    }
    
    /**
     * User-friendly error messages
     */
    private String getUserFriendlyError(String errorType) {
        switch (errorType) {
            case "network":
                return "Unable to connect. Please check your internet connection and try again.";
            case "server":
                return "Server error occurred. Please try again later.";
            case "generation":
                return "Resume generation failed. Please check your information and try again.";
            case "invalid_pdf":
                return "Invalid PDF file. Please select a valid PDF document.";
            case "file_read":
                return "Unable to read the selected file. Please try again.";
            case "save_file":
                return "Error saving resume. Please try again.";
            default:
                return "An unexpected error occurred. Please try again.";
        }
    }
    
    /**
     * Run on UI thread helper
     */
    private void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
