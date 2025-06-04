package com.example.homeautomationsystem;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class start extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ImageView logoIcon = findViewById(R.id.logoIcon);

        // Fade in and scale animation
        logoIcon.setAlpha(0f);
        logoIcon.setScaleX(0.7f);
        logoIcon.setScaleY(0.7f);

        logoIcon.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setStartDelay(1000)
                .start();

        // Navigate to main activity after delay
        new Handler().postDelayed(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String savedEmail = sharedPreferences.getString("email", null);

            if (savedEmail != null) {
                // User already logged in
                startActivity(new Intent(start.this, rooms.class));
            } else {
                // No session, show signup
                startActivity(new Intent(start.this, login.class));
            }
            finish();
        }, SPLASH_DURATION);
    }
}
