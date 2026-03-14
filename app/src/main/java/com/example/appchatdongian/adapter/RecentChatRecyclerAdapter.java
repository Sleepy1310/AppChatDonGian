package com.example.appchatdongian.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appchatdongian.ChatActivity;
import com.example.appchatdongian.R;
import com.example.appchatdongian.model.ChatroomModel;
import com.example.appchatdongian.model.UserModel;
import com.example.appchatdongian.utils.AndroidUtil;
import com.example.appchatdongian.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;


public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    Context context;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                UserModel otherUserModel = task.getResult().toObject(UserModel.class);

                if (otherUserModel != null) {
                    // 1. Kiểm tra ai là người gửi cuối
                    boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());

                    // 2. Hiển thị tên
                    holder.usernameText.setText(otherUserModel.getUsername());

                    // 3. Hiển thị nội dung tin nhắn
                    if (lastMessageSentByMe) {
                        holder.lastMessageText.setText("Bạn: " + model.getLastMessage());
                    } else {
                        holder.lastMessageText.setText(model.getLastMessage());
                    }

                    // 4. Hiển thị thời gian (Đưa ra ngoài if/else để lúc nào cũng hiện)
                    if (model.getLastMessageTimestamp() != null) {
                        holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));
                    }

                    // 5. Sự kiện Click (Đưa ra ngoài if/else để lúc nào cũng bấm được)
                    holder.itemView.setOnClickListener(v -> {
                        Intent intent = new Intent(context, ChatActivity.class);
                        AndroidUtil.passUserModelAsIntent(intent, otherUserModel);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    });
                }
            }
        });
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new ChatroomModelViewHolder(view);
    }

    class ChatroomModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilePic;

        public  ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.username_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}