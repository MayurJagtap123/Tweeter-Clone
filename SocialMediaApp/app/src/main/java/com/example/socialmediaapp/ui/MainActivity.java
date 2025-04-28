package com.example.socialmediaapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.ViewPagerAdapter;
import com.example.socialmediaapp.ui.fragment.HomeFragment;
import com.example.socialmediaapp.ui.fragment.MessagesFragment;
import com.example.socialmediaapp.ui.fragment.NotificationsFragment;
import com.example.socialmediaapp.ui.fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabCompose;
    private CircleImageView profileImage;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        viewPager = findViewById(R.id.viewPager);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabCompose = findViewById(R.id.fabCompose);
        profileImage = findViewById(R.id.profileImage);

        // Setup ViewPager
        setupViewPager();

        // Setup BottomNavigationView
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.navigation_search) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.navigation_notifications) {
                viewPager.setCurrentItem(2);
                return true;
            } else if (itemId == R.id.navigation_messages) {
                viewPager.setCurrentItem(3);
                return true;
            }
            return false;
        });

        // Handle ViewPager page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bottomNavigation.getMenu().getItem(position).setChecked(true);
            }
        });

        // Setup FAB
        fabCompose.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ComposeTweetActivity.class));
        });

        // Setup profile image click
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra("user_id", currentUser.getUid());
            startActivity(intent);
        });

        // Load user profile image
        loadUserProfile();
    }

    private void setupViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new HomeFragment());
        fragments.add(new SearchFragment());
        fragments.add(new NotificationsFragment());
        fragments.add(new MessagesFragment());

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(this, fragments);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(fragments.size());
        viewPager.setUserInputEnabled(false); // Disable swipe to change pages
    }

    private void loadUserProfile() {
        usersRef.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(MainActivity.this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.default_profile)
                            .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if user is still logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
