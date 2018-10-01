package ru.roma.musicplayer;

import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static ru.roma.musicplayer.ExoPlayerAdapter.ARTIST;
import static ru.roma.musicplayer.ExoPlayerAdapter.TITLE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();


    @BindView(R.id.play_stop)
    Button playStop;
    @BindView(R.id.textViewError)
    TextView textViewError;
    @BindView(R.id.artist)
    TextView artist;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.playList)
    RecyclerView playList;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private MediaBrowserCompat mediaBrowserCompat;
    private MediaControllerCallBack mediaControllerCallBack;
    private ValueAnimator animator;
    private ClickHandler clickHandler;
    private MusicListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        clickHandler = new ClickHandler();
        mediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, MediaPlayerService.class)
                , new MediaBrowserConnectionCallback(), null);

        mediaControllerCallBack = new MediaControllerCallBack();
        initializeRecycleList(playList);
        initActionBar();
    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.menu);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String title = sharedPreferences.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, getString(R.string.comedy_radio_name));
        actionBar.setTitle(title);
    }

    private void buildTransportControls() {
        PlaybackStateCompat state = MediaControllerCompat.getMediaController(MainActivity.this).getPlaybackState();
        showState(state);

        MediaMetadataCompat metadata = MediaControllerCompat.getMediaController(MainActivity.this).getMetadata();
        showMetadata(metadata);

        playStop.setOnClickListener(clickHandler);

        List queue = MediaControllerCompat.getMediaController(MainActivity.this).getQueue();
        adapter.setPlayList(queue);

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MainActivity.this);
        mediaController.registerCallback(mediaControllerCallBack);

        Bundle bundle = mediaController.getExtras();
        if (bundle != null) {
            String title = bundle.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE);
            getSupportActionBar().setTitle(title);
        }

    }

    private void showMetadata(MediaMetadataCompat metadata) {
        artist.setText(metadata.getString(ARTIST));
        title.setText(metadata.getString(TITLE));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowserCompat.connect();
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
        mediaBrowserCompat.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void animateBuffering() {
        if (animator == null) {
            animator = ValueAnimator.ofFloat(1f, 1.2f);
        }
        animator.setDuration(1000);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
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
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                showInitState();
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                animateBuffering();
                break;
            case PlaybackStateCompat.STATE_STOPPED:
            case PlaybackStateCompat.STATE_NONE:
                playStop.setBackground(getResources().getDrawable(R.drawable.play));
                break;
            case PlaybackStateCompat.STATE_ERROR:
                showErrorMessage(state.getErrorMessage());
                showInitState();
                Log.d(TAG, state.getErrorMessage().toString());
                break;
        }
    }

    private void showInitState() {
        playStop.setBackground(getResources().getDrawable(R.drawable.play));
        title.setText("");
        artist.setText("");
    }

    private void hideMessageError() {
        textViewError.setText("");
        textViewError.setVisibility(View.GONE);
    }

    private void showErrorMessage(CharSequence errorMessage) {
        textViewError.setText(errorMessage);
        textViewError.setVisibility(View.VISIBLE);
    }

    private void initializeRecycleList(RecyclerView list) {
        final LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);

        adapter = new MusicListAdapter();
        list.setLayoutManager(lm);
        list.setAdapter(adapter);
    }

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

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {

        @Override
        public void onConnected() {

            Log.d(TAG, "onConnected()");
            MediaSessionCompat.Token token = mediaBrowserCompat.getSessionToken();

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
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.d(TAG, "onPlaybackStateChanged");
            showState(state);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            adapter.setPlayList(queue);
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
