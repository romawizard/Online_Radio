package ru.roma.musicplayer;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import static ru.roma.musicplayer.ExoPlayerAdapter.ARTIST;
import static ru.roma.musicplayer.ExoPlayerAdapter.TITLE;

public class MediaNotificationProvider {

    public static final int NOTIFICATION_ID = 412;
    private static final String CHANNEL_ID = "ru.roma.musicplayer";
    private static final String CHANNEL_NAME = "MediaNotificationProvider";

    private MediaPlayerService service;
    private NotificationCompat.Action actionPlay;
    private NotificationCompat.Action actionPause;
    private android.support.v4.media.app.NotificationCompat.MediaStyle style;

    public MediaNotificationProvider(MediaPlayerService service) {
        this.service = service;

        actionPlay = new NotificationCompat.Action(R.drawable.play,
                service.getResources().getString(R.string.play),
                MediaButtonReceiver.buildMediaButtonPendingIntent(service.getApplicationContext(),PlaybackStateCompat.ACTION_PLAY));

        actionPause = new NotificationCompat.Action(R.drawable.pause,
                service.getResources().getString(R.string.pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(service.getApplicationContext(),PlaybackStateCompat.ACTION_PAUSE));
        style = new android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(service.getSessionToken())
                .setShowActionsInCompactView(0)
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver
                        .buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_STOP));
    }

    public Notification getNotification(PlaybackStateCompat state, MediaMetadataCompat metadata
            , MediaSessionCompat.Token token){
        String title = metadata.getString(TITLE);
        String artist = metadata.getString(ARTIST);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            createChannel();
        }

        NotificationCompat.Action action;
        if (state.getState() == PlaybackStateCompat.STATE_PLAYING){
            action = actionPause;
        }else {
            action = actionPlay;
        }

        Notification notification = new NotificationCompat.Builder(service,CHANNEL_ID)
                .setContentTitle(artist)
                .setContentText(title)
                .setContentIntent(createSessionActivity())

                .addAction(action)

                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_STOP))
                .setSmallIcon(R.drawable.radio_icon)
                .setColor(ContextCompat.getColor(service, R.color.colorButton))

                .setStyle(style)

                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setShowWhen(false)

                .build();
        return notification;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationManager manager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager.getNotificationChannel(CHANNEL_ID) == null){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setLightColor(R.color.colorButton);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }
    }

    private PendingIntent createSessionActivity() {
        Intent intent = new Intent(service, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(service, 0, intent, 0);
    }

}

