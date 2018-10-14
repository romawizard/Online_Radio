package ru.roma.musicplayer.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.roma.musicplayer.data.dao.RadioStationDao;
import ru.roma.musicplayer.data.entity.RadioStation;
import ru.roma.musicplayer.ui.ListOfRadioStationActivity;


public class RadioStationsManager {

    private RadioStationDao radioStationDao;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<RadioStation> radioStations;

    public RadioStationsManager(RadioStationDao radioStationDao) {
        this.radioStationDao = radioStationDao;
        loadData();
    }

    public LiveData<List<RadioStation>> getLiveData() {
        return radioStationDao.getLiveDataStationSortedByRating();
    }

    public void increaseRatingByTime(final String mediaId){
        executor.execute(new Runnable() {
            @Override
            public void run() {
               RadioStation station = radioStationDao.getStationByMediaId(mediaId);
               station.increaseRating();
               radioStationDao.insert(station);
            }
        });
    }

    private void loadData(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                radioStations = radioStationDao.getAllStationSortedByRating();
                          }
        });
    }

    public void increaseRatingByTime(final String mediaId, final long diff) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                RadioStation station = radioStationDao.getStationByMediaId(mediaId);
                station.increaseRating(diff);
                radioStationDao.insert(station);
            }
        });

    }

    public List<RadioStation> getRadioStations() {
        return radioStations;
    }
}
