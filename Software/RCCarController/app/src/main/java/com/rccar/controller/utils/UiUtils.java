package com.rccar.controller.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;

/**
 * Kelas utilitas berisi helper methods yang sering digunakan di seluruh aplikasi.
 */
public class UiUtils {

    /**
     * Konversi dp ke piksel berdasarkan density layar perangkat.
     */
    public static int dpToPx(Context context, int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics()));
    }

    /**
     * Membuat drawable lingkaran (circle shape) dengan warna fill dan stroke.
     *
     * @param fillColor   Warna isian lingkaran
     * @param strokeColor Warna border/outline lingkaran
     * @param strokeWidth Ketebalan border dalam piksel
     */
    public static GradientDrawable makeCircleDrawable(int fillColor, int strokeColor, int strokeWidth) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(fillColor);
        d.setStroke(strokeWidth, strokeColor);
        return d;
    }

    /**
     * Membuat drawable lingkaran sederhana tanpa stroke (untuk preview warna).
     */
    public static GradientDrawable makeCircleDrawable(int fillColor) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(fillColor);
        return d;
    }

    /**
     * Menerapkan ukuran tombol ke sebuah ImageButton menggunakan LayoutParams.
     *
     * @param button Tombol yang akan diubah ukurannya
     * @param sizePx Ukuran dalam piksel (width = height = sizePx)
     */
    public static void applyButtonSize(ImageButton button, int sizePx) {
        android.view.ViewGroup.LayoutParams params = button.getLayoutParams();
        if (params != null) {
            params.width  = sizePx;
            params.height = sizePx;
            button.setLayoutParams(params);
        }
    }

    /**
     * Memberikan getaran haptik singkat sebagai feedback saat tombol ditekan.
     * Mendukung Android 8+ (Oreo) dan di bawahnya.
     *
     * @param context Context aplikasi
     * @param durationMs Durasi getar dalam milidetik
     */
    public static void vibrate(Context context, long durationMs) {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator == null || !vibrator.hasVibrator()) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8+ menggunakan VibrationEffect
                vibrator.vibrate(VibrationEffect.createOneShot(
                        durationMs, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                // Fallback untuk Android lama
                vibrator.vibrate(durationMs);
            }
        } catch (Exception e) {
            // Abaikan error vibrasi agar tidak crash
        }
    }
}
