# 🃏 Game Card: Panduan & Spesifikasi Lengkap

Dokumentasi resmi dan panduan permainan untuk game Android **Card** (`com.apksafe.cardgame`).

---

## 📌 Ringkasan Game
* **Nama Aplikasi**: Card
* **Package ID**: `com.apksafe.cardgame`
* **Arsitektur**: Kotlin, Jetpack Compose, Room Database, Material Design 3
* **Model Jaringan**: **Real P2P LAN/Wi-Fi Socket Multiplayer** (Tanpa Firebase & Tanpa Akun Dummy)

---

## 🛡️ 1. Sistem Akun Lokal Nyata (Bukan Dummy / Palsu)
Game menggunakan akun lokal nyata yang didaftarkan langsung oleh pengguna di perangkat:

1. **Hashing Kata Sandi**:
   * Setiap akun baru menghasilkan *cryptographic random salt* (16 byte).
   * Password di-hash menggunakan algoritma **SHA-256** bersama salt (`SHA-256(password + salt)`).
   * Password teks mentah tidak pernah disimpan di database.
2. **Tanpa Akun Palsu (Zero Mock/Fake Accounts)**:
   * Database **tidak** menyuntikkan profil bot atau akun fiktif.
   * Hanya akun riil yang didaftarkan oleh pengguna yang tersimpan di database lokal Room (`CardDatabase`).
   * Riwayat kemenangan (*Wins*), kekalahan (*Losses*), dan level tersimpan secara persisten di penyimpanan aman lokal perangkat.

---

## 🌐 2. Multiplayer P2P Asli & Tanpa Firebase (Wi-Fi / Hotspot / Data)

### A. Larangan Akun dari Perangkat yang Sama
* Setiap akun yang didaftarkan ditandai dengan **Device ID** unik (`originDeviceId`).
* **Hotseat / Multiplayer Constraint**: Akun yang dibuat di perangkat fisik yang sama **dilarang saling bertanding** (`BLOCKED (Same Device)`). Hal ini memastikan pemain harus bertanding melawan perangkat lain.

### B. Real P2P Discovery & Socket (Bebas Firebase)
Untuk menghubungkan dua perangkat nyata di dunia nyata tanpa bergantung pada layanan cloud pihak ketiga seperti Firebase:
1. **UDP Broadcast Auto-Discovery (Port 8988)**:
   * Saat berada di Game Lobby, perangkat menyiarkan paket beacon real-time berisi username, level, avatar, dan IP perangkat.
   * Perangkat lain yang berada di jaringan Wi-Fi atau Hotspot yang sama secara otomatis mendeteksi kehadiran pemain tersebut.
2. **Koneksi Langsung (Host & Gabung via IP)**:
   * Pemain dapat memilih **Buat Room (Host)**, yang membuka TCP ServerSocket pada port `8989` dan menampilkan alamat IP lokal (misal `192.168.1.15:8989` atau `192.168.43.1` saat tethering).
   * Pemain di perangkat lain dapat langsung mengetikkan IP Host untuk terhubung dan memulai pertandingan.
3. **Validasi Handshake Anti-Kecurangan**:
   * Saat terhubung, kedua perangkat bertukar ID perangkat fisik.
   * Jika terdeteksi koneksi berasal dari perangkat yang sama, server otomatis menolak koneksi (`REJECT: SAME_DEVICE_FORBIDDEN`).

### C. Persyaratan Koneksi Jaringan
* Status koneksi jaringan terpantau secara langsung:
  * 🟢 **WiFi Connected (IP Lokal Ditampilkan)**
  * 🔵 **Mobile Data / Hotspot Active**
  * 🔴 **Offline / Disconnected**
* Tombol **Pindai Lawan**, **Buat Room**, dan **Start Battle** memerlukan koneksi aktif agar dua perangkat dapat berkomunikasi.

---

## ⏱️ 3. Sistem AFK (Away From Keyboard) 4 Menit
Untuk menjaga alur permainan tetap adil dan dinamis:

* **Batas Waktu**: Setiap giliran pemain memiliki batas waktu **4 menit (240 detik)**.
* **Indikator Visual & Haptik**:
  * Timer digital menghitung mundur dengan progress bar real-time.
  * Ketika waktu tersisa kurang dari 30 detik, warna timer berubah menjadi merah menyala dan perangkat memicu getaran (*haptic vibration feedback*).
* **Auto-Switch**: Jika pemain tidak melakukan tindakan kartu dalam waktu 4 menit, giliran secara otomatis dialihkan ke lawan (*Turn Auto-Switched*).
* **Mode Uji Coba Cepat (Test Mode)**: Tersedia toggle demo 10 detik di layar pertandingan untuk mempermudah verifikasi fungsionalitas auto-switch tanpa harus menunggu 4 menit penuh.

---

## ⚔️ 4. Sistem Pertarungan & 24 Kartu Elemental

### Mekanika Dasar:
* **Health Points (HP)**: Setiap pemain memulai dengan **30 HP**.
* **Mana (Energi)**: Bertambah 1 setiap ronde (maksimal 10).
* **Shield**: Menahan damage serangan sebelum mengurangi HP.

### 5 Elemen & Keunggulan Alami:
1. 🔥 **Fire (Api)** — Unggul vs Nature (+30% bonus damage)
2. 💧 **Water (Air)** — Unggul vs Fire (+30% bonus damage)
3. 🍃 **Nature (Alam)** — Unggul vs Lightning (+30% bonus damage)
4. ⚡ **Lightning (Petir)** — Unggul vs Water (+30% bonus damage)
5. 🌌 **Void (Kekosongan)** — Memberikan damage murni menembus shield

---

## 🏆 5. Pesan Kemenangan Spesial (Human Player Victory)
Ketika seorang pemain berhasil mengalahkan pemain non-bot:
* Game menampilkan dialog kemenangan perayaan khusus.
* Pesan resmi yang ditampilkan secara mencolok di layar dan log pertandingan:
  > **"Kamu menang, Selamat!"**

---

## 🎮 6. Panduan Singkat Bermain 2 Perangkat (Multiplayer Nyata)
1. Hubungkan kedua HP ke **Wi-Fi yang sama** ATAU nyalakan **Hotspot Pribadi (Tethering)** di salah satu HP dan hubungkan HP lainnya.
2. Buka game **Card** di kedua HP dan login dengan akun masing-masing.
3. Di HP 1: Masuk ke Game Lobby, tekan **Pindai Lawan** atau **Buat Room (Host)**.
4. Di HP 2: Tekan **Pindai Lawan** (profil HP 1 akan otomatis muncul) ATAU ketik IP HP 1 pada kolom **Gabung via IP**.
5. Tekan **Start Battle** untuk memulai pertarungan langsung antar perangkat nyata!

---

## 📊 7. Dashboard Statistik & Visualisasi Rasio Win/Loss (Recharts-Style DataViz)
Tab **Dashboard** menyajikan analisis performa pemain dengan visualisasi data deklaratif dan animasi dinamis:
1. **Interactive Win/Loss Donut Chart (Setara Recharts PieChart)**:
   - Dibuat menggunakan Jetpack Compose `Canvas` dengan transisi sudut `animateFloatAsState`.
   - Menampilkan irisan kemenangan (Hijau Emerald) dan kekalahan (Merah Crimson) dengan persentase di tengah lingkaran (*Win Rate*).
   - Legenda interaktif dengan rincian jumlah match dan persentase.
2. **Bar Rasio Proporsional & Metrik KPI**:
   - Menghitung **Win Rate (%)**, **W/L Ratio** (misal `3.00x`), **Total Pertandingan**, serta **Total Pergantian Giliran Akibat AFK 4 Menit**.
3. **Grafik Distribusi Penyelesaian Duel (Setara Recharts BarChart)**:
   - Visualisasi kondisi pertandingan berakhir: KO (*Penghabisan HP*), AFK Forfeit (*Batas Waktu 4 Menit*), atau Menyerah (*Surrender*).
4. **Indikator Tren Performa & Riwayat Duel Tersaring**:
   - Menampilkan urutan 8 pertandingan terakhir dalam bentuk *form badges* ([W] [L]) dan status *win streak*.
   - Filter interaktif: *Semua*, *Menang*, dan *Kalah* dengan kartu riwayat pertandingan lengkap.
   - Tombol simulasi cepat (*+ Win*, *+ Loss*) untuk kemudahan evaluasi dan pengujian animasi visualisasi.

