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

    @Query("SELECT * FROM Song WHERE song.deleted == 0 AND (song.title like :text OR song.album like :text OR song.artist like :text) " +
            "ORDER BY CASE WHEN :asc = 1 THEN song.title END ASC, CASE WHEN :asc = 0 THEN song.title END DESC")
    LiveData<List<Song>> getSongsSortedByTitle(String text, boolean asc);

    @Query("SELECT * FROM Song WHERE song.deleted == 0 AND (song.title like :text OR song.album like :text OR song.artist like :text) " +
            "ORDER BY CASE WHEN :asc = 1 THEN song.album END ASC, CASE WHEN :asc = 0 THEN song.album END DESC")
    LiveData<List<Song>> getSongsSortedByAlbum(String text, boolean asc);

    @Query("SELECT * FROM Song WHERE song.deleted == 0 AND (song.title like :text OR song.album like :text OR song.artist like :text)" +
            " ORDER BY CASE WHEN :asc = 1 THEN song.artist END ASC, CASE WHEN :asc = 0 THEN song.artist END DESC")
    LiveData<List<Song>> getSongsSortedByArtist(String text, boolean asc);

    @Query("SELECT * FROM Song WHERE song.deleted == 0 ORDER BY CASE WHEN :asc = 1 THEN song.title END ASC, CASE WHEN :asc = 0 THEN song.title END DESC")
    LiveData<List<Song>> getSongsSortedByTitle(boolean asc);

    @Query("SELECT * FROM Song WHERE song.deleted == 0 ORDER BY CASE WHEN :asc = 1 THEN song.album END ASC, CASE WHEN :asc = 0 THEN song.album END DESC")
    LiveData<List<Song>> getSongsSortedByAlbum(boolean asc);

    @Query("SELECT * FROM Song WHERE song.deleted == 0 ORDER BY CASE WHEN :asc = 1 THEN song.artist END ASC, CASE WHEN :asc = 0 THEN song.artist END DESC")
    LiveData<List<Song>> getSongsSortedByArtist(boolean asc);

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

    @Query("SELECT s.* FROM Song s JOIN PlaylistSongCrossRef ps ON ps.songId = s.songId WHERE s.deleted == 0 AND ps.playlistId = :playlistId ORDER BY ps.`order`")
    List<Song> getSongsForPlaylist(long playlistId);

    @Delete
    void deleteSong(Song song);
}
