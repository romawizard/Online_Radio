package ru.roma.musicplayer.ui.adaptaer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.roma.musicplayer.R;

public class RadioStationRatingAdapter extends RecyclerView.Adapter<RadioStationRatingAdapter.RatingHolder> {

    private List<MediaBrowserCompat.MediaItem> stations;
    private OnChangeRadioStation listener;

    public RadioStationRatingAdapter(OnChangeRadioStation listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RatingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rating_station_item,parent,false);
        return new RatingHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingHolder holder, int position) {
        holder.bind(stations.get(position));
    }

    @Override
    public int getItemCount() {
        return stations == null? 0: stations.size();
    }

    public void setStations(List<MediaBrowserCompat.MediaItem> stations) {
        this.stations = stations;
    }

    public List<MediaBrowserCompat.MediaItem> getStations() {
        return stations;
    }


    public class RatingHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.iconRating)
        ImageView iconRating;
        @BindView(R.id.stationNameRating)
        TextView stationName;
        String mediaId;


        public RatingHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            ButterKnife.bind(this,itemView);
        }

        public void bind(MediaBrowserCompat.MediaItem mediaItem) {
            Picasso.get().load(mediaItem.getDescription().getIconUri())
                    .resize(150,0)
                    .into(iconRating);
            stationName.setText(mediaItem.getDescription().getTitle());
            mediaId = mediaItem.getDescription().getMediaId();
        }

        @Override
        public void onClick(View v) {
            listener.onStationChanges(mediaId);
        }
    }


    public interface OnChangeRadioStation{

        void onStationChanges(String mediaId);
    }
}
