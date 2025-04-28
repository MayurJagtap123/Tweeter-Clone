package com.example.socialmediaapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.TweetAdapter;
import com.example.socialmediaapp.data.Tweet;
import com.example.socialmediaapp.data.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements TweetAdapter.TweetInteractionListener {
    private CircleImageView profileImage;
    private ImageView coverImage;
    private TextView userName;
    private TextView userHandle;
    private TextView userBio;
    private TextView locationText;
    private TextView joinDateText;
    private TextView followingCount;
    private TextView followersCount;
    private Button followButton;
    private RecyclerView recyclerTweets;
    private SwipeRefreshLayout swipeRefresh;
    private TweetAdapter tweetAdapter;
    private List<Tweet> tweetList;

    private String userId;
    private User userProfile;
    private DatabaseReference userRef;
    private DatabaseReference tweetsRef;
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Get user ID from intent
        userId = getIntent().getStringExtra("user_id");
        if (userId == null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Initialize Firebase references
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        tweetsRef = FirebaseDatabase.getInstance().getReference("tweets");

        // Initialize views
        initializeViews();
        setupToolbar();
        setupRecyclerView();

        // Load user data and tweets
        loadUserProfile();
        loadUserTweets();

        // Setup refresh listener
        swipeRefresh.setOnRefreshListener(this::loadUserTweets);
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profileImage);
        coverImage = findViewById(R.id.coverImage);
        userName = findViewById(R.id.userName);
        userHandle = findViewById(R.id.userHandle);
        userBio = findViewById(R.id.userBio);
        locationText = findViewById(R.id.locationText);
        joinDateText = findViewById(R.id.joinDateText);
        followingCount = findViewById(R.id.followingCount);
        followersCount = findViewById(R.id.followersCount);
        followButton = findViewById(R.id.followButton);
        recyclerTweets = findViewById(R.id.recyclerTweets);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        // Setup follow button
        if (userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            followButton.setText("Edit Profile");
            followButton.setOnClickListener(v -> startEditProfile());
        } else {
            followButton.setOnClickListener(v -> toggleFollow());
            checkFollowStatus();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    private void setupRecyclerView() {
        tweetList = new ArrayList<>();
        tweetAdapter = new TweetAdapter(this, tweetList, this);
        recyclerTweets.setLayoutManager(new LinearLayoutManager(this));
        recyclerTweets.setAdapter(tweetAdapter);
    }

    private void loadUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userProfile = dataSnapshot.getValue(User.class);
                if (userProfile != null) {
                    updateUI();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        userName.setText(userProfile.getUsername());
        userHandle.setText("@" + userProfile.getHandle());
        userBio.setText(userProfile.getBio());
        locationText.setText(userProfile.getLocation());
        followingCount.setText(String.valueOf(userProfile.getFollowingCount()));
        followersCount.setText(String.valueOf(userProfile.getFollowersCount()));

        if (userProfile.getProfileImageUrl() != null) {
            Glide.with(this)
                .load(userProfile.getProfileImageUrl())
                .placeholder(R.drawable.default_profile)
                .into(profileImage);
        }

        if (userProfile.getCoverImageUrl() != null) {
            Glide.with(this)
                .load(userProfile.getCoverImageUrl())
                .into(coverImage);
        }
    }

    private void loadUserTweets() {
        Query query = tweetsRef.orderByChild("userId").equalTo(userId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tweetList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Tweet tweet = snapshot.getValue(Tweet.class);
                    if (tweet != null) {
                        tweet.setTweetId(snapshot.getKey());
                        tweetList.add(tweet);
                    }
                }
                Collections.reverse(tweetList);
                tweetAdapter.notifyDataSetChanged();
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(ProfileActivity.this, "Error loading tweets", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkFollowStatus() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference followRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(currentUserId)
            .child("following")
            .child(userId);

        followRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isFollowing = dataSnapshot.exists();
                updateFollowButton();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void toggleFollow() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference currentUserFollowing = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(currentUserId)
            .child("following")
            .child(userId);

        DatabaseReference targetUserFollowers = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("followers")
            .child(currentUserId);

        if (isFollowing) {
            // Unfollow
            currentUserFollowing.removeValue();
            targetUserFollowers.removeValue();
            userProfile.setFollowersCount(userProfile.getFollowersCount() - 1);
        } else {
            // Follow
            currentUserFollowing.setValue(true);
            targetUserFollowers.setValue(true);
            userProfile.setFollowersCount(userProfile.getFollowersCount() + 1);
        }

        userRef.child("followersCount").setValue(userProfile.getFollowersCount());
    }

    private void updateFollowButton() {
        followButton.setText(isFollowing ? "Following" : "Follow");
    }

    private void startEditProfile() {
        // Start edit profile activity
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // TweetAdapter.TweetInteractionListener implementations
    @Override
    public void onTweetClicked(Tweet tweet, int position) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("tweet_id", tweet.getTweetId());
        startActivity(intent);
    }

    @Override
    public void onUserProfileClicked(String userId) {
        if (!userId.equals(this.userId)) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        }
    }

    @Override
    public void onReplyClicked(Tweet tweet) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("tweet_id", tweet.getTweetId());
        intent.putExtra("show_reply", true);
        startActivity(intent);
    }

    @Override
    public void onRetweetClicked(Tweet tweet) {
        // Handle retweet
    }

    @Override
    public void onLikeClicked(Tweet tweet) {
        // Handle like
    }

    @Override
    public void onShareClicked(Tweet tweet) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, tweet.getContent());
        startActivity(Intent.createChooser(shareIntent, "Share Tweet"));
    }
}
