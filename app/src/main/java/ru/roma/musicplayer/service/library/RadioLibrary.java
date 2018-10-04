package ru.roma.musicplayer.service.library;

import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;

import java.util.ArrayList;
import java.util.List;

import ru.roma.musicplayer.MediaPlayerApplication;
import ru.roma.musicplayer.R;

public class RadioLibrary {

    private static final List<MediaBrowserCompat.MediaItem> radioStations = new ArrayList<>();

    static {
        fillRadioStations();
    }

    private static void fillRadioStations() {
        String[] URLs = MediaPlayerApplication.getInstance().getResources().getStringArray(R.array.stations_url);
        String[] names = MediaPlayerApplication.getInstance().getResources().getStringArray(R.array.stations_name);

        for (int i = 0, n = names.length; i < n; i++) {
            MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                    .setMediaId(names[i])
                    .setTitle(names[i])
                    .setMediaUri(Uri.parse(URLs[i]))
                    .build();
            radioStations.add(new MediaBrowserCompat.MediaItem(description, MediaBrowser.MediaItem.FLAG_BROWSABLE));
        }
    }

    public static List<MediaBrowserCompat.MediaItem> getMediaItems() {
        return radioStations;
    }
}
