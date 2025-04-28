package com.example.socialmediaapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.MessageThreadAdapter;
import com.example.socialmediaapp.data.MessageThread;
import com.example.socialmediaapp.ui.NewMessageActivity;
import com.google.android.material.button.MaterialButton;
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

public class MessagesFragment extends Fragment implements MessageThreadAdapter.MessageThreadClickListener {
    private View emptyView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private SearchView searchView;
    private CircleImageView profileImage;
    private ImageButton newMessageButton;
    private MaterialButton startMessageButton;

    private MessageThreadAdapter messageAdapter;
    private List<MessageThread> messageThreads;
    private DatabaseReference messagesRef;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        messagesRef = FirebaseDatabase.getInstance().getReference("messages")
                .child(currentUserId);

        // Initialize views
        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        loadUserProfile();
        loadMessageThreads();
    }

    private void initializeViews(View view) {
        emptyView = view.findViewById(R.id.emptyView);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        searchView = view.findViewById(R.id.searchView);
        profileImage = view.findViewById(R.id.profileImage);
        newMessageButton = view.findViewById(R.id.newMessageButton);
        startMessageButton = view.findViewById(R.id.startMessageButton);
    }

    private void setupRecyclerView() {
        messageThreads = new ArrayList<>();
        messageAdapter = new MessageThreadAdapter(getContext(), messageThreads, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadMessageThreads);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchMessages(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadMessageThreads();
                }
                return true;
            }
        });

        View.OnClickListener startNewMessage = v -> 
            startActivity(new Intent(getContext(), NewMessageActivity.class));

        newMessageButton.setOnClickListener(startNewMessage);
        startMessageButton.setOnClickListener(startNewMessage);
    }

    private void loadUserProfile() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUserId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                if (profileImageUrl != null) {
                    Glide.with(MessagesFragment.this)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.default_profile)
                        .into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void loadMessageThreads() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        messagesRef.orderByChild("lastMessageTime")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    messageThreads.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        MessageThread thread = snapshot.getValue(MessageThread.class);
                        if (thread != null) {
                            thread.setThreadId(snapshot.getKey());
                            messageThreads.add(thread);
                        }
                    }

                    Collections.reverse(messageThreads);
                    updateUI();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    handleError();
                }
            });
    }

    private void searchMessages(String query) {
        progressBar.setVisibility(View.VISIBLE);
        Query searchQuery = messagesRef.orderByChild("otherUserName")
                .startAt(query)
                .endAt(query + "\uf8ff");

        searchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageThreads.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageThread thread = snapshot.getValue(MessageThread.class);
                    if (thread != null) {
                        thread.setThreadId(snapshot.getKey());
                        messageThreads.add(thread);
                    }
                }
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

        if (messageThreads.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            messageAdapter.notifyDataSetChanged();
        }
    }

    private void handleError() {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        Toast.makeText(getContext(), "Error loading messages", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onThreadClicked(MessageThread thread) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("thread_id", thread.getThreadId());
        intent.putExtra("other_user_id", thread.getOtherUserId());
        startActivity(intent);
    }
}
