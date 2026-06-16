# RC Car Controller

Aplikasi Android untuk mengendalikan RC Car berbasis Arduino secara nirkabel via Bluetooth, dilengkapi fitur pemantauan gas berbahaya menggunakan sensor MQ-02.

## Deskripsi Proyek

Sistem ini terdiri dari dua komponen utama:

- **Software** — Aplikasi Android (Java, MVVM) yang berfungsi sebagai remote control dan dashboard pemantauan gas.
- **Hardware** — RC Car berbasis Arduino UNO R3 yang dilengkapi sensor gas MQ-02, modul Bluetooth HC-06, motor driver L298N, dan buzzer.

Pengguna dapat mengendalikan arah gerak RC Car melalui 4 tombol arah (maju, mundur, kiri, kanan) di aplikasi. Sensor MQ-02 secara real-time memantau kadar gas di sekitar kendaraan. Apabila gas berbahaya terdeteksi (nilai ADC ≥ 375), buzzer pada hardware akan berbunyi dan aplikasi menampilkan notifikasi peringatan.

### Fitur Utama

| Fitur | Keterangan |
|-------|-----------|
| Kontrol RC Car | Gerak maju, mundur, kiri, kanan via 4 tombol arah |
| Pemantauan Gas | Pembacaan sensor MQ-02 secara real-time |
| Peringatan Gas | Overlay alert + buzzer saat gas berbahaya terdeteksi |
| Koneksi Bluetooth | Pairing & reconnect ke modul HC-06 |
| Pengaturan Tema | Ganti warna background dan warna tombol |
| Ukuran Tombol | Sesuaikan ukuran tombol kontrol |
| Reset Pengaturan | Kembalikan semua pengaturan ke default |

### Komponen Hardware

| Komponen | Fungsi |
|----------|--------|
| Arduino UNO R3 | Unit kontrol utama |
| MQ-02 Sensor | Detektor gas berbahaya |
| HC-06 Bluetooth | Komunikasi nirkabel |
| L298N Motor Driver | Pengendali motor DC |
| DC Motor | Penggerak roda |
| Buzzer | Alarm peringatan gas |

---

## Instalasi

### Prasyarat

- Android Studio (versi terbaru)
- Android SDK min API 26 (Android 8.0 Oreo)
- Arduino IDE
- Perangkat Android dengan Bluetooth
- Hardware RC Car yang sudah dirakit

### 1. Upload Kode ke Arduino

1. Buka **Arduino IDE**.
2. Buka file `Hardware/RC-Car_HardwareCode.cpp`.
3. Sambungkan Arduino UNO R3 ke komputer via USB.
4. Pilih board **Arduino UNO** dan port yang sesuai di menu *Tools*.
5. Klik **Upload**.

### 2. Instal Aplikasi Android

**Opsi A — Build dari Source:**

1. Buka **Android Studio**.
2. Pilih *Open* lalu arahkan ke folder `Software/RCCarController`.
3. Tunggu proses Gradle sync selesai.
4. Aktifkan **Developer Mode** & **USB Debugging** di perangkat Android.
5. Hubungkan perangkat ke komputer via USB.
6. Klik **Run** (▶) untuk build dan install ke perangkat.

**Opsi B — Install APK (sudah tersedia):**

1. Salin file APK berikut ke perangkat Android:
   ```
   Software/RCCarController/app/build/outputs/apk/debug/app-debug.apk
   ```
2. Aktifkan *Install dari Sumber Tidak Dikenal* di pengaturan perangkat.
3. Buka file APK dan ikuti proses instalasi.

---

## Cara Penggunaan

### Persiapan Awal

1. Pastikan hardware RC Car sudah menyala dan HC-06 dalam kondisi aktif (lampu indikator HC-06 berkedip).
2. Buka aplikasi **RC Car Controller** di perangkat Android.
3. Saat diminta, **izinkan akses Bluetooth** pada dialog permission.

### Menghubungkan Bluetooth

Aplikasi akan otomatis mencoba terhubung ke HC-06 saat pertama kali dibuka. Jika gagal:

1. Pastikan HC-06 sudah di-*pair* terlebih dahulu melalui menu Bluetooth di pengaturan Android.
   > PIN default HC-06: `1234`
2. Tap tombol **Reconnect** di toolbar untuk mencoba ulang.

### Mengendalikan RC Car

Aplikasi beroperasi dalam mode **landscape**. Gunakan 4 tombol arah:

- **↑ (Atas)** — RC Car bergerak maju. Tahan untuk terus maju, lepas untuk berhenti.
- **↓ (Bawah)** — RC Car bergerak mundur.
- **← (Kiri)** — RC Car belok kiri.
- **→ (Kanan)** — RC Car belok kanan.

RC Car akan **berhenti otomatis** saat tombol dilepas.

### Pemantauan Gas

- Nilai sensor MQ-02 ditampilkan secara real-time.
- Jika nilai ADC sensor ≥ 375, aplikasi menampilkan **overlay peringatan** dan buzzer pada hardware akan berbunyi.
- Tap **Dismiss** pada overlay untuk menutup peringatan.
- Peringatan dapat dinonaktifkan melalui menu **Settings**.

### Pengaturan (Settings)

Buka menu **≡ (drawer)** lalu pilih **Settings** untuk:

- **Tema Warna** — Ganti warna background panel dan warna tombol.
- **Ukuran Tombol** — Sesuaikan ukuran tombol kontrol.
- **Nonaktifkan Alert** — Matikan notifikasi peringatan gas.
- **Reset** — Kembalikan semua pengaturan ke nilai default.

---

## Struktur Proyek

```
RC-CAR_Controler/
├── Hardware/
│   └── RC-Car_HardwareCode.cpp          # Kode Arduino (C++)
└── Software/
    └── RCCarController/                 # Proyek Android Studio
        └── app/src/main/java/com/rccar/controller/
            ├── ui/
            │   ├── splash/SplashActivity.java
            │   ├── home/HomeActivity.java
            │   └── settings/SettingsActivity.java
            ├── viewmodel/
            │   ├── HomeViewModel.java
            │   └── SettingsViewModel.java
            ├── repository/
            │   ├── BluetoothRepository.java
            │   └── SettingsRepository.java
            ├── model/
            │   ├── AppSettings.java
            │   └── BluetoothState.java
            └── utils/
                ├── UiUtils.java
                └── ColorPickerDialog.java
```

---

## Teknologi

- **Android** — Java, minSdk 26 (Android 8.0), targetSdk 34, MVVM Architecture
- **Hardware** — C++ (Arduino IDE)
- **Komunikasi** — Bluetooth Classic SPP (HC-06, UUID: `00001101-0000-1000-8000-00805F9B34FB`)

---

## Catatan

> MAC address HC-06 di-hardcode di `HomeViewModel.java`. Sesuaikan dengan MAC address modul HC-06 yang digunakan jika berbeda, lalu rebuild APK.

## Team Member
- Vincent Hon