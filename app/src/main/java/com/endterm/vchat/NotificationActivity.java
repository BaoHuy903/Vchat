package com.endterm.vchat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
import java.util.Comparator;
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
            
            // [FIX] Bỏ orderBy để tránh lỗi "Missing Index" của Firestore
            // Chúng ta sẽ sắp xếp danh sách (sort) ngay trong Java sau khi tải về
            db.collection("Notifications")
                    .whereEqualTo("receiverId", firebaseUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            notificationList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Notification notification = document.toObject(Notification.class);
                                notificationList.add(notification);
                            }
                            
                            // [FIX] Sắp xếp danh sách theo thời gian mới nhất -> cũ nhất tại đây
                            Collections.sort(notificationList, (o1, o2) -> {
                                if (o1.getTimestamp() == null || o2.getTimestamp() == null) return 0;
                                return o2.getTimestamp().compareTo(o1.getTimestamp());
                            });

                            notificationAdapter.notifyDataSetChanged();
                            
                            if (notificationList.isEmpty()) {
                                // Toast.makeText(NotificationActivity.this, "Chưa có thông báo nào", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("NotifActivity", "Error getting documents: ", task.getException());
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
