package com.rccar.controller.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rccar.controller.model.AppSettings;
import com.rccar.controller.model.BluetoothState;
import com.rccar.controller.repository.BluetoothRepository;
import com.rccar.controller.repository.SettingsRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel untuk HomeActivity.
 * Mengelola status Bluetooth, koneksi, perintah kontrol,
 * deteksi gas, dan pengaturan UI.
 */
public class HomeViewModel extends AndroidViewModel {

    // MAC address modul HC-05/HC-06 pada Arduino
    // Ganti dengan MAC address perangkat Anda!
    private static final String DEVICE_MAC = " ";

    // Ambang batas nilai ADC sensor gas untuk memicu alert
    // Arduino mengirim nilai 0-1023; sesuaikan dengan kondisi sensor Anda
    private static final int GAS_THRESHOLD = 375;

    private final BluetoothRepository bluetoothRepo;
    private final SettingsRepository  settingsRepo;

    // Handler untuk operasi yang perlu kembali ke main thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ExecutorService untuk operasi Bluetooth di background thread
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ---- LiveData yang diobservasi oleh HomeActivity ----
    private final MutableLiveData<BluetoothState> bluetoothState = new MutableLiveData<>(BluetoothState.DISCONNECTED);
    private final MutableLiveData<AppSettings>    appSettings    = new MutableLiveData<>();
    private final MutableLiveData<Boolean>        gasAlertActive = new MutableLiveData<>(false);
    private final MutableLiveData<Integer>        gasValue       = new MutableLiveData<>(0);

    public HomeViewModel(@NonNull Application application) {
        super(application);
        bluetoothRepo = BluetoothRepository.getInstance();
        settingsRepo  = SettingsRepository.getInstance(application);

        // Load pengaturan tersimpan saat pertama kali
        appSettings.setValue(settingsRepo.loadSettings());

        // Setup listener untuk data yang masuk dari Arduino
        setupDataListener();
    }

    // ===================== Getter LiveData =====================

    public LiveData<BluetoothState> getBluetoothState() { return bluetoothState; }
    public LiveData<AppSettings>    getAppSettings()    { return appSettings; }
    public LiveData<Boolean>        getGasAlertActive() { return gasAlertActive; }
    public LiveData<Integer>        getGasValue()       { return gasValue; }

    // ===================== Koneksi Bluetooth =====================

    /**
     * Memulai koneksi Bluetooth ke Arduino di background thread.
     * Hasilnya dikirim kembali ke main thread via LiveData.
     */
    public void connect() {
        if (bluetoothState.getValue() == BluetoothState.CONNECTING) return;

        bluetoothState.setValue(BluetoothState.CONNECTING);

        executor.execute(() -> {
            boolean success = bluetoothRepo.connect(DEVICE_MAC);
            mainHandler.post(() -> {
                bluetoothState.setValue(success ? BluetoothState.CONNECTED : BluetoothState.ERROR);
            });
        });
    }

    /**
     * Memutuskan koneksi Bluetooth dan mereset status.
     */
    public void disconnect() {
        executor.execute(() -> {
            bluetoothRepo.disconnect();
            mainHandler.post(() -> {
                bluetoothState.setValue(BluetoothState.DISCONNECTED);
                gasAlertActive.setValue(false);
            });
        });
    }

    /**
     * "Reconnect" — putuskan lalu sambungkan ulang.
     * Dipanggil saat tombol "Reverse" ditekan di Navbar.
     */
    public void reconnect() {
        executor.execute(() -> {
            bluetoothRepo.disconnect();
            mainHandler.post(() -> bluetoothState.setValue(BluetoothState.DISCONNECTED));
            // Jeda sebentar sebelum reconnect
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            boolean success = bluetoothRepo.connect(DEVICE_MAC);
            mainHandler.post(() -> bluetoothState.setValue(
                    success ? BluetoothState.CONNECTED : BluetoothState.ERROR));
        });
    }

    // ===================== Perintah Kontrol =====================

    /** Kirim perintah maju */
    public void sendForward()  { bluetoothRepo.sendCommand(BluetoothRepository.CMD_FORWARD); }

    /** Kirim perintah mundur */
    public void sendBackward() { bluetoothRepo.sendCommand(BluetoothRepository.CMD_BACKWARD); }

    /** Kirim perintah belok kiri */
    public void sendLeft()     { bluetoothRepo.sendCommand(BluetoothRepository.CMD_LEFT); }

    /** Kirim perintah belok kanan */
    public void sendRight()    { bluetoothRepo.sendCommand(BluetoothRepository.CMD_RIGHT); }

    /** Kirim perintah berhenti */
    public void sendStop()     { bluetoothRepo.sendCommand(BluetoothRepository.CMD_STOP); }

    // ===================== Sensor Gas =====================

    /**
     * Setup listener untuk data yang diterima dari Arduino.
     * Arduino diharapkan mengirimkan nilai integer (0-1023) dari sensor gas.
     */
    private void setupDataListener() {
        bluetoothRepo.setDataListener(data -> {
            try {
                // Coba parse data sebagai nilai integer sensor gas
                int value = Integer.parseInt(data.trim());
                mainHandler.post(() -> {
                    gasValue.setValue(value);
                    // Cek apakah sensor gas aktif sebelum memicu alert
                    AppSettings settings = appSettings.getValue();
                    boolean sensorEnabled = settings != null && settings.isGasSensorEnabled();
                    gasAlertActive.setValue(sensorEnabled && value >= GAS_THRESHOLD);
                });
            } catch (NumberFormatException e) {
                // Data bukan angka, abaikan (mungkin pesan status lain)
            }
        });
    }

    /** Menutup gas alert secara manual oleh pengguna */
    public void dismissGasAlert() {
        gasAlertActive.setValue(false);
    }

    // ===================== Pengaturan =====================

    /**
     * Reload pengaturan dari repository (dipanggil saat kembali dari SettingsActivity).
     */
    public void reloadSettings() {
        appSettings.setValue(settingsRepo.loadSettings());
    }

    // ===================== Lifecycle =====================

    @Override
    protected void onCleared() {
        super.onCleared();
        // Bersihkan resource saat ViewModel dihancurkan
        executor.shutdown();
        bluetoothRepo.disconnect();
    }
}
