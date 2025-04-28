package com.example.socialmediaapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.data.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {
    private static final int PICK_PROFILE_IMAGE = 1;
    private static final int PICK_COVER_IMAGE = 2;

    private CircleImageView profileImage;
    private ImageView coverImage;
    private ImageButton editProfileImageButton;
    private ImageButton editCoverButton;
    private TextInputEditText nameInput;
    private TextInputEditText bioInput;
    private TextInputEditText locationInput;
    private TextInputEditText websiteInput;
    private Button saveButton;
    private ImageButton closeButton;
    private ProgressBar progressBar;

    private DatabaseReference userRef;
    private StorageReference storageRef;
    private String currentUserId;
    private Uri profileImageUri;
    private Uri coverImageUri;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        storageRef = FirebaseStorage.getInstance().getReference();

        initializeViews();
        setupListeners();
        loadUserData();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profileImage);
        coverImage = findViewById(R.id.coverImage);
        editProfileImageButton = findViewById(R.id.editProfileImageButton);
        editCoverButton = findViewById(R.id.editCoverButton);
        nameInput = findViewById(R.id.nameInput);
        bioInput = findViewById(R.id.bioInput);
        locationInput = findViewById(R.id.locationInput);
        websiteInput = findViewById(R.id.websiteInput);
        saveButton = findViewById(R.id.saveButton);
        closeButton = findViewById(R.id.closeButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        editProfileImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_PROFILE_IMAGE);
        });

        editCoverButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_COVER_IMAGE);
        });

        saveButton.setOnClickListener(v -> saveProfile());
        closeButton.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null) {
                    updateUI();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        nameInput.setText(currentUser.getUsername());
        bioInput.setText(currentUser.getBio());
        locationInput.setText(currentUser.getLocation());
        websiteInput.setText(currentUser.getWebsite());

        if (currentUser.getProfileImageUrl() != null) {
            Glide.with(this)
                .load(currentUser.getProfileImageUrl())
                .placeholder(R.drawable.default_profile)
                .into(profileImage);
        }

        if (currentUser.getCoverImageUrl() != null) {
            Glide.with(this)
                .load(currentUser.getCoverImageUrl())
                .into(coverImage);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_PROFILE_IMAGE) {
                profileImageUri = data.getData();
                Glide.with(this).load(profileImageUri).into(profileImage);
            } else if (requestCode == PICK_COVER_IMAGE) {
                coverImageUri = data.getData();
                Glide.with(this).load(coverImageUri).into(coverImage);
            }
        }
    }

    private void saveProfile() {
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        final Map<String, Object> updates = new HashMap<>();
        updates.put("username", nameInput.getText().toString().trim());
        updates.put("bio", bioInput.getText().toString().trim());
        updates.put("location", locationInput.getText().toString().trim());
        updates.put("website", websiteInput.getText().toString().trim());

        // Upload images if changed
        if (profileImageUri != null) {
            uploadImage(profileImageUri, "profile_images/", (imageUrl) -> {
                updates.put("profileImageUrl", imageUrl);
                if (coverImageUri == null) {
                    updateProfile(updates);
                }
            });
        }

        if (coverImageUri != null) {
            uploadImage(coverImageUri, "cover_images/", (imageUrl) -> {
                updates.put("coverImageUrl", imageUrl);
                if (profileImageUri == null) {
                    updateProfile(updates);
                }
            });
        }

        if (profileImageUri == null && coverImageUri == null) {
            updateProfile(updates);
        }
    }

    private void uploadImage(Uri imageUri, String path, OnImageUploadListener listener) {
        String imageFileName = UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child(path + imageFileName);

        UploadTask uploadTask = imageRef.putFile(imageUri);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return imageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                listener.onSuccess(downloadUri.toString());
            } else {
                Toast.makeText(EditProfileActivity.this,
                    "Failed to upload image", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
            }
        });
    }

    private void updateProfile(Map<String, Object> updates) {
        userRef.updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditProfileActivity.this,
                    "Profile updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                Toast.makeText(EditProfileActivity.this,
                    "Failed to update profile", Toast.LENGTH_SHORT).show();
            });
    }

    interface OnImageUploadListener {
        void onSuccess(String imageUrl);
    }
}
