package com.rccar.controller.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rccar.controller.model.AppSettings;
import com.rccar.controller.repository.SettingsRepository;

/**
 * ViewModel untuk mengelola data pengaturan aplikasi.
 * Memisahkan logika bisnis dari UI (Activity).
 * Data tetap aman saat konfigurasi berubah (rotasi layar, dll).
 */
public class SettingsViewModel extends AndroidViewModel {

    private final SettingsRepository repository;

    // LiveData yang diobservasi oleh SettingsActivity
    private final MutableLiveData<AppSettings> settingsLiveData = new MutableLiveData<>();

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        repository = SettingsRepository.getInstance(application);
        // Load pengaturan dari storage saat ViewModel dibuat
        settingsLiveData.setValue(repository.loadSettings());
    }

    /** Mengembalikan LiveData yang bisa diobservasi oleh UI */
    public LiveData<AppSettings> getSettings() {
        return settingsLiveData;
    }

    /** Mendapatkan nilai settings saat ini (non-null karena sudah diinisialisasi) */
    public AppSettings getCurrentSettings() {
        AppSettings s = settingsLiveData.getValue();
        return s != null ? s : new AppSettings();
    }

    /** Mengubah warna background dan menyimpannya */
    public void setBackgroundColor(int color) {
        AppSettings s = getCurrentSettings();
        s.setBackgroundColor(color);
        saveAndNotify(s);
    }

    /** Mengubah warna tombol aksi dan menyimpannya */
    public void setActionButtonColor(int color) {
        AppSettings s = getCurrentSettings();
        s.setActionButtonColor(color);
        saveAndNotify(s);
    }

    /** Mengubah warna outline tombol dan menyimpannya */
    public void setButtonOutlineColor(int color) {
        AppSettings s = getCurrentSettings();
        s.setButtonOutlineColor(color);
        saveAndNotify(s);
    }

    /** Mengubah ukuran semua tombol sekaligus dan menyimpannya */
    public void setButtonSize(int size) {
        AppSettings s = getCurrentSettings();
        s.setButtonSize(size);
        saveAndNotify(s);
    }

    /** Mengubah status sensor gas dan menyimpannya */
    public void setGasSensorEnabled(boolean enabled) {
        AppSettings s = getCurrentSettings();
        s.setGasSensorEnabled(enabled);
        saveAndNotify(s);
    }

    /** Reset semua pengaturan ke nilai default */
    public void resetToDefault() {
        repository.resetToDefault();
        // Reload dari repository agar UI terupdate
        settingsLiveData.setValue(repository.loadSettings());
    }

    /**
     * Helper: simpan ke repository lalu notify LiveData agar UI terupdate.
     * Menggunakan setValue() karena dipanggil dari main thread.
     */
    private void saveAndNotify(AppSettings settings) {
        repository.saveSettings(settings);
        settingsLiveData.setValue(settings);
    }
}
