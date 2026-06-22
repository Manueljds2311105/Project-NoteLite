package com.notelite;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent; // Import untuk Voice
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList; // Import untuk menangkap hasil suara
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class AddNoteActivity extends AppCompatActivity {

    EditText etTitle, etDescription;
    ImageView btnSave, btnBack, btnBookmark, btnArchive, btnMoreOptions, btnVoice; // Tambah btnVoice
    TextView tvLastEdited;

    ChipGroup chipGroupTags;

    long noteId = -1;
    boolean isEditMode = false;
    boolean isBookmarked = false;
    boolean isArchived = false;

    boolean hasUnsavedChanges = false;
    private boolean isFormatting = false;
    private boolean isDeleting = false; // FLAG UNTUK DETEKSI BACKSPACE

    // !!! API KEY KAMU !!!
    private final String API_KEY = "AIzaSyBqHfXx1BPfld7AnW_qasRDGFCY0JmZ85I";

    // ====================================================================
    // LAUNCHER UNTUK VOICE TO TEXT
    // ====================================================================
    private final ActivityResultLauncher<Intent> voiceToTextLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> resultText = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (resultText != null && !resultText.isEmpty()) {
                        String spokenText = resultText.get(0);

                        // Masukkan teks suara di posisi kursor berada
                        int start = Math.max(etDescription.getSelectionStart(), 0);
                        int end = Math.max(etDescription.getSelectionEnd(), 0);
                        etDescription.getText().replace(Math.min(start, end), Math.max(start, end), spokenText + " ");

                        setUnsavedChanges(true);
                    }
                }
            }
    );

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
        chipGroupTags = findViewById(R.id.chipGroupTags);

        // HUBUNGKAN BAR BAWAH
        btnMoreOptions = findViewById(R.id.btnMoreOptions);
        tvLastEdited = findViewById(R.id.tvLastEdited);
        btnVoice = findViewById(R.id.btnVoice); // Hubungkan Voice

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

            long lastEditedTime = intent.getLongExtra("extra_last_edited", 0);
            if (lastEditedTime > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH.mm", new Locale("id", "ID"));
                tvLastEdited.setText(getString(R.string.edited_on) + " " + sdf.format(new Date(lastEditedTime)));
            } else {
                tvLastEdited.setText(getString(R.string.edited_on));
            }

            String savedTags = intent.getStringExtra("extra_tags");
            if (savedTags != null && !savedTags.isEmpty()) {
                String[] tagsArray = savedTags.split(",");
                for (String tag : tagsArray) {
                    addChipToGroup(tag.trim());
                }
            }

            setUnsavedChanges(false);
        } else {
            tvLastEdited.setText(getString(R.string.new_note));
            setUnsavedChanges(true);
        }

        updateIcons();

        etTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                setUnsavedChanges(true);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // ====================================================================
        // TEXT WATCHER YANG SUDAH DIPERBAIKI (ANTI-BUG BACKSPACE)
        // ====================================================================
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isFormatting) setUnsavedChanges(true);

                // DETEKSI: Apakah user sedang menghapus teks (backspace)?
                isDeleting = (before > 0 && count == 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // CEGAH AUTO-FORMAT JIKA USER SEDANG MENGHAPUS
                if (isFormatting || isDeleting) return;

                int cursor = etDescription.getSelectionStart();
                if (cursor > 0 && s.charAt(cursor - 1) == '\n') {
                    int currentLineEnd = cursor - 1;
                    int currentLineStart = lastIndexOf(s, '\n', currentLineEnd - 1) + 1;
                    String previousLineText = s.subSequence(currentLineStart, currentLineEnd).toString();

                    if (previousLineText.trim().equals("-")) {
                        isFormatting = true;
                        s.delete(currentLineStart, cursor);
                        isFormatting = false;
                        return;
                    } else if (previousLineText.startsWith("- ")) {
                        isFormatting = true;
                        s.insert(cursor, "- ");
                        isFormatting = false;
                        return;
                    }

                    Pattern numberPattern = Pattern.compile("^(\\d+)\\. .*");
                    Matcher matcher = numberPattern.matcher(previousLineText);
                    Pattern emptyNumberPattern = Pattern.compile("^(\\d+)\\.\\s*$");
                    Matcher emptyMatcher = emptyNumberPattern.matcher(previousLineText);

                    if (emptyMatcher.matches()) {
                        isFormatting = true;
                        s.delete(currentLineStart, cursor);
                        isFormatting = false;
                    } else if (matcher.matches()) {
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

        btnSave.setOnClickListener(v -> saveNoteData());

        // ========================================================
        // FITUR: TOMBOL VOICE TO TEXT DITEKAN
        // ========================================================
        if(btnVoice != null) {
            btnVoice.setOnClickListener(v -> {
                Intent intentVoice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intentVoice.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intentVoice.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()); // Ikut bahasa HP
                intentVoice.putExtra(RecognizerIntent.EXTRA_PROMPT, "Bicara sekarang...");

                try {
                    voiceToTextLauncher.launch(intentVoice);
                } catch (Exception e) {
                    Toast.makeText(this, "Perangkat Anda tidak mendukung fitur Voice to Text", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // ========================================================
        // FITUR AI: BOTTOM SHEET DIALOG
        // ========================================================
        btnMoreOptions.setOnClickListener(v -> {
            String currentText = etDescription.getText().toString();
            if (currentText.trim().isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_empty_note), Toast.LENGTH_SHORT).show();
                return;
            }

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(AddNoteActivity.this);
            android.view.View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_bottom_ai, null);
            bottomSheetDialog.setContentView(bottomSheetView);

            // Fungsi 1: Typo
            bottomSheetView.findViewById(R.id.menuTypo).setOnClickListener(view -> {
                bottomSheetDialog.dismiss();
                Toast.makeText(this, getString(R.string.ai_loading_typo), Toast.LENGTH_SHORT).show();
                processTextWithAI("Perbaiki semua salah ketik (typo), tanda baca, dan ejaan pada teks berikut agar lebih rapi. JANGAN ubah bahasanya (jika Inggris tetap Inggris, jika Indonesia tetap Indonesia). Balas hanya dengan teks yang sudah diperbaiki. Teks: ", currentText);
            });

            // Fungsi 2: Formal
            bottomSheetView.findViewById(R.id.menuFormal).setOnClickListener(view -> {
                bottomSheetDialog.dismiss();
                Toast.makeText(this, getString(R.string.ai_loading_formal), Toast.LENGTH_SHORT).show();
                processTextWithAI("Tulis ulang teks berikut agar menjadi lebih formal, profesional, dan sopan. PENTING: JANGAN ubah bahasanya. Jika teks aslinya bahasa Inggris, perbaiki dalam bahasa Inggris. Jika bahasa Indonesia, perbaiki dalam bahasa Indonesia. Balas hanya dengan teks hasilnya. Teks: ", currentText);
            });

            // Fungsi 3: Translate Global
            bottomSheetView.findViewById(R.id.menuTranslateCustom).setOnClickListener(view -> {
                bottomSheetDialog.dismiss();

                EditText inputBahasa = new EditText(AddNoteActivity.this);
                // Mengambil Hint dari strings.xml
                inputBahasa.setHint(getString(R.string.dialog_translate_hint));

                android.widget.FrameLayout container = new android.widget.FrameLayout(AddNoteActivity.this);
                android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(50, 20, 50, 0);
                inputBahasa.setLayoutParams(params);
                container.addView(inputBahasa);

                new AlertDialog.Builder(AddNoteActivity.this)
                        // Mengambil Title dari strings.xml
                        .setTitle(getString(R.string.dialog_translate_title))
                        .setView(container)
                        // Mengambil Teks Tombol Positif dari strings.xml
                        .setPositiveButton(getString(R.string.action_translate), (dialog, which) -> {
                            String targetLang = inputBahasa.getText().toString().trim();
                            if (!targetLang.isEmpty()) {
                                Toast.makeText(AddNoteActivity.this, "AI sedang menerjemahkan ke " + targetLang + "...", Toast.LENGTH_SHORT).show();
                                processTextWithAI("Terjemahkan teks berikut ke dalam bahasa " + targetLang + " secara natural dan tata bahasa yang benar. Balas HANYA dengan hasil terjemahannya, tanpa basa-basi atau tanda kutip. Teks: ", currentText);
                            }
                        })
                        // Mengambil Teks Tombol Batal dari strings.xml
                        .setNegativeButton(getString(R.string.action_cancel), null)
                        .show();
            });

            // Fungsi 4: Tags
            bottomSheetView.findViewById(R.id.menuTags).setOnClickListener(view -> {
                bottomSheetDialog.dismiss();
                Toast.makeText(this, getString(R.string.ai_loading_tags), Toast.LENGTH_SHORT).show();
                generateTagsFromAI(currentText);
            });

            bottomSheetDialog.show();
        });
    }

    // ====================================================================
    // FUNGSI: AUTO SAVE SAAT APLIKASI DI-MINIMIZE ATAU TERTUTUP
    // ====================================================================
    @Override
    protected void onPause() {
        super.onPause();
        String title = etTitle.getText().toString().trim();
        String content = etDescription.getText().toString().trim();
        if (hasUnsavedChanges && (!title.isEmpty() || !content.isEmpty())) {
            autoSaveNoteData();
        }
    }

    private void autoSaveNoteData() {
        String title = etTitle.getText().toString();
        String content = etDescription.getText().toString();

        StringBuilder tagsBuilder = new StringBuilder();
        for (int i = 0; i < chipGroupTags.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTags.getChildAt(i);
            tagsBuilder.append(chip.getText().toString());
            if (i < chipGroupTags.getChildCount() - 1) {
                tagsBuilder.append(",");
            }
        }
        String allTags = tagsBuilder.toString();

        DatabaseHelper db = new DatabaseHelper(this);
        long currentTime = System.currentTimeMillis();

        if (isEditMode) {
            db.updateNote(noteId, title, content, isBookmarked, isArchived, allTags, currentTime);
        } else {
            long newId = db.addNote(title, content, isBookmarked, isArchived, allTags, currentTime);
            noteId = newId;
            isEditMode = true;
        }

        hasUnsavedChanges = false;
        setUnsavedChanges(false);
    }

    // ====================================================================
    // FUNGSI PENDUKUNG UI & DATA
    // ====================================================================
    private void addChipToGroup(String tagText) {
        if (tagText.isEmpty()) return;
        Chip chip = new Chip(this);
        chip.setText(tagText);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroupTags.removeView(chip);
            setUnsavedChanges(true);
        });
        chipGroupTags.addView(chip);
    }

    private void saveNoteData() {
        String title = etTitle.getText().toString();
        String content = etDescription.getText().toString();

        StringBuilder tagsBuilder = new StringBuilder();
        for (int i = 0; i < chipGroupTags.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTags.getChildAt(i);
            tagsBuilder.append(chip.getText().toString());
            if (i < chipGroupTags.getChildCount() - 1) {
                tagsBuilder.append(",");
            }
        }
        String allTags = tagsBuilder.toString();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHelper db = new DatabaseHelper(AddNoteActivity.this);
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH.mm", new Locale("id", "ID"));
        tvLastEdited.setText(getString(R.string.edited_on) + " " + sdf.format(new Date(currentTime)));

        if (isEditMode) {
            db.updateNote(noteId, title, content, isBookmarked, isArchived, allTags, currentTime);
        } else {
            long newId = db.addNote(title, content, isBookmarked, isArchived, allTags, currentTime);
            noteId = newId;
            isEditMode = true;
        }

        setResult(RESULT_OK);
        Toast.makeText(this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
        setUnsavedChanges(false);
    }

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
                        saveNoteData();
                        finish();
                    })
                    .setNegativeButton(getString(R.string.action_cancel), null)
                    .setNeutralButton(getString(R.string.action_dont_save), (dialog, which) -> finish())
                    .show();
        } else {
            finish();
        }
    }

    private void setUnsavedChanges(boolean isUnsaved) {
        this.hasUnsavedChanges = isUnsaved;
        int defaultColor = ContextCompat.getColor(this, R.color.text_color);
        int activeColor = Color.parseColor("#0091EA");

        btnSave.setImageTintList(ColorStateList.valueOf(isUnsaved ? defaultColor : activeColor));
    }

    private void updateIcons() {
        int defaultColor = ContextCompat.getColor(this, R.color.text_color);
        int activeColor = Color.parseColor("#0091EA");

        btnBookmark.setImageTintList(ColorStateList.valueOf(isBookmarked ? activeColor : defaultColor));
        btnArchive.setImageResource(isArchived ? R.drawable.ic_unarchive : R.drawable.ic_archive);
        btnArchive.setImageTintList(ColorStateList.valueOf(isArchived ? activeColor : defaultColor));
    }

    // ====================================================================
    // FUNGSI API AI GEMINI
    // ====================================================================
    private void generateTagsFromAI(String noteText) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
        String prompt = "Baca catatan berikut dan berikan maksimal 3 tag kategori singkat (satu kata per tag) yang relevan, dipisahkan dengan koma. PENTING: Gunakan bahasa yang sama dengan isi catatan aslinya. Jika catatan bahasa Inggris, tag harus bahasa Inggris. Jika catatan bahasa Indonesia, tag harus bahasa Indonesia. Catatan: " + noteText;
        executeAIApiRequest(url, prompt, true);
    }

    private void processTextWithAI(String instruction, String noteText) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
        String prompt = instruction + "\n\n" + noteText;
        executeAIApiRequest(url, prompt, false);
    }

    private void executeAIApiRequest(String url, String prompt, boolean isForTags) {
        JSONObject jsonBody = new JSONObject();
        try {
            JSONArray partsArray = new JSONArray();
            JSONObject textObject = new JSONObject();
            textObject.put("text", prompt);
            partsArray.put(textObject);

            JSONObject contentObject = new JSONObject();
            contentObject.put("role", "user");
            contentObject.put("parts", partsArray);

            JSONArray contentsArray = new JSONArray();
            contentsArray.put(contentObject);
            jsonBody.put("contents", contentsArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AddNoteActivity.this, "Gagal koneksi internet ke AI", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String aiResult = jsonObject.getJSONArray("candidates")
                                .getJSONObject(0).getJSONObject("content")
                                .getJSONArray("parts").getJSONObject(0)
                                .getString("text").trim();

                        runOnUiThread(() -> {
                            if (isForTags) {
                                Toast.makeText(AddNoteActivity.this, "Tag AI Berhasil!", Toast.LENGTH_SHORT).show();
                                chipGroupTags.removeAllViews();
                                String[] tagsArray = aiResult.split(",");
                                for (String tag : tagsArray) {
                                    String cleanTag = tag.trim();
                                    if (cleanTag.toLowerCase().startsWith("tags ai:")) cleanTag = cleanTag.substring(8).trim();
                                    addChipToGroup(cleanTag);
                                }
                            } else {
                                Toast.makeText(AddNoteActivity.this, "AI Selesai Menulis!", Toast.LENGTH_SHORT).show();
                                isFormatting = true;
                                etDescription.setText(aiResult);
                                etDescription.setSelection(etDescription.getText().length());
                                isFormatting = false;
                            }
                            setUnsavedChanges(true);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(AddNoteActivity.this, "Gagal baca balasan AI", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    int errorCode = response.code();
                    runOnUiThread(() -> new AlertDialog.Builder(AddNoteActivity.this)
                            .setTitle("Error AI (" + errorCode + ")")
                            .setMessage("Pastikan API Key sudah dimasukkan dengan benar.")
                            .setPositiveButton("OK", null).show());
                }
            }
        });
    }
}