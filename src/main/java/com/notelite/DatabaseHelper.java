package com.notelite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "NoteLite.db";
    // NAIK VERSI KE 5 (Update Fitur Auto Delete)
    private static final int DATABASE_VERSION = 5;

    private static final String TABLE_NAME = "notes";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_BOOKMARK = "is_bookmarked";
    private static final String COLUMN_ARCHIVE = "is_archived";
    private static final String COLUMN_DELETED = "is_deleted";

    // KOLOM BARU: Mencatat waktu kapan dihapus
    private static final String COLUMN_DELETED_TIME = "deleted_time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_CONTENT + " TEXT, " +
                COLUMN_BOOKMARK + " INTEGER DEFAULT 0, " +
                COLUMN_ARCHIVE + " INTEGER DEFAULT 0, " +
                COLUMN_DELETED + " INTEGER DEFAULT 0, " +
                COLUMN_DELETED_TIME + " INTEGER DEFAULT 0)"; // Default 0
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // TAMBAH
    public long addNote(String title, String content, boolean isBookmarked, boolean isArchived) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_BOOKMARK, isBookmarked ? 1 : 0);
        values.put(COLUMN_ARCHIVE, isArchived ? 1 : 0);
        values.put(COLUMN_DELETED, 0);
        values.put(COLUMN_DELETED_TIME, 0); // Waktu hapus 0 (karena baru dibuat)
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    // LOGIKA SEARCH, GET ALL, BOOKMARK, ARCHIVE (SAMA SEPERTI SEBELUMNYA)
    public Cursor getAllNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ARCHIVE + " = 0 AND " + COLUMN_DELETED + " = 0 ORDER BY " + COLUMN_ID + " DESC", null);
    }

    public Cursor getBookmarkedNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_BOOKMARK + " = 1 AND " + COLUMN_DELETED + " = 0 AND " + COLUMN_ARCHIVE + " = 0 ORDER BY " + COLUMN_ID + " DESC", null);
    }

    public Cursor getArchivedNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ARCHIVE + " = 1 AND " + COLUMN_DELETED + " = 0 ORDER BY " + COLUMN_ID + " DESC", null);
    }

    public Cursor getTrashedNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_DELETED + " = 1 ORDER BY " + COLUMN_ID + " DESC", null);
    }

    public Cursor searchNotes(String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE (" +
                COLUMN_TITLE + " LIKE ? OR " +
                COLUMN_CONTENT + " LIKE ?) AND " + COLUMN_DELETED + " = 0";
        String[] args = new String[]{"%" + keyword + "%", "%" + keyword + "%"};
        return db.rawQuery(query, args);
    }

    public int updateNote(long id, String title, String content, boolean isBookmarked, boolean isArchived) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_BOOKMARK, isBookmarked ? 1 : 0);
        values.put(COLUMN_ARCHIVE, isArchived ? 1 : 0);
        return db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // --- UPDATE PENTING DI SINI ---

    // 1. PINDAH KE SAMPAH (Catat Waktu Sekarang)
    public void moveToTrash(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DELETED, 1);

        // Simpan waktu saat ini (dalam milidetik)
        values.put(COLUMN_DELETED_TIME, System.currentTimeMillis());

        db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // 2. PULIHKAN (Reset Waktu)
    public void restoreFromTrash(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DELETED, 0);
        values.put(COLUMN_DELETED_TIME, 0); // Reset waktu jadi 0
        db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // 3. HAPUS PERMANEN
    public void deletePermanently(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // 4. BERSIH-BERSIH OTOMATIS (Auto Delete)
    public void deleteExpiredNotes() {
        SQLiteDatabase db = this.getWritableDatabase();

        // RUMUS: Waktu Sekarang - 30 Hari
        // Pastikan pakai "30L" (Long) biar perhitungannya gak error
        long limit = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);

        // Hapus catatan yang statusnya SAMPAH (1) DAN Waktunya LEBIH KECIL dari batas
        db.delete(TABLE_NAME, COLUMN_DELETED + " = 1 AND " + COLUMN_DELETED_TIME + " < ?", new String[]{String.valueOf(limit)});
        db.close();
    }
}