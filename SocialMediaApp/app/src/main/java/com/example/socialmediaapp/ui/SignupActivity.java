package com.example.socialmediaapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.data.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {
    private EditText nameInput;
    private EditText emailInput;
    private EditText handleInput;
    private EditText passwordInput;
    private Button signupButton;
    private TextView loginLink;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        // Initialize views
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        handleInput = findViewById(R.id.handleInput);
        passwordInput = findViewById(R.id.passwordInput);
        signupButton = findViewById(R.id.signupButton);
        loginLink = findViewById(R.id.loginLink);
        progressBar = findViewById(R.id.progressBar);

        // Setup click listeners
        signupButton.setOnClickListener(v -> attemptSignup());
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptSignup() {
        // Reset errors
        nameInput.setError(null);
        emailInput.setError(null);
        handleInput.setError(null);
        passwordInput.setError(null);

        // Get values
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String handle = handleInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(handle)) {
            handleInput.setError("Username is required");
            handleInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        signupButton.setEnabled(false);

        // Create user with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Create user profile
                    String userId = auth.getCurrentUser().getUid();
                    User user = new User(userId, email, name, handle);
                    
                    usersRef.child(userId).setValue(user)
                        .addOnCompleteListener(task1 -> {
                            progressBar.setVisibility(View.GONE);
                            signupButton.setEnabled(true);

                            if (task1.isSuccessful()) {
                                // Registration success
                                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                finish();
                            } else {
                                // Failed to create user profile
                                Toast.makeText(SignupActivity.this,
                                    "Failed to create user profile",
                                    Toast.LENGTH_SHORT).show();
                            }
                        });
                } else {
                    // Registration failed
                    progressBar.setVisibility(View.GONE);
                    signupButton.setEnabled(true);
                    Toast.makeText(SignupActivity.this,
                        "Registration failed: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
