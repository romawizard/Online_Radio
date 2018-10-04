package ru.roma.musicplayer.ui.utils;

import android.support.v4.media.session.MediaSessionCompat;

import java.util.Comparator;

import static ru.roma.musicplayer.service.player.ExoPlayerImpl.TIME;

public class PlayListComparator implements Comparator<MediaSessionCompat.QueueItem> {
    @Override
    public int compare(MediaSessionCompat.QueueItem o1, MediaSessionCompat.QueueItem o2) {
        long time1 = o1.getDescription().getExtras().getLong(TIME);
        long time2 = o2.getDescription().getExtras().getLong(TIME);

        return time1<time2 ? 1 : (time1 == time2)? 0 : -1;
    }
}
