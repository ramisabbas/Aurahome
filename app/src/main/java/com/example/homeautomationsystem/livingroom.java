package com.example.homeautomationsystem;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class livingroom extends AppCompatActivity {

    private TextView headingTextView;
    private CardView lightsCard, fansCard, humidityCard;
    private RecyclerView recyclerView;
    private LinearLayout humidityDataLayout;
    private DeviceAdapter adapter;
    private String selectedRoom;

    private TextView temperatureTextView; // Reference to the TextView
    private ProgressBar temperatureProgressBar; // Reference to the TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livingroom);

        headingTextView = findViewById(R.id.selected_heading);
        recyclerView = findViewById(R.id.deviceRecyclerView);
        humidityDataLayout = findViewById(R.id.humidityDataLayout);
        lightsCard = findViewById(R.id.lights_card);
        fansCard = findViewById(R.id.fans_card);
        humidityCard = findViewById(R.id.humidity_card);
        TextView title = findViewById(R.id.humidityLevelText);
        TextView humidityLevelText = findViewById(R.id.humidityLevelText);
        ImageView backButton = findViewById(R.id.back_button);

        // Initialize UI components
        temperatureTextView = findViewById(R.id.temperatureTextView);
        temperatureProgressBar = findViewById(R.id.temperatureProgressBar);
        // Fetch temperature from Firebase
        selectedRoom = getIntent().getStringExtra("room_name");

        if (selectedRoom != null) {
            title.setText(selectedRoom);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(livingroom.this, rooms.class);
                startActivity(intent);
            }
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String humidity = snapshot.child("Humi").getValue(String.class);
                if (snapshot.exists() && snapshot.child("Temp").getValue() != null) {
                    String tempString = snapshot.child("Temp").getValue().toString();
                    humidityLevelText.setText(humidity + "%");
                    try {
                        int temperature = Integer.parseInt(tempString);

                        // Set progress (e.g., assuming max temp is 100)
                        temperatureProgressBar.setProgress(temperature);

                        // Set text
                        temperatureTextView.setText(temperature + "°C");

                    } catch (NumberFormatException e) {
                        temperatureTextView.setText("N/A");
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "onCancelled: " + error.getMessage());
            }
        });

        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        adapter = new DeviceAdapter(new ArrayList<>(),selectedRoom);
        recyclerView.setAdapter(adapter);

        // Default selection
        showLights();

        // Card Click Listeners
        lightsCard.setOnClickListener(v -> showLights());
        fansCard.setOnClickListener(v -> showFans());
        humidityCard.setOnClickListener(v -> showHumidity());
    }

    private void showLights() {
        headingTextView.setText("Lights");
        humidityDataLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        setCardHighlight(lightsCard);
        fetchDevicesFromFirebase("lights");
    }



    private void showFans() {
        headingTextView.setText("Fans");
        humidityDataLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        setCardHighlight(fansCard);
        fetchDevicesFromFirebase("fans");
    }



    private void showHumidity() {
        headingTextView.setText("Humidity");
        humidityDataLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        setCardHighlight(humidityCard);

    }

    private void updateDevices(List<DeviceItem> devices) {
        adapter = new DeviceAdapter(devices,selectedRoom);
        recyclerView.setAdapter(adapter);
    }

    private void setCardHighlight(CardView selectedCard) {
        // Reset all cards to default state
        resetCard(lightsCard, R.id.lightText);
        resetCard(fansCard, R.id.fanText);
        resetCard(humidityCard, R.id.humidityText);

        // Highlight selected card
        selectedCard.setCardBackgroundColor(Color.parseColor("#FF8C42")); // Highlight color

        TextView selectedText = selectedCard.findViewById(getTextIdForCard(selectedCard));
        if (selectedText != null) {
            selectedText.setTextColor(Color.WHITE); // Highlight text color
        }
    }

    // Helper to reset a card's background and text color
    private void resetCard(CardView card, int textViewId) {
        card.setCardBackgroundColor(Color.WHITE);
        TextView text = card.findViewById(textViewId);
        if (text != null) {
            text.setTextColor(Color.BLACK);
        }
    }

    // Helper to get corresponding text ID for a given card
    private int getTextIdForCard(CardView card) {
        if (card == lightsCard) return R.id.lightText;
        if (card == fansCard) return R.id.fanText;
        if (card == humidityCard) return R.id.humidityText;
        return -1;
    }

    private void fetchDevicesFromFirebase(String type) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rooms")
                .child(selectedRoom).child(type);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<DeviceItem> devices = new ArrayList<>();
                for (DataSnapshot deviceSnap : snapshot.getChildren()) {
                    String name = deviceSnap.getKey();
                    boolean status = Boolean.TRUE.equals(deviceSnap.getValue(Boolean.class));
                    int icon = type.equals("lights") ? R.drawable.ic_bulb : R.drawable.ic_fan;
                    devices.add(new DeviceItem(name, icon, status));
                }
                updateDevices(devices);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to read " + type + " data", error.toException());
            }
        });
    }

}


