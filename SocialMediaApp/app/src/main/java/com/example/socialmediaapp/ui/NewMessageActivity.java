package com.example.socialmediaapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.UserAdapter;
import com.example.socialmediaapp.data.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NewMessageActivity extends AppCompatActivity implements UserAdapter.UserClickListener {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private SearchView searchView;
    private Button nextButton;
    private ImageButton closeButton;

    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference usersRef;
    private String currentUserId;
    private User selectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadUsers();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        searchView = findViewById(R.id.searchView);
        nextButton = findViewById(R.id.nextButton);
        closeButton = findViewById(R.id.closeButton);
    }

    private void setupRecyclerView() {
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);
    }

    private void setupListeners() {
        closeButton.setOnClickListener(v -> finish());

        nextButton.setOnClickListener(v -> {
            if (selectedUser != null) {
                startChat();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadUsers();
                } else {
                    searchUsers(newText);
                }
                return true;
            }
        });
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && !user.getUserId().equals(currentUserId)) {
                        userList.add(user);
                    }
                }
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("Error loading users");
            }
        });
    }

    private void searchUsers(String query) {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        Query searchQuery = usersRef.orderByChild("username")
                .startAt(query)
                .endAt(query + "\uf8ff");

        searchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && !user.getUserId().equals(currentUserId)) {
                        userList.add(user);
                    }
                }
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("Error searching users");
            }
        });
    }

    private void updateUI() {
        progressBar.setVisibility(View.GONE);
        
        if (userList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            userAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onUserClicked(User user) {
        selectedUser = user;
        nextButton.setEnabled(true);
    }

    private void startChat() {
        // Generate a unique thread ID using both user IDs
        String threadId = currentUserId.compareTo(selectedUser.getUserId()) < 0 
            ? currentUserId + "_" + selectedUser.getUserId()
            : selectedUser.getUserId() + "_" + currentUserId;

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("thread_id", threadId);
        intent.putExtra("other_user_id", selectedUser.getUserId());
        startActivity(intent);
        finish();
    }
}
