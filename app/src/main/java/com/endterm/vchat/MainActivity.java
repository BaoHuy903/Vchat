package com.endterm.vchat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
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
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration postsListener;

    private PostAdapter postAdapter;
    private List<Post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageView profileImageView = findViewById(R.id.iv_profile_toolbar);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        RecyclerView postsRecyclerView = findViewById(R.id.rv_posts);

        // --- RecyclerView Setup ---
        postsRecyclerView.setHasFixedSize(true);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        postsRecyclerView.setAdapter(postAdapter);

        // --- Toolbar Listeners ---
        profileImageView.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        // --- Bottom Navigation Setup ---
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

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sendToLogin();
        } else {
            loadPosts();
            seedInitialData(); // Seed users and posts
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
        Query query = db.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING);
        postsListener = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                Toast.makeText(MainActivity.this, "Lỗi tải bài viết.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (snapshots != null) {
                postList.clear();
                postList.addAll(snapshots.toObjects(Post.class));
                postAdapter.notifyDataSetChanged();
            }
        });
    }

    private void seedInitialData() {
        // --- Create Vchat Bot with Avatar ---
        // Using RoboHash to generate a cool robot avatar
        seedUser("vchat_bot_id", "Vchat Bot", "https://robohash.org/vchat_bot_id?set=set1", "Internet", "Bot", "AI Assistant", "101010");

        // --- User 1: Jane Doe (already exists check) ---
        seedUser("dummy_user_1", "Jane Doe", "https://i.pravatar.cc/150?u=janedoe", "Canada", "Female", "Designer", "555-0102");

        // --- User 2: John Smith ---
        seedUserWithPosts("dummy_user_2", "John Smith", "https://i.pravatar.cc/150?u=johnsmith", "USA", "Male", "Engineer", "555-0103",
                List.of("Just deployed a new feature! #coding #development", "Loving the new Android Studio features."),
                List.of()); // No image posts

        // --- User 3: Emily Jones ---
        seedUserWithPosts("dummy_user_3", "Emily Jones", "https://i.pravatar.cc/150?u=emilyjones", "UK", "Female", "Artist", "555-0104",
                List.of("My latest digital painting. What do you think? #art #digitalart"),
                List.of("https://picsum.photos/seed/vchat1/600/400")); // One image post
    }

    private void seedUser(String userId, String username, String imageUrl, String country, String genre, String work, String phone) {
        DocumentReference userRef = db.collection("Users").document(userId);
        userRef.get().addOnSuccessListener(document -> {
            if (!document.exists()) {
                User user = new User(userId, username, imageUrl, country, genre, work, phone);
                userRef.set(user).addOnSuccessListener(aVoid -> Log.d(TAG, "Seeded user: " + username));
            } else {
                 // Update existing bot avatar if needed (optional logic, but good for testing)
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
                // User doesn't exist, create user and posts
                User user = new User(userId, username, imageUrl, country, genre, work, phone);

                WriteBatch batch = db.batch();
                batch.set(userRef, user);

                // Create text posts
                for (String text : textPosts) {
                    String postId = UUID.randomUUID().toString();
                    DocumentReference postRef = db.collection("posts").document(postId);
                    Post post = new Post(postId, userId, text, null);
                    batch.set(postRef, post);
                }

                // Create image posts
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
}
