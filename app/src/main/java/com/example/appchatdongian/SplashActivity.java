package com.example.appchatdongian;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.appchatdongian.model.UserModel;
import com.example.appchatdongian.utils.AndroidUtil;
import com.example.appchatdongian.utils.FirebaseUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Khởi tạo SplashScreen API TRƯỚC khi gọi super.onCreate
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 2. Kiểm tra dữ liệu từ Notification
        String userId = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString("userId");
            Log.d("SPLASH_DEBUG", "Received UserId: " + userId);
        }

        if (userId != null) {
            handleNotificationNavigation(userId);
        } else {
            // Vào app bình thường sau 1 giây (để kịp thấy giao diện đẹp của bạn)
            new Handler(Looper.getMainLooper()).postDelayed(this::startToMain, 1000);
        }
    }

    private void handleNotificationNavigation(String userId) {
        FirebaseUtil.allUserCollectionReference().document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        UserModel model = task.getResult().toObject(UserModel.class);

                        if (model != null) {
                            // Mở MainActivity làm nền
                            Intent mainIntent = new Intent(this, MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(mainIntent);

                            // Mở ChatActivity với dữ liệu người dùng
                            Intent chatIntent = new Intent(this, ChatActivity.class);
                            AndroidUtil.passUserModelAsIntent(chatIntent, model);
                            startActivity(chatIntent);
                            finish();
                        } else {
                            startToMain();
                        }
                    } else {
                        startToMain();
                    }
                });
    }

    private void startToMain() {
        if (FirebaseUtil.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, LoginPhoneNumberActivity.class));
        }
        finish();
        // Hiệu ứng mờ dần khi chuyển Activity
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}