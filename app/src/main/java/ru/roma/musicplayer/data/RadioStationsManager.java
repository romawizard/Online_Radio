package ru.roma.musicplayer.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.net.Uri;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.roma.musicplayer.data.dao.RadioStationDao;
import ru.roma.musicplayer.data.entity.RadioStation;
import ru.roma.musicplayer.ui.ListOfRadioStationActivity;


public class RadioStationsManager {

    private RadioStationDao radioStationDao;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Map<String, RadioStation> radioStations;

    public RadioStationsManager(RadioStationDao radioStationDao) {
        this.radioStationDao = radioStationDao;
        radioStations = new LinkedHashMap<>();
        loadData();
    }

    public LiveData<List<RadioStation>> getLiveData() {
        return radioStationDao.getLiveDataStationSortedByRating();
    }

    public void increaseRatingByTime(final RadioStation radioStation, final long diff) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                radioStation.increaseRating(diff);
                radioStationDao.insert(radioStation);
            }
        });

    }

    public List<RadioStation> getRadioStations() {
        return new ArrayList<>(radioStations.values()) ;
    }

    public String getImageUri(String mediaId) {
       return radioStations.get(mediaId).getImageUri();
    }

    public RadioStation getRadioStationByMediaId(String mediaId) {
       return radioStations.get(mediaId);
    }

    public void increaseRating(final RadioStation radioStation) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                radioStation.increaseRating();
                radioStationDao.insert(radioStation);
            }
        });
    }

    private void loadData(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<RadioStation> stations = radioStationDao.getAllStationSortedByRating();
                for (RadioStation st: stations){
                    radioStations.put(st.getMediaId(),st);
                }
            }
        });
    }

    public void notifyDataChange() {
        loadData();
    }

    public LiveData<List<RadioStation>> searchStation(String query) {
        return radioStationDao.getRadioStationByQuery(query);
    }
}
