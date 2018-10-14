package ru.roma.musicplayer.service.library;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import java.util.Comparator;

public class RatingComparator implements Comparator<MediaBrowserCompat.MediaItem> {
    @Override
    public int compare(MediaBrowserCompat.MediaItem o1, MediaBrowserCompat.MediaItem o2) {
        float rating1 = o1.getDescription().getExtras().getFloat(MediaMetadataCompat.METADATA_KEY_RATING, 0.1f);
        float rating2 = o2.getDescription().getExtras().getFloat(MediaMetadataCompat.METADATA_KEY_RATING, 0.1f);

        if (rating1 < rating2)
            return 1;
        if (rating1 > rating2)
            return -1;
        else return 0;
    }
}
