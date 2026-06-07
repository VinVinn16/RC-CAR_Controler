# RC-Car Controller — Setup Guide Lengkap

## 📁 Struktur Folder Project

```
RCCarController/
├── app/
│   ├── build.gradle                          ← Dependency & config app
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml               ← Izin & deklarasi Activity
│       ├── java/com/rccar/controller/
│       │   ├── model/
│       │   │   ├── AppSettings.java          ← Data class pengaturan
│       │   │   └── BluetoothState.java       ← Enum status Bluetooth
│       │   ├── repository/
│       │   │   ├── BluetoothRepository.java  ← Kelola koneksi BT
│       │   │   └── SettingsRepository.java   ← Simpan/baca pengaturan
│       │   ├── viewmodel/
│       │   │   ├── HomeViewModel.java        ← Logic HomeActivity
│       │   │   └── SettingsViewModel.java    ← Logic SettingsActivity
│       │   ├── ui/
│       │   │   ├── splash/SplashActivity.java
│       │   │   ├── home/HomeActivity.java    ← Layar utama controller
│       │   │   └── settings/SettingsActivity.java
│       │   └── utils/
│       │       ├── ColorPickerDialog.java    ← Dialog pilih warna custom
│       │       └── UiUtils.java             ← Helper konversi dp, vibrate, dll
│       └── res/
│           ├── layout/
│           │   ├── activity_splash.xml
│           │   ├── activity_home.xml
│           │   ├── activity_settings.xml
│           │   └── nav_header.xml
│           ├── drawable/
│           │   ├── bg_controller_panel.xml   ← Gradien panel ungu
│           │   ├── btn_action.xml            ← Style default tombol
│           │   ├── circle_color_preview.xml  ← Preview warna settings
│           │   ├── ic_arrow_up/down/left/right.xml
│           │   ├── ic_settings.xml
│           │   └── ic_launcher_foreground.xml
│           ├── menu/nav_menu.xml
│           ├── mipmap-*/ic_launcher*.xml
│           └── values/
│               ├── strings.xml
│               ├── colors.xml
│               └── themes.xml
├── build.gradle                              ← Project-level gradle
├── settings.gradle
└── gradle.properties
```

---

## ⚙️ Step-by-Step Setup di Android Studio Iguana

### Step 1 — Import Project
1. Buka **Android Studio Iguana**
2. Pilih **"Open"** (bukan New Project)
3. Arahkan ke folder `RCCarController/`
4. Klik **OK** dan tunggu Gradle sync selesai

### Step 2 — Ganti MAC Address Arduino
Buka file:
```
app/src/main/java/com/rccar/controller/viewmodel/HomeViewModel.java
```
Cari baris:
```java
private static final String DEVICE_MAC = "98:DA:60:0C:F5:DE";
```
Ganti dengan MAC address HC-05/HC-06 Anda.

**Cara cari MAC address:**
- Android Settings → Bluetooth → Paired Devices → tap nama HC-05/HC-06 → lihat MAC Address
- Atau cek label fisik di modul HC-05/HC-06

### Step 3 — (Opsional) Sesuaikan Ambang Sensor Gas
Buka `HomeViewModel.java`, cari:
```java
private static final int GAS_THRESHOLD = 400;
```
Nilai 0–1023 dari sensor ADC Arduino. Sesuaikan dengan kondisi sensor Anda:
- Nilai rendah (~200) = lebih sensitif
- Nilai tinggi (~700) = kurang sensitif

### Step 4 — Pasangkan HC-05/HC-06 dengan Android
1. Nyalakan Arduino + modul Bluetooth
2. Android: Settings → Bluetooth → Scan
3. Pilih **HC-05** atau **HC-06**
4. PIN pairing: `1234` atau `0000`
5. Pastikan status: **Paired** (bukan hanya Connected)

### Step 5 — Run Aplikasi
1. Hubungkan HP Android via USB
2. Aktifkan **Developer Options** + **USB Debugging**
3. Klik **Run** (▶) di Android Studio
4. Pilih perangkat Anda
5. Aplikasi akan terbuka otomatis di HP

### Step 6 — Izin Bluetooth (Runtime)
Saat pertama buka, aplikasi akan meminta izin:
- **Bluetooth Connect** → Izinkan
- **Bluetooth Scan** → Izinkan

Jika ditolak, masuk Settings HP → Apps → RC-Car Controller → Permissions → aktifkan semua.

---

## 🔧 Kode Arduino (Referensi)

```cpp
#include <SoftwareSerial.h>

SoftwareSerial BT(10, 11); // RX, TX
int motorPin1 = 2, motorPin2 = 3; // Motor A
int motorPin3 = 4, motorPin4 = 5; // Motor B
int gasSensorPin = A0;
int buzzerPin = 8;
int GAS_THRESHOLD = 400;

void setup() {
  BT.begin(9600);
  Serial.begin(9600);
  pinMode(motorPin1, OUTPUT);
  pinMode(motorPin2, OUTPUT);
  pinMode(motorPin3, OUTPUT);
  pinMode(motorPin4, OUTPUT);
  pinMode(buzzerPin, OUTPUT);
}

void loop() {
  // Baca sensor gas dan kirim ke Android setiap 500ms
  static unsigned long lastSend = 0;
  if (millis() - lastSend > 500) {
    int gasVal = analogRead(gasSensorPin);
    BT.println(gasVal); // Kirim ke Android
    // Aktifkan buzzer jika gas melebihi threshold
    digitalWrite(buzzerPin, gasVal > GAS_THRESHOLD ? HIGH : LOW);
    lastSend = millis();
  }

  // Terima perintah dari Android
  if (BT.available()) {
    char cmd = BT.read();
    switch (cmd) {
      case 'F': forward(); break;
      case 'B': backward(); break;
      case 'L': turnLeft(); break;
      case 'R': turnRight(); break;
      case 'S': stopCar(); break;
    }
  }
}

void forward()  { digitalWrite(motorPin1,HIGH); digitalWrite(motorPin2,LOW);
                  digitalWrite(motorPin3,HIGH); digitalWrite(motorPin4,LOW); }
void backward() { digitalWrite(motorPin1,LOW);  digitalWrite(motorPin2,HIGH);
                  digitalWrite(motorPin3,LOW);  digitalWrite(motorPin4,HIGH); }
void turnLeft() { digitalWrite(motorPin1,LOW);  digitalWrite(motorPin2,HIGH);
                  digitalWrite(motorPin3,HIGH); digitalWrite(motorPin4,LOW); }
void turnRight(){ digitalWrite(motorPin1,HIGH); digitalWrite(motorPin2,LOW);
                  digitalWrite(motorPin3,LOW);  digitalWrite(motorPin4,HIGH); }
void stopCar()  { digitalWrite(motorPin1,LOW);  digitalWrite(motorPin2,LOW);
                  digitalWrite(motorPin3,LOW);  digitalWrite(motorPin4,LOW); }
```

---

## 🎮 Cara Penggunaan Aplikasi

### HomeActivity (Layar Utama)
| Elemen | Fungsi |
|--------|--------|
| Tombol kiri atas | Maju (FORWARD) |
| Tombol kiri bawah | Mundur (BACKWARD) |
| Tombol kanan kiri | Belok kiri (LEFT) |
| Tombol kanan kanan | Belok kanan (RIGHT) |
| Tombol **Reconnect** | Putuskan + sambung ulang Bluetooth |
| Menu (☰) | Buka drawer menu |

### SettingsActivity (Menu → Pengaturan)
| Elemen | Fungsi |
|--------|--------|
| Lingkaran **Latar Belakang** | Tap → pilih warna background panel |
| Lingkaran **Tombol Aksi** | Tap → pilih warna isian tombol |
| Lingkaran **Outline Tombol** | Tap → pilih warna border tombol |
| SeekBar **Ukuran Tombol** | Geser → ubah ukuran semua tombol (60–130 dp) |
| Switch **Sensor Gas** | On/Off deteksi gas berbahaya |
| Tombol **Set as Default Style** | Reset semua ke warna & ukuran default |

### Gas Alert
- Muncul overlay merah saat sensor gas ≥ threshold (400 by default)
- Buzzer Arduino ikut berbunyi
- Tekan **"Tutup Peringatan"** untuk menutup overlay
- Alert tidak muncul jika Sensor Gas dimatikan di Settings

---

## ❓ Troubleshooting

| Masalah | Solusi |
|---------|--------|
| "Gagal terhubung" | Pastikan HC-05/06 sudah dipair, MAC address benar, Arduino menyala |
| App tidak bisa rotate landscape | Normal — app dikunci landscape by design |
| Tombol tidak merespons | Cek izin Bluetooth, pastikan connected dulu |
| Gas alert terus muncul | Sesuaikan `GAS_THRESHOLD` di `HomeViewModel.java` atau matikan sensor di Settings |
| Gradle sync error | Pastikan Android Studio Iguana + SDK 34 terinstall |

---

## 📋 Minimum Requirements

- **Android:** 8.0 (API 26) ke atas
- **Hardware:** Bluetooth 2.0+ (Classic BT, bukan BLE)
- **Arduino:** HC-05 atau HC-06 (SPP/Serial Profile)
- **Android Studio:** Iguana (2023.2.1) atau lebih baru
- **Gradle:** 8.4 / AGP 8.3.0
