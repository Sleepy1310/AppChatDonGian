package com.example.appchatdongian;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appchatdongian.adapter.ChatRecyclerAdapter;
import com.example.appchatdongian.model.ChatMessageModel;
import com.example.appchatdongian.model.ChatroomModel;
import com.example.appchatdongian.model.UserModel;
import com.example.appchatdongian.utils.AndroidUtil;
import com.example.appchatdongian.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;

import org.json.JSONObject;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.InputStream;
import java.util.Collections;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    UserModel otherUser;
    String chatroomId;
    EditText messageInput;
    ImageButton sendBtn;
    ImageButton backBtn;
    TextView otherUsername;
    RecyclerView recyclerView;
    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        // 1. Ánh xạ View
        messageInput = findViewById(R.id.chat_message_input);
        sendBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        View mainView = findViewById(R.id.main);
        View bottomLayout = findViewById(R.id.bottom_layout);

        // 2. Xử lý đẩy giao diện khi hiện bàn phím (IME)
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

            int bottomPadding = Math.max(systemBars.bottom, ime.bottom);
            if (bottomLayout != null) {
                bottomLayout.setPadding(0, 0, 0, bottomPadding);
            }
            return WindowInsetsCompat.CONSUMED;
        });

        // 3. Lấy dữ liệu người dùng từ Intent
        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        if (otherUser != null) {
            otherUsername.setText(otherUser.getUsername());
            chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(), otherUser.getUserId());
        }

        backBtn.setOnClickListener(v -> finish());

        // 4. Khởi tạo dữ liệu và giao diện
        getOrCreateChatroomModel();
        setupChatRecyclerView();

        // 5. Sự kiện gửi tin nhắn
        sendBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) return;
            sendMessageToUser(message);
        });
    }

    void setupChatRecyclerView() {
        Query query = FirebaseUtil.getChatroomMessagesReference(chatroomId)
                .orderBy("timestamp", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class)
                .build();

        adapter = new ChatRecyclerAdapter(options, getApplicationContext());

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        adapter.startListening();

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
    }

    void getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel == null) {
                    chatroomModel = new ChatroomModel(
                            chatroomId,
                            Arrays.asList(FirebaseUtil.currentUserId(), otherUser.getUserId()),
                            Timestamp.now(),
                            ""
                    );
                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                }
            }
        });
    }

    void sendMessageToUser(String message) {
        if (chatroomModel == null) return;

        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessage(message);
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message, FirebaseUtil.currentUserId(), Timestamp.now());
        FirebaseUtil.getChatroomMessagesReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        messageInput.setText("");
                        sendNotification(message);
                    }
                });
    }

    String getAccessToken() {
        try {
            InputStream inputStream = getAssets().open("service-account.json");
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (Exception e) {
            Log.e("FCM_ERROR", "Lỗi lấy Access Token", e);
            return null;
        }
    }

    void sendNotification(String message) {
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                UserModel currentUserModel = task.getResult().toObject(UserModel.class);
                // THÊM KIỂM TRA NULL ĐỂ HẾT CẢNH BÁO
                if (currentUserModel != null && otherUser != null) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        JSONObject messageObj = new JSONObject();

                        JSONObject notificationObj = new JSONObject();
                        notificationObj.put("title", currentUserModel.getUsername());
                        notificationObj.put("body", message);

                        JSONObject dataObj = new JSONObject();
                        dataObj.put("userId", FirebaseUtil.currentUserId());

                        messageObj.put("notification", notificationObj);
                        messageObj.put("data", dataObj);
                        messageObj.put("token", otherUser.getFcmToken());

                        jsonObject.put("message", messageObj);
                        callApi(jsonObject);
                    } catch (Exception e) {
                        Log.e("FCM_ERROR", "Lỗi tạo JSON thông báo", e);
                    }
                }
            }
        });
    }

    void callApi(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/v1/projects/appchatdongian-9c19d/messages:send";

        new Thread(() -> {
            String accessToken = getAccessToken();
            if (accessToken == null) return;

            RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .header("Authorization", "Bearer " + accessToken)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull java.io.IOException e) {
                    Log.e("FCM_API", "Gửi tin nhắn thất bại", e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws java.io.IOException {
                    if (!response.isSuccessful()) {
                        Log.d("FCM_RESPONSE", "Gửi thông báo lỗi: " + response.body().string());
                    } else {
                        Log.d("FCM_RESPONSE", "Gửi thông báo thành công!");
                    }
                }
            });
        }).start();
    }
}