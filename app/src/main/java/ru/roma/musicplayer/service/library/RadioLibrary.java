package ru.roma.musicplayer.service.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.roma.musicplayer.MediaPlayerApplication;
import ru.roma.musicplayer.R;
import ru.roma.musicplayer.ui.MainActivity;

public class RadioLibrary {

    public static List<MediaBrowserCompat.MediaItem> createRadioStations(Context context) {
        final List<MediaBrowserCompat.MediaItem> radioStations = new ArrayList<>();
        String[] image_path = context.getResources().getStringArray(R.array.image_path);
        String[] URLs = context.getResources().getStringArray(R.array.stations_url);
        String[] names =context.getResources().getStringArray(R.array.stations_name);

        for (int i = 0, n = names.length; i < n; i++) {
            MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                    .setIconUri(Uri.parse(image_path[i]))
                    .setMediaId(names[i])
                    .setTitle(names[i])
                    .setMediaUri(Uri.parse(URLs[i]))
//                    .setIconBitmap(BitmapFactory.decodeResource(MediaPlayerApplication.getInstance().getResources(),ids[i]))
                    .build();
            radioStations.add(new MediaBrowserCompat.MediaItem(description, MediaBrowser.MediaItem.FLAG_PLAYABLE));
        }
        return radioStations;
    }
}
