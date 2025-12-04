package com.endterm.vchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
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

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private TextInputEditText workPositionEditText, fullNameEditText, phoneEditText, emailEditText;
    private AutoCompleteTextView countryAutoComplete, genreAutoComplete;
    private BottomNavigationView bottomNavigationView;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase instances
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
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
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Load user data if logged in
        if (currentUser != null) {
            loadUserProfile();
        } else {
            // Handle case where user is not logged in
            // For example, redirect to LoginActivity
            // startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            // finish();
        }

        // Setup Bottom Navigation
        setupBottomNavigation();
    }

    private void loadUserProfile() {
        DocumentReference userRef = db.collection("users").document(currentUser.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Get data from Firestore document
                String name = documentSnapshot.getString("fullName");
                String email = documentSnapshot.getString("email");
                String phone = documentSnapshot.getString("phoneNumber");
                String workPosition = documentSnapshot.getString("workPosition");
                String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                String country = documentSnapshot.getString("country");
                String genre = documentSnapshot.getString("genre");

                // Set data to the views
                fullNameEditText.setText(name);
                emailEditText.setText(email);
                phoneEditText.setText(phone);
                workPositionEditText.setText(workPosition);
                countryAutoComplete.setText(country);
                genreAutoComplete.setText(genre);

                // Load profile image using Glide
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(this).load(profileImageUrl).into(profileImageView);
                } else {
                    // Optionally, set a default image if no profile image URL is available
                    profileImageView.setImageResource(R.drawable.ic_launcher_background); // Or any placeholder
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ProfileActivity.this, "Failed to load profile data.", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupBottomNavigation() {
        // We assume that nav_chat is the profile button in this context
        bottomNavigationView.setSelectedItemId(R.id.nav_chat);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_search) {
                startActivity(new Intent(ProfileActivity.this, AddFriendActivity.class));
                return true;
            } else if (itemId == R.id.nav_chat) {
                // Already on this screen
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button press
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
