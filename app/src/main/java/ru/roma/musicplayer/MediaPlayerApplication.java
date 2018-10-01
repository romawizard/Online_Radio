package ru.roma.musicplayer;

import android.app.Application;

public class MediaPlayerApplication extends Application {

    private static MediaPlayerApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static MediaPlayerApplication getInstance() {
        return instance;
    }
}
