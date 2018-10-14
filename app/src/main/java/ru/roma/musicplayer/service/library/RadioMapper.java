package ru.roma.musicplayer.service.library;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.roma.musicplayer.data.entity.RadioStation;

public class RadioMapper {

    // should be called only one time to fill database initial data from res
    public static List<RadioStation> mapToRadioStation(List<MediaBrowserCompat.MediaItem> mediaItems){
        List<RadioStation> radioStations = new ArrayList<>();
        for (MediaBrowserCompat.MediaItem mediaItem : mediaItems){
            MediaDescriptionCompat description = mediaItem.getDescription();
            String mediaId = description.getMediaId();
            String title = description.getTitle().toString();
            String uri = description.getMediaUri().toString();
            float rating =1f;
            int icon = Integer.parseInt(mediaItem.getDescription().getIconUri().toString());

            RadioStation station = new RadioStation(mediaId,title,uri,rating,icon);
            radioStations.add(station);
        }
        return radioStations;
    }

    public static List<MediaBrowserCompat.MediaItem> mapToMediaItem (Context context, List<RadioStation> radioStations){
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        for (RadioStation station : radioStations){
            Bundle bundle = new Bundle();
            bundle.putFloat(MediaMetadataCompat.METADATA_KEY_RATING,station.getRating());
            MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                    .setIconUri(Uri.parse(String.valueOf(station.getIcon())))
                    .setMediaId(station.getMediaId())
                    .setTitle(station.getTitle())
                    .setMediaUri(Uri.parse(station.getMediaUri()))
                    .setIconBitmap(BitmapFactory.decodeResource(context.getResources(),station.getIcon()))
                    .setExtras(bundle)
                    .build();
            mediaItems.add(new MediaBrowserCompat.MediaItem(description, MediaBrowser.MediaItem.FLAG_PLAYABLE));
        }
        return mediaItems;
    }

    public static List<MediaBrowserCompat.MediaItem> getSortedRadioStations(List<MediaBrowserCompat.MediaItem> mediaItems){
         Collections.sort(mediaItems,new RatingComparator());
         return mediaItems;
    }
}

