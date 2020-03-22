package ch.zhaw.engineering.tbdappname.services.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;

@Dao
public interface RadioStationDao {

    @Query("SELECT * FROM RadioStation  WHERE name like :text OR genres like :text ORDER BY CASE WHEN :asc = 1 THEN name END ASC, CASE WHEN :asc = 0 THEN name END DESC")
    LiveData<List<RadioStation>> getRadioStations(String text, boolean asc);

    @Query("SELECT * FROM RadioStation ORDER BY CASE WHEN :asc = 1 THEN name END ASC, CASE WHEN :asc = 0 THEN name END DESC")
    LiveData<List<RadioStation>> getRadioStations(boolean asc);

    @Query("SELECT * FROM RadioStation WHERE id = :id LIMIT 1")
    RadioStation findById(long id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(RadioStation station);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertRadioStation(RadioStation station);


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insertRadioStations(List<RadioStation> station);
}
