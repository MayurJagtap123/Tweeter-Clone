package com.example.socialmediaapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.NotificationAdapter;
import com.example.socialmediaapp.data.Notification;
import com.example.socialmediaapp.ui.PostDetailActivity;
import com.example.socialmediaapp.ui.ProfileActivity;
import com.google.android.material.tabs.TabLayout;
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

public class NotificationsFragment extends Fragment implements NotificationAdapter.NotificationClickListener {
    private View emptyView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private TabLayout tabLayout;

    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private DatabaseReference notificationsRef;
    private String currentUserId;
    private boolean showingMentions = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notificationsRef = FirebaseDatabase.getInstance().getReference("notifications")
                .child(currentUserId);

        // Initialize views
        emptyView = view.findViewById(R.id.emptyView);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        tabLayout = view.findViewById(R.id.tabLayout);

        // Setup RecyclerView
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), notificationList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(notificationAdapter);

        // Setup SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this::loadNotifications);

        // Setup TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showingMentions = tab.getPosition() == 1;
                loadNotifications();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadNotifications();
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        Query query = showingMentions ?
                notificationsRef.orderByChild("type").equalTo("mention") :
                notificationsRef.orderByChild("timestamp");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    if (notification != null) {
                        notification.setId(snapshot.getKey());
                        notificationList.add(notification);
                    }
                }

                Collections.reverse(notificationList);
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleError();
            }
        });
    }

    private void updateUI() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);

        if (notificationList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            notificationAdapter.notifyDataSetChanged();
        }
    }

    private void handleError() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        Toast.makeText(getContext(), "Error loading notifications", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotificationClicked(Notification notification) {
        switch (notification.getType()) {
            case "follow":
                openProfile(notification.getSourceUserId());
                break;
            case "like":
            case "retweet":
            case "reply":
            case "mention":
                openTweet(notification.getTweetId());
                break;
        }
    }

    @Override
    public void onUserClicked(String userId) {
        openProfile(userId);
    }

    private void openProfile(String userId) {
        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    private void openTweet(String tweetId) {
        Intent intent = new Intent(getContext(), PostDetailActivity.class);
        intent.putExtra("tweet_id", tweetId);
        startActivity(intent);
    }
}
