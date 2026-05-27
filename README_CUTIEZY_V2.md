# Cutiezy Agen WebView v2

Base URL: https://cutiezy.id/auth/login

## Perbaikan v2

1. Tap/click lebih halus seperti aplikasi Android native.
2. Kontak didukung melalui fallback native Contact Picker untuk website yang memakai Contact Picker API.
3. Kamera didukung untuk input upload file, termasuk ambil foto langsung.
4. Share teks, link, dan gambar/file didukung melalui fallback native Android Share Sheet, termasuk WhatsApp jika terpasang.
5. `window.print()` diarahkan ke pilihan:
   - Print Sistem / Simpan PDF
   - Printer Bluetooth Thermal
6. Bluetooth printer menggunakan printer yang sudah dipairing di pengaturan Bluetooth.
7. Target SDK 35 dan compile SDK 35 agar sesuai syarat Google Play saat ini.
8. Disediakan workflow debug APK dan workflow signed AAB untuk Play Store.

## Cara build APK debug di GitHub Actions

1. Upload semua isi folder ini ke repository GitHub.
2. Buka tab Actions.
3. Jalankan workflow `Build Cutiezy Agen APK`.
4. Download artifact `Cutiezy-Agen-debug-apk`.
5. Extract ZIP artifact, lalu install `Cutiezy-Agen-debug.apk` ke HP.

## Cara build AAB untuk Play Store

Google Play membutuhkan AAB release yang ditandatangani upload key. Workflow yang disediakan: `Build Play Store Signed AAB`.

Tambahkan Secrets berikut di GitHub Repository Settings > Secrets and variables > Actions:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Setelah Secrets lengkap, jalankan workflow `Build Play Store Signed AAB`. Artifact yang keluar adalah:

- `Cutiezy-Agen-release.aab` untuk upload ke Play Console
- `Cutiezy-Agen-release.apk` untuk tes manual

## Catatan printer Bluetooth

Printer thermal harus sudah dipairing dulu dari Settings Bluetooth HP. Fitur ini memakai Bluetooth SPP umum. Beberapa printer thermal murah memakai charset berbeda, jadi jika simbol tertentu tidak tampil sempurna, teks struk dari website perlu dibuat sederhana.

## Catatan Play Store

Karena aplikasi meminta izin Kamera dan Bluetooth Connect, isi Data Safety dan Privacy Policy di Play Console harus sesuai fungsi aplikasi. Kamera digunakan untuk upload foto dari WebView. Bluetooth digunakan untuk print struk ke printer thermal.
