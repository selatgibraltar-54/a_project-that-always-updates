# 🃏 Game Card: Panduan & Spesifikasi Lengkap

Dokumentasi resmi dan panduan permainan untuk game Android **Card** (`com.apksafe.cardgame`).

---

## 📌 Ringkasan Game
* **Nama Aplikasi**: Card
* **Package ID**: `com.apksafe.cardgame`
* **Arsitektur**: Kotlin, Jetpack Compose, Room Database, Material Design 3
* **Model Jaringan**: Network-Aware Multiplayer & Local Offline Secure Auth

---

## 🛡️ 1. Sistem Akun Lokal Aman (Offline Security)
Game menggunakan sistem autentikasi lokal yang tersimpan secara terenkripsi di penyimpanan aman perangkat:

1. **Hashing Kata Sandi**:
   * Setiap akun baru menghasilkan *cryptographic random salt* (16 byte).
   * Password di-hash menggunakan algoritma **SHA-256** bersama salt (`SHA-256(password + salt)`).
   * Password teks mentah tidak pernah disimpan di database.
2. **Multi-Akun / Profile Switcher**:
   * Pengguna dapat mendaftarkan beberapa profil di perangkat yang sama.
   * Riwayat kemenangan (*Wins*), kekalahan (*Losses*), dan level tersimpan di database lokal Room (`CardDatabase`).

---

## 🌐 2. Aturan Multiplayer & Isolasi Perangkat

### A. Larangan Akun dari Perangkat yang Sama
* Setiap akun yang didaftarkan ditandai dengan **Device ID** unik (`originDeviceId`).
* **Hotseat / Multiplayer Constraint**: Akun yang dibuat di perangkat fisik yang sama **tidak dapat** saling bertanding (`BLOCKED (Same Device)`). Hal ini mencegah kecurangan atau manipulasi skor lokal.
* Menu **Game Lobby** memindai dan memfilter pemain: hanya profil dari perangkat lain yang terdeteksi secara eksternal (`ONLINE`) yang dapat diajak bertanding (*Start Battle*).

### B. Persyaratan Koneksi Internet (WiFi / Mobile Data)
* Komponen **NetworkMonitor** memantau status jaringan secara real-time via Android `ConnectivityManager`.
* Indikator status jaringan ditampilkan di bagian atas Game Lobby:
  * 🟢 **WiFi Connected**
  * 🔵 **Mobile Data (Cellular) Active**
  * 🔴 **Offline / Disconnected**
* Tombol **Pindai Rival** dan **Start Battle** secara otomatis dinonaktifkan jika perangkat tidak mendeteksi koneksi WiFi atau Data Seluler aktif.

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
* **Health Points (HP)**: Setiap pemain memulai dengan **100 HP**.
* **Mana (Energi)**: Bertambah 1 setiap ronde (maksimal 10).
* **Shield**: Menahan damage serangan sebelum mengurangi HP.

### 5 Elemen & Keunggulan Alami:
1. 🔥 **Fire (Api)** — Unggul vs Nature (+30% bonus damage)
2. 💧 **Water (Air)** — Unggul vs Fire (+30% bonus damage)
3. 🍃 **Nature (Alam)** — Unggul vs Lightning (+30% bonus damage)
4. ⚡ **Lightning (Petir)** — Unggul vs Water (+30% bonus damage)
5. 🌌 **Void (Kekosongan)** — Memberikan damage murni menembus shield

### Daftar Koleksi 24 Kartu:
| No | Nama Kartu | Elemen | Biaya Mana | Efek |
|:---|:---|:---|:---:|:---|
| 1 | Flame Burst | Fire | 2 | 14 Damage |
| 2 | Inferno Dragon | Fire | 5 | 28 Damage + Burn 4/turn |
| 3 | Lava Shield | Fire | 3 | +18 Shield |
| 4 | Phoenix Flare | Fire | 4 | 20 Damage & Heal 8 HP |
| 5 | Magma Golem | Fire | 6 | 32 Damage |
| 6 | Aqua Splash | Water | 2 | 12 Damage & Kurangi 1 Mana lawan |
| 7 | Tidal Wave | Water | 4 | 22 Damage |
| 8 | Frozen Barrier | Water | 3 | +22 Shield |
| 9 | Healing Springs | Water | 3 | Heal 20 HP |
| 10 | Leviathan Wrath | Water | 6 | 30 Damage & +10 Shield |
| 11 | Vine Whip | Nature | 2 | 13 Damage |
| 12 | Ancient Treant | Nature | 4 | 18 Damage & +12 Shield |
| 13 | Bark Armor | Nature | 2 | +16 Shield |
| 14 | Spore Cloud | Nature | 3 | Racun 6 damage selama 3 turn |
| 15 | Gaia Blessing | Nature | 5 | Heal 25 HP & +10 Shield |
| 16 | Spark Jolt | Lightning | 1 | 8 Damage |
| 17 | Thunder Strike | Lightning | 3 | 20 Damage |
| 18 | Storm Surge | Lightning | 4 | 24 Damage |
| 19 | Overcharge | Lightning | 2 | Tambah 2 Mana untuk turn ini |
| 20 | Plasma Cannon | Lightning | 6 | 35 Massive Damage |
| 21 | Shadow Dagger | Void | 2 | 12 Pure Damage (abaikan Shield) |
| 22 | Abyssal Rift | Void | 4 | 22 Pure Damage |
| 23 | Soul Leech | Void | 3 | Curi 12 HP lawan menjadi HP sendiri |
| 24 | Black Hole | Void | 7 | 40 Ultimate Damage ke lawan |

---

## 🏆 5. Pesan Kemenangan Spesial (Human Player Victory)
Ketika seorang pemain berhasil mengalahkan lawan pemain non-bot:
* Game menampilkan dialog kemenangan perayaan khusus.
* Pesan resmi yang ditampilkan secara mencolok di layar:
  > **"ini lebih seru dari pada judi mainini aja ok?"**

---

## 🎮 6. Panduan Singkat Memulai Permainan
1. **Buat Akun**: Buka aplikasi dan masukkan nama pengguna serta kata sandi di layar *Secure Local Authentication*.
2. **Masuk ke Game Lobby**: Pastikan WiFi atau Data Seluler aktif.
3. **Pilih Lawan Non-Device**: Pilih salah satu pemain eksternal di daftar pemain yang berstatus *ONLINE*.
4. **Mulai Pertandingan**: Tekan tombol **Start Battle**.
5. **Mainkan Kartu Anda**: Perhatikan biaya Mana dan batas waktu 4 menit agar giliran tidak terlewat otomatis.
