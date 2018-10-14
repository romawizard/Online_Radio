package ru.roma.musicplayer.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ru.roma.musicplayer.data.entity.RadioStation;

@Dao
public interface RadioStationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<RadioStation> stations);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RadioStation station);

    @Query("SELECT * FROM RadioStation WHERE mediaId = :mediaId")
    RadioStation getStationByMediaId(String mediaId);

    @Query("SELECT * FROM radiostation")
    List<RadioStation> getAllStation();


    @Query("SELECT * FROM radiostation ORDER BY rating DESC")
    LiveData<List<RadioStation>> getLiveDataStationSortedByRating();

    @Query("SELECT * FROM radiostation ORDER BY rating  DESC")
    List<RadioStation>getAllStationSortedByRating();

    @Update
    void updateStation(RadioStation radioStation);
}
