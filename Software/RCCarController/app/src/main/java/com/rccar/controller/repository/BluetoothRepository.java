package com.rccar.controller.repository;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Repository untuk mengelola seluruh operasi Bluetooth ke modul HC-05/HC-06.
 * Menangani koneksi, pengiriman perintah, penerimaan data sensor, dan pemutusan koneksi.
 * Singleton agar socket tidak duplikat.
 */
public class BluetoothRepository {

    private static final String TAG = "BluetoothRepository";

    // UUID standar Serial Port Profile (SPP) — digunakan semua modul HC-05/HC-06
    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // ---- Perintah yang dikirim ke Arduino ----
    public static final String CMD_FORWARD  = "F";
    public static final String CMD_BACKWARD = "B";
    public static final String CMD_LEFT     = "L";
    public static final String CMD_RIGHT    = "R";
    public static final String CMD_STOP     = "S";

    // Singleton instance
    private static BluetoothRepository instance;

    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket  bluetoothSocket;
    private OutputStream     outputStream;
    private InputStream      inputStream;

    // Listener untuk data masuk dari Arduino (misal: nilai sensor gas)
    private OnDataReceivedListener dataListener;

    // Thread pembaca data dari Arduino
    private Thread readerThread;
    private volatile boolean isReading = false;

    public interface OnDataReceivedListener {
        void onDataReceived(String data);
    }

    private BluetoothRepository() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static synchronized BluetoothRepository getInstance() {
        if (instance == null) {
            instance = new BluetoothRepository();
        }
        return instance;
    }

    public void setDataListener(OnDataReceivedListener listener) {
        this.dataListener = listener;
    }

    /** Cek apakah hardware Bluetooth tersedia di perangkat */
    public boolean isBluetoothAvailable() {
        return bluetoothAdapter != null;
    }

    /** Cek apakah Bluetooth sudah diaktifkan pengguna */
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /** Cek apakah socket sedang dalam keadaan terhubung */
    public boolean isConnected() {
        return bluetoothSocket != null && bluetoothSocket.isConnected();
    }

    /**
     * Melakukan koneksi ke perangkat Bluetooth berdasarkan MAC address.
     * ⚠️ HARUS dipanggil dari background thread karena operasi blocking!
     *
     * @param macAddress MAC address perangkat HC-05/HC-06 (contoh: "98:DA:60:0C:F5:DE")
     * @return true jika berhasil terhubung
     */
    public boolean connect(String macAddress) {
        try {
            // Putuskan koneksi lama jika masih ada
            disconnect();

            // Dapatkan objek perangkat Bluetooth dari MAC address
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);

            // Buat RFCOMM socket menggunakan UUID SPP
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);

            // Hentikan scan discovery agar koneksi lebih stabil dan cepat
            bluetoothAdapter.cancelDiscovery();

            // Lakukan koneksi — ini operasi blocking, makanya harus di background thread
            bluetoothSocket.connect();

            // Siapkan stream untuk komunikasi dua arah
            outputStream = bluetoothSocket.getOutputStream();
            inputStream  = bluetoothSocket.getInputStream();

            // Mulai thread pembaca data dari Arduino
            startReaderThread();

            Log.d(TAG, "Terhubung ke: " + macAddress);
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Gagal terhubung: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    /**
     * Mengirim perintah string ke Arduino melalui Bluetooth.
     * Aman dipanggil dari UI thread karena operasi write cepat.
     */
    public void sendCommand(String command) {
        if (outputStream == null || !isConnected()) return;
        try {
            outputStream.write(command.getBytes());
            outputStream.flush();
            Log.d(TAG, "Kirim: " + command);
        } catch (IOException e) {
            Log.e(TAG, "Gagal kirim perintah: " + e.getMessage());
        }
    }

    /**
     * Membaca data masuk dari Arduino secara terus-menerus di background thread.
     * Data yang masuk diteruskan ke listener (misal: nilai ADC sensor gas).
     */
    private void startReaderThread() {
        isReading = true;
        readerThread = new Thread(() -> {
            byte[] buffer = new byte[256];
            int bytes;
            while (isReading && isConnected()) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0 && dataListener != null) {
                        String received = new String(buffer, 0, bytes).trim();
                        dataListener.onDataReceived(received);
                    }
                } catch (IOException e) {
                    // Koneksi terputus saat membaca
                    if (isReading) {
                        Log.e(TAG, "Koneksi terputus saat membaca: " + e.getMessage());
                        isReading = false;
                    }
                    break;
                }
            }
        });
        readerThread.start();
    }

    /**
     * Memutuskan koneksi dan membersihkan semua resource.
     * Aman dipanggil berkali-kali.
     */
    public void disconnect() {
        isReading = false;
        try {
            if (outputStream != null) { outputStream.close(); outputStream = null; }
            if (inputStream  != null) { inputStream.close();  inputStream  = null; }
            if (bluetoothSocket != null) { bluetoothSocket.close(); bluetoothSocket = null; }
        } catch (IOException e) {
            Log.e(TAG, "Error saat disconnect: " + e.getMessage());
        }
    }
}
