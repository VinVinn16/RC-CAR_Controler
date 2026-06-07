package com.rccar.controller.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.rccar.controller.R;
import com.rccar.controller.model.AppSettings;
import com.rccar.controller.model.BluetoothState;
import com.rccar.controller.ui.settings.SettingsActivity;
import com.rccar.controller.utils.UiUtils;
import com.rccar.controller.viewmodel.HomeViewModel;

/**
 * HomeActivity: Layar utama aplikasi controller RC Car.
 * Berisi 4 tombol arah, navbar dengan menu dan tombol Reconnect,
 * serta overlay alert saat gas terdeteksi.
 */
public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 100;
    private static final int REQUEST_SETTINGS = 200;

    // ViewModel
    private HomeViewModel viewModel;

    // View references
    private DrawerLayout      drawerLayout;
    private NavigationView    navigationView;
    private ImageButton       btnUp, btnDown, btnLeft, btnRight;
    private Button            btnReconnect;
    private TextView          tvStatus;
    private LinearLayout      controllerPanel;
    private View              gasAlertOverlay;
    private TextView          tvGasAlert;
    private Button            btnDismissAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inisialisasi ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Inisialisasi semua view
        initViews();

        // Setup toolbar/navbar
        setupToolbar();

        // Setup tombol kontrol dengan touch listener
        setupControlButtons();

        // Setup gas alert overlay
        setupGasAlert();

        // Observasi LiveData dari ViewModel
        observeViewModel();

        // Minta izin Bluetooth (Android 12+)
        requestBluetoothPermissionsIfNeeded();
    }

    /** Menginisialisasi semua view dari layout */
    private void initViews() {
        drawerLayout    = findViewById(R.id.drawer_layout);
        navigationView  = findViewById(R.id.navigation_view);
        btnUp           = findViewById(R.id.btn_up);
        btnDown         = findViewById(R.id.btn_down);
        btnLeft         = findViewById(R.id.btn_left);
        btnRight        = findViewById(R.id.btn_right);
        btnReconnect    = findViewById(R.id.btn_reconnect);
        tvStatus        = findViewById(R.id.tv_connection_status);
        controllerPanel = findViewById(R.id.controller_panel);
        gasAlertOverlay = findViewById(R.id.gas_alert_overlay);
        tvGasAlert      = findViewById(R.id.tv_gas_alert);
        btnDismissAlert = findViewById(R.id.btn_dismiss_alert);
    }

    /** Setup Toolbar sebagai ActionBar dengan DrawerToggle untuk menu */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }

        // Toggle untuk membuka/menutup NavigationDrawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Tombol Reconnect di toolbar
        btnReconnect.setOnClickListener(v -> {
            viewModel.reconnect();
            Toast.makeText(this, "Menghubungkan ulang...", Toast.LENGTH_SHORT).show();
        });

        // Item navigasi dalam drawer
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawers();
            if (item.getItemId() == R.id.nav_settings) {
                // Buka SettingsActivity
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, REQUEST_SETTINGS);
                return true;
            }
            return false;
        });
    }

    /** Setup touch listener pada semua tombol kontrol arah */
    private void setupControlButtons() {
        setupTouchButton(btnUp,    () -> viewModel.sendForward(),  () -> viewModel.sendStop());
        setupTouchButton(btnDown,  () -> viewModel.sendBackward(), () -> viewModel.sendStop());
        setupTouchButton(btnLeft,  () -> viewModel.sendLeft(),     () -> viewModel.sendStop());
        setupTouchButton(btnRight, () -> viewModel.sendRight(),    () -> viewModel.sendStop());
    }

    /**
     * Memberikan touch listener pada tombol.
     * Saat ditekan (ACTION_DOWN): jalankan onPress + animasi press.
     * Saat dilepas (ACTION_UP/CANCEL): jalankan onRelease + animasi release.
     */
    private void setupTouchButton(ImageButton button, Runnable onPress, Runnable onRelease) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Animasi tombol ditekan
                    v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(80).start();
                    v.setAlpha(0.7f);
                    // Kirim perintah ke Arduino
                    onPress.run();
                    // Feedback haptik
                    UiUtils.vibrate(this, 30);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Animasi tombol dilepas
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                    v.setAlpha(1f);
                    // Kirim perintah stop
                    onRelease.run();
                    break;
            }
            return true;
        });
    }

    /** Setup gas alert overlay dan tombol dismiss */
    private void setupGasAlert() {
        btnDismissAlert.setOnClickListener(v -> {
            viewModel.dismissGasAlert();
        });
    }

    /** Mengobservasi semua LiveData dari HomeViewModel */
    private void observeViewModel() {

        // Observasi status Bluetooth
        viewModel.getBluetoothState().observe(this, state -> {
            updateBluetoothStatusUI(state);
        });

        // Observasi pengaturan tampilan
        viewModel.getAppSettings().observe(this, settings -> {
            applySettings(settings);
        });

        // Observasi status gas alert
        viewModel.getGasAlertActive().observe(this, isActive -> {
            if (isActive != null) {
                gasAlertOverlay.setVisibility(isActive ? View.VISIBLE : View.GONE);
                if (isActive) {
                    // Animasi fade-in alert
                    gasAlertOverlay.setAlpha(0f);
                    gasAlertOverlay.animate().alpha(1f).setDuration(300).start();
                    // Getar panjang sebagai peringatan
                    UiUtils.vibrate(this, 500);
                }
            }
        });

        // Observasi nilai sensor gas untuk tampilkan di alert
        viewModel.getGasValue().observe(this, value -> {
            if (value != null) {
                tvGasAlert.setText("⚠ Gas Berbahaya Terdeteksi!\nNilai Sensor: " + value);
            }
        });
    }

    /**
     * Mengupdate tampilan status koneksi Bluetooth di toolbar.
     */
    private void updateBluetoothStatusUI(BluetoothState state) {
        switch (state) {
            case CONNECTING:
                tvStatus.setText("Menghubungkan...");
                tvStatus.setTextColor(0xFFFFAA00);
                break;
            case CONNECTED:
                tvStatus.setText("● Terhubung");
                tvStatus.setTextColor(0xFF44FF88);
                break;
            case DISCONNECTED:
                tvStatus.setText("● Tidak Terhubung");
                tvStatus.setTextColor(0xFFFF5555);
                break;
            case ERROR:
                tvStatus.setText("✕ Gagal Terhubung");
                tvStatus.setTextColor(0xFFFF3333);
                Toast.makeText(this, "Gagal terhubung ke Bluetooth", Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * Menerapkan pengaturan dari ViewModel ke tampilan.
     * Mengubah warna background, warna tombol, outline, dan ukuran tombol.
     */
    private void applySettings(AppSettings settings) {
        // ---- Background panel controller ----
        // Buat gradient dari warna yang dipilih ke warna lebih gelap
        int bgColor  = settings.getBackgroundColor();
        int bgColor2 = darkenColor(bgColor, 0.6f); // Versi lebih gelap untuk gradien

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{bgColor, bgColor2});
        gradient.setCornerRadius(UiUtils.dpToPx(this, 20));
        controllerPanel.setBackground(gradient);

        // ---- Tombol aksi: warna dan outline ----
        int btnColor  = settings.getActionButtonColor();
        int outColor  = settings.getButtonOutlineColor();
        int strokePx  = UiUtils.dpToPx(this, 3);

        // Terapkan ke semua 4 tombol
        ImageButton[] buttons = {btnUp, btnDown, btnLeft, btnRight};
        int sizePx = UiUtils.dpToPx(this, settings.getButtonSize());
        for (ImageButton btn : buttons) {
            btn.setBackground(UiUtils.makeCircleDrawable(btnColor, outColor, strokePx));
            UiUtils.applyButtonSize(btn, sizePx);
        }
    }

    /**
     * Menggelapkan warna dengan faktor tertentu (untuk membuat efek gradien).
     * @param color   Warna asli dalam format ARGB
     * @param factor  Faktor 0.0 (hitam total) sampai 1.0 (warna asli)
     */
    private int darkenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int)(((color >> 16) & 0xFF) * factor);
        int g = (int)(((color >> 8)  & 0xFF) * factor);
        int b = (int)((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Minta izin Bluetooth saat pertama kali buka (Android 12+) */
    private void requestBluetoothPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean hasConnect = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
            boolean hasScan = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;

            if (!hasConnect || !hasScan) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        },
                        REQUEST_BLUETOOTH_PERMISSIONS);
                return; // Tunggu hasil permission dulu
            }
        }
        // Jika sudah ada izin, langsung koneksi
        viewModel.connect();
    }

    /** Callback hasil permintaan izin */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                viewModel.connect();
            } else {
                Toast.makeText(this, "Izin Bluetooth diperlukan!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /** Callback kembali dari SettingsActivity */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SETTINGS) {
            // Reload pengaturan karena mungkin sudah berubah di SettingsActivity
            viewModel.reloadSettings();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ViewModel menangani disconnect saat onCleared()
    }
}
