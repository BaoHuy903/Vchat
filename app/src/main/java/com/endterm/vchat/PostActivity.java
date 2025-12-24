package com.endterm.vchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {

    private ImageView close, galleryIcon, uploadIcon;
    private ImageButton backButton;
    private Button addTaskButton;
    private EditText description;
    private CircleImageView profileImage;
    private TextView username, email;

    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // Initialize Views
        close = findViewById(R.id.close);
        backButton = findViewById(R.id.back_button);
        addTaskButton = findViewById(R.id.add_task_button);
        description = findViewById(R.id.description);
        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        galleryIcon = findViewById(R.id.gallery_icon);
        uploadIcon = findViewById(R.id.upload_icon);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Set Click Listeners
        close.setOnClickListener(v -> finish());
        backButton.setOnClickListener(v -> finish());
        addTaskButton.setOnClickListener(v -> uploadPost());
        
        galleryIcon.setOnClickListener(v -> Toast.makeText(this, "Add image feature coming soon!", Toast.LENGTH_SHORT).show());
        uploadIcon.setOnClickListener(v -> Toast.makeText(this, "Upload feature coming soon!", Toast.LENGTH_SHORT).show());

        // Load user info
        loadUserInfo();
    }

    private void loadUserInfo() {
        if (firebaseUser != null) {
            email.setText(firebaseUser.getEmail());
            
            DocumentReference userRef = db.collection("Users").document(firebaseUser.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        username.setText(user.getUsername());
                        if (user.getImageurl() != null && !user.getImageurl().isEmpty()) {
                            Glide.with(this).load(user.getImageurl()).into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.ic_launcher_background);
                        }
                    }
                }
            });
        }
    }

    private void uploadPost() {
        String postContent = description.getText().toString().trim();

        if (TextUtils.isEmpty(postContent)) {
            Toast.makeText(this, "Please write something...", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Adding task...");
        pd.setCancelable(false);
        pd.show();

        if (firebaseUser == null) {
            pd.dismiss();
            Toast.makeText(this, "You need to be logged in to post.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseUser.getUid();

        // Generate a new, unique ID for the post
        String postId = db.collection("posts").document().getId();

        // Create the post using the correct constructor
        Post newPost = new Post(
                postId,
                userId,
                postContent,
                null // No image URL for now
        );

        // Save the post to Firestore
        db.collection("posts").document(postId).set(newPost)
                .addOnSuccessListener(aVoid -> {
                    pd.dismiss();
                    Toast.makeText(PostActivity.this, "Task added!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PostActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(PostActivity.this, "Failed to add task.", Toast.LENGTH_SHORT).show();
                });
    }
}
