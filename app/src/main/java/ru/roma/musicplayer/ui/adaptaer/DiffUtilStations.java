package ru.roma.musicplayer.ui.adaptaer;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.util.DiffUtil;
import android.text.TextUtils;

import java.util.List;

public class DiffUtilStations extends DiffUtil.Callback {

    private List<MediaBrowserCompat.MediaItem> oldList;
    private List<MediaBrowserCompat.MediaItem> newList;

    public DiffUtilStations(List<MediaBrowserCompat.MediaItem> oldList, List<MediaBrowserCompat.MediaItem> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList ==null? 0: oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList ==null? 0: newList.size();

    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        String oldId = oldList.get(oldItemPosition).getMediaId();
        String newId = newList.get(newItemPosition).getMediaId();
        return TextUtils.equals(oldId,newId);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return false;
    }
}
