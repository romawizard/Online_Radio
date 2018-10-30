package ru.roma.musicplayer.service.notification;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

import ru.roma.musicplayer.R;
import ru.roma.musicplayer.ui.MainActivity;

public class MediaNotificationProvider {

    public static final int NOTIFICATION_ID = 412;
    private static final String CHANNEL_ID = "ru.roma.musicplayer";
    private static final String CHANNEL_NAME = "MediaNotificationProvider";

    private Context context;
    private NotificationCompat.Action actionPlay;
    private NotificationCompat.Action actionPause;
    private android.support.v4.media.app.NotificationCompat.MediaStyle style;
    private android.support.v4.media.app.NotificationCompat.MediaStyle emptyStyle;


    public MediaNotificationProvider(Context context, MediaSessionCompat.Token token) {
        this.context = context;

        actionPlay = new NotificationCompat.Action(R.drawable.play,
                context.getResources().getString(R.string.play),
                MediaButtonReceiver.buildMediaButtonPendingIntent(context.getApplicationContext(), PlaybackStateCompat.ACTION_PLAY));

        actionPause = new NotificationCompat.Action(R.drawable.pause,
                context.getResources().getString(R.string.pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(context.getApplicationContext(), PlaybackStateCompat.ACTION_PAUSE));
        style = new android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(token)
                .setShowActionsInCompactView(0)
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver
                        .buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP));

        emptyStyle = new android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(token)
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver
                        .buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    public Notification getNotification(PlaybackStateCompat state, MediaMetadataCompat metadata, Bitmap image) {
        String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);

        NotificationCompat.Action action;
        if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
            action = actionPause;
        } else {
            action = actionPlay;
        }

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(artist)
                .setContentText(title)
                .setContentIntent(createSessionActivity())

                .addAction(action)

                .setLargeIcon(image)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
                .setSmallIcon(R.drawable.radio_icon)
                .setColor(ContextCompat.getColor(context, R.color.colorBlue))

                .setStyle(style)

                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setShowWhen(false)

                .build();
        return notification;
    }

    public Notification getErrorNotification() {
        return builtNotification("Error", "check what happened");
    }

    public Notification getBufferingNotification() {
        return builtNotification("Buffering", "please waite...");
    }

    public Notification getEmptyNotification(){
        return builtNotification("","");
    }

    private Notification builtNotification(String title, String text) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(createSessionActivity())

                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
                .setSmallIcon(R.drawable.radio_icon)
                .setColor(ContextCompat.getColor(context, R.color.colorBlue))

                .setStyle(emptyStyle)

                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setShowWhen(false)

                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setLightColor(R.color.colorBlue);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }
    }

    private PendingIntent createSessionActivity() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    public void release() {
        context = null;
    }
}

