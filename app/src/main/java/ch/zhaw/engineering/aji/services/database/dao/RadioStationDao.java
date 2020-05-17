package ch.zhaw.engineering.aji.services.database.dao;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.aji.services.database.entity.RadioStation;

@Dao
public abstract class RadioStationDao {
    public static RadioStationDao getInstance(Context context) {
        return AppDatabase.getInstance(context).radioStationDao();
    }

    public void insertAll(List<RadioStationDto> stations) {
        List<RadioStation> radios = new ArrayList<>(stations.size());
        for (RadioStationDto radio : stations) {
            radios.add(radio.toRadioStation());
        }
        insertRadioStations(radios);
    }

    public LiveData<List<RadioStationDto>> getRadioStations(boolean ascending, String searchText) {
        LiveData<List<RadioStation>> stations;
        if (searchText != null && searchText.length() >= 3) {
            stations = getRadioStations("%" + searchText + "%", ascending);
        } else {
            stations = getRadioStations(ascending);
        }
        return Transformations.map(stations, radios -> {
            List<RadioStationDto> result = new ArrayList<>();
            for (RadioStation radio : radios) {
                result.add(RadioStationDto.fromRadioStation(radio));
            }
            return result;
        });
    }

    public RadioStationDto getRadioStationById(long radioStationId) {
        return RadioStationDto.fromRadioStation(getRadioStation(radioStationId));
    }

    public LiveData<RadioStationDto> getRadioStationLiveDataById(long radioStationId) {
        return Transformations.map(getRadioStationLiveData(radioStationId), RadioStationDto::fromRadioStation);
    }

    public void updateRadioStation(RadioStationDto updatedRadioStation) {
        update(updatedRadioStation.toRadioStation());
    }

    public long createRadioStation(RadioStationDto radioStation) {
       return insertRadioStation(radioStation.toRadioStation());
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insertRadioStation(RadioStation station);

    @Query("DELETE FROM RadioStation WHERE id = :id")
    public abstract void deleteRadioStationById(long id);

    @Query("SELECT * FROM RadioStation WHERE id = :id LIMIT 1")
    public abstract RadioStation getRadioStation(long id);

    @Query("SELECT * FROM RadioStation WHERE id = :id LIMIT 1")
    protected abstract LiveData<RadioStation> getRadioStationLiveData(long id);

    @Query("SELECT * FROM RadioStation WHERE LOWER(name) like LOWER(:text) OR LOWER(genres) like LOWER(:text) ORDER BY CASE WHEN :asc = 1 THEN LOWER(name) END ASC, CASE WHEN :asc = 0 THEN LOWER(name) END DESC")
    protected abstract LiveData<List<RadioStation>> getRadioStations(String text, boolean asc);

    @Query("SELECT * FROM RadioStation ORDER BY CASE WHEN :asc = 1 THEN LOWER(name) END ASC, CASE WHEN :asc = 0 THEN LOWER(name) END DESC")
    protected abstract LiveData<List<RadioStation>> getRadioStations(boolean asc);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void update(RadioStation station);


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract long[] insertRadioStations(List<RadioStation> station);

}
