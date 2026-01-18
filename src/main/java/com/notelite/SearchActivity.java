package com.notelite;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    EditText etSearch;
    ImageView btnBack;
    RecyclerView recyclerView;

    // UPDATE: Kita pakai 2 variabel terpisah (Bukan LinearLayout lagi)
    // Supaya cocok dengan XML baru
    ImageView imgEmpty;
    TextView tvEmpty;

    NoteAdapter adapter;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = new DatabaseHelper(this);

        // Inisialisasi View
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.rvSearchResults);

        // UPDATE: Hubungkan dengan ID baru di XML
        imgEmpty = findViewById(R.id.imgEmptySearch);
        tvEmpty = findViewById(R.id.tvEmptySearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        etSearch.requestFocus();

        // LOGIKA PENCARIAN (Real-time saat mengetik)
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // KLIK ITEM -> EDIT
        adapter.setOnItemClickListener(note -> {
            Intent intent = new Intent(SearchActivity.this, AddNoteActivity.class);
            intent.putExtra("extra_id", note.getId());
            intent.putExtra("extra_title", note.getTitle());
            intent.putExtra("extra_content", note.getContent());
            intent.putExtra("extra_bookmarked", note.isBookmarked());
            // Kirim status Arsip juga biar tombolnya sinkron
            intent.putExtra("extra_archived", note.isArchived());

            startActivity(intent);
        });
    }

    private void searchNotes(String keyword) {
        // KONDISI 1: Kolom pencarian kosong
        if (keyword.isEmpty()) {
            adapter.setNotes(new ArrayList<>()); // Kosongkan list
            recyclerView.setVisibility(View.GONE);

            // Sembunyikan pesan kosong juga (biar bersih)
            imgEmpty.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
            return;
        }

        Cursor cursor = db.searchNotes(keyword);
        List<Note> noteList = new ArrayList<>();

        if (cursor != null && cursor.getCount() > 0) {
            // KONDISI 2: ADA HASIL
            recyclerView.setVisibility(View.VISIBLE);

            // Sembunyikan Gambar & Teks Kosong
            imgEmpty.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String title = cursor.getString(1);
                String content = cursor.getString(2);

                boolean isBookmarked = false;
                if(cursor.getColumnCount() > 3) isBookmarked = cursor.getInt(3) == 1;

                boolean isArchived = false;
                if(cursor.getColumnCount() > 4) isArchived = cursor.getInt(4) == 1;

                // boolean isDeleted = false; (Search tidak menampilkan sampah, jadi false saja)

                // Masukkan 6 parameter (tambah false di akhir)
                noteList.add(new Note(id, title, content, isBookmarked, isArchived, false));
            }
        } else {
            // KONDISI 3: TIDAK DITEMUKAN
            recyclerView.setVisibility(View.GONE);

            // TAMPILKAN Gambar & Teks Kosong
            imgEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.VISIBLE);
        }

        adapter.setNotes(noteList);
    }
}