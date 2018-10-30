package ru.roma.musicplayer.data.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.util.Log;

@Entity
public class RadioStation {

    public static final String TAG = RadioStation.class.getCanonicalName();

    @NonNull
    @PrimaryKey
    private String mediaId;

    private String title;

    private String stationUrl;

    private float rating;

    private String imageUri;

    public RadioStation(String mediaId, String title, String stationUrl, float rating, String imageUri) {
        this.mediaId = mediaId;
        this.title = title;
        this.stationUrl = stationUrl;
        this.rating = rating;
        this.imageUri = imageUri;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStationUrl() {
        return stationUrl;
    }

    public void setStationUrl(String stationUrl) {
        this.stationUrl = stationUrl;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public void increaseRating(){
       rating +=1f;
    }

    @Override
    public String toString() {
        return "RadioStation{" +
                "mediaId='" + mediaId + '\'' +
                ", title='" + title + '\'' +
                ", stationUrl='" + stationUrl + '\'' +
                ", rating=" + rating +
                ", icon=" + imageUri +
                '}';
    }

    public void increaseRating(long diff) {
        float inc = diff/1000000f;
        Log.d(TAG,"inc = " + inc);
        rating += inc;
    }
}
