package com.endterm.vchat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    FirebaseUser fuser;
    FirebaseFirestore db;

    ImageButton btn_send;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mChat;

    RecyclerView recyclerView;

    Intent intent;
    
    // Flag to identify if we are chatting with the bot
    boolean isChattingWithBot = false;
    String botName = "Vchat Bot"; // The display name to trigger bot logic

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.chat_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.chat_profile_image);
        username = findViewById(R.id.chat_username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.et_message);

        intent = getIntent();
        final String userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        btn_send.setOnClickListener(v -> {
            String msg = text_send.getText().toString();
            if (!msg.equals("")) {
                sendMessage(fuser.getUid(), userid, msg);
            }
            text_send.setText("");
        });

        DocumentReference userRef = db.collection("Users").document(userid);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    username.setText(user.getUsername());
                    
                    // Check if this user is the Bot
                    if (user.getUsername().equalsIgnoreCase(botName)) {
                        isChattingWithBot = true;
                    }

                    if (user.getImageurl() != null && !user.getImageurl().equals("")) {
                        Glide.with(getApplicationContext()).load(user.getImageurl()).into(profile_image);
                    } else {
                        profile_image.setImageResource(R.drawable.ic_launcher_background);
                    }
                    readMessages(fuser.getUid(), userid, user.getImageurl());
                }
            }
        });
    }

    private void sendMessage(String sender, String receiver, String message) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Chats").add(hashMap);
        
        // Trigger Bot reply if applicable
        if (isChattingWithBot) {
            triggerBotReply(receiver, sender, message);
        }
    }

    private void triggerBotReply(String botId, String myId, String myMessage) {
        // Simulate thinking time (1-2 seconds)
        new Handler().postDelayed(() -> {
            String botReply = getBotResponse(myMessage);
            
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", botId); // Bot is the sender now
            hashMap.put("receiver", myId); // You are the receiver
            hashMap.put("message", botReply);
            hashMap.put("timestamp", FieldValue.serverTimestamp());

            db.collection("Chats").add(hashMap);
        }, 1500);
    }
    
    private String getBotResponse(String userMessage) {
        String msg = userMessage.toLowerCase();
        
        if (msg.contains("hello") || msg.contains("hi") || msg.contains("chào")) {
            return "Chào bạn! Tôi có thể giúp gì cho bạn hôm nay?";
        } else if (msg.contains("how are you") || msg.contains("khỏe không")) {
            return "Tôi là bot nên luôn khỏe mạnh! Còn bạn thì sao?";
        } else if (msg.contains("time") || msg.contains("giờ")) {
            return "Bây giờ là thời điểm tuyệt vời để code!";
        } else if (msg.contains("bot")) {
            return "Vâng, tôi là Vchat Bot siêu cấp vip pro.";
        } else if (msg.contains("bye") || msg.contains("tạm biệt")) {
            return "Tạm biệt! Hẹn gặp lại nhé.";
        } else {
            // Random default responses
            String[] responses = {
                "Thật thú vị!",
                "Bạn có thể nói rõ hơn không?",
                "Tôi đang lắng nghe đây.",
                "Tuyệt vời!",
                "Tôi chưa hiểu ý bạn lắm, nhưng nghe có vẻ hay đấy."
            };
            return responses[new Random().nextInt(responses.length)];
        }
    }

    private void readMessages(final String myid, final String userid, final String imageurl) {
        mChat = new ArrayList<>();
        messageAdapter = new MessageAdapter(ChatActivity.this, mChat);
        recyclerView.setAdapter(messageAdapter);

        db.collection("Chats").orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(ChatActivity.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        mChat.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Chat chat = document.toObject(Chat.class);
                            if ((chat.getReceiver().equals(myid) && chat.getSender().equals(userid)) ||
                                    (chat.getReceiver().equals(userid) && chat.getSender().equals(myid))) {
                                mChat.add(chat);
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                        if (!mChat.isEmpty()) {
                           recyclerView.scrollToPosition(mChat.size() - 1);
                        }
                    }
                });
    }
}
