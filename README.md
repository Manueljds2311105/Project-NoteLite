# Project Pemrograman Mobile

**Nama:** Manuel Johansen Dolok Saribu

**NIM:** 312410493

**Jadwal / SCRUM:**
- **Phase 1:** https://sharing.clickup.com/90181794017/g/h/2kzm1y71-598/9483b8f20001182
- **Phase 2:** https://sharing.clickup.com/90181794017/g/h/2kzm1y71-638/d4b552bdc352db3

**Design Figma:**
https://www.figma.com/design/ompdfSKeVv6k7tWoZEZvVP/Untitled?node-id=0-1&t=M33jD40kG8aLUsvf-1

**Prototype Figma:**
https://www.figma.com/proto/ompdfSKeVv6k7tWoZEZvVP/Untitled?node-id=70-5349&p=f&t=Suv3nfGweoWAFyFX-1&scaling=scale-down&content-scaling=fixed&page-id=0%3A1&starting-point-node-id=70%3A5349

**Video Demo / UX:**
* [UX Figma (Video Prototype)](https://youtu.be/85oFqgyMks0?si=EKgfIQ9roLB1VOcF)
* [UX Hasil Implementasi Android Studio Phase 1](https://youtu.be/H5aYpjsEQ5w?si=0Dbr-Brzsus0am_N)
* [UX Hasil Implementasi Android Studio Phase 2](https://youtube.com/shorts/WLBFeTCFwOQ?si=eFxiTqXtjqa4cg99)

---

# 📱 NoteLite ✨

### Smart Android Note-Taking Application Powered by AI

## 📌 Deskripsi

**NoteLite** adalah aplikasi pencatatan digital berbasis Android yang dirancang untuk produktivitas modern. Tidak hanya sekadar aplikasi catatan biasa, NoteLite mengintegrasikan teknologi **Voice-to-Text** dan **Kecerdasan Buatan (AI) Google Gemini** untuk membantu pengguna merapikan, menerjemahkan, dan mengelola catatan dengan cepat, cerdas, dan efisien.

## 🎯 Tujuan Pengembangan

-   Mengembangkan aplikasi pencatatan tingkat lanjut berbasis Android.
-   Mengimplementasikan integrasi API eksternal (Google Gemini AI) pada aplikasi mobile.
-   Menerapkan fitur aksesibilitas (*Voice-to-Text*) untuk kemudahan input pengguna.
-   Menjadi *final project* (UAS) yang merepresentasikan pemahaman komprehensif terkait UI/UX, database, dan *state management*.

## ✨ Fitur Unggulan (Key Features)

1. **Voice-to-Text Integration 🎙️**
   Menulis catatan jauh lebih cepat. Pengguna dapat mendiktekan pemikirannya langsung menjadi teks tanpa perlu mengetik manual (menggunakan API pengenalan suara bawaan Android).

2. **AI-Powered Assistant (Powered by Gemini) 🤖**
   Dilengkapi dengan asisten AI cerdas yang dapat diakses melalui *Bottom Sheet Menu*:
   * ✨ **Auto-Fix Typo & Ejaan:** Merapikan tata bahasa dan kesalahan ketik secara otomatis.
   * 💼 **Bahasa Formal:** Mengubah gaya bahasa catatan menjadi lebih profesional dan sopan.
   * 🌐 **Smart Translate:** Menerjemahkan isi catatan ke berbagai bahasa dengan input dinamis.
   * 🏷️ **Generate Auto Tags:** AI akan menganalisis isi catatan dan memberikan rekomendasi label/kategori (*chip tags*) secara otomatis.

3. **Auto-Save & Bullet Formatting 💾**
   * Catatan akan tersimpan otomatis saat aplikasi diminimize atau ditutup, mencegah kehilangan data.
   * Format *bulleting* dan *numbering* otomatis (seperti MS Word) saat mengetik daftar.

4. **Modern UI & Dark Mode Ready 🌙**
   Antarmuka dibangun dengan Material Vector Assets yang rapi, responsif, dan otomatis menyesuaikan dengan tema gelap (Dark Mode) perangkat.

## 🛠️ Teknologi yang Digunakan

-   **Bahasa Pemrograman:** Java
-   **Platform:** Android (SDK 36)
-   **IDE:** Android Studio
-   **Database:** SQLite (DatabaseHelper)
-   **AI Integration:** Google Gemini API (Generative Language API) via **OkHttp3**
-   **Build System:** Gradle

## 📂 File Project

    Project-NoteLite
    │
    ├── src/                  # Source code aplikasi (Java & XML)
    ├── Pictures Project/     # Screenshot dan aset gambar
    ├── UI & UX.pdf           # Dokumen desain UI/UX
    ├── app-release.apk       # File APK siap install
    ├── build.gradle          # Konfigurasi build Gradle
    └── README.md             # Dokumentasi project

## ▶️ Cara Menjalankan Aplikasi

1.  Buka project menggunakan **Android Studio**.
2.  Pastikan koneksi internet aktif untuk menggunakan fitur AI Gemini dan *Voice-to-Text*.
3.  Jalankan aplikasi melalui **Emulator** atau sambungkan **perangkat Android fisik**.
4.  Alternatif lain: Install langsung file **`app-release.apk`** ke perangkat Android.

## 👨‍💻 Pengembang

**Nama:** Manuel Johansen Dolok Saribu
**Project:** NoteLite (UAS Pemrograman Mobile)
