package com.example.socialmediaapp.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapter.MessageAdapter;
import com.example.socialmediaapp.data.Message;
import com.example.socialmediaapp.data.MessageThread;
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

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageButton backButton;
    private CircleImageView profileImage;
    private TextView userName;

    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private String threadId;
    private String otherUserId;
    private String currentUserId;
    private DatabaseReference messagesRef;
    private DatabaseReference threadRef;
    private User otherUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get thread ID and other user ID from intent
        threadId = getIntent().getStringExtra("thread_id");
        otherUserId = getIntent().getStringExtra("other_user_id");
        
        if (threadId == null || otherUserId == null) {
            finish();
            return;
        }

        // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        messagesRef = FirebaseDatabase.getInstance().getReference("messages")
                .child(threadId).child("messages");
        threadRef = FirebaseDatabase.getInstance().getReference("messages")
                .child(threadId);

        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadOtherUser();
        loadMessages();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadOtherUser() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(otherUserId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                otherUser = dataSnapshot.getValue(User.class);
                if (otherUser != null) {
                    userName.setText(otherUser.getUsername());
                    if (otherUser.getProfileImageUrl() != null) {
                        Glide.with(ChatActivity.this)
                            .load(otherUser.getProfileImageUrl())
                            .placeholder(R.drawable.default_profile)
                            .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Error loading user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        message.setMessageId(snapshot.getKey());
                        messages.add(message);
                        // Mark received messages as read
                        if (!message.getSenderId().equals(currentUserId) && !message.isRead()) {
                            snapshot.getRef().child("read").setValue(true);
                        }
                    }
                }
                messageAdapter.notifyDataSetChanged();
                if (!messages.isEmpty()) {
                    recyclerView.smoothScrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) return;

        messageInput.setText("");

        String messageId = messagesRef.push().getKey();
        Message message = new Message(currentUserId, content);

        if (messageId != null) {
            messagesRef.child(messageId).setValue(message)
                .addOnSuccessListener(aVoid -> {
                    // Update thread metadata
                    MessageThread thread = new MessageThread(otherUserId, otherUser.getUsername(), 
                        otherUser.getProfileImageUrl());
                    thread.updateLastMessage(content, currentUserId);
                    threadRef.setValue(thread);

                    // Also update the thread for other user
                    DatabaseReference otherThreadRef = FirebaseDatabase.getInstance()
                        .getReference("messages")
                        .child(threadId);
                    otherThreadRef.setValue(thread);
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show()
                );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Mark all messages as read when leaving the chat
        for (Message message : messages) {
            if (!message.getSenderId().equals(currentUserId) && !message.isRead()) {
                messagesRef.child(message.getMessageId()).child("read").setValue(true);
            }
        }
    }
}
