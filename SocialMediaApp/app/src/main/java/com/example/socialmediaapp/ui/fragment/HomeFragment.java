package com.example.socialmediaapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.TweetAdapter;
import com.example.socialmediaapp.data.Tweet;
import com.example.socialmediaapp.ui.PostDetailActivity;
import com.example.socialmediaapp.ui.ProfileActivity;
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

public class HomeFragment extends Fragment implements TweetAdapter.TweetInteractionListener {
    private RecyclerView recyclerTweets;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView textEmpty;
    private TweetAdapter tweetAdapter;
    private List<Tweet> tweetList;
    private DatabaseReference tweetsRef;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        tweetsRef = FirebaseDatabase.getInstance().getReference().child("tweets");

        // Initialize views
        recyclerTweets = view.findViewById(R.id.recyclerTweets);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressBar);
        textEmpty = view.findViewById(R.id.textEmpty);

        // Setup RecyclerView
        tweetList = new ArrayList<>();
        tweetAdapter = new TweetAdapter(getContext(), tweetList, this);
        recyclerTweets.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTweets.setAdapter(tweetAdapter);

        // Setup SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this::loadTweets);

        // Load initial tweets
        loadTweets();
    }

    private void loadTweets() {
        progressBar.setVisibility(View.VISIBLE);
        textEmpty.setVisibility(View.GONE);

        Query tweetsQuery = tweetsRef.orderByChild("timestamp").limitToLast(100);
        tweetsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                textEmpty.setVisibility(tweetList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), "Error loading tweets: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTweetClicked(Tweet tweet, int position) {
        Intent intent = new Intent(getContext(), PostDetailActivity.class);
        intent.putExtra("tweet_id", tweet.getTweetId());
        startActivity(intent);
    }

    @Override
    public void onUserProfileClicked(String userId) {
        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    @Override
    public void onReplyClicked(Tweet tweet) {
        // Handle reply
        Intent intent = new Intent(getContext(), PostDetailActivity.class);
        intent.putExtra("tweet_id", tweet.getTweetId());
        intent.putExtra("show_reply", true);
        startActivity(intent);
    }

    @Override
    public void onRetweetClicked(Tweet tweet) {
        // Handle retweet
        DatabaseReference userRetweetsRef = FirebaseDatabase.getInstance()
                .getReference("tweets")
                .child(tweet.getTweetId())
                .child("retweets")
                .child(currentUserId);

        userRetweetsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User has already retweeted, so undo retweet
                    userRetweetsRef.removeValue();
                    tweet.setRetweetsCount(tweet.getRetweetsCount() - 1);
                } else {
                    // User hasn't retweeted, so add retweet
                    userRetweetsRef.setValue(true);
                    tweet.setRetweetsCount(tweet.getRetweetsCount() + 1);
                }
                tweetsRef.child(tweet.getTweetId()).child("retweetsCount")
                        .setValue(tweet.getRetweetsCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error updating retweet",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLikeClicked(Tweet tweet) {
        // Handle like
        DatabaseReference userLikesRef = FirebaseDatabase.getInstance()
                .getReference("tweets")
                .child(tweet.getTweetId())
                .child("likes")
                .child(currentUserId);

        userLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User has already liked, so unlike
                    userLikesRef.removeValue();
                    tweet.setLikesCount(tweet.getLikesCount() - 1);
                } else {
                    // User hasn't liked, so add like
                    userLikesRef.setValue(true);
                    tweet.setLikesCount(tweet.getLikesCount() + 1);
                }
                tweetsRef.child(tweet.getTweetId()).child("likesCount")
                        .setValue(tweet.getLikesCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error updating like",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onShareClicked(Tweet tweet) {
        // Handle share
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, tweet.getContent());
        startActivity(Intent.createChooser(shareIntent, "Share Tweet"));
    }
}
