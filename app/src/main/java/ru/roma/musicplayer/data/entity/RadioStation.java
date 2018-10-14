package ru.roma.musicplayer.data.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.util.Log;

@Entity
public class RadioStation {

    public static final String TAG = RadioStation.class.getCanonicalName();

    @NonNull
    @PrimaryKey
    private String mediaId;

    private String title;

    private String mediaUri;

    private float rating;

    private int icon;

    public RadioStation(String mediaId, String title, String mediaUri, float rating, int icon) {
        this.mediaId = mediaId;
        this.title = title;
        this.mediaUri = mediaUri;
        this.rating = rating;
        this.icon = icon;
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

    public String getMediaUri() {
        return mediaUri;
    }

    public void setMediaUri(String mediaUri) {
        this.mediaUri = mediaUri;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void increaseRating(){
       rating +=1f;
    }

    @Override
    public String toString() {
        return "RadioStation{" +
                "mediaId='" + mediaId + '\'' +
                ", title='" + title + '\'' +
                ", mediaUri='" + mediaUri + '\'' +
                ", rating=" + rating +
                ", icon=" + icon +
                '}';
    }

    public void increaseRating(long diff) {
        float inc = diff/1000000f;
        Log.d(TAG,"inc = " + inc);
        rating += inc;
    }
}
