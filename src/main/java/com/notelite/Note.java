package com.notelite;

public class Note {
    private long id;
    private String title;
    private String content;
    private boolean isBookmarked;
    private boolean isArchived;
    private boolean isDeleted;

    // --- TAMBAHAN BARU UNTUK TAGS AI ---
    private String tags;

    // --- TAMBAHAN BARU UNTUK WAKTU TERAKHIR DIEDIT ---
    private long lastEdited;

    // Constructor di-update untuk menerima data tags DAN lastEdited
    public Note(long id, String title, String content, boolean isBookmarked, boolean isArchived, boolean isDeleted, String tags, long lastEdited) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isBookmarked = isBookmarked;
        this.isArchived = isArchived;
        this.isDeleted = isDeleted;
        this.tags = tags;
        this.lastEdited = lastEdited; // Simpan data waktu
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public boolean isBookmarked() { return isBookmarked; }
    public boolean isArchived() { return isArchived; }
    public boolean isDeleted() { return isDeleted; }

    // --- GETTER UNTUK MENGAMBIL DATA TAGS & WAKTU ---
    public String getTags() { return tags; }
    public long getLastEdited() { return lastEdited; }
}