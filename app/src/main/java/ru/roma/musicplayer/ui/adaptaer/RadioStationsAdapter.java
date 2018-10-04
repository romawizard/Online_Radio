package ru.roma.musicplayer.ui.adaptaer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.roma.musicplayer.R;

public class RadioStationsAdapter extends RecyclerView.Adapter<RadioStationsAdapter.StationsHolder> {

    private List<MediaBrowserCompat.MediaItem> stations;
    private OnStationChange listener;

    public RadioStationsAdapter(OnStationChange listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public StationsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.station_item,parent,false);
        return new StationsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationsHolder holder, int position) {
        holder.bind(stations.get(position));
    }


    @Override
    public int getItemCount() {
        return stations == null ? 0 : stations.size();
    }

    public void setStations(List<MediaBrowserCompat.MediaItem> stations) {
        this.stations = stations;
        notifyDataSetChanged();
    }

    public class StationsHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView stationName;
        String url;

        public StationsHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            stationName =  itemView.findViewById(R.id.stationName);
        }


        public void bind(MediaBrowserCompat.MediaItem mediaItem) {
            stationName.setText(mediaItem.getMediaId());
            url =  mediaItem.getDescription().getMediaUri().toString();

        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,stationName.getText().toString());
            listener.onRadioStationChanges(url,bundle);
        }
    }


    public interface OnStationChange{

        void onRadioStationChanges(String url, @Nullable Bundle bundle);
    }
}
