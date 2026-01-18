package com.notelite;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria; 
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationActivity extends AppCompatActivity implements LocationListener {

    // Greeting & Flag manual (Logic Mapping)
    String[] greetings = {"Halo", "Halo", "Hello", "สวัสดี", "Kamusta"};
    int[] flags = {
            R.drawable.flag_indonesia,
            R.drawable.flag_malaysia,
            R.drawable.flag_singapore,
            R.drawable.flag_thailand,
            R.drawable.flag_philippines
    };

    TextView tvLocation, tvGreeting;
    ImageView imgFlag;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        tvLocation = findViewById(R.id.tvLocation);
        imgFlag = findViewById(R.id.imgFlag);
        tvGreeting = findViewById(R.id.tvGreeting);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            getLocation();
        }
    }

    
    private void getLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Cek ketersediaan provider
        boolean isGpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkOn = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // Jika dua-duanya mati -> Tampilkan Default State
        if (!isGpsOn && !isNetworkOn) {
            showDefaultState();
            return;
        }

        // --- LOGIKA SMART PROVIDER ---
        // Kita minta sistem memilihkan provider terbaik yang SEDANG AKTIF (True)
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE); // Akurasi sedang cukup untuk negara
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);

        String bestProvider = locationManager.getBestProvider(criteria, true);

        // Fallback manual jika sistem bingung mengembalikan null
        if (bestProvider == null) {
            if (isNetworkOn) bestProvider = LocationManager.NETWORK_PROVIDER;
            else if (isGpsOn) bestProvider = LocationManager.GPS_PROVIDER;
        }

        // Jika masih null juga, menyerah
        if (bestProvider == null) {
            showDefaultState();
            return;
        }

        try {
            // Request update menggunakan provider yang dipilih sistem (GPS atau Network)
            locationManager.requestSingleUpdate(bestProvider, this, Looper.getMainLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
            showDefaultState();
        }
    }
    // -----------------------------------------------------------

    private void showDefaultState() {
        // Ambil teks "Lokasi Nonaktif" dari strings.xml
        tvLocation.setText(getString(R.string.location_disabled));
        tvGreeting.setText("");

        // Sembunyikan gambar (Invisible = Layout tidak geser)
        imgFlag.setVisibility(View.INVISIBLE);

        // Pindah ke Home setelah 1.5 detik
        new Handler().postDelayed(this::goToHome, 1500);
    }

    private void goToHome() {
        startActivity(new Intent(LocationActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);

            if (addresses != null && !addresses.isEmpty()) {
                String countryCode = addresses.get(0).getCountryCode();
                String countryName = addresses.get(0).getCountryName(); // Nama Negara dari Google
                updateUI(countryCode, countryName);
            } else {
                updateUI("ID", "Indonesia");
            }
        } catch (IOException e) {
            e.printStackTrace();
            updateUI("ID", "Indonesia");
        }
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(@NonNull String provider) {}

    @Override public void onProviderDisabled(@NonNull String provider) {
        // Kalau provider yang sedang dipakai dimatikan user, coba cari lokasi ulang
        // Siapa tahu provider lain (misal GPS mati, tapi Network hidup) masih bisa
        getLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, getString(R.string.toast_permission_denied), Toast.LENGTH_SHORT).show();
                showDefaultState();
            }
        }
    }

    private void updateUI(String countryCode, String countryNameFromGoogle) {
        // Munculkan gambar lagi
        imgFlag.setVisibility(View.VISIBLE);

        int index = 0; // Default

        if (countryCode != null) {
            switch (countryCode.toUpperCase()) {
                case "ID": index = 0; break;
                case "MY": index = 1; break;
                case "SG": index = 2; break;
                case "TH": index = 3; break;
                case "PH": index = 4; break;
                default: index = 0; break;
            }
        }

        tvLocation.setText(countryNameFromGoogle);
        tvGreeting.setText(greetings[index]);
        imgFlag.setImageResource(flags[index]);

        new Handler().postDelayed(this::goToHome, 2000);
    }

}
