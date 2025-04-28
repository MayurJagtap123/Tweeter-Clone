package com.example.socialmediaapp.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.socialmediaapp.R;
import static com.example.socialmediaapp.R.drawable;
import com.example.socialmediaapp.ui.ChatActivity;
import com.example.socialmediaapp.ui.MainActivity;
import com.example.socialmediaapp.ui.PostDetailActivity;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String CHANNEL_ID = "twitter_clone_notifications";
    private static final String CHANNEL_NAME = "Twitter Clone";
    private static final String CHANNEL_DESC = "Twitter Clone Notifications";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Get notification data
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String type = remoteMessage.getData().get("type");
        String id = remoteMessage.getData().get("id");

        // Create notification based on type
        if (type != null && id != null) {
            switch (type) {
                case "message":
                    sendMessageNotification(title, body, id);
                    break;
                case "tweet":
                    sendTweetNotification(title, body, id);
                    break;
                default:
                    sendDefaultNotification(title, body);
                    break;
            }
        } else {
            sendDefaultNotification(title, body);
        }
    }

    private void sendMessageNotification(String title, String body, String threadId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("thread_id", threadId);
        sendNotification(title, body, intent);
    }

    private void sendTweetNotification(String title, String body, String tweetId) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("tweet_id", tweetId);
        sendNotification(title, body, intent);
    }

    private void sendDefaultNotification(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        sendNotification(title, body, intent);
    }

    private void sendNotification(String title, String body, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // Set notification sound
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Build notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(drawable.ic_notifications)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            notificationManager.createNotificationChannel(channel);
        }

        // Show notification
        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // TODO: Send token to server
    }
}
