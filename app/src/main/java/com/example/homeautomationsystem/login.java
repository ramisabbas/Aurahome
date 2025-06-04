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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText usernameEditText, passwordEditText, nameEditText;
    private Button loginButton;

    private CardView loginCard;
    private TextView pendingMessage;
    private LinearLayout titleContainer;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        nameEditText = findViewById(R.id.name); // email

        usernameEditText = findViewById(R.id.username); // email
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        titleContainer = findViewById(R.id.title_container);
        loginCard = findViewById(R.id.login_card);
        pendingMessage = findViewById(R.id.pending_message);
        backButton = findViewById(R.id.back_button);

        // Handle Back button click
        backButton.setOnClickListener(v -> {
            // Show login form, hide pending message and title container
            loginCard.setVisibility(View.VISIBLE);
            pendingMessage.setVisibility(View.GONE);
            titleContainer.setVisibility(View.GONE);
            // Clear input fields
            nameEditText.setText("");

            usernameEditText.setText("");
            passwordEditText.setText("");
        });
        // Set touch listener on root layout to dismiss keyboard
        View rootLayout = findViewById(android.R.id.content);
        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboardAndClearFocus();
            }
            return false;
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString().trim();
                String email = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Name validation: alphabets only
                if (name.isEmpty() || !name.matches("^[a-zA-Z]+$")) {
                    Toast.makeText(login.this, "Please enter a valid name (alphabets only)", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Email validation: must end with @gmail.com
                if (email.isEmpty() || !email.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
                    Toast.makeText(login.this, "Please enter a valid Gmail address", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Password length validation
                if (password.length() <= 6) {
                    Toast.makeText(login.this, "Password must be more than 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.equals("1234567")) {
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();


                    editor.putString("name", name);
                    editor.putString("email", email);
                    editor.putString("purpose", "");
                    editor.putString("organization", "");
                    editor.apply();

                    Toast.makeText(login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(login.this, rooms.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
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

