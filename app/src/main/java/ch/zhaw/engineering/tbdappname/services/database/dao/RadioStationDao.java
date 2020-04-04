package ch.zhaw.engineering.tbdappname.services.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;

@Dao
public abstract class RadioStationDao {
    public void insertAll(List<RadioStationDto> stations) {
        List<RadioStation> radios = new ArrayList<>(stations.size());
        for (RadioStationDto radio : stations) {
            radios.add(radio.toRadioStation());
        }
        insertRadioStations(radios);
    }

    public LiveData<List<RadioStation>> getRadioStations(boolean ascending, String searchText) {
        if (searchText != null && searchText.length() >= 3) {
            return getRadioStations("%" + searchText + "%", ascending);
        }
        return getRadioStations(ascending);
    }

    @Query("SELECT * FROM RadioStation  WHERE name like :text OR genres like :text ORDER BY CASE WHEN :asc = 1 THEN name END ASC, CASE WHEN :asc = 0 THEN name END DESC")
    protected abstract LiveData<List<RadioStation>> getRadioStations(String text, boolean asc);

    @Query("SELECT * FROM RadioStation ORDER BY CASE WHEN :asc = 1 THEN name END ASC, CASE WHEN :asc = 0 THEN name END DESC")
    protected abstract  LiveData<List<RadioStation>> getRadioStations(boolean asc);

    @Query("SELECT * FROM RadioStation WHERE id = :id LIMIT 1")
    public abstract RadioStation findById(long id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void update(RadioStation station);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract long insertRadioStation(RadioStation station);


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract long[] insertRadioStations(List<RadioStation> station);
}
