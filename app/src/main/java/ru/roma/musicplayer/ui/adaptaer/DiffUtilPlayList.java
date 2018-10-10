package ru.roma.musicplayer.ui.adaptaer;

import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.util.DiffUtil;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

public class DiffUtilPlayList extends DiffUtil.Callback {

    private List<MediaSessionCompat.QueueItem> oldList;
    private List<MediaSessionCompat.QueueItem> newList;

    public static final String TAG = DiffUtilPlayList.class.getCanonicalName();

    public DiffUtilPlayList(List<MediaSessionCompat.QueueItem> oldList, List<MediaSessionCompat.QueueItem> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList == null? 0: oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList == null? 0: newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getQueueId() ==
                newList.get(newItemPosition).getQueueId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        String oldArtist =  oldList.get(oldItemPosition).getDescription().getSubtitle().toString();
        String newArtist =  newList.get(newItemPosition).getDescription().getSubtitle().toString();

        String oldTitle =  oldList.get(oldItemPosition).getDescription().getTitle().toString();
        String newTitle =  newList.get(newItemPosition).getDescription().getTitle().toString();

        return TextUtils.equals(oldArtist,newArtist) && TextUtils.equals(oldTitle,newTitle);
    }
}
