package com.rccar.controller.ui.settings;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.rccar.controller.R;
import com.rccar.controller.model.AppSettings;
import com.rccar.controller.utils.ColorPickerDialog;
import com.rccar.controller.utils.UiUtils;
import com.rccar.controller.viewmodel.SettingsViewModel;

/**
 * SettingsActivity: Layar pengaturan untuk kustomisasi tampilan dan fitur sensor.
 * Pengguna dapat mengubah:
 * - Warna background, tombol aksi, dan outline tombol
 * - Ukuran semua tombol sekaligus (dengan SeekBar)
 * - Status sensor gas (On/Off)
 * - Reset ke style default
 */
public class SettingsActivity extends AppCompatActivity {

    private SettingsViewModel viewModel;

    // View untuk color preview
    private View vPreviewBg;
    private View vPreviewBtn;
    private View vPreviewOutline;

    // View untuk ukuran tombol
    private SeekBar seekBarButtonSize;
    private TextView tvButtonSizeValue;

    // Switch sensor gas
    private SwitchCompat switchGasSensor;

    // Tombol reset default
    private Button btnSetDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Inisialisasi ViewModel
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        // Setup Toolbar dengan tombol back
        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pengaturan");
        }

        // Inisialisasi semua view
        initViews();

        // Setup event listener
        setupListeners();

        // Observasi perubahan settings
        observeViewModel();
    }

    /** Mendapatkan referensi semua view dari layout */
    private void initViews() {
        vPreviewBg      = findViewById(R.id.view_color_bg);
        vPreviewBtn     = findViewById(R.id.view_color_btn);
        vPreviewOutline = findViewById(R.id.view_color_outline);
        seekBarButtonSize = findViewById(R.id.seekbar_button_size);
        tvButtonSizeValue = findViewById(R.id.tv_button_size_value);
        switchGasSensor   = findViewById(R.id.switch_gas_sensor);
        btnSetDefault     = findViewById(R.id.btn_set_default);

        // Setup range SeekBar sesuai konstanta AppSettings
        int min = AppSettings.MIN_BUTTON_SIZE;
        int max = AppSettings.MAX_BUTTON_SIZE;
        seekBarButtonSize.setMax(max - min);
    }

    /** Setup semua event listener */
    private void setupListeners() {

        // ---- Klik preview warna → buka ColorPickerDialog ----

        vPreviewBg.setOnClickListener(v -> {
            int currentColor = viewModel.getCurrentSettings().getBackgroundColor();
            new ColorPickerDialog(this, currentColor, color -> {
                viewModel.setBackgroundColor(color);
            }).show();
        });

        vPreviewBtn.setOnClickListener(v -> {
            int currentColor = viewModel.getCurrentSettings().getActionButtonColor();
            new ColorPickerDialog(this, currentColor, color -> {
                viewModel.setActionButtonColor(color);
            }).show();
        });

        vPreviewOutline.setOnClickListener(v -> {
            int currentColor = viewModel.getCurrentSettings().getButtonOutlineColor();
            new ColorPickerDialog(this, currentColor, color -> {
                viewModel.setButtonOutlineColor(color);
            }).show();
        });

        // ---- SeekBar ukuran tombol ----
        seekBarButtonSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser) {
                    // Konversi progress ke nilai dp yang sesungguhnya
                    int actualSize = AppSettings.MIN_BUTTON_SIZE + progress;
                    tvButtonSizeValue.setText(actualSize + " dp");
                    viewModel.setButtonSize(actualSize);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        // ---- Switch sensor gas ----
        switchGasSensor.setOnCheckedChangeListener((CompoundButton btn, boolean isChecked) -> {
            viewModel.setGasSensorEnabled(isChecked);
        });

        // ---- Tombol reset default ----
        btnSetDefault.setOnClickListener(v -> {
            viewModel.resetToDefault();
            Toast.makeText(this, "Style direset ke default", Toast.LENGTH_SHORT).show();
        });
    }

    /** Mengobservasi LiveData dari SettingsViewModel dan update UI */
    private void observeViewModel() {
        viewModel.getSettings().observe(this, settings -> {
            if (settings == null) return;

            // Update preview warna background
            vPreviewBg.setBackground(UiUtils.makeCircleDrawable(settings.getBackgroundColor()));

            // Update preview warna tombol
            vPreviewBtn.setBackground(UiUtils.makeCircleDrawable(settings.getActionButtonColor()));

            // Update preview warna outline
            vPreviewOutline.setBackground(UiUtils.makeCircleDrawable(settings.getButtonOutlineColor()));

            // Update SeekBar ukuran (konversi balik dari dp ke progress)
            int progress = settings.getButtonSize() - AppSettings.MIN_BUTTON_SIZE;
            seekBarButtonSize.setProgress(progress);
            tvButtonSizeValue.setText(settings.getButtonSize() + " dp");

            // Update switch sensor gas
            switchGasSensor.setChecked(settings.isGasSensorEnabled());
        });
    }

    /** Handle tombol back di toolbar */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
