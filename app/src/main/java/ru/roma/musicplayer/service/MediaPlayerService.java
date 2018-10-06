package ru.roma.musicplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import ru.roma.musicplayer.R;
import ru.roma.musicplayer.service.library.RadioLibrary;
import ru.roma.musicplayer.service.notification.MediaNotificationProvider;
import ru.roma.musicplayer.service.player.AbstractPlayer;
import ru.roma.musicplayer.service.player.ExoPlayerImpl;
import ru.roma.musicplayer.ui.MainActivity;


public class MediaPlayerService extends MediaBrowserServiceCompat {

    public static final String PARENT_ID = "parent_id";
    private static final String TAG = MediaPlayerService.class.getCanonicalName();
    private final String PLAYER_TAG = "Media Player Service";
    private MediaSessionCompat mediaSession;
    private AbstractPlayer player;
    private AudioManager.OnAudioFocusChangeListener afChangeListener;
    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private BecomingNoisyReceiver noisyReceiver = new BecomingNoisyReceiver();
    private MediaNotificationManager notificationManager;
    private String url;
    private String currentStationName;
    private boolean isStarted = false;
    private boolean isReceiverRegistered = false;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate " + this);
        super.onCreate();
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        url = preferences.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, getString(R.string.comedy_radio));
        player = new ExoPlayerImpl(url, new PlayerListener());
        afChangeListener = new FocusChangeListener();
//        startMediaPlayerService();
        initMediaSession();
        notificationManager = new MediaNotificationManager();

    }

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, PLAYER_TAG);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(null);

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().
                setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_STOP |
                        PlaybackStateCompat.ACTION_PLAY);

        mediaSession.setPlaybackState(stateBuilder.build());

        MediaSessionCallback sessionCallback = new MediaSessionCallback();
        mediaSession.setCallback(sessionCallback);
        mediaSession.setSessionActivity(createSessionActivity());

        setSessionToken(mediaSession.getSessionToken());

        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(ExoPlayerImpl.TITLE, "")
                .putString(ExoPlayerImpl.ARTIST, "")
                .build();
        mediaSession.setMetadata(metadata);

    }

    private PendingIntent createReceiver() {
        ComponentName componentName = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setComponent(componentName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private PendingIntent createSessionActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(PARENT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(RadioLibrary.getMediaItems());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy " + this);
        if (isReceiverRegistered) {
            unregisterReceiver(noisyReceiver);
        }
        player.release();
        mediaSession.release();
        notificationManager.destroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    private void saveToSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, url);
        if (!TextUtils.isEmpty(currentStationName)) {
            editor.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, currentStationName);
        }
        editor.apply();
    }

    private void startMediaPlayerService() {
        Intent intent = new Intent(MediaPlayerService.this, MediaPlayerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        isStarted = true;
        Log.d(TAG, "Service is started");
    }

    @Override
    public String toString() {

        String isAlive = "false";
        if (mediaSession != null) {
            if (mediaSession.isActive()) {
                isAlive = "true";
            }
        }
        return super.toString() + '\'' +
                "MediaPlayerService{" +
                "url='" + url + '\'' +
                ", isStarted=" + isStarted +
                ", isReceiverRegistered=" + isReceiverRegistered +
                ", mediaSession isAlive= " + isAlive +
                '}';
    }


    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            if (TextUtils.equals(url, mediaId) && player.isPlaying()) {
                return;
            }
            url = mediaId;
            try {
                currentStationName = extras.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "");
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            saveToSharedPreferences();
            player.setUrlStation(mediaId);
            mediaSession.setExtras(extras);
            onPlay();
        }

        @Override
        public void onPlay() {
            Log.d(TAG, "onPlay " + MediaPlayerService.this);
            mediaSession.setActive(true);
            if (!isStarted) {
                startMediaPlayerService();
            }
            startForeground(MediaNotificationProvider.NOTIFICATION_ID, notificationManager.provider.getBufferingNotification());
            if (!isReceiverRegistered) {
                registerReceiver(noisyReceiver, intentFilter);
                isReceiverRegistered = true;
            }
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            int result = audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                player.play();
            }
        }

        @Override
        public void onPause() {
            Log.d(TAG, "onPause " + MediaPlayerService.this);
            if (isReceiverRegistered) {
                unregisterReceiver(noisyReceiver);
                isReceiverRegistered = false;
            }
            player.pause();
            stopForeground(false);

//            AudioManager audioManager = (AudioManager) MediaPlayerService.this.getSystemService(Context.AUDIO_SERVICE);
//            audioManager.abandonAudioFocus(afChangeListener);
        }

        @Override
        public void onStop() {
            Log.d(TAG, "onStop " + MediaPlayerService.this);
            AudioManager audioManager = (AudioManager) MediaPlayerService.this.getSystemService(Context.AUDIO_SERVICE);
            audioManager.abandonAudioFocus(afChangeListener);
            if (isReceiverRegistered) {
                unregisterReceiver(noisyReceiver);
                isReceiverRegistered = false;
            }
            player.stop();
            if (isStarted) {
                stopSelf();
                stopForeground(true);
                isStarted = false;
                mediaSession.setActive(false);
            }
        }
    }


    private class PlayerListener implements AbstractPlayer.OnPlayerListener {

        @Override
        public void onStateChanged(PlaybackStateCompat state) {
            mediaSession.setPlaybackState(state);
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                case PlaybackStateCompat.STATE_PAUSED:
                    notificationManager.showNotification(state);
                    break;
                case PlaybackStateCompat.STATE_STOPPED: {
                    stopForeground(true);
                    break;
                }
                case PlaybackStateCompat.STATE_ERROR:
                    notificationManager.showNotification(state);
                    break;
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            mediaSession.setMetadata(metadata);
            notificationManager.showNotification(mediaSession.getController().getPlaybackState());
        }

        @Override
        public void OnPlayListChanged(List playList) {
            mediaSession.setQueue(playList);
        }
    }


    private class FocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

        boolean isPlayingBeforeLossAudioFocus = false;

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    mediaSession.getController().getTransportControls().stop();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    isPlayingBeforeLossAudioFocus = player.isPlaying();
                    mediaSession.getController().getTransportControls().pause();
                    break;
                case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                    if (isPlayingBeforeLossAudioFocus) {
                        mediaSession.getController().getTransportControls().play();
                    }
                    break;
            }
        }
    }


    private class MediaNotificationManager {

        private MediaNotificationProvider provider;

        MediaNotificationManager() {
            MediaSessionCompat.Token token = mediaSession.getSessionToken();
            provider = new MediaNotificationProvider(getApplicationContext(), token);
        }

        public void showNotification(PlaybackStateCompat state) {
            if (state.getState() == PlaybackStateCompat.STATE_NONE || state.getState() == PlaybackStateCompat.STATE_STOPPED) {
                startForeground(MediaNotificationProvider.NOTIFICATION_ID, provider.getEmptyNotification());
                return;
            }
            if (state.getState() == PlaybackStateCompat.STATE_ERROR) {
                NotificationManager manager = (NotificationManager) MediaPlayerService.this
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(MediaNotificationProvider.NOTIFICATION_ID, provider.getErrorNotification());
                stopForeground(false);
                return;
            }

            MediaMetadataCompat metadata = mediaSession.getController().getMetadata();
            Notification notification = provider.getNotification(state, metadata);

            if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                startForeground(MediaNotificationProvider.NOTIFICATION_ID, notification);

            } else {
                NotificationManager manager = (NotificationManager) MediaPlayerService.this
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(MediaNotificationProvider.NOTIFICATION_ID, notification);
            }
        }

        void destroy() {
            provider.destroy();
        }
    }


    private class BecomingNoisyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                mediaSession.getController().getTransportControls().pause();
            }
        }
    }
}
