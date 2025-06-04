package com.example.homeautomationsystem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class ThresholdsActivity extends AppCompatActivity {

    private EditText temperatureEditText, waterEditText, humidityEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thresholds);

        // Initialize UI elements
        temperatureEditText = findViewById(R.id.temperature_edittext);
        waterEditText = findViewById(R.id.water_edittext);
        humidityEditText = findViewById(R.id.humidity_edittext);
        saveButton = findViewById(R.id.save_button);
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThresholdsActivity.this, rooms.class);
                startActivity(intent);
            }
        });
        // Set touch listener on root layout to dismiss keyboard
        View rootLayout = findViewById(android.R.id.content);
        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboardAndClearFocus();
            }
            return false;
        });

        // Retrieve existing thresholds from Firebase
        fetchThresholds();

        // Handle Save button click
        saveButton.setOnClickListener(v -> {
            String temperatureStr = temperatureEditText.getText().toString().trim();
            String waterStr = waterEditText.getText().toString().trim();
            String humidityStr = humidityEditText.getText().toString().trim();

            // Validate inputs
            if (temperatureStr.isEmpty() || waterStr.isEmpty() || humidityStr.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                float temperature = Float.parseFloat(temperatureStr);
                float water = Float.parseFloat(waterStr);
                float humidity = Float.parseFloat(humidityStr);

                // Validate ranges (example ranges, adjust as needed)
                if (temperature < -50 || temperature > 100) {
                    Toast.makeText(this, "Temperature must be between -50°C and 100°C", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (water < 0 || water > 10000) {
                    Toast.makeText(this, "Water must be between 0 and 1000 liters", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (humidity < 0 || humidity > 100) {
                    Toast.makeText(this, "Humidity must be between 0% and 100%", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save to Firebase
                saveThresholds(temperature, water, humidity);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Retrieve thresholds from Firebase
    private void fetchThresholds() {
        FirebaseDatabase.getInstance().getReference("Thresholds")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Float temperature = dataSnapshot.child("temperature").getValue(Float.class);
                            Float water = dataSnapshot.child("water").getValue(Float.class);
                            Float humidity = dataSnapshot.child("humidity").getValue(Float.class);

                            // Populate EditText fields
                            if (temperature != null) {
                                temperatureEditText.setText(String.valueOf(temperature));
                            }
                            if (water != null) {
                                waterEditText.setText(String.valueOf(water));
                            }
                            if (humidity != null) {
                                humidityEditText.setText(String.valueOf(humidity));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ThresholdsActivity.this, "Failed to load thresholds: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Save thresholds to Firebase
    private void saveThresholds(float temperature, float water, float humidity) {
        Map<String, Object> thresholds = new HashMap<>();
        thresholds.put("temperature", temperature);
        thresholds.put("water", water);
        thresholds.put("humidity", humidity);

        FirebaseDatabase.getInstance().getReference("Thresholds")
                .setValue(thresholds)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ThresholdsActivity.this, "Thresholds saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ThresholdsActivity.this, "Failed to save thresholds: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Method to hide keyboard and clear focus from EditText fields
    private void hideKeyboardAndClearFocus() {
        View currentFocusedView = getCurrentFocus();
        if (currentFocusedView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), 0);
            }
            currentFocusedView.clearFocus();
        }
    }
}