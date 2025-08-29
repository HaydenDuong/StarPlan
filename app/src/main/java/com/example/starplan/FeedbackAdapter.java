package com.example.starplan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {

    private Context context;
    private List<FeedbackItemModel> feedbackItems;

    public FeedbackAdapter(Context context) {
        this.context = context;
        this.feedbackItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.feedback_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeedbackItemModel item = feedbackItems.get(position);
        holder.tvCriteriaName.setText(item.getCriteriaName());
        holder.tvCriteriaScore.setText(String.format(Locale.getDefault(), "Score: %d/%d", item.getScore(), item.getMaxScore()));
        holder.tvCriteriaJustification.setText(item.getJustification());
    }

    @Override
    public int getItemCount() {
        return feedbackItems.size();
    }

    public void updateData(JSONObject detailedFeedbackJson) {
        feedbackItems.clear();
        if (detailedFeedbackJson != null) {
            Iterator<String> keys = detailedFeedbackJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    JSONObject criteriaObject = detailedFeedbackJson.getJSONObject(key);
                    String name = formatCriteriaName(key);
                    int score = criteriaObject.getInt("score");
                    int maxScore = criteriaObject.optInt("max_score", 10); // Assuming max 10 if not specified
                    String justification = criteriaObject.getString("justification");
                    feedbackItems.add(new FeedbackItemModel(name, score, maxScore, justification));
                } catch (JSONException e) {
                    // Log error or handle
                }
            }
        }
        notifyDataSetChanged();
    }

    private String formatCriteriaName(String key) {
        String[] words = key.split("_");
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                formattedName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase()).append(" ");
            }
        }
        return formattedName.toString().trim();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCriteriaName, tvCriteriaScore, tvCriteriaJustification;

        ViewHolder(View itemView) {
            super(itemView);
            tvCriteriaName = itemView.findViewById(R.id.tv_criteria_name);
            tvCriteriaScore = itemView.findViewById(R.id.tv_criteria_score);
            tvCriteriaJustification = itemView.findViewById(R.id.tv_criteria_justification);
        }
    }
}
