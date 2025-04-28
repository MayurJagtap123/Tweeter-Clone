package com.example.socialmediaapp.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.data.Tweet;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.TweetViewHolder> {
    private Context context;
    private List<Tweet> tweets;
    private TweetInteractionListener listener;

    public interface TweetInteractionListener {
        void onTweetClicked(Tweet tweet, int position);
        void onUserProfileClicked(String userId);
        void onReplyClicked(Tweet tweet);
        void onRetweetClicked(Tweet tweet);
        void onLikeClicked(Tweet tweet);
        void onShareClicked(Tweet tweet);
    }

    public TweetAdapter(Context context, List<Tweet> tweets, TweetInteractionListener listener) {
        this.context = context;
        this.tweets = tweets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TweetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new TweetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TweetViewHolder holder, int position) {
        Tweet tweet = tweets.get(position);
        
        // Load profile image
        Glide.with(context)
            .load(tweet.getUserProfileImage())
            .placeholder(R.drawable.default_profile)
            .into(holder.profileImage);

        // Set user information
        holder.userName.setText(tweet.getUserName());
        holder.userHandle.setText("@" + tweet.getUserHandle());
        holder.tweetTime.setText(getRelativeTimeSpan(tweet.getTimestamp()));

        // Set tweet content
        holder.tweetContent.setText(tweet.getContent());

        // Handle tweet image if present
        if (tweet.getMediaUrl() != null && !tweet.getMediaUrl().isEmpty()) {
            holder.tweetImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                .load(tweet.getMediaUrl())
                .into(holder.tweetImage);
        } else {
            holder.tweetImage.setVisibility(View.GONE);
        }

        // Set interaction counts
        holder.replyCount.setText(formatCount(tweet.getRepliesCount()));
        holder.retweetCount.setText(formatCount(tweet.getRetweetsCount()));
        holder.likeCount.setText(formatCount(tweet.getLikesCount()));

        // Set click listeners
        holder.itemView.setOnClickListener(v -> 
            listener.onTweetClicked(tweet, position));

        holder.profileImage.setOnClickListener(v -> 
            listener.onUserProfileClicked(tweet.getUserId()));

        holder.replyButton.setOnClickListener(v -> 
            listener.onReplyClicked(tweet));

        holder.retweetButton.setOnClickListener(v -> 
            listener.onRetweetClicked(tweet));

        holder.likeButton.setOnClickListener(v -> 
            listener.onLikeClicked(tweet));

        holder.shareButton.setOnClickListener(v -> 
            listener.onShareClicked(tweet));
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public void updateTweets(List<Tweet> newTweets) {
        this.tweets = newTweets;
        notifyDataSetChanged();
    }

    private String formatCount(int count) {
        if (count < 1000) return String.valueOf(count);
        if (count < 1000000) return String.format("%.1fK", count/1000.0);
        return String.format("%.1fM", count/1000000.0);
    }

    private String getRelativeTimeSpan(long timestamp) {
        return DateUtils.getRelativeTimeSpanString(
            timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString();
    }

    static class TweetViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userName;
        TextView userHandle;
        TextView tweetTime;
        TextView tweetContent;
        ImageView tweetImage;
        LinearLayout replyButton;
        LinearLayout retweetButton;
        LinearLayout likeButton;
        LinearLayout shareButton;
        TextView replyCount;
        TextView retweetCount;
        TextView likeCount;

        TweetViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            userHandle = itemView.findViewById(R.id.userHandle);
            tweetTime = itemView.findViewById(R.id.tweetTime);
            tweetContent = itemView.findViewById(R.id.tweetContent);
            tweetImage = itemView.findViewById(R.id.tweetImage);
            replyButton = itemView.findViewById(R.id.replyButton);
            retweetButton = itemView.findViewById(R.id.retweetButton);
            likeButton = itemView.findViewById(R.id.likeButton);
            shareButton = itemView.findViewById(R.id.shareButton);
            replyCount = itemView.findViewById(R.id.replyCount);
            retweetCount = itemView.findViewById(R.id.retweetCount);
            likeCount = itemView.findViewById(R.id.likeCount);
        }
    }
}
