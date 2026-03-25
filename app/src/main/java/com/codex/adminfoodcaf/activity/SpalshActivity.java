package com.codex.adminfoodcaf.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.codex.adminfoodcaf.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SpalshActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());

        if (windowInsetsController != null) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        setContentView(com.codex.adminfoodcaf.R.layout.activity_spalsh);

        ImageView imageView = findViewById(R.id.spalshLogo);

        Glide.with(this)
                .asBitmap()
                .load(R.drawable.img)
                .override(250)
                .into(imageView);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            findViewById(R.id.progress_circular).setVisibility(View.VISIBLE);
        }, 500);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            findViewById(R.id.progress_circular).setVisibility(View.INVISIBLE);
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            Intent intent;
            if (currentUser != null) {
                intent = new Intent(SpalshActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SpalshActivity.this, SigninActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2500);
    }

}