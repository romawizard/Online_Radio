package ru.roma.musicplayer;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListOfRadioStationActivity extends AppCompatActivity implements RadioStationsAdapter.OnStationChange {

    private final String TAG = ListOfRadioStationActivity.class.getCanonicalName();
    @BindView(R.id.listStations)
    RecyclerView listStations;
    @BindView(R.id.listToolBar)
    Toolbar listToolBar;
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
    }

    @Override
    public void onRadioStationChanges(String url, Bundle bundle) {
        MediaControllerCompat.getMediaController(ListOfRadioStationActivity.this).getTransportControls()
                .playFromMediaId(url, bundle);
    }

    private void showListOfRadioStation() {
        Log.d(TAG, "showListOfRadioStation");
        String root = mediaBrowser.getRoot();
        mediaBrowser.subscribe(root, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                adapter.setStations(children);
                runLayoutAnimation();
            }
        });
    }

    private void runLayoutAnimation() {

        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_dawn);
        listStations.setLayoutAnimation(controller);
        listStations.getAdapter().notifyDataSetChanged();
        listStations.scheduleLayoutAnimation();
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
            showListOfRadioStation();
        }
    }

    private class ControllerCallback extends MediaControllerCompat.Callback {

    }
}
