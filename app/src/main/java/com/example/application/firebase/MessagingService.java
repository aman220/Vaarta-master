package com.example.application.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.application.ChatActivity;
import com.example.application.R;
import com.example.application.activities.IncomingInvitationActivity;
import com.example.application.models.User;
import com.example.application.utilities.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
//        Log.d("FCM", "token:" + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        User user = new User();
        user.id = remoteMessage.getData().get(Constants.KEY_USER_ID);
        user.name = remoteMessage.getData().get(Constants.KEY_NAME);
        user.token = remoteMessage.getData().get(Constants.KEY_PCM_TOKEN);

        int notificationId = new Random().nextInt();
        String channelId = "chat_message";
        Intent intent = new Intent(  this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.KEY_USER, user);
        PendingIntent pendingIntent = PendingIntent.getActivity( this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder( this, channelId);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(user.name);
        builder.setContentText(remoteMessage.getData().get(Constants.KEY_MESSAGE));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
                remoteMessage.getData().get(Constants.KEY_MESSAGE)
        ));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence channelName = "Chat Message";
            String channelDescription = "This notification channel is used for chat message notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
             NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManagercompat = NotificationManagerCompat.from(this);
        notificationManagercompat.notify(notificationId, builder.build());







        // for send remote message in video call to user

         String type = remoteMessage.getData().get(Constants.REMOTE_MSG_TYPE);

         if (type != null) {
            if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                Intent intent1 = new Intent(getApplicationContext(), IncomingInvitationActivity.class);

                intent1.putExtra(Constants.REMOTE_MSG_MEETING_TYPE, remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_TYPE));
                intent1.putExtra(Constants.KEY_NAME, remoteMessage.getData().get(Constants.KEY_NAME));
                intent1.putExtra(Constants.KEY_EMAIL, remoteMessage.getData().get(Constants.KEY_EMAIL));
                intent1.putExtra(Constants.REMOTE_MSG_INVITER_TOKEN, remoteMessage.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN));
                intent1.putExtra(Constants.REMOTE_MSG_MEETING_ROOM, remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_ROOM));
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
            } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                Intent intent1 = new Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE);
                intent1.putExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE, remoteMessage.getData().get(Constants.REMOTE_MSG_INVITATION_RESPONSE));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent1);
            }
        }
    }
}
