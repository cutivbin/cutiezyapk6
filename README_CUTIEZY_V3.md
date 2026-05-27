# Cutiezy Agen WebView v3

Perbaikan utama:

1. Loader transisi halus saat pindah halaman: halaman meredup, muncul loader, lalu terang kembali setelah selesai.
2. Link custom seperti `my.bluetoothprint.scheme://...` tidak lagi dibuka di WebView sehingga tidak memunculkan halaman web tidak tersedia.
3. Jika aplikasi Bluetooth Print terpasang, link struk Bluetooth dibuka ke aplikasi tersebut seperti di Chrome.
4. Jika aplikasi Bluetooth Print tidak terpasang, aplikasi memakai printer Bluetooth native bawaan Cutiezy.
5. Contact Picker native diperbaiki agar hasil kontak mengembalikan `tel` dalam bentuk array, sesuai kode web yang membaca `contact.tel[0]`.
6. Error halaman utama otomatis dialihkan ke `https://cutiezy.id/index`.
7. Version code dinaikkan ke 4 dan target SDK tetap 35 untuk kebutuhan Play Store.

Build debug APK:

```bash
gradle :app:assembleDebug --stacktrace --no-daemon
```

Build Play Store AAB perlu GitHub Secrets:

- ANDROID_KEYSTORE_BASE64
- ANDROID_KEYSTORE_PASSWORD
- ANDROID_KEY_ALIAS
- ANDROID_KEY_PASSWORD
