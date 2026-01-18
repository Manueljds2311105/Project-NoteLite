package com.notelite;

public class Note {
    private long id;
    private String title;
    private String content;
    private boolean isBookmarked;
    private boolean isArchived;
    private boolean isDeleted;


    public Note(long id, String title, String content, boolean isBookmarked, boolean isArchived, boolean isDeleted) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isBookmarked = isBookmarked;
        this.isArchived = isArchived;
        this.isDeleted = isDeleted;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public boolean isBookmarked() { return isBookmarked; }
    public boolean isArchived() { return isArchived; }
    public boolean isDeleted() { return isDeleted; }
}