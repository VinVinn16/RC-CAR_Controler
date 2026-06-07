package com.rccar.controller.model;

/**
 * Model class untuk menyimpan semua pengaturan aplikasi.
 * Digunakan oleh Repository dan ViewModel untuk mengelola state pengaturan.
 */
public class AppSettings {

    // Warna background utama tampilan controller (format ARGB int)
    private int backgroundColor;

    // Warna isi tombol aksi (format ARGB int)
    private int actionButtonColor;

    // Warna outline/border tombol aksi (format ARGB int)
    private int buttonOutlineColor;

    // Ukuran tombol dalam dp (dibatasi antara MIN dan MAX)
    private int buttonSize;

    // Status sensor gas: true = aktif, false = nonaktif
    private boolean gasSensorEnabled;

    // ---- Konstanta batas ukuran tombol ----
    public static final int MIN_BUTTON_SIZE = 60;
    public static final int MAX_BUTTON_SIZE = 130;
    public static final int DEFAULT_BUTTON_SIZE = 85;

    // ---- Warna default (tema ungu-biru seperti gambar referensi) ----
    public static final int DEFAULT_BG_COLOR      = 0xFF5B2D8E; // Ungu gradien
    public static final int DEFAULT_BUTTON_COLOR  = 0xFF000000; // Hitam
    public static final int DEFAULT_OUTLINE_COLOR = 0xFFFFDD00; // Kuning

    /** Constructor dengan semua nilai default */
    public AppSettings() {
        this.backgroundColor   = DEFAULT_BG_COLOR;
        this.actionButtonColor = DEFAULT_BUTTON_COLOR;
        this.buttonOutlineColor= DEFAULT_OUTLINE_COLOR;
        this.buttonSize        = DEFAULT_BUTTON_SIZE;
        this.gasSensorEnabled  = true;
    }

    /** Constructor lengkap untuk restore dari SharedPreferences */
    public AppSettings(int backgroundColor, int actionButtonColor,
                       int buttonOutlineColor, int buttonSize, boolean gasSensorEnabled) {
        this.backgroundColor    = backgroundColor;
        this.actionButtonColor  = actionButtonColor;
        this.buttonOutlineColor = buttonOutlineColor;
        this.buttonSize         = buttonSize;
        this.gasSensorEnabled   = gasSensorEnabled;
    }

    // ===================== Getter & Setter =====================

    public int getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(int c) { this.backgroundColor = c; }

    public int getActionButtonColor() { return actionButtonColor; }
    public void setActionButtonColor(int c) { this.actionButtonColor = c; }

    public int getButtonOutlineColor() { return buttonOutlineColor; }
    public void setButtonOutlineColor(int c) { this.buttonOutlineColor = c; }

    public int getButtonSize() { return buttonSize; }
    public void setButtonSize(int size) {
        // Clamp agar tetap dalam batas yang diizinkan
        this.buttonSize = Math.max(MIN_BUTTON_SIZE, Math.min(MAX_BUTTON_SIZE, size));
    }

    public boolean isGasSensorEnabled() { return gasSensorEnabled; }
    public void setGasSensorEnabled(boolean enabled) { this.gasSensorEnabled = enabled; }
}
