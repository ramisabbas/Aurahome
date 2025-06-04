package com.example.homeautomationsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private ImageView logoutIcon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.users_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(userAdapter);
        logoutIcon = findViewById(R.id.logout_icon);
        // Fetch users from Firebase
        fetchUsers();
        // Handle Logout icon click
        logoutIcon.setOnClickListener(v -> {
            // Clear SharedPreferences (if used)
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear().apply();

            // Navigate to login activity
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void fetchUsers() {
        FirebaseDatabase.getInstance().getReference("Users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        userList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String uid = snapshot.getKey();
                            String name = snapshot.child("name").getValue(String.class);
                            String email = snapshot.child("email").getValue(String.class);
                            String purpose = snapshot.child("purpose").getValue(String.class);
                            String organization = snapshot.child("organization").getValue(String.class);
                            String status = snapshot.child("status").getValue(String.class);

                            // Ensure all fields are non-null
                            User user = new User(
                                    name != null ? name : "",
                                    email != null ? email : "",
                                    purpose != null ? purpose : "",
                                    organization != null ? organization : "",
                                    status != null ? status : "inactive",
                                    uid
                            );
                            userList.add(user);
                        }
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(AdminUsersActivity.this, "Failed to load users: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}