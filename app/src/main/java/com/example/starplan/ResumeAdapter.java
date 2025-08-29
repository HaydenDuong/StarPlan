package com.example.starplan;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ResumeAdapter extends RecyclerView.Adapter<ResumeAdapter.ViewHolder> {

    private static final String TAG = "ResumeAdapter";

    private List<String> resumeFilesInternal; // Renamed for clarity
    private Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String fileName);
    }

    public ResumeAdapter(Context context, List<String> initialFiles, OnItemClickListener listener) {
        this.context = context;
        // Adapter always works with its own copy of the list.
        this.resumeFilesInternal = new ArrayList<>(initialFiles != null ? initialFiles : new ArrayList<>());
        this.listener = listener;
        Log.d(TAG, "Constructor - Initial internal list size: " + this.resumeFilesInternal.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resume_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder - Position: " + position + ", Current internal list size: " + resumeFilesInternal.size());

        if (position < 0 || position >= resumeFilesInternal.size()) {
            Log.e(TAG, "onBindViewHolder - Invalid position: " + position + ". List size is " + resumeFilesInternal.size());
            return;
        }
        String fileName = resumeFilesInternal.get(position);
        Log.d(TAG, "onBindViewHolder - Binding FileName: " + fileName + " at position " + position);

        holder.tvFileName.setText(fileName);

        if (fileName.toLowerCase().endsWith(".pdf")) {
            holder.ivFileIcon.setImageResource(R.drawable.ic_pdf_file_gray);
        } else if (fileName.toLowerCase().endsWith(".docx")) {
            holder.ivFileIcon.setImageResource(R.drawable.ic_pdf_file_gray);
        } else {
            holder.ivFileIcon.setImageResource(R.drawable.ic_pdf_file_gray);
        }
        
        holder.tvFileLastEdited.setVisibility(View.GONE);

        if (selectedPosition == position) {
            holder.llResumeItemRoot.setBackgroundResource(R.drawable.selected_file_border_background);
            holder.tvFileName.setTypeface(null, Typeface.BOLD);
            holder.ivFileIcon.setColorFilter(ContextCompat.getColor(context, R.color.brand_blue));
            holder.ivSelectionIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.llResumeItemRoot.setBackgroundResource(R.drawable.dotted_border_background);
            holder.tvFileName.setTypeface(null, Typeface.NORMAL);
            holder.ivFileIcon.setColorFilter(ContextCompat.getColor(context, android.R.color.darker_gray));
            holder.ivSelectionIndicator.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "itemView onClick - AdapterPosition: " + holder.getAdapterPosition() + ", FileName: " + fileName);
            if (listener != null) {
                listener.onItemClick(fileName);
            }
            int newPosition = holder.getAdapterPosition();
            if (newPosition != RecyclerView.NO_POSITION && selectedPosition != newPosition) {
                int previousSelectedPosition = selectedPosition;
                selectedPosition = newPosition;
                if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousSelectedPosition);
                }
                notifyItemChanged(selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = resumeFilesInternal != null ? resumeFilesInternal.size() : 0;
        Log.d(TAG, "getItemCount() returning: " + count);
        return count;
    }

    public void updateData(List<String> newResumeFiles) {
        Log.d(TAG, "updateData - Received new list content: " + (newResumeFiles != null ? newResumeFiles.toString() : "null"));
        Log.d(TAG, "updateData - Received new list size: " + (newResumeFiles != null ? newResumeFiles.size() : "null"));

        this.resumeFilesInternal.clear();
        Log.d(TAG, "updateData - Internal list size AFTER clear: " + this.resumeFilesInternal.size());

        if (newResumeFiles != null) {
            // Add all items from the new list to our internal list.
            // Since 'newResumeFiles' is a new list from the Activity,
            // 'addAll' will populate 'resumeFilesInternal' correctly.
            this.resumeFilesInternal.addAll(newResumeFiles);
            Log.d(TAG, "updateData - Internal list content AFTER addAll: " + this.resumeFilesInternal.toString());
        } else {
            Log.d(TAG, "updateData - newResumeFiles is null, skipping addAll.");
        }

        Log.d(TAG, "updateData - Final internal list size: " + this.resumeFilesInternal.size());
        notifyDataSetChanged();
    }
    
    // Method to get a copy of the current files for external use (e.g. by Activity)
    public List<String> getCurrentFiles() {
        return new ArrayList<>(this.resumeFilesInternal);
    }


    public String getSelectedFile() {
        if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < resumeFilesInternal.size()) {
            return resumeFilesInternal.get(selectedPosition);
        }
        return null;
    }
    
    public void setSelectedPosition(int position) {
        Log.d(TAG, "setSelectedPosition - New position: " + position + ", Old selectedPosition: " + selectedPosition);
        if (position >= 0 && position < resumeFilesInternal.size()) {
            int previousSelectedPosition = selectedPosition;
            selectedPosition = position;
            if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelectedPosition);
            }
            notifyItemChanged(selectedPosition);
        } else {
            Log.w(TAG, "setSelectedPosition - Invalid position attempted: " + position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llResumeItemRoot;
        ImageView ivFileIcon;
        TextView tvFileName;
        TextView tvFileLastEdited;
        ImageView ivSelectionIndicator;

        ViewHolder(View itemView) {
            super(itemView);
            llResumeItemRoot = itemView.findViewById(R.id.ll_resume_item_root);
            ivFileIcon = itemView.findViewById(R.id.iv_file_icon);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            tvFileLastEdited = itemView.findViewById(R.id.tv_file_last_edited);
            ivSelectionIndicator = itemView.findViewById(R.id.iv_selection_indicator);
        }
    }
}
