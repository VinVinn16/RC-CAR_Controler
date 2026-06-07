package com.rccar.controller.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rccar.controller.R;
import com.rccar.controller.ui.home.HomeActivity;

/**
 * SplashActivity: Layar pertama yang ditampilkan saat aplikasi dibuka.
 * Menampilkan logo dan nama aplikasi selama 2 detik, kemudian berpindah ke HomeActivity.
 */
public class SplashActivity extends AppCompatActivity {

    // Durasi splash screen dalam milidetik
    private static final long SPLASH_DURATION_MS = 2000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Ambil view dari layout
        TextView tvAppName    = findViewById(R.id.tv_splash_app_name);
        TextView tvSubtitle   = findViewById(R.id.tv_splash_subtitle);

        // Animasi fade-in untuk teks
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(800);
        fadeIn.setFillAfter(true);

        tvAppName.startAnimation(fadeIn);

        // Animasi subtitle sedikit tertunda
        AlphaAnimation fadeInDelayed = new AlphaAnimation(0f, 1f);
        fadeInDelayed.setDuration(800);
        fadeInDelayed.setStartOffset(300);
        fadeInDelayed.setFillAfter(true);
        tvSubtitle.startAnimation(fadeInDelayed);

        // Pindah ke HomeActivity setelah SPLASH_DURATION_MS
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            startActivity(intent);
            // Animasi transisi halus
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish(); // Tutup SplashActivity agar tidak bisa di-back
        }, SPLASH_DURATION_MS);
    }
}
