package com.example.socialmediaapp.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailActivity extends AppCompatActivity {
    private String tweetId;
    private Tweet currentTweet;
    private User currentUser;

    private CircleImageView profileImage;
    private TextView userName;
    private TextView userHandle;
    private TextView tweetContent;
    private ImageView tweetImage;
    private TextView tweetTime;
    private TextView retweetCount;
    private TextView likeCount;
    private ImageButton likeButton;
    private ImageButton retweetButton;
    private EditText replyInput;
    private ImageButton sendReplyButton;
    private RecyclerView repliesRecyclerView;
    private TweetAdapter repliesAdapter;
    private List<Tweet> replies;

    private DatabaseReference tweetRef;
    private DatabaseReference repliesRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Get tweet ID from intent
        tweetId = getIntent().getStringExtra("tweet_id");
        if (tweetId == null) {
            finish();
            return;
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        tweetRef = FirebaseDatabase.getInstance().getReference("tweets").child(tweetId);
        repliesRef = FirebaseDatabase.getInstance().getReference("tweets");

        // Initialize views
        initializeViews();
        setupToolbar();
        setupRecyclerView();

        // Load data
        loadTweet();
        loadCurrentUser();
        loadReplies();

        // Show reply input if requested
        if (getIntent().getBooleanExtra("show_reply", false)) {
            replyInput.requestFocus();
        }
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
        userHandle = findViewById(R.id.userHandle);
        tweetContent = findViewById(R.id.tweetContent);
        tweetImage = findViewById(R.id.tweetImage);
        tweetTime = findViewById(R.id.tweetTime);
        retweetCount = findViewById(R.id.retweetCount);
        likeCount = findViewById(R.id.likeCount);
        likeButton = findViewById(R.id.likeButton);
        retweetButton = findViewById(R.id.retweetButton);
        replyInput = findViewById(R.id.replyInput);
        sendReplyButton = findViewById(R.id.sendReplyButton);
        repliesRecyclerView = findViewById(R.id.repliesRecyclerView);

        sendReplyButton.setOnClickListener(v -> sendReply());
        likeButton.setOnClickListener(v -> toggleLike());
        retweetButton.setOnClickListener(v -> toggleRetweet());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tweet");
    }

    private void setupRecyclerView() {
        replies = new ArrayList<>();
        repliesAdapter = new TweetAdapter(this, replies, null);
        repliesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        repliesRecyclerView.setAdapter(repliesAdapter);
    }

    private void loadTweet() {
        tweetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentTweet = dataSnapshot.getValue(Tweet.class);
                if (currentTweet != null) {
                    updateTweetUI();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PostDetailActivity.this, "Error loading tweet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCurrentUser() {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void updateTweetUI() {
        userName.setText(currentTweet.getUserName());
        userHandle.setText("@" + currentTweet.getUserHandle());
        tweetContent.setText(currentTweet.getContent());
        retweetCount.setText(String.valueOf(currentTweet.getRetweetsCount()));
        likeCount.setText(String.valueOf(currentTweet.getLikesCount()));

        Glide.with(this)
            .load(currentTweet.getUserProfileImage())
            .placeholder(R.drawable.default_profile)
            .into(profileImage);

        if (currentTweet.getMediaUrl() != null && !currentTweet.getMediaUrl().isEmpty()) {
            tweetImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                .load(currentTweet.getMediaUrl())
                .into(tweetImage);
        } else {
            tweetImage.setVisibility(View.GONE);
        }

        updateLikeButton();
        updateRetweetButton();
    }

    private void loadReplies() {
        repliesRef.orderByChild("replyToTweetId").equalTo(tweetId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    replies.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Tweet reply = snapshot.getValue(Tweet.class);
                        if (reply != null) {
                            reply.setTweetId(snapshot.getKey());
                            replies.add(reply);
                        }
                    }
                    repliesAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(PostDetailActivity.this, "Error loading replies", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void sendReply() {
        String replyContent = replyInput.getText().toString().trim();
        if (TextUtils.isEmpty(replyContent)) {
            replyInput.setError("Reply cannot be empty");
            return;
        }

        String replyId = repliesRef.push().getKey();
        Tweet reply = new Tweet(
            auth.getCurrentUser().getUid(),
            currentUser.getUsername(),
            currentUser.getHandle(),
            currentUser.getProfileImageUrl(),
            replyContent,
            null
        );
        reply.setReplyToTweetId(tweetId);

        repliesRef.child(replyId).setValue(reply)
            .addOnSuccessListener(aVoid -> {
                replyInput.setText("");
                currentTweet.setRepliesCount(currentTweet.getRepliesCount() + 1);
                tweetRef.child("repliesCount").setValue(currentTweet.getRepliesCount());
            })
            .addOnFailureListener(e -> 
                Toast.makeText(PostDetailActivity.this, "Failed to send reply", Toast.LENGTH_SHORT).show()
            );
    }

    private void toggleLike() {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference likeRef = tweetRef.child("likes").child(userId);

        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Unlike
                    likeRef.removeValue();
                    currentTweet.setLikesCount(currentTweet.getLikesCount() - 1);
                } else {
                    // Like
                    likeRef.setValue(true);
                    currentTweet.setLikesCount(currentTweet.getLikesCount() + 1);
                }
                tweetRef.child("likesCount").setValue(currentTweet.getLikesCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void toggleRetweet() {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference retweetRef = tweetRef.child("retweets").child(userId);

        retweetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Undo retweet
                    retweetRef.removeValue();
                    currentTweet.setRetweetsCount(currentTweet.getRetweetsCount() - 1);
                } else {
                    // Retweet
                    retweetRef.setValue(true);
                    currentTweet.setRetweetsCount(currentTweet.getRetweetsCount() + 1);
                }
                tweetRef.child("retweetsCount").setValue(currentTweet.getRetweetsCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void updateLikeButton() {
        String userId = auth.getCurrentUser().getUid();
        boolean isLiked = currentTweet.getLikes() != null && 
                         currentTweet.getLikes().containsKey(userId);
        likeButton.setSelected(isLiked);
    }

    private void updateRetweetButton() {
        String userId = auth.getCurrentUser().getUid();
        boolean isRetweeted = currentTweet.getRetweets() != null && 
                            currentTweet.getRetweets().containsKey(userId);
        retweetButton.setSelected(isRetweeted);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
