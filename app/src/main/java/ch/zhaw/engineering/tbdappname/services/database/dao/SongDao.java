package ch.zhaw.engineering.tbdappname.services.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.entity.Song;

@Dao
public interface SongDao {

    @Query("SELECT * FROM Song WHERE song.deleted == 0")
    LiveData<List<Song>> getSongs();

    @Query("SELECT * FROM Song")
    List<Song> getSongList();

    @Query("SELECT COUNT(*) FROM SONG")
    long getCount();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long[] insertSongs(List<Song> songs);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertSong(Song songs);

    @Update
    public int updateSongs(List<Song> songs);

    @Query("DELETE FROM song")
    public void deleteAll();

    @Update
    public void updateSong(Song song);

    @Query("SELECT 1 FROM Song WHERE song.filepath == :filepath")
    public boolean exists(String filepath);

    @Query("SELECT * FROM Song WHERE song.filepath == :filepath")
    public Song getSongByPath(String filepath);


    @Delete
    void deleteSong(Song song);
}
