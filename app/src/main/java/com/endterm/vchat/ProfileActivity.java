package com.endterm.vchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private TextInputEditText workPositionEditText, fullNameEditText, phoneEditText, emailEditText;
    private AutoCompleteTextView countryAutoComplete, genreAutoComplete;
    private Button logoutButton, saveProfileButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        profileImageView = findViewById(R.id.profile_image);
        workPositionEditText = findViewById(R.id.et_work_position);
        fullNameEditText = findViewById(R.id.et_full_name);
        phoneEditText = findViewById(R.id.et_phone);
        emailEditText = findViewById(R.id.et_email);
        countryAutoComplete = findViewById(R.id.actv_country);
        genreAutoComplete = findViewById(R.id.actv_genre);
        logoutButton = findViewById(R.id.logout_button);
        saveProfileButton = findViewById(R.id.save_profile_button);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Check if user is logged in. If not, redirect to LoginActivity.
        if (currentUser == null) {
            sendToLogin();
            return; 
        }

        // Load user data
        loadUserProfile();

        // Setup Buttons
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(ProfileActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            sendToLogin();
        });

        saveProfileButton.setOnClickListener(v -> updateUserProfile());

        // Setup Bottom Navigation
        setupBottomNavigation();
    }

    private void loadUserProfile() {
        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                
                if (user != null) {
                    fullNameEditText.setText(user.getUsername());
                    emailEditText.setText(currentUser.getEmail()); // Email is from FirebaseAuth
                    workPositionEditText.setText(user.getWorkPosition());
                    phoneEditText.setText(user.getPhone());
                    countryAutoComplete.setText(user.getCountry(), false);
                    genreAutoComplete.setText(user.getGenre(), false);

                    if (user.getImageurl() != null && !user.getImageurl().isEmpty()) {
                        Glide.with(this).load(user.getImageurl()).into(profileImageView);
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_launcher_background);
                    }
                }
            } else {
                 Toast.makeText(ProfileActivity.this, "User profile does not exist.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ProfileActivity.this, "Failed to load profile data.", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUserProfile() {
        String username = fullNameEditText.getText().toString();
        String workPosition = workPositionEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String country = countryAutoComplete.getText().toString();
        String genre = genreAutoComplete.getText().toString();

        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("workPosition", workPosition);
        updates.put("phone", phone);
        updates.put("country", country);
        updates.put("genre", genre);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show());
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_search) {
                startActivity(new Intent(ProfileActivity.this, AddFriendActivity.class));
                return true;
            } else if (itemId == R.id.nav_add) {
                 startActivity(new Intent(ProfileActivity.this, PostActivity.class));
                 return true;
            } else if (itemId == R.id.nav_notifications) {
                 startActivity(new Intent(ProfileActivity.this, NotificationActivity.class));
                 return true;
            } else if (itemId == R.id.nav_chat) {
                 startActivity(new Intent(ProfileActivity.this, ContactActivity.class));
                 return true;
            }
            return false;
        });
    }

    private void sendToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
