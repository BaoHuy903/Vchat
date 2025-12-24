package com.endterm.vchat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Chờ 2 giây (2000ms) rồi mới chuyển màn hình
        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            Intent intent;
            if (currentUser != null) {
                // Nếu đã đăng nhập -> Chuyển thẳng vào MainActivity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // Nếu chưa đăng nhập -> Chuyển vào LoginActivity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish(); // Đóng SplashActivity để người dùng không back lại được
        }, 2000);
    }
}
