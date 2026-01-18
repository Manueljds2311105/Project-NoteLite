package com.notelite;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    RecyclerView recyclerView;
    NoteAdapter adapter;
    ImageView imgIllustration;
    TextView tvEmptyText, tvTitle;
    DatabaseHelper db;
    FloatingActionButton fabAdd;

    // Variabel Global
    CardView headerContainer;
    ImageView btnSearch;

    // 0=Home, 1=Bookmark, 2=Archive, 3=Trash
    private int currentMode = 0;

    final ActivityResultLauncher<Intent> addNoteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) loadNotesFromDatabase();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        db.deleteExpiredNotes();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        imgIllustration = findViewById(R.id.imgIllustration);
        tvEmptyText = findViewById(R.id.tvEmptyText);
        tvTitle = findViewById(R.id.tvTitle);
        ImageView btnMenu = findViewById(R.id.btnMenu);
        fabAdd = findViewById(R.id.fabAdd);

        // Inisialisasi Variabel Global
        headerContainer = findViewById(R.id.headerContainer);
        btnSearch = findViewById(R.id.btnSearch);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        // --- Logika Back Button ---
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    if (currentMode != 0) {
                        navigationView.setCheckedItem(R.id.nav_notes);
                        updateMode(0, getString(R.string.app_name));
                    } else {
                        setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            }
        });

        adapter.setOnItemClickListener(note -> {
            if (currentMode == 3) {
                Toast.makeText(this, getString(R.string.toast_restore_edit), Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                intent.putExtra("extra_id", note.getId());
                intent.putExtra("extra_title", note.getTitle());
                intent.putExtra("extra_content", note.getContent());
                intent.putExtra("extra_bookmarked", note.isBookmarked());
                intent.putExtra("extra_archived", note.isArchived());
                addNoteLauncher.launch(intent);
            }
        });

        adapter.setOnItemLongClickListener(note -> {
            if (currentMode == 3) showTrashOptionsDialog(note);
            else showMoveToTrashDialog(note);
        });

        // Tombol Menu tetap membuka Drawer
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Setup awal fungsi search (Default Hidup)
        setupSearchFunction(true);

        fabAdd.setOnClickListener(v -> addNoteLauncher.launch(new Intent(MainActivity.this, AddNoteActivity.class)));

        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();

            if (id == R.id.nav_notes) {
                updateMode(0, getString(R.string.app_name));
                return true;
            } else if (id == R.id.nav_bookmarks) {
                updateMode(1, getString(R.string.menu_bookmarks));
                return true;
            } else if (id == R.id.nav_archive) {
                updateMode(2, getString(R.string.menu_archive));
                return true;
            } else if (id == R.id.nav_trash) {
                updateMode(3, getString(R.string.menu_trash));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return false;
            }
            return false;
        });

        loadNotesFromDatabase();
    }

    // --- FUNGSI UPDATE: Menggunakan GONE ---
    private void setupSearchFunction(boolean isEnabled) {
        if (isEnabled) {
            View.OnClickListener searchAction = v -> startActivity(new Intent(MainActivity.this, SearchActivity.class));
            headerContainer.setOnClickListener(searchAction);
            tvTitle.setOnClickListener(searchAction);
            btnSearch.setOnClickListener(searchAction);
            btnSearch.setVisibility(View.VISIBLE); // Munculkan ikon
        } else {
            // Matikan semua klik
            headerContainer.setOnClickListener(null);
            tvTitle.setOnClickListener(null);
            btnSearch.setOnClickListener(null);
            btnSearch.setVisibility(View.GONE); // GONE = Hilang Total
        }
    }

    private void updateMode(int mode, String title) {
        currentMode = mode;
        tvTitle.setText(title);

        // Cek apakah ini mode Sampah?
        if (mode == 3) { // 3 = Sampah
            setupSearchFunction(false); // MATIKAN SEARCH
        } else {
            setupSearchFunction(true);  // HIDUPKAN SEARCH
        }

        loadNotesFromDatabase();
    }

    private void showMoveToTrashDialog(Note note) {
        new android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_move_trash_title))
                .setMessage(getString(R.string.dialog_move_trash_msg))
                .setPositiveButton(getString(R.string.action_delete), (dialog, which) -> {
                    db.moveToTrash(note.getId());
                    loadNotesFromDatabase();
                    Toast.makeText(this, getString(R.string.toast_trash_moved), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    private void showTrashOptionsDialog(Note note) {
        String[] options = {getString(R.string.action_restore), getString(R.string.action_delete_permanent)};
        new android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_trash_title))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        db.restoreFromTrash(note.getId());
                        loadNotesFromDatabase();
                        Toast.makeText(this, getString(R.string.toast_restored), Toast.LENGTH_SHORT).show();
                    } else {
                        new android.app.AlertDialog.Builder(this)
                                .setTitle(getString(R.string.dialog_delete_confirm_title))
                                .setMessage(getString(R.string.dialog_delete_confirm_msg))
                                .setPositiveButton(getString(R.string.action_delete), (d, w) -> {
                                    db.deletePermanently(note.getId());
                                    loadNotesFromDatabase();
                                    Toast.makeText(this, getString(R.string.toast_deleted_permanent), Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton(getString(R.string.action_cancel), null)
                                .show();
                    }
                })
                .show();
    }

    private void loadNotesFromDatabase() {
        Cursor cursor;

        // Tombol FAB (Tambah) juga hilang di mode sampah
        if (currentMode == 3) fabAdd.setVisibility(View.GONE);
        else fabAdd.setVisibility(View.VISIBLE);

        if (currentMode == 1) cursor = db.getBookmarkedNotes();
        else if (currentMode == 2) cursor = db.getArchivedNotes();
        else if (currentMode == 3) cursor = db.getTrashedNotes();
        else cursor = db.getAllNotes();

        List<Note> noteList = new ArrayList<>();

        if (cursor.getCount() == 0) {
            imgIllustration.setVisibility(View.VISIBLE);
            tvEmptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            if (currentMode == 1) {
                imgIllustration.setImageResource(R.drawable.img_empty_bookmark);
                tvEmptyText.setText(getString(R.string.empty_bookmark));
            } else if (currentMode == 2) {
                imgIllustration.setImageResource(R.drawable.img_empty_archive);
                tvEmptyText.setText(getString(R.string.empty_archive));
            } else if (currentMode == 3) {
                imgIllustration.setImageResource(R.drawable.img_empty_trash);
                tvEmptyText.setText(getString(R.string.empty_trash));
            } else {
                imgIllustration.setImageResource(R.drawable.img_empty_state);
                tvEmptyText.setText(getString(R.string.empty_home));
            }
        } else {
            imgIllustration.setVisibility(View.GONE);
            tvEmptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String title = cursor.getString(1);
                String content = cursor.getString(2);
                boolean isBookmarked = cursor.getInt(3) == 1;
                boolean isArchived = cursor.getInt(4) == 1;
                boolean isDeleted = cursor.getInt(5) == 1;

                noteList.add(new Note(id, title, content, isBookmarked, isArchived, isDeleted));
            }
            adapter.setNotes(noteList);
        }
    }
}