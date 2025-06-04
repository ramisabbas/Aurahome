package com.example.homeautomationsystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.UUID;

public class RegisterComplaintActivity extends AppCompatActivity {

    private EditText complaintTitle, complaintDescription;
    private Button submitBtn;
    private DatabaseReference complaintsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_complaint);

        complaintTitle = findViewById(R.id.complaint_title);
        complaintDescription = findViewById(R.id.complaint_description);
        submitBtn = findViewById(R.id.submit_complaint_btn);
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterComplaintActivity.this, rooms.class);
                startActivity(intent);
            }
        });

        complaintsRef = FirebaseDatabase.getInstance().getReference("complaints");

        submitBtn.setOnClickListener(v -> {
            String title = complaintTitle.getText().toString().trim();
            String description = complaintDescription.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            saveComplaintToFirebase(title, description);
        });
    }

    private void saveComplaintToFirebase(String title, String description) {
        String complaintId = UUID.randomUUID().toString();

        HashMap<String, Object> complaint = new HashMap<>();
        complaint.put("id", complaintId);
        complaint.put("title", title);
        complaint.put("description", description);
        complaint.put("timestamp", System.currentTimeMillis());

        complaintsRef.child(complaintId).setValue(complaint)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Complaint submitted successfully", Toast.LENGTH_SHORT).show();
                    complaintTitle.setText("");
                    complaintDescription.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit complaint: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
