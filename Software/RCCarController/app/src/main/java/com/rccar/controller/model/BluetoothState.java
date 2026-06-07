package com.rccar.controller.model;

/**
 * Enum untuk merepresentasikan status koneksi Bluetooth.
 * Diobservasi oleh UI melalui LiveData di ViewModel.
 */
public enum BluetoothState {
    DISCONNECTED,  // Tidak terhubung / koneksi terputus
    CONNECTING,    // Sedang mencoba terhubung
    CONNECTED,     // Berhasil terhubung
    ERROR          // Terjadi error saat proses koneksi
}
