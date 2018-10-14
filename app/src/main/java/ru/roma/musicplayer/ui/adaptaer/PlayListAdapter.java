package ru.roma.musicplayer.ui.adaptaer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ru.roma.musicplayer.R;
import ru.roma.musicplayer.service.library.RadioLibrary;

import static ru.roma.musicplayer.service.player.ExoPlayerImpl.TIME;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.MusicViewHolder> {

    private List<MediaSessionCompat.QueueItem> playList;
    private final String TAG = PlayListAdapter.class.getCanonicalName();
    private PlayListListener listListener;

    public PlayListAdapter(PlayListListener listListener) {
        this.listListener = listListener;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.music_item,parent,false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        holder.bind(playList.get(position));
    }

    @Override
    public int getItemCount() {
        return playList == null? 0 : playList.size();
    }

    public void setPlayList(List<MediaSessionCompat.QueueItem> playList) {
        this.playList = playList;
    }

    public List<MediaSessionCompat.QueueItem> getPlayList() {
        return playList;
    }

    public interface PlayListListener{

        void onChooseTrack(String trackName);
    }

    public class  MusicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView title;
        private TextView artist;
        private TextView time;
        private ImageView icon;
        private String currentTitle, currentArtist;

        public MusicViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            title = itemView.findViewById(R.id.playListTitle);
            artist = itemView.findViewById(R.id.playListArtist);
            time = itemView.findViewById(R.id.time);
            icon = itemView.findViewById(R.id.playListIcon);
        }

        public void bind(MediaSessionCompat.QueueItem musicItem) {
            currentArtist = musicItem.getDescription().getSubtitle().toString();
            currentTitle = musicItem.getDescription().getTitle().toString();
            title.setText(currentTitle);
            artist.setText(currentArtist);
            icon.setImageBitmap(RadioLibrary.getBitmapById(musicItem.getDescription().getMediaId()));
            String timestamp = new SimpleDateFormat("HH:mm").format(new Date(musicItem.getDescription().getExtras().getLong(TIME)));
            time.setText(timestamp);
        }

        @Override
        public void onClick(View v) {
            ClipboardManager clipboard = (ClipboardManager)v.getContext(). getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", currentArtist + " " + currentTitle);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(v.getContext(),R.string.copied,Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onLongClick(View v) {
            listListener.onChooseTrack(currentArtist + " " + currentTitle);
            return true;
        }
    }
}
