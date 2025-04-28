package com.example.socialmediaapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.data.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<User> users;
    private UserClickListener listener;
    private User selectedUser;

    public interface UserClickListener {
        void onUserClicked(User user);
    }

    public UserAdapter(Context context, List<User> users, UserClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        // Load profile image
        Glide.with(context)
            .load(user.getProfileImageUrl())
            .placeholder(R.drawable.default_profile)
            .into(holder.profileImage);

        // Set user info
        holder.userName.setText(user.getUsername());
        holder.userHandle.setText("@" + user.getHandle());
        
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            holder.userBio.setVisibility(View.VISIBLE);
            holder.userBio.setText(user.getBio());
        } else {
            holder.userBio.setVisibility(View.GONE);
        }

        // Set selection state
        boolean isSelected = user.equals(selectedUser);
        holder.itemView.setSelected(isSelected);
        holder.itemView.setBackgroundResource(
            isSelected ? R.color.twitter_extra_light_gray : android.R.color.transparent
        );

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Update selection
                User previousSelected = selectedUser;
                selectedUser = user;
                
                // Update UI for previous and new selection
                if (previousSelected != null) {
                    notifyItemChanged(users.indexOf(previousSelected));
                }
                notifyItemChanged(position);
                
                listener.onUserClicked(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userName;
        TextView userHandle;
        TextView userBio;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            userHandle = itemView.findViewById(R.id.userHandle);
            userBio = itemView.findViewById(R.id.userBio);
        }
    }

    public void clearSelection() {
        User previousSelected = selectedUser;
        selectedUser = null;
        if (previousSelected != null) {
            notifyItemChanged(users.indexOf(previousSelected));
        }
    }
}
