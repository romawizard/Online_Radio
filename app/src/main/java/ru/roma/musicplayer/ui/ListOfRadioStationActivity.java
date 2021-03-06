package ru.roma.musicplayer.ui;

import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import ru.roma.musicplayer.MediaPlayerApplication;
import ru.roma.musicplayer.R;
import ru.roma.musicplayer.data.RadioStationsManager;
import ru.roma.musicplayer.data.entity.RadioStation;
import ru.roma.musicplayer.service.MediaPlayerService;
import ru.roma.musicplayer.service.library.RadioMapper;
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
    private RadioStationsManager manager;
    private ActionBar actionBar;
    private SearchView searchView;

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG,"onNewIntent");
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG,"search query = " + query);
            searchRadioStationsByQuery(query);
        }
    }

    private void searchRadioStationsByQuery(String query) {
        manager.searchStation(query).observe(this, new Observer<List<RadioStation>>() {
            @Override
            public void onChanged(@Nullable List<RadioStation> radioStations) {
                showListOfRadioStation(RadioMapper.mapToMediaItem(ListOfRadioStationActivity.this,radioStations));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optinal_menu,menu);

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        searchView.setQueryRefinementEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchRadioStationsByQuery(newText);
                return true;
            }
        });
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_radio_station);
        ButterKnife.bind(this);


        manager = MediaPlayerApplication.getInstance().getManager();

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
        handleIntent(getIntent());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searchView.setOnQueryTextListener(null);
    }

    private void showListOfRadioStation(List<MediaBrowserCompat.MediaItem> mediaItems) {
                DiffUtilStations diffUtilStations = new DiffUtilStations(adapter.getStations(),mediaItems,getCurrentMediaId());
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffUtilStations);
                adapter.setStations(mediaItems);
                diffResult.dispatchUpdatesTo(adapter);
            }

    private void showContent() {
        subscribeToLiveData();
        if (isPlaying()) {
            showPlayingLayout();
        }
    }

    private void initializeRecycleList() {
        final LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);

        adapter = new RadioStationsAdapter(this);
        listStations.setLayoutManager(lm);
        listStations.setAdapter(adapter);
        listStations.setItemAnimator(new SlideInRightAnimator());
    }

    private void initActionBar() {
        setSupportActionBar(listToolBar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.stations));
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void subscribeToLiveData() {

//        String root = mediaBrowser.getRoot();
//        mediaBrowser.subscribe(root, new MediaBrowserCompat.SubscriptionCallback() {
//            @Override
//            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
//                super.onChildrenLoaded(parentId, children);
//                showListOfRadioStation(children);
//            }
//        });


        MediaPlayerApplication.getInstance().getManager().getLiveData()
                .observe(ListOfRadioStationActivity.this, new Observer<List<RadioStation>>() {
                    @Override
                    public void onChanged(@Nullable List<RadioStation> radioStations) {
                        showListOfRadioStation(RadioMapper.mapToMediaItem(ListOfRadioStationActivity.this,radioStations));
                    }
                });
    }

    private void showMetadata(MediaMetadataCompat metadata) {
        artistInPlayingLayout.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        titleInPlayingLayout.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        Picasso.get()
                .load(manager.getImageUri(getCurrentMediaId()))
                .resize(150,0)
                .into(stationIcon);
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
