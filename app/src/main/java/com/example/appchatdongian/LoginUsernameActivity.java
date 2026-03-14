package com.example.appchatdongian;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appchatdongian.model.UserModel;
import com.example.appchatdongian.utils.FirebaseUtil;
import com.google.firebase.Timestamp;

public class LoginUsernameActivity extends AppCompatActivity {

    EditText usernameInput;
    Button finishBtn;
    ProgressBar progressBar;
    String phoneNumber;
    UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_username);

        // Kiểm tra tránh null nếu layout không có ID main
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        usernameInput = findViewById(R.id.login_username);
        finishBtn = findViewById(R.id.login_finish_btn);
        progressBar = findViewById(R.id.login_progress_bar);

        phoneNumber = getIntent().getStringExtra("mobile");
        getUsername();

        finishBtn.setOnClickListener(v -> setUsername());
    }

    void setUsername() {
        String username = usernameInput.getText().toString();
        if (username.isEmpty() || username.length() < 3) {
            usernameInput.setError("Tên người dùng phải có ít nhất 3 ký tự");
            return;
        }

        setInProgress(true);
        if (userModel != null) {
            userModel.setUsername(username);
        } else {
            userModel = new UserModel(phoneNumber, username, Timestamp.now(), FirebaseUtil.currentUserId());
        }

        // Thay thế bằng Lambda
        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful()) {
                Intent intent = new Intent(LoginUsernameActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    void getUsername() {
        setInProgress(true);
        // Thay thế bằng Lambda
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful()) {
                // Gán vào biến userModel của class (this.userModel)
                userModel = task.getResult().toObject(UserModel.class);
                if (userModel != null) {
                    usernameInput.setText(userModel.getUsername());
                }
            }
        });
    }

    void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            finishBtn.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            finishBtn.setVisibility(View.VISIBLE);
        }
    }
}