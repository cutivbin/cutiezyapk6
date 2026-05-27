# Cutiezy Agen v4.1 Fixed

Perbaikan build cloud:
- Menghapus dependensi AndroidX WebKit dan pemakaian WebViewCompat DOCUMENT_START_SCRIPT agar build GitHub Actions lebih stabil.
- Tetap mempertahankan pull-to-refresh, loader halus, contact picker bridge, kamera/upload, share gambar, unduh struk blob/data URL, print Bluetooth, dan fallback cutiezy.id/index.
- Version naik ke 1.0.5 / versionCode 6.
- Workflow menambahkan FORCE_JAVASCRIPT_ACTIONS_TO_NODE24 agar warning Node.js 20 tidak membingungkan.
