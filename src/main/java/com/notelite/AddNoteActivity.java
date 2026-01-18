package com.notelite;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

// Import tambahan untuk Regex (Logika Angka)
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddNoteActivity extends AppCompatActivity {

    EditText etTitle, etDescription;
    ImageView btnSave, btnBack, btnBookmark, btnArchive;

    long noteId = -1;
    boolean isEditMode = false;

    boolean isBookmarked = false;
    boolean isArchived = false;

    boolean hasUnsavedChanges = false;

    // Variabel untuk mencegah infinite loop saat format otomatis berjalan
    private boolean isFormatting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnArchive = findViewById(R.id.btnArchive);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                checkUnsavedChanges();
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra("extra_id")) {
            isEditMode = true;
            noteId = intent.getLongExtra("extra_id", -1);
            etTitle.setText(intent.getStringExtra("extra_title"));
            etDescription.setText(intent.getStringExtra("extra_content"));

            isBookmarked = intent.getBooleanExtra("extra_bookmarked", false);
            isArchived = intent.getBooleanExtra("extra_archived", false);

            setUnsavedChanges(false);
        } else {
            setUnsavedChanges(true);
        }

        updateIcons();

        // 1. TEXT WATCHER UNTUK JUDUL (Hanya cek perubahan)
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                setUnsavedChanges(true);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 2. TEXT WATCHER UNTUK ISI (LOGIKA SMART LIST ADA DI SINI)
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Tetap nyalakan indikator "Belum Disimpan"
                if (!isFormatting) setUnsavedChanges(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                int cursor = etDescription.getSelectionStart();

                // Cek apakah baru saja menekan ENTER
                if (cursor > 0 && s.charAt(cursor - 1) == '\n') {

                    int currentLineEnd = cursor - 1;
                    int currentLineStart = lastIndexOf(s, '\n', currentLineEnd - 1) + 1;

                    // Ambil teks baris sebelumnya
                    String previousLineText = s.subSequence(currentLineStart, currentLineEnd).toString();

                    // --- LOGIKA BULLET (- ) ---
                    if (previousLineText.trim().equals("-")) {
                        // Kalau baris cuma "-" lalu Enter -> Hapus "-" (Stop List)
                        isFormatting = true;
                        s.delete(currentLineStart, cursor);
                        isFormatting = false;
                        return;
                    }
                    else if (previousLineText.startsWith("- ")) {
                        // Kalau baris "- Belanja" lalu Enter -> Buat "- " baru
                        isFormatting = true;
                        s.insert(cursor, "- ");
                        isFormatting = false;
                        return;
                    }

                    // --- LOGIKA NUMBERING (1. ) ---
                    Pattern numberPattern = Pattern.compile("^(\\d+)\\. .*");
                    Matcher matcher = numberPattern.matcher(previousLineText);

                    Pattern emptyNumberPattern = Pattern.compile("^(\\d+)\\.\\s*$");
                    Matcher emptyMatcher = emptyNumberPattern.matcher(previousLineText);

                    if (emptyMatcher.matches()) {
                        // Kalau baris cuma "2. " lalu Enter -> Hapus (Stop List)
                        isFormatting = true;
                        s.delete(currentLineStart, cursor);
                        isFormatting = false;
                    }
                    else if (matcher.matches()) {
                        // Kalau baris "1. Makan" lalu Enter -> Buat "2. " baru
                        try {
                            int currentNumber = Integer.parseInt(matcher.group(1));
                            int nextNumber = currentNumber + 1;

                            isFormatting = true;
                            s.insert(cursor, nextNumber + ". ");
                            isFormatting = false;
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        btnBack.setOnClickListener(v -> checkUnsavedChanges());

        btnBookmark.setOnClickListener(v -> {
            isBookmarked = !isBookmarked;
            updateIcons();
            setUnsavedChanges(true);
            if(isBookmarked) Toast.makeText(this, getString(R.string.toast_bookmarked), Toast.LENGTH_SHORT).show();
        });

        btnArchive.setOnClickListener(v -> {
            isArchived = !isArchived;
            updateIcons();
            setUnsavedChanges(true);
            if (isArchived) Toast.makeText(this, getString(R.string.toast_archived), Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, getString(R.string.toast_unarchived), Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String content = etDescription.getText().toString();

            if (title.isEmpty() && content.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHelper db = new DatabaseHelper(AddNoteActivity.this);

            if (isEditMode) {
                db.updateNote(noteId, title, content, isBookmarked, isArchived);
            } else {
                long newId = db.addNote(title, content, isBookmarked, isArchived);
                noteId = newId;
                isEditMode = true;
            }

            setResult(RESULT_OK);
            Toast.makeText(this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();

            setUnsavedChanges(false);
        });
    }

    // Helper Function untuk Logic Smart List
    private int lastIndexOf(CharSequence s, char c, int startIndex) {
        for (int i = startIndex; i >= 0; i--) {
            if (s.charAt(i) == c) return i;
        }
        return -1;
    }

    private void checkUnsavedChanges() {
        String title = etTitle.getText().toString().trim();
        String content = etDescription.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            finish();
            return;
        }

        if (hasUnsavedChanges) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_unsaved_title))
                    .setMessage(getString(R.string.dialog_unsaved_msg))
                    .setPositiveButton(getString(R.string.action_save), (dialog, which) -> {
                        btnSave.performClick();
                        finish();
                    })
                    .setNegativeButton(getString(R.string.action_cancel), null)
                    .setNeutralButton(getString(R.string.action_dont_save), (dialog, which) -> {
                        finish();
                    })
                    .show();
        } else {
            finish();
        }
    }

    private void setUnsavedChanges(boolean isUnsaved) {
        this.hasUnsavedChanges = isUnsaved;

        int defaultColor = ContextCompat.getColor(this, R.color.text_color);
        int activeColor = Color.parseColor("#0091EA");

        if (isUnsaved) {
            btnSave.setImageTintList(ColorStateList.valueOf(defaultColor));
        } else {
            btnSave.setImageTintList(ColorStateList.valueOf(activeColor));
        }
    }

    private void updateIcons() {
        int defaultColor = ContextCompat.getColor(this, R.color.text_color);
        int activeColor = Color.parseColor("#0091EA");

        if (isBookmarked) {
            btnBookmark.setImageTintList(ColorStateList.valueOf(activeColor));
        } else {
            btnBookmark.setImageTintList(ColorStateList.valueOf(defaultColor));
        }

        if (isArchived) {
            btnArchive.setImageResource(R.drawable.ic_unarchive);
            btnArchive.setImageTintList(ColorStateList.valueOf(activeColor));
        } else {
            btnArchive.setImageResource(R.drawable.ic_archive);
            btnArchive.setImageTintList(ColorStateList.valueOf(defaultColor));
        }
    }
}