package ru.roma.musicplayer.service.library;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.roma.musicplayer.MediaPlayerApplication;
import ru.roma.musicplayer.R;

public class RadioLibrary {

    private static final Map<String, MediaBrowserCompat.MediaItem> radioStations = new LinkedHashMap<>();

    static {
        fillRadioStations();
    }

    private static void fillRadioStations() {
        TypedArray tArray = MediaPlayerApplication.getInstance().getResources().obtainTypedArray(
                R.array.icon_id);
        int count = tArray.length();
        int[] ids = new int[count];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = tArray.getResourceId(i, 0);
        }

        String[] URLs = MediaPlayerApplication.getInstance().getResources().getStringArray(R.array.stations_url);
        String[] names = MediaPlayerApplication.getInstance().getResources().getStringArray(R.array.stations_name);

        for (int i = 0, n = names.length; i < n; i++) {
            MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                    .setMediaId(names[i])
                    .setTitle(names[i])
                    .setMediaUri(Uri.parse(URLs[i]))
                    .setIconBitmap(BitmapFactory.decodeResource(MediaPlayerApplication.getInstance().getResources(),ids[i]))
                    .build();
            radioStations.put(names[i], new MediaBrowserCompat.MediaItem(description, MediaBrowser.MediaItem.FLAG_PLAYABLE));
        }
    }

    public static List<MediaBrowserCompat.MediaItem> getMediaItems() {
        return new ArrayList<>( radioStations.values());
    }

    public static MediaBrowserCompat.MediaItem getMediaItemById(String id){
        return radioStations.get(id);
    }

    public static Bitmap getBitmapById(String id){
        return radioStations.get(id).getDescription().getIconBitmap();
    }
    public static String getUrlById(String id){
        return radioStations.get(id).getDescription().getMediaUri().toString();
    }
}
