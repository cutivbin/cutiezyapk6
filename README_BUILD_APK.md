# Cutiezy Agen WebView APK

Base URL aplikasi:

```text
https://cutiezy.id/auth/login
```

Logo splash screen dimuat dari:

```text
https://cutiezy.id/library/assets/images/logo2.png
```

## Cara build APK di Android Studio

1. Buka Android Studio.
2. Pilih **Open** lalu pilih folder `CutiezyAgenWebView`.
3. Tunggu Gradle Sync selesai.
4. Untuk APK testing:
   - Klik **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
   - File APK biasanya muncul di:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Cara build release APK

1. Klik **Build > Generate Signed Bundle / APK**.
2. Pilih **APK**.
3. Buat atau pilih keystore.
4. Pilih build variant **release**.
5. Hasil APK release ada di:

```text
app/build/outputs/apk/release/app-release.apk
```

## Yang sudah disiapkan

- WebView login ke `https://cutiezy.id/auth/login`
- Splash screen biru dengan logo Cutiezy dan teks `Cutiezy Agen`
- JavaScript, DOM Storage, Cookie, upload file, dan tombol back WebView aktif
- Link `tel:`, `mailto:`, `whatsapp:`, dan `intent:` diarahkan ke aplikasi luar
- SSL error tidak dipaksa lanjut agar lebih aman
- App name: `Cutiezy Agen`
- Package name: `id.cutiezy.agen`
