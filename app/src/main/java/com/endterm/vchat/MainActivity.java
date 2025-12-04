package com.endterm.vchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private EditText searchEditText;
    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Corrected to use the renamed layout file
        setContentView(R.layout.activity_main);

        searchEditText = findViewById(R.id.et_search_toolbar);
        profileImageView = findViewById(R.id.iv_profile_toolbar);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set Home as the selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // --- Toolbar Listeners ---

        // Listener for the search bar
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String searchQuery = v.getText().toString();
                Toast.makeText(MainActivity.this, "Searching for: " + searchQuery, Toast.LENGTH_SHORT).show();
                // TODO: Implement your search logic here
                return true;
            }
            return false;
        });

        // Listener for the profile image
        profileImageView.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        // --- Bottom Navigation Listener ---

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Reload the page (recreate the activity)
                recreate();
                return true;
            } else if (itemId == R.id.nav_search) {
                // Go to Add Friend
                startActivity(new Intent(MainActivity.this, AddFriendActivity.class));
                return true;
            } else if (itemId == R.id.nav_add) {
                // Go to Post
                // startActivity(new Intent(MainActivity.this, PostActivity.class));
                Toast.makeText(this, "Post (add) clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_notifications) {
                // Go to Notifications
                // startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
                Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_chat) {
                // Go to Contact
                startActivity(new Intent(MainActivity.this, ContactActivity.class));
                return true;
            }
            return false;
        });
    }
}
