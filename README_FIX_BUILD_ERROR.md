# Fix Build Error v2.1

Perbaikan utama pada versi ini:

- `android.useAndroidX=true` di `gradle.properties` karena project memakai dependency `androidx.core` dan `androidx.webkit`.
- Version dinaikkan ke `1.0.2` / `versionCode 3`.
- Workflow memakai `android-actions/setup-android@v3` dan `--no-daemon` agar lebih stabil di GitHub Actions.

Jika repo lama sudah berisi v2, upload ulang semua file versi ini dan pilih replace/overwrite.
