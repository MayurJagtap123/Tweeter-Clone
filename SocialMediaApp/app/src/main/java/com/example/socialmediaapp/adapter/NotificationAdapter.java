package com.example.socialmediaapp.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.data.Notification;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private Context context;
    private List<Notification> notifications;
    private NotificationClickListener listener;

    public interface NotificationClickListener {
        void onNotificationClicked(Notification notification);
        void onUserClicked(String userId);
    }

    public NotificationAdapter(Context context, List<Notification> notifications, NotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        // Load user profile image
        Glide.with(context)
            .load(notification.getSourceUserImage())
            .placeholder(R.drawable.default_profile)
            .into(holder.profileImage);

        // Set notification type icon
        switch (notification.getType()) {
            case "follow":
                holder.typeIcon.setImageResource(R.drawable.ic_person_add);
                holder.typeIcon.setColorFilter(context.getResources().getColor(R.color.twitter_blue));
                break;
            case "like":
                holder.typeIcon.setImageResource(R.drawable.ic_like);
                holder.typeIcon.setColorFilter(context.getResources().getColor(R.color.like_red));
                break;
            case "retweet":
                holder.typeIcon.setImageResource(R.drawable.ic_retweet);
                holder.typeIcon.setColorFilter(context.getResources().getColor(R.color.retweet_green));
                break;
            case "reply":
            case "mention":
                holder.typeIcon.setImageResource(R.drawable.ic_reply);
                holder.typeIcon.setColorFilter(context.getResources().getColor(R.color.twitter_blue));
                break;
        }

        // Set notification text
        holder.notificationText.setText(notification.getNotificationText());

        // Set tweet content if available
        if (notification.getTweetContent() != null && !notification.getType().equals("follow")) {
            holder.tweetContent.setVisibility(View.VISIBLE);
            holder.tweetContent.setText(notification.getTweetContent());
        } else {
            holder.tweetContent.setVisibility(View.GONE);
        }

        // Set time
        holder.timeText.setText(getRelativeTimeSpan(notification.getTimestamp()));

        // Set click listeners
        holder.itemView.setOnClickListener(v -> 
            listener.onNotificationClicked(notification));
        
        holder.profileImage.setOnClickListener(v -> 
            listener.onUserClicked(notification.getSourceUserId()));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    private String getRelativeTimeSpan(long timestamp) {
        return DateUtils.getRelativeTimeSpanString(
            timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        ImageView typeIcon;
        TextView notificationText;
        TextView tweetContent;
        TextView timeText;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            typeIcon = itemView.findViewById(R.id.typeIcon);
            notificationText = itemView.findViewById(R.id.notificationText);
            tweetContent = itemView.findViewById(R.id.tweetContent);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }
}
