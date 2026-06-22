package com.notelite;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class NoteLiteApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // LOGIKA TEMA DISINI (Agar dijalankan paling awal)
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        int nightMode = prefs.getInt("NightMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
}