package com.example.socialmediaapp.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.data.MessageThread;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageThreadAdapter extends RecyclerView.Adapter<MessageThreadAdapter.MessageThreadViewHolder> {
    private Context context;
    private List<MessageThread> threads;
    private MessageThreadClickListener listener;
    private String currentUserId;

    public interface MessageThreadClickListener {
        void onThreadClicked(MessageThread thread);
    }

    public MessageThreadAdapter(Context context, List<MessageThread> threads, MessageThreadClickListener listener) {
        this.context = context;
        this.threads = threads;
        this.listener = listener;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MessageThreadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message_thread, parent, false);
        return new MessageThreadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageThreadViewHolder holder, int position) {
        MessageThread thread = threads.get(position);

        // Load user profile image
        Glide.with(context)
            .load(thread.getOtherUserImage())
            .placeholder(R.drawable.default_profile)
            .into(holder.profileImage);

        // Set user name
        holder.userName.setText(thread.getOtherUserName());

        // Set last message
        if (thread.getLastMessage() != null) {
            holder.lastMessage.setVisibility(View.VISIBLE);
            String messagePrefix = thread.getLastMessageSenderId().equals(currentUserId) ? "You: " : "";
            holder.lastMessage.setText(messagePrefix + thread.getLastMessage());
        } else {
            holder.lastMessage.setVisibility(View.GONE);
        }

        // Set time
        if (thread.getLastMessageTime() > 0) {
            holder.timeText.setVisibility(View.VISIBLE);
            holder.timeText.setText(getRelativeTimeSpan(thread.getLastMessageTime()));
        } else {
            holder.timeText.setVisibility(View.GONE);
        }

        // Set unread indicator
        holder.unreadIndicator.setVisibility(
            (!thread.isRead() && !thread.getLastMessageSenderId().equals(currentUserId)) 
            ? View.VISIBLE : View.GONE
        );

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onThreadClicked(thread);
            }
        });
    }

    @Override
    public int getItemCount() {
        return threads.size();
    }

    private String getRelativeTimeSpan(long timestamp) {
        return DateUtils.getRelativeTimeSpanString(
            timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString();
    }

    static class MessageThreadViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userName;
        TextView lastMessage;
        TextView timeText;
        View unreadIndicator;

        MessageThreadViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            timeText = itemView.findViewById(R.id.timeText);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }
    }
}
