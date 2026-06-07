package com.rccar.controller.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.rccar.controller.model.AppSettings;

/**
 * Repository untuk mengelola penyimpanan pengaturan aplikasi secara persisten.
 * Menggunakan SharedPreferences sebagai storage layer.
 * Singleton: hanya ada satu instance selama aplikasi berjalan.
 */
public class SettingsRepository {

    private static final String PREFS_NAME     = "rc_car_settings";
    private static final String KEY_BG_COLOR   = "bg_color";
    private static final String KEY_BTN_COLOR  = "button_color";
    private static final String KEY_OUTL_COLOR = "outline_color";
    private static final String KEY_BTN_SIZE   = "button_size";
    private static final String KEY_GAS_SENSOR = "gas_sensor_enabled";

    private static SettingsRepository instance;
    private final SharedPreferences prefs;

    private SettingsRepository(Context context) {
        // Gunakan applicationContext agar tidak terjadi memory leak
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Mendapatkan instance Singleton */
    public static synchronized SettingsRepository getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsRepository(context);
        }
        return instance;
    }

    /**
     * Membaca semua pengaturan dari SharedPreferences.
     * Jika belum ada data, nilai default dari AppSettings akan digunakan.
     */
    public AppSettings loadSettings() {
        int    bgColor    = prefs.getInt(KEY_BG_COLOR,   AppSettings.DEFAULT_BG_COLOR);
        int    btnColor   = prefs.getInt(KEY_BTN_COLOR,  AppSettings.DEFAULT_BUTTON_COLOR);
        int    outlColor  = prefs.getInt(KEY_OUTL_COLOR, AppSettings.DEFAULT_OUTLINE_COLOR);
        int    btnSize    = prefs.getInt(KEY_BTN_SIZE,   AppSettings.DEFAULT_BUTTON_SIZE);
        boolean gasSensor = prefs.getBoolean(KEY_GAS_SENSOR, true);

        return new AppSettings(bgColor, btnColor, outlColor, btnSize, gasSensor);
    }

    /**
     * Menyimpan seluruh pengaturan ke SharedPreferences.
     * Menggunakan apply() (asynchronous) agar tidak memblokir UI thread.
     */
    public void saveSettings(AppSettings settings) {
        prefs.edit()
            .putInt(KEY_BG_COLOR,   settings.getBackgroundColor())
            .putInt(KEY_BTN_COLOR,  settings.getActionButtonColor())
            .putInt(KEY_OUTL_COLOR, settings.getButtonOutlineColor())
            .putInt(KEY_BTN_SIZE,   settings.getButtonSize())
            .putBoolean(KEY_GAS_SENSOR, settings.isGasSensorEnabled())
            .apply();
    }

    /** Reset semua pengaturan ke nilai default pabrik */
    public void resetToDefault() {
        saveSettings(new AppSettings());
    }
}
