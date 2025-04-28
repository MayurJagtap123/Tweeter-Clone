package com.example.socialmediaapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.appcompat.widget.SearchView;
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
import com.example.socialmediaapp.data.User;
import com.example.socialmediaapp.ui.PostDetailActivity;
import com.example.socialmediaapp.ui.ProfileActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements TweetAdapter.TweetInteractionListener {
    private View emptyView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private SearchView searchView;
    
    private TweetAdapter tweetAdapter;
    private List<Tweet> tweetList;
    private DatabaseReference tweetsRef;
    private DatabaseReference usersRef;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase references
        tweetsRef = FirebaseDatabase.getInstance().getReference("tweets");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        emptyView = view.findViewById(R.id.emptyView);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        searchView = view.findViewById(R.id.searchView);

        // Setup RecyclerView
        tweetList = new ArrayList<>();
        tweetAdapter = new TweetAdapter(getContext(), tweetList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(tweetAdapter);

        // Setup SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener(() -> {
            if (!searchView.getQuery().toString().isEmpty()) {
                performSearch(searchView.getQuery().toString());
            }
        });

        // Setup SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    tweetList.clear();
                    tweetAdapter.notifyDataSetChanged();
                    showEmptyView();
                }
                return true;
            }
        });

        showEmptyView();
    }

    private void performSearch(String query) {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        
        // Search in tweets
        Query tweetQuery = tweetsRef.orderByChild("content")
                .startAt(query)
                .endAt(query + "\uf8ff");

        tweetQuery.addListenerForSingleValueEvent(new ValueEventListener() {
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
                
                // Also search in user handles and names
                searchUsers(query);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleSearchError();
            }
        });
    }

    private void searchUsers(String query) {
        Query userQuery = usersRef.orderByChild("handle")
                .startAt(query)
                .endAt(query + "\uf8ff");

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        // Get user's latest tweet
                        Query latestTweetQuery = tweetsRef.orderByChild("userId")
                                .equalTo(user.getUserId())
                                .limitToLast(1);
                        
                        latestTweetQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot tweetSnapshot) {
                                for (DataSnapshot tweetData : tweetSnapshot.getChildren()) {
                                    Tweet tweet = tweetData.getValue(Tweet.class);
                                    if (tweet != null) {
                                        tweet.setTweetId(tweetData.getKey());
                                        tweetList.add(tweet);
                                    }
                                }
                                finishSearch();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                handleSearchError();
                            }
                        });
                    }
                }
                if (!dataSnapshot.exists()) {
                    finishSearch();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleSearchError();
            }
        });
    }

    private void finishSearch() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        
        if (tweetList.isEmpty()) {
            showEmptyView();
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        
        tweetAdapter.notifyDataSetChanged();
    }

    private void handleSearchError() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        Toast.makeText(getContext(), "Error performing search", Toast.LENGTH_SHORT).show();
    }

    private void showEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    // TweetAdapter.TweetInteractionListener implementations
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
        Intent intent = new Intent(getContext(), PostDetailActivity.class);
        intent.putExtra("tweet_id", tweet.getTweetId());
        intent.putExtra("show_reply", true);
        startActivity(intent);
    }

    @Override
    public void onRetweetClicked(Tweet tweet) {
        // Handle retweet in activity
    }

    @Override
    public void onLikeClicked(Tweet tweet) {
        // Handle like in activity
    }

    @Override
    public void onShareClicked(Tweet tweet) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, tweet.getContent());
        startActivity(Intent.createChooser(shareIntent, "Share Tweet"));
    }
}
