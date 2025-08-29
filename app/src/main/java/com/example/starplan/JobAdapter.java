package com.example.starplan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.List;
import java.util.Random;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {
    private List<Job> jobs;
    private Context context;
    private Random random = new Random();

    public JobAdapter(Context context, List<Job> jobs) {
        this.context = context;
        this.jobs = jobs;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobs.get(position);
        
        holder.jobTitle.setText(job.jobTitle);
        holder.companyName.setText(job.company);
        holder.companyInitials.setText(job.getCompanyInitials());
        holder.salaryRange.setText(job.salaryRange);
        
        // Generate random avatar color
        int[] colors = {
            Color.parseColor("#5E6BFF"),
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#4ECDC4"),
            Color.parseColor("#45B7D1"),
            Color.parseColor("#96CEB4"),
            Color.parseColor("#FECA57")
        };
        holder.companyAvatar.setCardBackgroundColor(colors[position % colors.length]);
        
        // Add skills chips (limit to first 3 for space)
        holder.skillsChipGroup.removeAllViews();
        int skillCount = Math.min(3, job.skills.size());
        for (int i = 0; i < skillCount; i++) {
            Chip chip = new Chip(context);
            chip.setText(job.skills.get(i));
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setChipStrokeColorResource(R.color.brand_blue);
            chip.setChipStrokeWidth(2f);
            chip.setTextColor(Color.parseColor("#5E6BFF"));
            chip.setTextSize(12f);
            holder.skillsChipGroup.addView(chip);
        }
        
        // Calculate time posted (simplified)
        holder.timePosted.setText("Posted 3 days ago");
        
        // Click listener to open job details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, JobDetailActivity.class);
            intent.putExtra("job_id", job.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle, companyName, companyInitials, salaryRange, timePosted;
        CardView companyAvatar;
        ChipGroup skillsChipGroup;
        ImageView favoriteIcon;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            companyName = itemView.findViewById(R.id.companyName);
            companyInitials = itemView.findViewById(R.id.companyInitials);
            companyAvatar = itemView.findViewById(R.id.companyAvatar);
            salaryRange = itemView.findViewById(R.id.salaryRange);
            timePosted = itemView.findViewById(R.id.timePosted);
            skillsChipGroup = itemView.findViewById(R.id.skillsChipGroup);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }
    }
}
