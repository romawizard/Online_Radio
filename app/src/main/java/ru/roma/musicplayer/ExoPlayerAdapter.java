package ru.roma.musicplayer;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import saschpe.exoplayer2.ext.icy.IcyHttpDataSource;
import saschpe.exoplayer2.ext.icy.IcyHttpDataSourceFactory;

public class ExoPlayerAdapter extends PlayerAdapter {

    public static final String TITLE = "title";
    public static final String ARTIST = "artist";
    public static final String TIME = "time";
    private static final String NAME = "ru.roma.musicplayer";
    private final String TAG = ExoPlayerAdapter.class.getCanonicalName();
    private final List<MediaSessionCompat.QueueItem> playList;
    private String source;
    private SimpleExoPlayer player;
    private Comparator comparator;
    private ExtractorsFactory extractorsFactory;
    private DefaultDataSourceFactory dataSourceFactory;

    public ExoPlayerAdapter(String source, OnPlayerListener listener) {
        super(listener);
        this.source = source;
        playList = new ArrayList<>();
        comparator = new PlayListComparator();
    }

    private void initPlayer() {
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        RenderersFactory renderer = new DefaultRenderersFactory(MediaPlayerApplication.getInstance());
        extractorsFactory = new DefaultExtractorsFactory();
        IcyListener icyListener = new IcyListener();
        IcyHttpDataSourceFactory icyFactory = new IcyHttpDataSourceFactory.Builder(
                Util.getUserAgent(MediaPlayerApplication.getInstance(), NAME))
                .setIcyHeadersListener(icyListener)
                .setIcyMetadataChangeListener(icyListener)
                .build();
        dataSourceFactory = new DefaultDataSourceFactory(MediaPlayerApplication.getInstance()
                , null, icyFactory);

        player = ExoPlayerFactory.newSimpleInstance(renderer, trackSelector);
        player.addListener(new PlayerEventListener());
        player.prepare(createMediaSource());
    }

    private ExtractorMediaSource createMediaSource() {
        return new ExtractorMediaSource.Factory(dataSourceFactory).
                setExtractorsFactory(extractorsFactory)
                .createMediaSource(Uri.parse(source));
    }

    @Override
    public void play() {
        Log.d(TAG, "exoPlayer play");
        if (player == null) {
            initPlayer();
        }
        player.setPlayWhenReady(true);
    }

    @Override
    public void stop() {
        Log.d(TAG, "exoPlayer stop");
        changeStateToStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void pause() {
        Log.d(TAG, "exoPlayer pause");
        if (player != null) {
            player.setPlayWhenReady(false);
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
        source = url;
        playList.clear();
        listener.OnPlayListChanged(playList);
        listener.onMetadataChanged(createEmptyMetadata());
        if (player != null) {
            player.prepare(createMediaSource());
        }
    }

    private MediaMetadataCompat createEmptyMetadata() {
        return  new MediaMetadataCompat.Builder()
                .putString(TITLE, "")
                .putString(ARTIST, "")
                .build();
    }

    @Override
    public void release() {
        if (player != null) {
            player.release();
        }
    }

    @Override
    public boolean isPlaying() {
        if (player != null) {
            return player.getPlayWhenReady();
        } else {
            return false;
        }
    }

    private class PlayerEventListener extends Player.DefaultEventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_BUFFERING:
                    changeStateToBuffering();
                    break;
                case Player.STATE_READY:
                    if (playWhenReady) {
                        changeStateToPlaying();
                    } else {
                        changeStateToPaused();
                    }
                    break;
                case Player.STATE_ENDED:
                    changeStateToStop();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            if (player != null) {
                player.stop();
                player.release();
                player = null;
            }
            playList.clear();
            listener.OnPlayListChanged(playList);
            switch (error.type) {
                case ExoPlaybackException.TYPE_SOURCE:
                    sendError(error.getSourceException());
                    break;
                case ExoPlaybackException.TYPE_RENDERER:
                    sendError(error.getRendererException());
                    break;
                case ExoPlaybackException.TYPE_UNEXPECTED:
                    sendError(error.getUnexpectedException());
                    break;
                default:
                    sendError(error);
            }
        }
    }


    private class IcyListener implements IcyHttpDataSource.IcyHeadersListener, IcyHttpDataSource.IcyMetadataListener {

        @Override
        public void onIcyHeaders(IcyHttpDataSource.IcyHeaders icyHeaders) {
            Log.d(TAG, "onIcyHeaders " + icyHeaders.toString());
        }

        @Override
        public void onIcyMetaData(IcyHttpDataSource.IcyMetadata icyMetadata) {
            Log.d(TAG, "onIcyMetaDat " + icyMetadata.toString());
            String info = icyMetadata.getStreamTitle().trim();
            String[] data = info.split(" - ");
            String title = data[1];
            String artist = data[0].replace("-", "");

            if (TextUtils.equals(artist, "VIP")) {
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putLong(TIME, System.currentTimeMillis());
            MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                    .setTitle(title)
                    .setSubtitle(artist)
                    .setExtras(bundle)
                    .build();
            MediaSessionCompat.QueueItem item = new MediaSessionCompat.QueueItem(description, playList.size());
            playList.add(item);
            Collections.sort(playList, comparator);

            MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                    .putString(TITLE, title)
                    .putString(ARTIST, artist)
                    .build();

            listener.onMetadataChanged(metadata);
            listener.OnPlayListChanged(playList);
        }
    }
}
