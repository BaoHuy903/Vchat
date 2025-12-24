package com.endterm.vchat;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notificationList);
        recyclerView.setAdapter(notificationAdapter);

        readNotifications();
        setupBottomNavigation();
    }

    private void readNotifications() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Notifications").whereEqualTo("userid", firebaseUser.getUid())
                    .orderBy("timestamp", Query.Direction.DESCENDING) // Assuming you will add a timestamp field
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            notificationList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Notification notification = document.toObject(Notification.class);
                                notificationList.add(notification);
                            }
                            // Reverse the list to show newest first, as orderBy is not available without an index
                            // Collections.reverse(notificationList);
                            notificationAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }
    
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_notifications);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_search) {
                startActivity(new Intent(getApplicationContext(), AddFriendActivity.class));
                return true;
            } else if (itemId == R.id.nav_add) {
                startActivity(new Intent(getApplicationContext(), PostActivity.class));
                return true;
            } else if (itemId == R.id.nav_notifications) {
                return true; // Already here
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(getApplicationContext(), ContactActivity.class));
                return true;
            }
            return false;
        });
    }
}
