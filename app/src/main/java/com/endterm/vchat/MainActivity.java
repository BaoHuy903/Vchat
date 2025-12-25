package com.endterm.vchat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration postsListener;

    private PostAdapter postAdapter;
    private List<Post> postList;
    
    // Biến điều khiển giao diện Loading
    private RelativeLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo Firebase an toàn
        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage());
            Toast.makeText(this, "Lỗi kết nối Firebase", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            ImageView profileImageView = findViewById(R.id.iv_profile_toolbar);
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            RecyclerView postsRecyclerView = findViewById(R.id.rv_posts);
            
            // Ánh xạ giao diện Loading
            loadingLayout = findViewById(R.id.loading_layout);

            // --- RecyclerView Setup ---
            postsRecyclerView.setHasFixedSize(true);
            postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            postList = new ArrayList<>();
            postAdapter = new PostAdapter(this, postList);
            postsRecyclerView.setAdapter(postAdapter);

            // --- Toolbar Listeners ---
            if (profileImageView != null) {
                profileImageView.setOnClickListener(v -> {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                });
            }

            // --- Bottom Navigation Setup ---
            if (bottomNavigationView != null) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
                bottomNavigationView.setOnItemSelectedListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        return true; // Already on home
                    } else if (itemId == R.id.nav_search) {
                        startActivity(new Intent(MainActivity.this, AddFriendActivity.class));
                        return true;
                    } else if (itemId == R.id.nav_add) {
                        startActivity(new Intent(MainActivity.this, PostActivity.class));
                        return true;
                    } else if (itemId == R.id.nav_notifications) {
                        startActivity(new Intent(MainActivity.this, NotificationActivity.class));
                        return true;
                    } else if (itemId == R.id.nav_chat) {
                        startActivity(new Intent(MainActivity.this, ContactActivity.class));
                        return true;
                    }
                    return false;
                });
            }
        } catch (Exception e) {
             Log.e(TAG, "Error setup UI: " + e.getMessage());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                sendToLogin();
            } else {
                // Hiển thị loading khi bắt đầu tải
                if (loadingLayout != null) {
                    loadingLayout.setVisibility(View.VISIBLE);
                }
                loadPosts();
                
                // [SAFETY] Bọc seedInitialData trong try-catch để tránh crash khi khởi động
                try {
                    seedInitialData(); 
                } catch (Exception e) {
                    Log.e(TAG, "Error seeding data: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error onStart: " + e.getMessage());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (postsListener != null) {
            postsListener.remove();
        }
    }

    private void sendToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadPosts() {
        try {
            Query query = db.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING);
            postsListener = query.addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    // Ẩn loading kể cả khi lỗi
                    if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
                    return;
                }
                if (snapshots != null) {
                    try {
                        postList.clear();
                        postList.addAll(snapshots.toObjects(Post.class));
                        postAdapter.notifyDataSetChanged();
                    } catch (Exception ex) {
                         Log.e(TAG, "Error parsing posts: " + ex.getMessage());
                    }
                    
                    // Ẩn giao diện Loading sau khi tải xong
                    if (loadingLayout != null) {
                        loadingLayout.setVisibility(View.GONE);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading posts query: " + e.getMessage());
            if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
        }
    }

    private void seedInitialData() {
        // --- Create Vchat Bot with Avatar ---
        seedUser("vchat_bot_id", "Vchat Bot", "https://robohash.org/vchat_bot_id?set=set1", "Internet", "Bot", "AI Assistant", "101010");

        // --- User 1: Jane Doe ---
        seedUser("dummy_user_1", "Jane Doe", "https://i.pravatar.cc/150?u=janedoe", "Canada", "Female", "Designer", "555-0102");

        // --- User 2: John Smith ---
        seedUserWithPosts("dummy_user_2", "John Smith", "https://i.pravatar.cc/150?u=johnsmith", "USA", "Male", "Engineer", "555-0103",
                List.of("Just deployed a new feature! #coding #development", "Loving the new Android Studio features."),
                List.of()); 

        // --- User 3: Emily Jones ---
        seedUserWithPosts("dummy_user_3", "Emily Jones", "https://i.pravatar.cc/150?u=emilyjones", "UK", "Female", "Artist", "555-0104",
                List.of("My latest digital painting. What do you think? #art #digitalart"),
                List.of("https://picsum.photos/seed/vchat1/600/400"));
                
        // --- SEED NOTIFICATIONS ---
        seedNotifications();
    }

    private void seedUser(String userId, String username, String imageUrl, String country, String genre, String work, String phone) {
        DocumentReference userRef = db.collection("Users").document(userId);
        userRef.get().addOnSuccessListener(document -> {
            if (!document.exists()) {
                User user = new User(userId, username, imageUrl, country, genre, work, phone);
                userRef.set(user).addOnSuccessListener(aVoid -> Log.d(TAG, "Seeded user: " + username));
            } else {
                 if (userId.equals("vchat_bot_id")) {
                     userRef.update("imageurl", imageUrl);
                 }
            }
        });
    }

    private void seedUserWithPosts(String userId, String username, String imageUrl, String country, String genre, String work, String phone, List<String> textPosts, List<String> imagePosts) {
        DocumentReference userRef = db.collection("Users").document(userId);
        userRef.get().addOnSuccessListener(document -> {
            if (!document.exists()) {
                User user = new User(userId, username, imageUrl, country, genre, work, phone);
                WriteBatch batch = db.batch();
                batch.set(userRef, user);

                for (String text : textPosts) {
                    String postId = UUID.randomUUID().toString();
                    DocumentReference postRef = db.collection("posts").document(postId);
                    Post post = new Post(postId, userId, text, null);
                    batch.set(postRef, post);
                }

                for (String postImageUrl : imagePosts) {
                     String postId = UUID.randomUUID().toString();
                    DocumentReference postRef = db.collection("posts").document(postId);
                    Post post = new Post(postId, userId, "A beautiful day!", postImageUrl);
                    batch.set(postRef, post);
                }
                batch.commit().addOnSuccessListener(aVoid -> Log.d(TAG, "Seeded user and posts for: " + username));
            }
        });
    }
    
    private void seedNotifications() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        
        // [FIX] Đổi tên key thành "notif_seeded_v3" để BẮT BUỘC tạo lại dữ liệu mới cho bạn
        boolean isSeeded = getSharedPreferences("VchatPrefs", MODE_PRIVATE).getBoolean("notif_seeded_v3", false);
        if (isSeeded) return;

        WriteBatch batch = db.batch();
        Random random = new Random();
        String myId = currentUser.getUid();
        
        String[] senders = {"vchat_bot_id", "dummy_user_1", "dummy_user_2", "dummy_user_3"};
        String[] actions = {"liked your post", "commented: Nice!", "started following you", "commented: Amazing!"};
        
        for (int i = 0; i < 15; i++) {
            DocumentReference ref = db.collection("Notifications").document();
            
            String senderId = senders[random.nextInt(senders.length)];
            String action = actions[random.nextInt(actions.length)];
            boolean isPost = !action.contains("following");
            String postId = isPost ? UUID.randomUUID().toString() : "";
            
            // Notification with receiverId = currentUserId
            Notification notif = new Notification(senderId, action, postId, isPost, myId);
            batch.set(ref, notif);
        }
        
        batch.commit().addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Seeded 15 notifications.");
            Toast.makeText(MainActivity.this, "Đã tạo 15 thông báo mới!", Toast.LENGTH_SHORT).show();
            // Lưu trạng thái đã seed để không tạo lại lần sau
            getSharedPreferences("VchatPrefs", MODE_PRIVATE).edit().putBoolean("notif_seeded_v3", true).apply();
        });
    }
}
