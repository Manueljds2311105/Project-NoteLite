package com.notelite;

import android.content.SharedPreferences; // Tambahan Import
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class SettingsActivity extends AppCompatActivity {

    ImageView btnBack;
    LinearLayout btnTheme, btnLanguage, btnAbout;
    TextView tvCurrentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Inisialisasi View
        btnBack = findViewById(R.id.btnBack);
        btnTheme = findViewById(R.id.btnTheme);
        btnLanguage = findViewById(R.id.btnLanguage);
        btnAbout = findViewById(R.id.btnAbout);
        tvCurrentTheme = findViewById(R.id.tvCurrentTheme);

        // Tombol Kembali
        btnBack.setOnClickListener(v -> finish());

        // 1. LOGIKA GANTI TEMA
        btnTheme.setOnClickListener(v -> showThemeDialog());

        // 2. LOGIKA GANTI BAHASA
        btnLanguage.setOnClickListener(v -> showLanguageDialog());

        // 3. LOGIKA TENTANG APLIKASI
        btnAbout.setOnClickListener(v -> showAboutDialog());

        // Update teks status (Terang/Gelap) saat halaman dibuka
        updateUIState();
    }

    // Fungsi untuk mengubah teks status tema secara otomatis
    private void updateUIState() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // Jika Mode Gelap
            tvCurrentTheme.setText(getString(R.string.theme_dark));
        } else {
            // Jika Mode Terang
            tvCurrentTheme.setText(getString(R.string.theme_light));
        }
    }

    private void showThemeDialog() {
        // Pilihan Tema
        String[] themes = {
                getString(R.string.theme_light),   // 0
                getString(R.string.theme_dark),    // 1
                getString(R.string.theme_system)   // 2
        };

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.pref_theme_title))
                .setItems(themes, (dialog, which) -> {
                    int mode;
                    if (which == 0) {
                        mode = AppCompatDelegate.MODE_NIGHT_NO; // Paksa Terang
                    } else if (which == 1) {
                        mode = AppCompatDelegate.MODE_NIGHT_YES; // Paksa Gelap
                    } else {
                        mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM; // Ikuti Sistem
                    }

                    // --- [LOGIKA PENYIMPANAN TEMA] ---
                    // Simpan pilihan ke SharedPreferences agar diingat saat aplikasi dibuka lagi
                    SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("NightMode", mode);
                    editor.apply(); // Simpan permanen
                    // ---------------------------------

                    // Terapkan Tema
                    AppCompatDelegate.setDefaultNightMode(mode);
                })
                .show();
    }

    private void showLanguageDialog() {
        // Pilihan Bahasa
        String[] languages = {"Indonesia", "English"};

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.pref_lang_title))
                .setItems(languages, (dialog, which) -> {
                    LocaleListCompat appLocale;
                    if (which == 0) {
                        // Kode Bahasa Indonesia
                        appLocale = LocaleListCompat.forLanguageTags("id");
                    } else {
                        // Kode Bahasa Inggris
                        appLocale = LocaleListCompat.forLanguageTags("en");
                    }
                    // Terapkan Bahasa
                    AppCompatDelegate.setApplicationLocales(appLocale);
                })
                .show();
    }

    private void showAboutDialog() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.pref_about_title))
                    .setMessage(getString(R.string.about_msg))
                    .setPositiveButton(getString(R.string.action_ok), null)
                    .show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}