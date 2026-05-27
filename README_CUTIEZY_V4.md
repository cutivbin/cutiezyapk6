# Cutiezy Agen WebView v4

Perubahan v4:

1. Pull down / slide ke bawah untuk refresh halaman.
2. Tombol Unduh Struk di halaman struk kembali ditangani native APK. File disimpan ke Download/Cutiezy.
3. Transisi halaman lebih halus: halaman baru masuk dengan slide lembut, back bergerak sebaliknya.
4. Jika sedang di https://cutiezy.id/index lalu menekan Back, aplikasi keluar/masuk background dan tidak kembali ke halaman error.
5. Tetap mempertahankan fitur v3: kontak native, kamera/upload, share gambar WhatsApp, print bluetooth scheme, fallback ke index saat halaman error.

Build APK testing lewat GitHub Actions: `Build Cutiezy Agen APK`.
Build Play Store AAB: `Build Play Store Signed AAB` setelah mengisi GitHub Secrets keystore.
