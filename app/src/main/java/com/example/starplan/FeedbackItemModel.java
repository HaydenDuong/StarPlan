package com.example.starplan;

public class FeedbackItemModel {
    private String criteriaName;
    private int score;
    private int maxScore;
    private String justification;

    public FeedbackItemModel(String criteriaName, int score, int maxScore, String justification) {
        this.criteriaName = criteriaName;
        this.score = score;
        this.maxScore = maxScore;
        this.justification = justification;
    }

    public String getCriteriaName() {
        return criteriaName;
    }

    public int getScore() {
        return score;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public String getJustification() {
        return justification;
    }
}
