package com.example.homeautomationsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class signup extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText nameEditText, usernameEditText, passwordEditText, purposeEditText, organizationEditText;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        nameEditText = findViewById(R.id.name);
        usernameEditText = findViewById(R.id.username); // email
        passwordEditText = findViewById(R.id.password);
        purposeEditText = findViewById(R.id.purpose);
        organizationEditText = findViewById(R.id.organization);
        signupButton = findViewById(R.id.loginButton); // reused button
        TextView registerText = findViewById(R.id.register_text);

        // Set touch listener on root layout to dismiss keyboard
        View rootLayout = findViewById(android.R.id.content); // Root view of the activity
        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboardAndClearFocus();
            }
            return false; // Allow other touch events to proceed
        });

        // Handle "Already have an account? Log In" click
        if (registerText != null) {
            registerText.setOnClickListener(v -> {
                Intent intent = new Intent(signup.this, login.class);
                startActivity(intent);
            });
        } else {
            Toast.makeText(this, "register_text not found in layout!", Toast.LENGTH_LONG).show();
        }

        // Handle Sign Up button click
        signupButton.setOnClickListener(v -> {
            // Get input values
            String name = nameEditText.getText().toString().trim();
            String email = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String purpose = purposeEditText.getText().toString().trim();
            String organization = organizationEditText.getText().toString().trim();

            // Validate inputs
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || purpose.isEmpty()) {
                Toast.makeText(signup.this, "Name, Email, Password, and Purpose are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create user with Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();

                                // Prepare user data
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("name", name);
                                userData.put("email", email);
                                userData.put("purpose", purpose);
                                userData.put("organization", organization.isEmpty() ? "" : organization);
                                userData.put("status", "inactive"); // Set status to inactive

                                // Save user data to Firebase Realtime Database
                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(uid)
                                        .setValue(userData)
                                        .addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                // Save user data locally (optional)
                                                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString("name", name);
                                                editor.putString("email", email);
                                                editor.putString("purpose", purpose);
                                                editor.putString("organization", organization);
                                                editor.putString("status", "inactive");
                                                editor.apply();

                                                // Inform user that account is pending approval
                                                Toast.makeText(signup.this, "Registration successful! Your account is pending admin approval.", Toast.LENGTH_LONG).show();

                                                // Navigate to login activity
                                                Intent intent = new Intent(signup.this, login.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(signup.this, "Failed to save user data: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(signup.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    // Method to hide keyboard and clear focus from EditText fields
    private void hideKeyboardAndClearFocus() {
        // Get the current focused view
        View currentFocusedView = getCurrentFocus();
        if (currentFocusedView != null) {
            // Hide the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), 0);
            }
            // Clear focus from the EditText
            currentFocusedView.clearFocus();
        }
    }
}