package ru.roma.musicplayer.service.player;

import android.content.Context;
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

import ru.roma.musicplayer.MediaPlayerApplication;
import ru.roma.musicplayer.data.entity.RadioStation;
import ru.roma.musicplayer.utils.PlayListComparator;
import saschpe.exoplayer2.ext.icy.IcyHttpDataSource;
import saschpe.exoplayer2.ext.icy.IcyHttpDataSourceFactory;

public class ExoPlayerImpl extends AbstractPlayer {

    public static final String TIME = "time";
    private static final String NAME = "ru.roma.musicplayer";
    private final String TAG = ExoPlayerImpl.class.getCanonicalName();
    private final List<MediaSessionCompat.QueueItem> playList;
    private RadioStation radioStation;
    private SimpleExoPlayer player;
    private Comparator comparator = new PlayListComparator();
    private ExtractorsFactory extractorsFactory;
    private DefaultDataSourceFactory dataSourceFactory;
    private static int id = 0;

    public ExoPlayerImpl(RadioStation radioStation, OnPlayerListener listener) {
        super(listener);
        playList = new ArrayList<>();
        this.radioStation = radioStation;
    }

    private void initPlayer(Context context) {
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        RenderersFactory renderer = new DefaultRenderersFactory(context);
        extractorsFactory = new DefaultExtractorsFactory();
        IcyListener icyListener = new IcyListener();
        IcyHttpDataSourceFactory icyFactory = new IcyHttpDataSourceFactory.Builder(
                Util.getUserAgent(context, NAME))
                .setIcyHeadersListener(icyListener)
                .setIcyMetadataChangeListener(icyListener)
                .build();
        dataSourceFactory = new DefaultDataSourceFactory(context, null, icyFactory);

        player = ExoPlayerFactory.newSimpleInstance(renderer, trackSelector);
        player.addListener(new PlayerEventListener());
        player.prepare(createMediaSource());
    }

    private ExtractorMediaSource createMediaSource() {
        return new ExtractorMediaSource.Factory(dataSourceFactory).
                setExtractorsFactory(extractorsFactory)
                .createMediaSource(Uri.parse(radioStation.getStationUrl()));
    }

    @Override
    public void play(Context context) {
        Log.d(TAG, "exoPlayer play");
        if (player == null) {
            initPlayer(context);
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
    public void prepare(RadioStation radioStation) {
        this.radioStation = radioStation;
        playList.clear();
        listener.OnPlayListChanged(playList);
        listener.onMetadataChanged(createEmptyMetadata());
        if (player != null) {
            player.prepare(createMediaSource());
        }
    }

    private MediaMetadataCompat createEmptyMetadata() {
        return  new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,radioStation.getMediaId())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "")
                .build();
    }

    @Override
    public void release() {
        if (player != null) {
            player.release();
        }
        listener = null;
    }

    @Override
    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady();
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
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.d(TAG,"onPlayerError");
            if (player != null) {
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
                    error.printStackTrace();
                    sendError(new RuntimeException("player Error"));
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
            Log.d(TAG, "onIcyMetaData " + icyMetadata.toString());
            String info = icyMetadata.getStreamTitle().trim();
            String[] data = info.split(" - ");
            String title = data[1];
            String artist = data[0].replace("-", "");


            Bundle bundle = new Bundle();
            bundle.putLong(TIME, System.currentTimeMillis());
            MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                    .setMediaId(radioStation.getMediaId())
                    .setIconUri(Uri.parse(radioStation.getImageUri()))
                    .setTitle(title)
                    .setSubtitle(artist)
                    .setExtras(bundle)
                    .build();
            MediaSessionCompat.QueueItem item = new MediaSessionCompat.QueueItem(description,id++);
            playList.add(item);

            Collections.sort(playList, comparator);

            MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,radioStation.getMediaId())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,radioStation.getImageUri())
                    .build();

            listener.onMetadataChanged(metadata);
            listener.OnPlayListChanged(playList);
            Log.d(TAG,"send playList to listener");
        }
    }
}
