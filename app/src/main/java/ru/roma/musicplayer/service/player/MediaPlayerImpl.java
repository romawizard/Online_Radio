package ru.roma.musicplayer.service.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;

import ru.roma.musicplayer.MediaPlayerApplication;
import ru.roma.musicplayer.data.entity.RadioStation;

public class MediaPlayerImpl extends AbstractPlayer implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {

    private final String TAG = MediaPlayerImpl.class.getCanonicalName();
    private WifiManager.WifiLock lock;
    private MediaPlayer player;
    private String source;
    private boolean isPrepared;

    public MediaPlayerImpl(String source, OnPlayerListener listener) {
        super(listener);
        this.source = source;
    }

    @Override
    public void play(Context context) {
        if (player == null) {
            player = new MediaPlayer();
            player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            lock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
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
    public void prepare(RadioStation radioStation) {

    }

    @Override
    public void release() {
        if (player != null) {
            player.release();
        }
        if (lock != null && lock.isHeld()) {
            lock.release();
        }
        listener = null;
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
