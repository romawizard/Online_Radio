package ru.roma.musicplayer.service.player;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;

public abstract class AbstractPlayer {

    protected PlaybackStateCompat.Builder builder;
    protected OnPlayerListener listener;

    AbstractPlayer(OnPlayerListener listener) {
        this.listener = listener;
        builder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_STOP);
    }

    public abstract void play();

    public abstract void stop();

    public abstract void pause();

    public abstract void skipNext();

    public abstract void skipPrevious();

    public abstract void prepare(String url);

    public abstract void release();

    protected void changeStateToPlaying() {
        PlaybackStateCompat state = builder.setState(PlaybackStateCompat.STATE_PLAYING
                , PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f).build();
        listener.onStateChanged(state);
    }

    protected void changeStateToStop() {
        PlaybackStateCompat state = builder.setState(PlaybackStateCompat.STATE_STOPPED
                , PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f).build();
        listener.onStateChanged(state);
    }

    protected void sendError(Exception e) {
        PlaybackStateCompat state = builder.setErrorMessage(PlaybackStateCompat.ERROR_CODE_ACTION_ABORTED, e.getMessage())
                .setState(PlaybackStateCompat.STATE_ERROR, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
                .build();
        listener.onStateChanged(state);
    }

    protected void changeStateToPaused() {
        PlaybackStateCompat state = builder.setState(PlaybackStateCompat.STATE_PAUSED
                , PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f).build();
        listener.onStateChanged(state);
    }

    protected void changeStateToBuffering() {
        PlaybackStateCompat state = builder.setState(PlaybackStateCompat.STATE_BUFFERING
                , PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f).build();
        listener.onStateChanged(state);
    }

    public abstract boolean isPlaying();

    public interface OnPlayerListener {

        void onStateChanged(PlaybackStateCompat state);

        void onMetadataChanged(MediaMetadataCompat metadata);

        void OnPlayListChanged(List playList);
    }
}
