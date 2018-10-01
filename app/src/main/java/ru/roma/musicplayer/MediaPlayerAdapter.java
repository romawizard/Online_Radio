package ru.roma.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.io.IOException;

public class MediaPlayerAdapter extends PlayerAdapter implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {

    private final String TAG = MediaPlayerAdapter.class.getCanonicalName();
    private WifiManager.WifiLock lock;
    private MediaPlayer player;
    private String source;
    private boolean isPrepared;

    public MediaPlayerAdapter(String source, OnPlayerListener listener) {
        super(listener);
        this.source = source;
    }

    @Override
    public void play() {
        if (player == null) {
            player = new MediaPlayer();
            player.setWakeMode(MediaPlayerApplication.getInstance(), PowerManager.PARTIAL_WAKE_LOCK);
            lock = ((WifiManager) MediaPlayerApplication.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, TAG);
            lock.acquire();
            player.setOnErrorListener(this);
            player.setOnBufferingUpdateListener(this);
            try {
                player.setDataSource(source);
            } catch (IOException e) {
                e.printStackTrace();
                sendError(e);
            }
            player.setOnPreparedListener(this);
            player.prepareAsync();
            changeStateToBuffering();
        } else {
            if (isPrepared) {
                player.start();
                changeStateToPlaying();
            }
        }
    }

    @Override
    public void stop() {
        changeStateToStop();

        if (player != null) {
            player.release();
            player = null;
        }
        if (lock != null && lock.isHeld()) {
            lock.release();
        }
        isPrepared = false;
    }

    @Override
    public void pause() {
        if (player != null) {
            player.pause();
            changeStateToPaused();
        }
    }

    @Override
    public void skipNext() {

    }

    @Override
    public void skipPrevious() {

    }

    @Override
    public void setUrlStation(String url) {

    }

    @Override
    public void release() {
        if (player != null) {
            player.release();
        }
        if (lock != null && lock.isHeld()) {
            lock.release();
        }
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        player.start();
        changeStateToPlaying();
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, mp.toString() + "what = " + what + " extra = " + extra);
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.d(TAG, "percent = " + percent);
    }
}
