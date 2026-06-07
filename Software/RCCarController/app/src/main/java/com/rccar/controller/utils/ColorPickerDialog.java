package com.rccar.controller.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rccar.controller.R;

/**
 * Dialog custom untuk memilih warna menggunakan color wheel + brightness slider.
 * Tampilan mirip dengan gambar referensi (color wheel bulat + slider di bawah).
 */
public class ColorPickerDialog extends Dialog {

    private ColorWheelView colorWheelView;
    private SeekBar brightnessSeekBar;
    private View    previewColor;
    private Button  btnCancel;
    private Button  btnConfirm;

    private float[] currentHsv = {0f, 1f, 1f}; // Hue, Saturation, Value

    private OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public ColorPickerDialog(Context context, int initialColor, OnColorSelectedListener listener) {
        super(context, R.style.ColorPickerDialogTheme);
        this.listener = listener;
        Color.colorToHSV(initialColor, currentHsv);
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        buildUI();
    }

    /** Membangun UI dialog secara programmatic */
    private void buildUI() {
        Context ctx = getContext();

        // Root layout
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF2A2A2A);

        int pad = dp(24);
        root.setPadding(pad, pad, pad, pad);

        // ---- Judul ----
        TextView title = new TextView(ctx);
        title.setText("Pilih Warna");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22f);
        title.setPadding(0, 0, 0, dp(16));
        root.addView(title);

        // ---- Color Wheel ----
        colorWheelView = new ColorWheelView(ctx);
        int wheelSize = dp(260);
        LinearLayout.LayoutParams wheelParams = new LinearLayout.LayoutParams(wheelSize, wheelSize);
        wheelParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        wheelParams.bottomMargin = dp(20);
        colorWheelView.setLayoutParams(wheelParams);
        colorWheelView.setHsv(currentHsv);
        colorWheelView.setOnHueSatChangedListener((h, s) -> {
            currentHsv[0] = h;
            currentHsv[1] = s;
            updatePreview();
        });
        root.addView(colorWheelView);

        // ---- Row: Preview + Brightness Slider ----
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(24));

        // Lingkaran preview warna
        previewColor = new View(ctx);
        int previewSize = dp(56);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(previewSize, previewSize);
        previewParams.rightMargin = dp(16);
        previewColor.setLayoutParams(previewParams);
        previewColor.setBackground(makeCircleDrawable(Color.HSVToColor(currentHsv)));
        row.addView(previewColor);

        // Brightness slider
        brightnessSeekBar = new SeekBar(ctx);
        LinearLayout.LayoutParams seekParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        brightnessSeekBar.setLayoutParams(seekParams);
        brightnessSeekBar.setMax(100);
        brightnessSeekBar.setProgress((int)(currentHsv[2] * 100));
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                currentHsv[2] = progress / 100f;
                updatePreview();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
        row.addView(brightnessSeekBar);
        root.addView(row);

        // ---- Tombol Batal & Konfirmasi ----
        LinearLayout btnRow = new LinearLayout(ctx);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(android.view.Gravity.END);

        btnCancel = new Button(ctx);
        btnCancel.setText("Batal");
        btnCancel.setTextColor(Color.WHITE);
        btnCancel.setBackgroundColor(Color.parseColor("#444444"));
        btnCancel.setOnClickListener(v -> dismiss());

        btnConfirm = new Button(ctx);
        btnConfirm.setText("Konfirmasi");
        btnConfirm.setTextColor(Color.WHITE);
        btnConfirm.setBackgroundColor(
                Color.HSVToColor(currentHsv)
        );
        btnConfirm.setPadding(dp(24), dp(12), dp(24), dp(12));
        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onColorSelected(Color.HSVToColor(currentHsv));
            }
            dismiss();
        });

        btnRow.addView(btnCancel);
        btnRow.addView(btnConfirm);
        root.addView(btnRow);

        android.widget.ScrollView scrollView =
                new android.widget.ScrollView(ctx);

        scrollView.addView(root);

        setContentView(scrollView);

        // Set lebar dialog
        if (getWindow() != null) {
            getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        updatePreview();
        updateBrightnessSlider();
    }

    /** Update tampilan preview sesuai warna yang dipilih */
    private void updatePreview() {
        int color = Color.HSVToColor(currentHsv);

        previewColor.setBackground(makeCircleDrawable(color));

        btnConfirm.setBackgroundColor(color);

        updateBrightnessSlider();
    }

    /** Update warna latar slider brightness sesuai hue saat ini */
    private void updateBrightnessSlider() {
        // Ubah thumb color sesuai warna saat ini
        int color = Color.HSVToColor(currentHsv);
        brightnessSeekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        brightnessSeekBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    /** Membuat drawable lingkaran dengan warna tertentu */
    private android.graphics.drawable.GradientDrawable makeCircleDrawable(int color) {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        d.setColor(color);
        return d;
    }

    /** Konversi dp ke piksel */
    private int dp(int dp) {
        return Math.round(dp * getContext().getResources().getDisplayMetrics().density);
    }

    // ============================================================
    // Inner Class: Color Wheel View
    // ============================================================

    /**
     * View custom yang menggambar color wheel (lingkaran HSV).
     * Hue ditentukan oleh sudut, Saturation oleh jarak dari pusat.
     */
    public static class ColorWheelView extends View {

        private Paint  paint;
        private Bitmap wheelBitmap;
        private float  hue;
        private float  saturation;
        private float  cursorX, cursorY;
        private int    radius;
        private OnHueSatChangedListener listener;

        public interface OnHueSatChangedListener {
            void onHueSatChanged(float hue, float saturation);
        }

        public ColorWheelView(Context context) { super(context); init(); }
        public ColorWheelView(Context context, AttributeSet attrs) { super(context, attrs); init(); }

        private void init() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        public void setOnHueSatChangedListener(OnHueSatChangedListener l) { this.listener = l; }

        public void setHsv(float[] hsv) {
            this.hue = hsv[0];
            this.saturation = hsv[1];
            invalidate();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            radius = Math.min(w, h) / 2;
            buildWheelBitmap(w, h);
            updateCursorPosition();
        }

        /** Membangun bitmap color wheel menggunakan SweepGradient dan RadialGradient */
        private void buildWheelBitmap(int w, int h) {
            wheelBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(wheelBitmap);
            float cx = w / 2f, cy = h / 2f;

            // Sweep gradient untuk Hue (warna melingkar)
            int[] hueColors = new int[361];
            for (int i = 0; i <= 360; i++) {
                hueColors[i] = Color.HSVToColor(new float[]{i, 1f, 1f});
            }
            Shader sweepShader = new SweepGradient(cx, cy, hueColors, null);

            // Radial gradient untuk Saturation (putih ke transparan dari tengah ke tepi)
            Shader radialShader = new RadialGradient(cx, cy, radius,
                    Color.WHITE, Color.TRANSPARENT, Shader.TileMode.CLAMP);

            // Gabungkan kedua shader
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setShader(new ComposeShader(sweepShader, radialShader, PorterDuff.Mode.SCREEN));
            canvas.drawCircle(cx, cy, radius, p);
        }

        /** Menghitung posisi kursor dari nilai HSV saat ini */
        private void updateCursorPosition() {
            float cx = getWidth() / 2f, cy = getHeight() / 2f;
            double angle = Math.toRadians(hue);
            cursorX = (float)(cx + saturation * radius * Math.cos(angle));
            cursorY = (float)(cy + saturation * radius * Math.sin(angle));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (wheelBitmap == null) return;

            // Gambar color wheel
            canvas.drawBitmap(wheelBitmap, 0, 0, null);

            // Gambar lingkaran cursor
            Paint cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            cursorPaint.setStyle(Paint.Style.STROKE);
            cursorPaint.setColor(Color.WHITE);
            cursorPaint.setStrokeWidth(3f);
            canvas.drawCircle(cursorX, cursorY, 14f, cursorPaint);
            cursorPaint.setStyle(Paint.Style.FILL);
            cursorPaint.setColor(Color.BLACK);
            cursorPaint.setAlpha(80);
            canvas.drawCircle(cursorX, cursorY, 13f, cursorPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float cx = getWidth() / 2f, cy = getHeight() / 2f;
            float dx = event.getX() - cx;
            float dy = event.getY() - cy;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist > radius) {
                // Clamp ke tepi lingkaran
                float scale = radius / dist;
                dx *= scale;
                dy *= scale;
                dist = radius;
            }

            // Hitung Hue dari sudut (dalam derajat)
            hue = (float)(Math.toDegrees(Math.atan2(dy, dx)));
            if (hue < 0) hue += 360;

            // Hitung Saturation dari jarak ke pusat
            saturation = dist / radius;

            cursorX = cx + dx;
            cursorY = cy + dy;

            if (listener != null) listener.onHueSatChanged(hue, saturation);
            invalidate();
            return true;
        }
    }
}
