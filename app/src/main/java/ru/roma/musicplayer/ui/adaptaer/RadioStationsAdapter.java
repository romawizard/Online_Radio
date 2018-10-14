package ru.roma.musicplayer.ui.adaptaer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import es.claucookie.miniequalizerlibrary.EqualizerView;
import ru.roma.musicplayer.R;
import ru.roma.musicplayer.service.library.RadioLibrary;


public class RadioStationsAdapter extends RecyclerView.Adapter<RadioStationsAdapter.StationsHolder> {

    public static final String TAG = RadioStationsAdapter.class.getCanonicalName();
    private List<MediaBrowserCompat.MediaItem> stations;
    private OnStationChange listener;
    private EqualizerView previousEqualizer;

    public RadioStationsAdapter(OnStationChange listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public StationsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.station_item, parent, false);
        return new StationsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationsHolder holder, int position) {
        holder.bind(stations.get(position));
    }

    public void stopAnimation() {
        if (previousEqualizer != null){
            previousEqualizer.stopBars();
            previousEqualizer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return stations == null ? 0 : stations.size();
    }

    public List<MediaBrowserCompat.MediaItem> getStations() {
        return stations;
    }

    public void setStations(List<MediaBrowserCompat.MediaItem> stations) {
        this.stations = stations;
    }

    public void startAnimation() {
        if (previousEqualizer != null){
            previousEqualizer.animateBars();
            previousEqualizer.setVisibility(View.VISIBLE);
        }
    }


    public interface OnStationChange {

        void onRadioStationChanges(String url, @Nullable Bundle bundle);

        String getCurrentMediaId();

        boolean isPlaying();

    }

    public class StationsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView stationName;
        private ImageView icon;
        private EqualizerView equalizer;
        private String mediaId;

        public StationsHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            stationName = itemView.findViewById(R.id.stationName);
            icon = itemView.findViewById(R.id.stationIcon);
            equalizer = itemView.findViewById(R.id.equalizer);
        }

        public void bind(MediaBrowserCompat.MediaItem mediaItem) {
            stationName.setText(mediaItem.getDescription().getTitle());
            mediaId = mediaItem.getDescription().getMediaId();
            icon.setImageBitmap(RadioLibrary.getBitmapById(mediaId));
            if (TextUtils.equals(mediaId, listener.getCurrentMediaId()) && listener.isPlaying()) {
                equalizer.setVisibility(View.VISIBLE);
                equalizer.animateBars();
                previousEqualizer = equalizer;
            } else {
                equalizer.stopBars();
                equalizer.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            listener.onRadioStationChanges(mediaId,null);
            equalizer.setVisibility(View.VISIBLE);
            equalizer.animateBars();
                if (previousEqualizer!= null && previousEqualizer!= equalizer){
                    previousEqualizer.stopBars();
                    previousEqualizer.setVisibility(View.GONE);
                }
                previousEqualizer = equalizer;
            }
        }
}