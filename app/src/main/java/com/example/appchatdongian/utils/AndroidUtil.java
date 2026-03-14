package com.example.appchatdongian.utils;

import android.content.Intent;
import android.widget.Toast;
import android.content.Context;

import com.example.appchatdongian.model.UserModel;


public class AndroidUtil {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public  static void passUserModelAsIntent(Intent intent , UserModel userModel) {
        intent.putExtra("username", userModel.getUsername());
        intent.putExtra("phone", userModel.getPhone());
        intent.putExtra("userId", userModel.getUserId());
        intent.putExtra("fcmToken", userModel.getFcmToken());
    }

    public static UserModel getUserModelFromIntent(Intent intent) {
        if (intent == null || intent.getStringExtra("userId") == null) {
            return null; // Trả về null nếu không có ID người dùng
        }
        UserModel userModel = new UserModel();
        userModel.setUserId(intent.getStringExtra("userId"));
        userModel.setPhone(intent.getStringExtra("phone"));
        userModel.setUsername(intent.getStringExtra("username"));
        userModel.setFcmToken(intent.getStringExtra("fcmToken"));
        return userModel;
    }

}

