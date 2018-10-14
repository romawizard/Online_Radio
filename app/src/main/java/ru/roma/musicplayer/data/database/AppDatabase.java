package ru.roma.musicplayer.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import ru.roma.musicplayer.data.dao.RadioStationDao;
import ru.roma.musicplayer.data.entity.RadioStation;

@Database(entities = {RadioStation.class},version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RadioStationDao getRadioStationDao();
}
