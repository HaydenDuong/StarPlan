package com.example.starplan;

import java.util.List;

public class Job {
    public int id;
    public String jobTitle;
    public String company;
    public String description;
    public List<String> responsibilities;
    public List<String> qualifications;
    public List<String> skills;
    public String location;
    public String employmentType;
    public String experienceLevel;
    public String remote;
    public String salaryRange;
    public String datePosted;
    public String validThrough;

    // Constructor
    public Job() {}

    // Helper method to get company initials for avatar
    public String getCompanyInitials() {
        if (company == null || company.isEmpty()) return "??";
        String[] words = company.split("\\s+");
        if (words.length == 1) {
            return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase();
        } else {
            return (words[0].substring(0, 1) + words[1].substring(0, 1)).toUpperCase();
        }
    }
}
