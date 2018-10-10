package ru.roma.musicplayer.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import ru.roma.musicplayer.R;
import ru.roma.musicplayer.service.MediaPlayerService;
import ru.roma.musicplayer.service.library.RadioLibrary;
import ru.roma.musicplayer.ui.adaptaer.DiffUtilStations;
import ru.roma.musicplayer.ui.adaptaer.RadioStationsAdapter;

public class ListOfRadioStationActivity extends AppCompatActivity implements RadioStationsAdapter.OnStationChange {

    private final String TAG = ListOfRadioStationActivity.class.getCanonicalName();
    @BindView(R.id.listStations)
    RecyclerView listStations;
    @BindView(R.id.listToolBar)
    Toolbar listToolBar;
    @BindView(R.id.artistInPlayingLayout)
    TextView artistInPlayingLayout;
    @BindView(R.id.titleInPlayingLayout)
    TextView titleInPlayingLayout;
    @BindView(R.id.playPausePlayingLayout)
    Button playPausePlayingLayout;
    @BindView(R.id.playingLayout)
    ConstraintLayout playingLayout;
    @BindView(R.id.stationIcon)
    ImageView stationIcon;
    private MediaBrowserCompat mediaBrowser;
    private RadioStationsAdapter adapter;
    private ControllerCallback controllerCallback;
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_radio_station);
        ButterKnife.bind(this);

        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MediaPlayerService.class)
                , new ConnectionCallback(), null);

        controllerCallback = new ControllerCallback();
        initializeRecycleList();
        initActionBar();
        playingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListOfRadioStationActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    private void initActionBar() {
        setSupportActionBar(listToolBar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.stations));
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (MediaControllerCompat.getMediaController(ListOfRadioStationActivity.this) != null) {
            MediaControllerCompat.getMediaController(ListOfRadioStationActivity.this).unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();
    }

    private void initializeRecycleList() {
        final LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);

        adapter = new RadioStationsAdapter(this);
        listStations.setLayoutManager(lm);
        listStations.setAdapter(adapter);
        listStations.setItemAnimator(new SlideInRightAnimator());

    }

    @Override
    public void onRadioStationChanges(String mediaId, Bundle bundle) {
        MediaControllerCompat.getMediaController(ListOfRadioStationActivity.this).getTransportControls()
                .playFromMediaId(mediaId, bundle);
    }

    @Override
    public String getCurrentMediaId() {
        String result = MediaControllerCompat.getMediaController(ListOfRadioStationActivity.this)
                .getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        return result;
    }

    @Override
    public boolean isPlaying() {
        PlaybackStateCompat state = MediaControllerCompat.getMediaController(ListOfRadioStationActivity.this)
                .getPlaybackState();
        return state.getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    private void showListOfRadioStation() {
        Log.d(TAG, "showListOfRadioStation");
        String root = mediaBrowser.getRoot();
        mediaBrowser.subscribe(root, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                DiffUtilStations diffUtilStations = new DiffUtilStations(adapter.getStations(), children);
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffUtilStations);
                adapter.setStations(children);
                diffResult.dispatchUpdatesTo(adapter);
            }
        });
    }

    private void showContent() {
        showListOfRadioStation();
        if (isPlaying()) {
            showPlayingLayout();
        }
    }

    private void showMetadata(MediaMetadataCompat metadata) {
        artistInPlayingLayout.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        titleInPlayingLayout.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        stationIcon.setImageBitmap(RadioLibrary.getBitmapById(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)));
    }

    private void showPlayingLayout() {
        animateLayout();
        showMetadata(MediaControllerCompat.getMediaController(ListOfRadioStationActivity.this).getMetadata());
        handleButtonEvent();
    }

    private void handleButtonEvent() {
        playPausePlayingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaybackStateCompat state = MediaControllerCompat.getMediaController(ListOfRadioStationActivity.this).getPlaybackState();
                if (state.getState() == PlaybackStateCompat.STATE_PLAYING ||
                        state.getState() == PlaybackStateCompat.STATE_BUFFERING) {
                    MediaControllerCompat.getMediaController(ListOfRadioStationActivity.this).getTransportControls().pause();
                } else {
                    MediaControllerCompat.getMediaController(ListOfRadioStationActivity.this).getTransportControls().play();
                }
            }
        });
    }

    private void animateLayout() {
        if (playingLayout.getVisibility() == View.GONE) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
            playingLayout.startAnimation(animation);
            playingLayout.setVisibility(View.VISIBLE);
        }
    }

    private class ConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            MediaSessionCompat.Token token = mediaBrowser.getSessionToken();

            MediaControllerCompat controller = null;
            try {
                controller = new MediaControllerCompat(ListOfRadioStationActivity.this, token);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            MediaControllerCompat.setMediaController(ListOfRadioStationActivity.this, controller);
            MediaControllerCompat.getMediaController(ListOfRadioStationActivity.this).registerCallback(controllerCallback);
            showContent();
        }
    }


    private class ControllerCallback extends MediaControllerCompat.Callback {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PAUSED:
                    Log.d(TAG, " state change to paused");
                    adapter.stopAnimation();
                    playPausePlayingLayout.setBackground(getDrawable(R.drawable.play));
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    showPlayingLayout();
                    playPausePlayingLayout.setBackground(getDrawable(R.drawable.pause));
                    adapter.startAnimation();

            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            showMetadata(metadata);
        }
    }
}
