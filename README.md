# Word Super Lite

Prototipe awal aplikasi pengolah kata Android bergaya Microsoft Word desktop.

## Sasaran
- Minimum Android 4.2 Jelly Bean (API 17)
- Antarmuka ribbon desktop, prioritas orientasi landscape
- Offline-first
- Membuka dan menyimpan DOCX sederhana
- Mendukung keyboard fisik dan layar tablet

## Membuka proyek
1. Ekstrak ZIP.
2. Buka folder `Word-Super-Lite` melalui Android Studio.
3. Gunakan JDK 11 dan Android Gradle Plugin 7.4.2.
4. Izinkan Android Studio mengunduh komponen Gradle/Android SDK yang diperlukan.
5. Jalankan pada emulator atau perangkat Android.

`compileSdk` 33 digunakan untuk proses build; `minSdk` 17 menetapkan Android 4.2 sebagai versi minimum target. Pada Android 4.2, pembukaan memakai pemilih konten yang tersedia dan penyimpanan awal diarahkan ke folder `WordSuperLite` di penyimpanan eksternal. Android 4.4+ memakai Storage Access Framework. Perangkat lama tetap memerlukan pengujian langsung.

## Status prototipe 0.1.0
Yang sudah ada:
- Kerangka aplikasi Java native.
- Tampilan awal bergaya ribbon desktop.
- Area mengetik dokumen.
- Penghitung kata.
- Shortcut Ctrl+S, Ctrl+O, Ctrl+N, Ctrl+A.
- Pembacaan dan penulisan DOCX dasar berisi paragraf teks.

## Batasan penting
Mesin DOCX saat ini masih sangat awal. Pembaca mengambil teks paragraf dari `word/document.xml`; penulis membuat DOCX sederhana. Format karakter, tabel, gambar, header/footer, nomor halaman, footnote, daftar isi, section break, margin khusus, dan tata letak halaman belum dipertahankan. Jangan gunakan prototipe ini untuk menyunting satu-satunya salinan skripsi/dokumen penting. Simpan salinan cadangan.

## Rencana pengembangan
1. Editor rich-text dengan format per rentang teks.
2. Mesin halaman A4/F4, margin, header/footer, page number.
3. DOCX round-trip yang mempertahankan elemen yang belum didukung.
4. Tabel, gambar, daftar isi, footnote, dan section break.
5. Optimasi memori dan pengujian pada perangkat API 17 / RAM 1.5 GB.
