package com.example.starplan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ResumeAdapter extends RecyclerView.Adapter<ResumeAdapter.ResumeViewHolder> {
    private List<Resume> resumes;
    private Context context;

    public ResumeAdapter(Context context, List<Resume> resumes) {
        this.context = context;
        this.resumes = resumes;
    }

    @NonNull
    @Override
    public ResumeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_resume, parent, false);
        return new ResumeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResumeViewHolder holder, int position) {
        Resume resume = resumes.get(position);
        
        holder.resumeName.setText(resume.getDisplayName());
        holder.resumeDate.setText(resume.createdDate);
        holder.resumeIndustry.setText(resume.industry);
        
        // Click to view PDF
        holder.itemView.setOnClickListener(v -> {
            try {
                // Try to open PDF from assets
                String assetPath = "file:///android_asset/sample_resumes/" + resume.filename;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(assetPath), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "Cannot open PDF. Please install a PDF viewer.", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Edit button
        holder.btnEdit.setOnClickListener(v -> 
            Toast.makeText(context, "Edit Resume - Coming Soon", Toast.LENGTH_SHORT).show());
        
        // Menu button
        holder.btnMenu.setOnClickListener(v -> 
            Toast.makeText(context, "Resume Options - Coming Soon", Toast.LENGTH_SHORT).show());

    @Override
    public int getItemCount() {
        return resumes.size();
    }

    public static class ResumeViewHolder extends RecyclerView.ViewHolder {
        TextView resumeName, resumeDate, resumeIndustry;
        ImageView resumeThumbnail, btnEdit, btnMenu;

        public ResumeViewHolder(@NonNull View itemView) {
            super(itemView);
            resumeName = itemView.findViewById(R.id.resumeName);
            resumeDate = itemView.findViewById(R.id.resumeDate);
            resumeIndustry = itemView.findViewById(R.id.resumeIndustry);
            resumeThumbnail = itemView.findViewById(R.id.resumeThumbnail);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }
    }
}
