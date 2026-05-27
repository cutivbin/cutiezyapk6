# Build APK Online Tanpa Android Studio

Project ini sudah disiapkan agar bisa dibuild otomatis lewat GitHub Actions.

## Cara build APK via GitHub Actions

1. Buka https://github.com lalu login.
2. Buat repository baru, misalnya `cutiezy-agen-apk`.
3. Upload semua isi folder `CutiezyAgenWebView` ke repository tersebut.
   - Pastikan folder `.github/workflows/build-apk.yml` ikut ter-upload.
4. Masuk tab **Actions**.
5. Pilih workflow **Build Cutiezy Agen APK**.
6. Klik **Run workflow**.
7. Setelah proses selesai, buka run paling atas.
8. Pada bagian **Artifacts**, download `Cutiezy-Agen-debug-apk`.
9. Di dalamnya ada file:

```text
Cutiezy-Agen-debug.apk
```

## Catatan penting

- APK debug bisa langsung dicoba/install, tetapi Android biasanya akan menampilkan peringatan karena belum signed release resmi.
- Untuk upload ke Play Store, perlu APK/AAB release yang ditandatangani dengan keystore.
- WebView membuka URL utama:

```text
https://cutiezy.id/auth/login
```

- Splash screen memakai logo:

```text
https://cutiezy.id/library/assets/images/logo2.png
```
