package ru.roma.musicplayer.ui;

import android.animation.ValueAnimator;
import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
import ru.roma.musicplayer.ui.adaptaer.DiffUtilPlayList;
import ru.roma.musicplayer.ui.adaptaer.DiffUtilStations;
import ru.roma.musicplayer.ui.adaptaer.PlayListAdapter;
import ru.roma.musicplayer.ui.adaptaer.RadioStationRatingAdapter;

public class MainActivity extends AppCompatActivity implements RadioStationRatingAdapter.OnChangeRadioStation,
        PlayListAdapter.PlayListListener {

    private static final String TAG = MainActivity.class.getCanonicalName();
    @BindView(R.id.play_stop)
    AppCompatButton playStop;
    //    @BindView(R.id.textViewError)
//    TextView textViewError;
//    @BindView(R.id.artist)
//    TextView artist;
//    @BindView(R.id.title)
//    TextView title;

    @BindView(R.id.stationByRating)
    RecyclerView stationsByRating;
    @BindView(R.id.collapsingToolBar)
    CollapsingToolbarLayout collapsingToolBar;
    @BindView(R.id.playList)
    RecyclerView playList;
    @BindView(R.id.stationImage)
    ImageView stationImage;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCallBack mediaControllerCallBack;
    private ValueAnimator animator;
    private ClickHandler clickHandler;
    private PlayListAdapter playListAdapter;
    private RadioStationRatingAdapter ratingAdapter;
    private String currentMediaId = "";
    private Parcelable stationsByRatingState;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, ListOfRadioStationActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStationChanges(String mediaId) {
        MediaControllerCompat.getMediaController(MainActivity.this)
                .getTransportControls().playFromMediaId(mediaId, null);

        RadioStationsManager manager = MediaPlayerApplication.getInstance().getManager();
        String path = manager.getImageUri(mediaId);
        Picasso.get()
                .load(path)
                .placeholder(getDrawable(R.drawable.white))
                .into(stationImage);
    }

    @Override
    public void onChooseTrack(String trackName) {
        try {
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, trackName);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application can handle this request."
                    + " Please install a webBrowser", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorTransparent));
        setContentView(R.layout.coordinator_layout);
        ButterKnife.bind(this);
        clickHandler = new ClickHandler();
        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MediaPlayerService.class)
                , new MediaBrowserConnectionCallback(), null);
        mediaControllerCallBack = new MediaControllerCallBack();
        initializePlayList();
        initializeRatingList();
        initActionBar();
        subscribeToLiveData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowser.connect();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (MediaControllerCompat.getMediaController(MainActivity.this) != null) {
            MediaControllerCompat.getMediaController(MainActivity.this).unregisterCallback(mediaControllerCallBack);
        }
        mediaBrowser.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stationsByRatingState = stationsByRating.getLayoutManager().onSaveInstanceState();
    }

    private void initializeRatingList() {
        final LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.HORIZONTAL);

        ratingAdapter = new RadioStationRatingAdapter(this);
        stationsByRating.setLayoutManager(lm);
        stationsByRating.setAdapter(ratingAdapter);
        stationsByRating.setItemAnimator(new SlideInRightAnimator());

        if (stationsByRatingState != null){
            stationsByRating.getLayoutManager().onRestoreInstanceState(stationsByRatingState);
        }
    }

    private void initActionBar() {

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.menu);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String title = sharedPreferences.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID
                , getString(R.string.comedy_radio_name));

        collapsingToolBar.setTitle(title);
    }


    private void buildTransportControls() {
        PlaybackStateCompat state = MediaControllerCompat.getMediaController(MainActivity.this).getPlaybackState();
        showState(state);

        MediaMetadataCompat metadata = MediaControllerCompat.getMediaController(MainActivity.this).getMetadata();
        currentMediaId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        showMetadata(metadata);

        playStop.setOnClickListener(clickHandler);

        List queue = MediaControllerCompat.getMediaController(MainActivity.this).getQueue();
        dispatchQueueToAdapter(queue);

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MainActivity.this);
        mediaController.registerCallback(mediaControllerCallBack);

        String stationName = mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        Log.d(TAG, "buildTransportControls() mediaId = " + stationName);
        collapsingToolBar.setTitle(stationName);

        RadioStationsManager manager = MediaPlayerApplication.getInstance().getManager();
        String path = manager.getImageUri(currentMediaId);
        Picasso.get()
                .load(path)
                .placeholder(getDrawable(R.drawable.white))
                .into(stationImage);
//        stationImage.setImageBitmap(RadioLibrary.getBitmapById(stationName));
    }

    private void showMetadata(MediaMetadataCompat metadata) {
//        artist.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
//        title.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
    }

    private void animateBuffering() {
        if (animator == null) {
            animator = ValueAnimator.ofFloat(1f, 1.2f);
            animator.setDuration(1000);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.setRepeatCount(ValueAnimator.INFINITE);
        }
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                playStop.setScaleX((Float) animation.getAnimatedValue());
                playStop.setScaleY((Float) animation.getAnimatedValue());
            }
        });
        animator.start();
    }

    private void stopBufferingAnimation() {
        if (animator != null) {
            animator.cancel();
            animator.removeAllUpdateListeners();
            animator = null;
            Log.d(TAG, "animation is canceled");
        }
        playStop.setScaleY(1f);
        playStop.setScaleX(1f);
    }

    private void showState(PlaybackStateCompat state) {
        Log.d(TAG, "show state " + state.toString());
        if (state.getState() != PlaybackStateCompat.STATE_BUFFERING) {
            stopBufferingAnimation();
        }
        if (state.getState() != PlaybackStateCompat.STATE_ERROR) {
            hideMessageError();
        }

        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                playStop.setBackground(getResources().getDrawable(R.drawable.stop));
                showMetadata(MediaControllerCompat.getMediaController(MainActivity.this).getMetadata());
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                playStop.setBackground(getResources().getDrawable(R.drawable.play));
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                animateBuffering();
                break;
            case PlaybackStateCompat.STATE_STOPPED:
            case PlaybackStateCompat.STATE_NONE:
                showInitState();
                break;
            case PlaybackStateCompat.STATE_ERROR:
                showInitState();
                showErrorMessage(state.getErrorMessage());
                Log.d(TAG, state.getErrorMessage().toString());
                break;
        }
    }

    private void showInitState() {
        playStop.setBackground(getResources().getDrawable(R.drawable.play));
//        title.setText("");
//        artist.setText("");
    }

    private void hideMessageError() {
//        textViewError.setText("");
//        textViewError.setVisibility(View.GONE);
    }

    private void showErrorMessage(CharSequence errorMessage) {
//        textViewError.setText(errorMessage);
//        textViewError.setVisibility(View.VISIBLE);
    }

    private void initializePlayList() {
        final LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);

        playListAdapter = new PlayListAdapter(this);
        playList.setLayoutManager(lm);
        playList.setAdapter(playListAdapter);
        playList.setItemAnimator(new SlideInRightAnimator());
    }

    private void dispatchQueueToAdapter(List<MediaSessionCompat.QueueItem> queue) {
        DiffUtilPlayList diffUtilPlayList = new DiffUtilPlayList(playListAdapter.getPlayList(), queue);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(diffUtilPlayList);
        playListAdapter.setPlayList(queue);
        result.dispatchUpdatesTo(playListAdapter);
    }

    private void showRadioStationsSortedByRating(List<MediaBrowserCompat.MediaItem> mediaItems) {
        DiffUtilStations diffUtilStations = new DiffUtilStations(ratingAdapter.getStations()
                , mediaItems, currentMediaId);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffUtilStations);
        ratingAdapter.setStations(mediaItems);
        diffResult.dispatchUpdatesTo(ratingAdapter);
    }

    private void subscribeToLiveData() {
        MediaPlayerApplication.getInstance().getManager().getLiveData()
                .observe(this, new Observer<List<RadioStation>>() {
                    @Override
                    public void onChanged(@Nullable List<RadioStation> radioStations) {
                        Log.d(TAG, "radio station from db " + radioStations);
                        showRadioStationsSortedByRating(RadioMapper.mapToMediaItem(MainActivity.this, radioStations));
                    }
                });
    }


    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {

        @Override
        public void onConnected() {

            Log.d(TAG, "onConnected()");
            MediaSessionCompat.Token token = mediaBrowser.getSessionToken();

            MediaControllerCompat controller = null;
            try {
                controller = new MediaControllerCompat(MainActivity.this, token);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            MediaControllerCompat.setMediaController(MainActivity.this, controller);
            buildTransportControls();
        }

    }


    private class MediaControllerCallBack extends MediaControllerCompat.Callback {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            Log.d(TAG, "onMetadataChanged");
            showMetadata(metadata);
            currentMediaId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            collapsingToolBar.setTitle(currentMediaId);
        }


        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.d(TAG, "onPlaybackStateChanged");
            showState(state);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            Log.d(TAG, "onQueueChanged " + queue.toString());
            dispatchQueueToAdapter(queue);
        }
    }


    private class ClickHandler implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play_stop:
                    PlaybackStateCompat state = MediaControllerCompat.getMediaController(MainActivity.this).getPlaybackState();
                    if (state.getState() == PlaybackStateCompat.STATE_PLAYING ||
                            state.getState() == PlaybackStateCompat.STATE_BUFFERING) {
                        MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().stop();
                    } else {
                        MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().play();
                    }
                    break;

            }
        }
    }
}
