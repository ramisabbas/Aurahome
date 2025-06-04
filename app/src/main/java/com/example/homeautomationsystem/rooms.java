
package com.example.homeautomationsystem;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class rooms extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String PREFS_ROOM_NAME = "RoomsPrefs";
    private static final String SELECTED_CARD_KEY = "selectedCardId";
    private LinearLayout roomContainer;
    private int roomCount = 5; // Start with room 5 (Room 1–4 already exist)
    private CardView livingroomCard, kitchenCard, bedroomCard, studyroomCard;
    private CardView selectedCard; // To track the currently selected card
    private SharedPreferences sharedPreferences;

    private DrawerLayout drawerLayout;
    private NotificationManager notificationManager;
    private static final String CHANNEL_ID = "ThresholdAlerts";
    private int notificationId = 0; // Unique ID for each notification

    private LinearLayout recentActivityList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        // Retrieve user's name from SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "");
        String email = sharedPreferences.getString("email", "No Email");
        // Set greeting message
        TextView greetingTextView = findViewById(R.id.welcome_text);
        if (!name.isEmpty()) {
            greetingTextView.setText("Hi, " + name + "!");
        } else {
            greetingTextView.setText("Hi!");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }



        TextView tempTextView = findViewById(R.id.tempTextView);
        TextView humidityTextView = findViewById(R.id.humidityTextView);
        TextView levelTextView = findViewById(R.id.levelTextView);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String humidity = snapshot.child("Humi").getValue(String.class);
                String level = snapshot.child("Level").getValue(String.class);
                String temp = snapshot.child("Temp").getValue(String.class);

                // Update UI
                tempTextView.setText(temp + "°C");
                humidityTextView.setText(humidity + "%");
                levelTextView.setText(level);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "onCancelled: " + error.getMessage());
            }
        });


        // Initialize all CardViews
        livingroomCard = findViewById(R.id.livingroom);
        kitchenCard = findViewById(R.id.kitchen);
        bedroomCard = findViewById(R.id.bedroom);
        studyroomCard = findViewById(R.id.studyroom);
        recentActivityList = findViewById(R.id.recent_activity_list);
        // Array of all CardViews for easier management
        CardView[] allCards = {livingroomCard, kitchenCard, bedroomCard, studyroomCard};

        // Set click listeners for each card
        if (livingroomCard != null) {
            livingroomCard.setOnClickListener(v -> {
                updateSelectedCard(livingroomCard, allCards);
                Intent intent = new Intent(rooms.this, livingroom.class);
                intent.putExtra("room_name", "Living Room"); // or any room user selects

                startActivity(intent);
            });
        }

        if (kitchenCard != null) {
            kitchenCard.setOnClickListener(v -> {
                updateSelectedCard(kitchenCard, allCards);
                Intent intent = new Intent(rooms.this, livingroom.class);
                intent.putExtra("room_name", "Kitchen"); // or any room user selects

                startActivity(intent);
            });
        }

        if (bedroomCard != null) {
            bedroomCard.setOnClickListener(v -> {
                updateSelectedCard(bedroomCard, allCards);
                Intent intent = new Intent(rooms.this, livingroom.class);
                intent.putExtra("room_name", "Bed Room"); // or any room user selects

                startActivity(intent);
            });
        }

        if (studyroomCard != null) {
            studyroomCard.setOnClickListener(v -> {
                updateSelectedCard(studyroomCard, allCards);
                Intent intent = new Intent(rooms.this, livingroom.class);
                intent.putExtra("room_name", "Study Room"); // or any room user selects

                startActivity(intent);
            });
        }

        // Restore the previously selected card or default to Living Room
        restoreSelectedCard(allCards);
// Fetch and display recent activities
        fetchRecentActivities();
        // Setup Drawer Icon Click
        ImageView drawerIcon = findViewById(R.id.drawer_icon);
        drawerIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Setup Bell Icon Click
        ImageView bellIcon = findViewById(R.id.bell_icon);
        bellIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationActivity.class);
            startActivity(intent);
        });



        // Setup NavigationView
        NavigationView navView = findViewById(R.id.nav_view);

        // Set up Navigation Drawer header
        View headerView = navView.getHeaderView(0);
        TextView userNameText = headerView.findViewById(R.id.user_name_text);
        TextView userEmailText = headerView.findViewById(R.id.user_email_text);
// Update header TextViews
        userNameText.setText(name);
        userEmailText.setText(email);

        // Important: Setup the motion sensor switch BEFORE setting the navigation item listener
        Menu menu = navView.getMenu();
        MenuItem motionMenuItem = menu.findItem(R.id.nav_motion_sensor);
        if (motionMenuItem != null) {
            // Get the action view from the menu item
            View actionView = MenuItemCompat.getActionView(motionMenuItem);
            // Or alternatively:
            // View actionView = motionMenuItem.getActionView();




            if (actionView != null) {
                Switch motionSwitch = actionView.findViewById(R.id.switch_motion_sensor);
                if (motionSwitch != null) {
                    // Initialize switch state from Firebase
                    DatabaseReference motionRef = FirebaseDatabase.getInstance().getReference("motion_sensor_enabled");
                    motionRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Boolean isEnabled = dataSnapshot.getValue(Boolean.class);
                            if (isEnabled != null) {
                                motionSwitch.setChecked(isEnabled);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("FirebaseError", "Failed to read motion sensor value", error.toException());
                        }
                    });

                    // Set listener for switch state changes
                    motionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        motionRef.setValue(isChecked);
                    });
                }
            }
        }
        navView.setNavigationItemSelectedListener(item -> {
            // Use if-else instead of switch to avoid constant expression issue
            if (item.getItemId() == R.id.nav_home) {

                // Optionally, start a Notifications Activity
                // Intent intent = new Intent(rooms.this, NotificationsActivity.class);
                // startActivity(intent);
            } else if (item.getItemId() == R.id.nav_threshold) {
                // Clear user session

                Intent intent = new Intent(this, ThresholdsActivity.class);
                startActivity(intent);
            }else if (item.getItemId() == R.id.nav_notifications) {
                Intent intent = new Intent(this, NotificationActivity.class);
                startActivity(intent);
            }else if (item.getItemId() == R.id.nav_motion_sensor) {
                // Don't close the drawer when clicking the menu item itself
                // The switch will handle its own state
                return false;
            } else if (item.getItemId() == R.id.nav_register_complaint) {
                Intent intent = new Intent(this, RegisterComplaintActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_logout) {
                // Clear user session
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear().apply();
                // Navigate to Login Activity
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(rooms.this, login.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Initialize Notification Manager and Channel
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        // Fetch and compare thresholds with room values
        fetchAndCompareThresholds();
        fetchAndDoneMotion();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to update the selected card's background color and reset others
    private void updateSelectedCard(CardView newlySelectedCard, CardView[] allCards) {
        // Reset all cards to white
        for (CardView card : allCards) {
            if (card != null) {
                card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                // Update the text color of the card's TextView to black
                LinearLayout layout = (LinearLayout) card.getChildAt(0);
                TextView textView = (TextView) layout.getChildAt(1);
                textView.setTextColor(Color.parseColor("#000000"));
                // Update the icon tint to black
                ImageView imageView = (ImageView) layout.getChildAt(0);
                imageView.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN);
            }
        }

        // Set the selected card's background to the highlighted color
        if (newlySelectedCard != null) {
            newlySelectedCard.setCardBackgroundColor(Color.parseColor("#FF8C42"));
            // Update the text color of the selected card's TextView to white
            LinearLayout layout = (LinearLayout) newlySelectedCard.getChildAt(0);
            TextView textView = (TextView) layout.getChildAt(1);
            textView.setTextColor(Color.parseColor("#FFFFFF"));
            // Update the icon tint to white
            ImageView imageView = (ImageView) layout.getChildAt(0);
            imageView.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_IN);
        }

        // Save the selected card's ID to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SELECTED_CARD_KEY, newlySelectedCard != null ? newlySelectedCard.getId() : -1);
        editor.apply();

        selectedCard = newlySelectedCard; // Update the currently selected card
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Alerts";
            String channelDescription = "Alerts for sensor thresholds";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(channelDescription);
            channel.setSound(soundUri, audioAttributes); // 🔊 Set the sound here

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void fetchAndCompareThresholds() {
        // Listen for Thresholds changes
        FirebaseDatabase.getInstance().getReference("Thresholds")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot thresholdSnapshot) {
                        if (!thresholdSnapshot.exists()) {
                            Toast.makeText(rooms.this, "Thresholds not found", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Float thresholdTemp = thresholdSnapshot.child("temperature").getValue(Float.class);
                        Float thresholdWater = thresholdSnapshot.child("water").getValue(Float.class);
                        Float thresholdHumidity = thresholdSnapshot.child("humidity").getValue(Float.class);

                        if (thresholdTemp == null || thresholdWater == null || thresholdHumidity == null) {
                            Toast.makeText(rooms.this, "Invalid threshold data", Toast.LENGTH_LONG).show();
                            return;
                        }

                        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String globalHumidity = snapshot.child("Humi").getValue(String.class);
                                String globalWater = snapshot.child("Level").getValue(String.class);
                                String smoke = snapshot.child("Smoke").getValue(String.class);
                                String globalTemp = snapshot.child("Temp").getValue(String.class);

                                // Global Temperature
                                checkAndHandleAlert("Global_temperature", "Global Temperature Alert",
                                        "Temperature (" + globalTemp + "°C) exceeds threshold (" + thresholdTemp + "°C)",
                                        globalTemp != null && Float.parseFloat(globalTemp) > thresholdTemp);

                                // Global Water
                                checkAndHandleAlert("Global_water", "Global Water Alert",
                                        "Water (" + globalWater + "L) exceeds threshold (" + thresholdWater + "L)",
                                        globalWater != null && Float.parseFloat(globalWater) > thresholdWater);

                                // Global Humidity
                                checkAndHandleAlert("Global_humidity", "Global Humidity Alert",
                                        "Humidity (" + globalHumidity + "%) exceeds threshold (" + thresholdHumidity + "%)",
                                        globalHumidity != null && Float.parseFloat(globalHumidity) > thresholdHumidity);

                                if ("YES".equalsIgnoreCase(smoke)) {
                                    checkAndHandleAlert(
                                            "Gas_Leakage",
                                            "Gas Leakage Detected",
                                            "Dangerous gas levels detected! Take immediate action.",
                                            "YES".equalsIgnoreCase(smoke)
                                    );
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("FirebaseError", "onCancelled: " + error.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(rooms.this, "Failed to load thresholds: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void fetchAndDoneMotion() {

        DatabaseReference motionRef = FirebaseDatabase.getInstance().getReference("motion_sensor_enabled");
        motionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean isEnabled = dataSnapshot.getValue(Boolean.class);
                if (isEnabled != null&&isEnabled) {
                    DatabaseReference motionRef2 = FirebaseDatabase.getInstance()
                            .getReference("Motion");

                    // Retrieve and map "YES"/"NO" to boolean
                    motionRef2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String status = snapshot.getValue(String.class);
                            if ("Yes".equalsIgnoreCase(status)) {
                                checkAndHandleAlert(
                                        "Motion_detected",                               // Alert ID or key
                                        "Motion Alert",                                  // Title
                                        "Motion detected at the sensor location.",       // Message
                                        true                                             // Condition: always true when motion is detected
                                );

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("FirebaseError", "Failed to read motion sensor value", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to read motion sensor value", error.toException());
            }
        });

    }


    private void checkAndHandleAlert(String alertKey, String title, String message, boolean isExceeding) {
        // Reference to the specific alert in ActiveAlerts
        FirebaseDatabase.getInstance().getReference("ActiveAlerts").child(alertKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (isExceeding) {
                            // Condition is exceeded
                            if (!snapshot.exists()) {
                                // Check if we've already sent a notification today for this alert type
                                checkLastNotificationTime(alertKey, title, message);
                            }
                        } else {
                            // Condition is resolved, remove the alert if it exists
                            if (snapshot.exists()) {
                                FirebaseDatabase.getInstance().getReference("ActiveAlerts").child(alertKey)
                                        .removeValue()
                                        .addOnCompleteListener(task -> {
                                            if (!task.isSuccessful()) {
                                                Log.e("AlertError", "Failed to clear alert: " +
                                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("FirebaseError", "Failed to check active alerts: " + databaseError.getMessage());
                    }
                });
    }

    private void checkLastNotificationTime(String alertKey, String title, String message) {
        // Get today's date at midnight (start of day)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDayTimestamp = calendar.getTimeInMillis();

        // Check if we've already sent a notification today for this alert type
        FirebaseDatabase.getInstance().getReference("AlertHistory").child(alertKey)
                .orderByChild("timestamp")
                .startAt(startOfDayTimestamp)
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // If no notifications found for today, send one
                        if (!dataSnapshot.exists()) {
                            saveAndShowNotification(alertKey, title, message);
                        } else {
                            // A notification was already sent today, don't send another one
                            Log.d("AlertSystem", "Alert for " + alertKey + " already sent today");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("FirebaseError", "Failed to check alert history: " + databaseError.getMessage());
                    }
                });
    }

    @SuppressLint("NotificationPermission")
    private void saveAndShowNotification(String alertKey, String title, String message) {
        // Current timestamp
        long timestamp = System.currentTimeMillis();

        // Save to ActiveAlerts
        Map<String, Object> alert = new HashMap<>();
        alert.put("title", title);
        alert.put("message", message);
        alert.put("timestamp", timestamp);

        FirebaseDatabase.getInstance().getReference("ActiveAlerts").child(alertKey)
                .setValue(alert)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("FirebaseError", "Failed to save active alert: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });

        // Save to AlertHistory to track when notifications are sent
        Map<String, Object> historyEntry = new HashMap<>();
        historyEntry.put("title", title);
        historyEntry.put("message", message);
        historyEntry.put("timestamp", timestamp);

        FirebaseDatabase.getInstance().getReference("AlertHistory").child(alertKey)
                .push()
                .setValue(historyEntry)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("FirebaseError", "Failed to save alert history: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });

        // Check if we've already saved this alert to Notifications today
        // Get today's date at midnight (start of day)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDayTimestamp = calendar.getTimeInMillis();

        // Create a unique key for today's date (YYYYMMDD format)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String todayKey = dateFormat.format(new Date());
        String notificationKey = alertKey + "_" + todayKey;

        // Check if this notification has already been saved today
        FirebaseDatabase.getInstance().getReference("NotificationRegistry")
                .child(notificationKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // This notification hasn't been saved today, save it to Notifications
                            Map<String, Object> notification = new HashMap<>();
                            notification.put("title", title);
                            notification.put("message", message);
                            notification.put("timestamp", timestamp);
                            notification.put("alertKey", alertKey);

                            // Save to Notifications
                            FirebaseDatabase.getInstance().getReference("Notifications")
                                    .push()
                                    .setValue(notification)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // Record that we've saved this notification today
                                            FirebaseDatabase.getInstance().getReference("NotificationRegistry")
                                                    .child(notificationKey)
                                                    .setValue(timestamp);
                                        } else {
                                            Log.e("FirebaseError", "Failed to save notification: " +
                                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Failed to check notification registry: " + error.getMessage());
                    }
                });

        // Show notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(notificationId++, builder.build());
    }
    private void fetchRecentActivities() {
        FirebaseDatabase.getInstance().getReference("RecentActivity")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Map<String, Object>> activities = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Map<String, Object> activity = new HashMap<>();
                            activity.put("description", snapshot.child("description").getValue(String.class));
                            activity.put("timestamp", snapshot.child("timestamp").getValue(Long.class));
                            activities.add(activity);
                        }

                        // Sort by timestamp (newest first)
                        Collections.sort(activities, (a, b) -> {
                            Long timeA = (Long) a.get("timestamp");
                            Long timeB = (Long) b.get("timestamp");
                            return Long.compare(timeB != null ? timeB : 0, timeA != null ? timeA : 0);
                        });

                        // Limit to 3 most recent activities
                        int limit = Math.min(activities.size(), 3);
                        recentActivityList.removeAllViews();

                        for (int i = 0; i < limit; i++) {
                            Map<String, Object> activity = activities.get(i);
                            String description = (String) activity.get("description");
                            Long timestamp = (Long) activity.get("timestamp");

                            // Inflate activity item layout
                            View activityView = LayoutInflater.from(rooms.this)
                                    .inflate(R.layout.item_recent_activity, recentActivityList, false);

                            // Set icon based on device type
                            ImageView icon = activityView.findViewById(R.id.activity_icon);
                            String lowerDesc = description.toLowerCase();
                            if (lowerDesc.contains("light")) {
                                icon.setImageResource(R.drawable.ic_bulb);
                            } else if (lowerDesc.contains("fan")) {
                                icon.setImageResource(R.drawable.ic_fan);
                            } else if (lowerDesc.contains("ac")) {
                                icon.setImageResource(R.drawable.ic_ac);
                            }

                            // Set description
                            TextView descText = activityView.findViewById(R.id.activity_description);
                            descText.setText(description);

                            // Format and set timestamp
                            TextView timeText = activityView.findViewById(R.id.activity_timestamp);
                            if (timestamp != null) {
                                Date date = new Date(timestamp);
                                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                                timeText.setText(sdf.format(date));
                            } else {
                                timeText.setText("Unknown time");
                            }

                            recentActivityList.addView(activityView);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(rooms.this, "Failed to load recent activities: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    // Method to restore the previously selected card
    private void restoreSelectedCard(CardView[] allCards) {
        int selectedCardId = sharedPreferences.getInt(SELECTED_CARD_KEY, R.id.livingroom); // Default to Living Room if no previous selection

        // Find the CardView with the saved ID
        for (CardView card : allCards) {
            if (card != null && card.getId() == selectedCardId) {
                updateSelectedCard(card, allCards);
                break;
            }
        }
    }
}




