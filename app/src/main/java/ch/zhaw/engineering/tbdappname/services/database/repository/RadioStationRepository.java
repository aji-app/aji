package ch.zhaw.engineering.tbdappname.services.database.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.AppDatabase;
import ch.zhaw.engineering.tbdappname.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;


public class RadioStationRepository {
    private final RadioStationDao mRadioStationDao;

    public RadioStationRepository(RadioStationDao radioStationDao) {
        mRadioStationDao = radioStationDao;
    }

    public static RadioStationRepository getInstance(Context context) {
        RadioStationDao radioStationDao = AppDatabase.getInstance(context).radioStationDao();
        return new RadioStationRepository(radioStationDao);
    }

    public RadioStation findById(long id) {
        return mRadioStationDao.getRadioStationById(id);
    }

    public LiveData<List<RadioStationDto>> getRadioStations(boolean ascending, String searchText) {
        return mRadioStationDao.getRadioStations(ascending, searchText);
    }

    public void update(RadioStation station) {
//        mRadioStationDao.update(station);
    }

    public void insert(RadioStation station) {
//        mRadioStationDao.insertRadioStation(station);
    }

    public void insertAll(List<RadioStationDto> stations) {
        mRadioStationDao.insertAll(stations);
    }
}
