package com.example.starplan;

public class Resume {
    public String id;
    public String name;
    public String filename;
    public String createdDate;
    public String fileType;
    public String industry;
    public String thumbnailPath;
    public boolean isActive;

    public Resume() {}

    public Resume(String id, String name, String filename, String createdDate, 
                  String fileType, String industry, String thumbnailPath) {
        this.id = id;
        this.name = name;
        this.filename = filename;
        this.createdDate = createdDate;
        this.fileType = fileType;
        this.industry = industry;
        this.thumbnailPath = thumbnailPath;
        this.isActive = true;
    }

    public String getDisplayName() {
        return name != null ? name : "Resume_" + createdDate.split("-")[0];
    }
}
