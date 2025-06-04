package com.example.homeautomationsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.notifications_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notificationList);
        recyclerView.setAdapter(notificationAdapter);
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotificationActivity.this, rooms.class);
                startActivity(intent);
            }
        });
        // Fetch notifications from Firebase
        fetchNotifications();
    }

    private void fetchNotifications() {
        FirebaseDatabase.getInstance().getReference("Notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        notificationList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Notification notification = snapshot.getValue(Notification.class);
                            if (notification != null) {
                                notificationList.add(notification);
                            }
                        }
                        // Reverse the list so latest appears on top
                        Collections.reverse(notificationList);
                        notificationAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(NotificationActivity.this, "Failed to load notifications: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}