package com.example.socialmediaapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.data.Tweet;
import com.example.socialmediaapp.data.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageMetadata;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ComposeTweetActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTweet;
    private ImageView tweetImage;
    private TextView textCharCount;
    private Button buttonTweet;
    private CircleImageView profileImage;
    private Uri imageUri;

    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_tweet);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editTweet = findViewById(R.id.editTweet);
        tweetImage = findViewById(R.id.tweetImage);
        textCharCount = findViewById(R.id.textCharCount);
        buttonTweet = findViewById(R.id.buttonTweet);
        profileImage = findViewById(R.id.profileImage);
        ImageButton buttonClose = findViewById(R.id.buttonClose);
        ImageButton buttonImage = findViewById(R.id.buttonImage);

        // Load user data
        loadUserData();

        // Setup character count
        editTweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int remaining = 280 - s.length();
                textCharCount.setText(String.valueOf(remaining));
                buttonTweet.setEnabled(s.length() > 0 && remaining >= 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup click listeners
        buttonClose.setOnClickListener(v -> finish());

        buttonImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
        });

        buttonTweet.setOnClickListener(v -> publishTweet());
    }

    private void loadUserData() {
        String userId = auth.getCurrentUser().getUid();
        databaseRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null && currentUser.getProfileImageUrl() != null) {
                    Glide.with(ComposeTweetActivity.this)
                        .load(currentUser.getProfileImageUrl())
                        .placeholder(R.drawable.default_profile)
                        .into(profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ComposeTweetActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            tweetImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUri).into(tweetImage);
        }
    }

    private void publishTweet() {
        buttonTweet.setEnabled(false);
        String content = editTweet.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "Tweet cannot be empty", Toast.LENGTH_SHORT).show();
            buttonTweet.setEnabled(true);
            return;
        }

        if (imageUri != null) {
            // Show uploading progress
            Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

            // Create unique filename with extension
            String imageFileName = UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storageRef.child("images").child("tweets").child(imageFileName);

            // Create file metadata
            StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

            // Upload with metadata
            imageRef.putFile(imageUri, metadata)
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if (progress < 100) {
                        buttonTweet.setText("Uploading " + (int) progress + "%");
                    }
                })
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            createTweet(content, uri.toString());
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ComposeTweetActivity.this, 
                                "Failed to get download URL: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                            buttonTweet.setEnabled(true);
                            buttonTweet.setText("Tweet");
                        });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ComposeTweetActivity.this, 
                        "Failed to upload image: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    buttonTweet.setEnabled(true);
                    buttonTweet.setText("Tweet");
                });
        } else {
            createTweet(content, null);
        }
    }

    private void createTweet(String content, String imageUrl) {
        buttonTweet.setText("Publishing...");
        String tweetId = databaseRef.child("tweets").push().getKey();
        
        if (tweetId == null) {
            Toast.makeText(ComposeTweetActivity.this, "Failed to generate tweet ID", Toast.LENGTH_SHORT).show();
            buttonTweet.setEnabled(true);
            buttonTweet.setText("Tweet");
            return;
        }

        Tweet tweet = new Tweet(
            auth.getCurrentUser().getUid(),
            currentUser.getUsername(),
            currentUser.getHandle(),
            currentUser.getProfileImageUrl(),
            content,
            imageUrl
        );

        databaseRef.child("tweets").child(tweetId).setValue(tweet)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(ComposeTweetActivity.this, "Tweet published successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(ComposeTweetActivity.this, 
                    "Failed to publish tweet: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
                buttonTweet.setEnabled(true);
                buttonTweet.setText("Tweet");
            });
    }
}
