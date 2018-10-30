package ru.roma.musicplayer;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.SharedPreferences;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import ru.roma.musicplayer.data.RadioStationsManager;
import ru.roma.musicplayer.data.database.AppDatabase;
import ru.roma.musicplayer.service.library.RadioLibrary;
import ru.roma.musicplayer.service.library.RadioMapper;

public class MediaPlayerApplication extends Application {

    private static MediaPlayerApplication instance;
    private AppDatabase database;
    public static final String KEY_FIRST_LOAD = "key first load";
    private RadioStationsManager manager;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        instance = this;
        database = Room.databaseBuilder(this,AppDatabase.class,getString(R.string.app_name))
                .build();
        fillDatabase();
        manager = new RadioStationsManager(database.getRadioStationDao());
    }

    public static MediaPlayerApplication getInstance() {
        return instance;
    }

    public RadioStationsManager getManager() {
        return manager;
    }

    private void fillDatabase(){
        boolean isFirstLoad = getSharedPreferences(getString(R.string.app_name),MODE_PRIVATE)
                .getBoolean(KEY_FIRST_LOAD,true);
        if (isFirstLoad){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    database.getRadioStationDao()
                            .insert(RadioMapper.mapToRadioStation(RadioLibrary.createRadioStations(getApplicationContext())));
                    manager.notifyDataChange();
                }
            })   ;
            thread.start();
            SharedPreferences sp =getSharedPreferences(getString(R.string.app_name),MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(KEY_FIRST_LOAD,false);
            editor.apply();
        }

    }
}
