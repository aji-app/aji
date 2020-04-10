package ch.zhaw.engineering.tbdappname.services.database.dao;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.AppDatabase;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;

@Dao
public abstract class SongDao {
    /*
     * Public API
     *
     */
    public static SongDao getInstance(Context context) {
        return AppDatabase.getInstance(context).songDao();
    }

    public void toggleFavorite(long songId) {
        Song song = getSongById(songId);
        if (song != null) {
            song.setFavorite(!song.isFavorite());
            updateSong(song);
        }
    }

    @Transaction
    public void deleteSongById(long songId) {
        deleteSongsFromPlaylist(songId);
        deleteSongBySongId(songId);
    }

    public LiveData<List<Song>> getSortedSongs(SortType sortType, boolean ascending, String searchText) {
        if (searchText == null) {
            switch (sortType) {

                case TITLE:
                    return getSongsSortedByTitle(ascending);
                case ARTIST:
                    return getSongsSortedByArtist(ascending);
                case ALBUM:
                    return getSongsSortedByAlbum(ascending);
            }
        }
        String searchQuery = "%" + searchText + "%";
        switch (sortType) {

            case TITLE:
                return getSongsSortedByTitle(searchQuery, ascending);
            case ARTIST:
                return getSongsSortedByArtist(searchQuery, ascending);
            case ALBUM:
                return getSongsSortedByAlbum(searchQuery, ascending);
        }

        return new MutableLiveData<>();
    }

    @Query("SELECT * FROM Song")
    public abstract List<Song> getAllSongs();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insertSong(Song songs);

    @Insert()
    public abstract long[] insertSongs(List<Song> songs);

    @Update
    public abstract void updateSong(Song song);

    @Query("SELECT 1 FROM Song WHERE song.filepath = :filepath")
    public abstract boolean exists(String filepath);

    @Query("SELECT * FROM Song WHERE song.filepath = :filepath")
    public abstract Song getSongByPath(String filepath);

    @Query("UPDATE song SET deleted = 1 WHERE songId = :songId")
    protected abstract void deleteSongBySongId(long songId);

    @Query("SELECT * FROM Song WHERE song.deleted = 0 ORDER BY random() LIMIT 1")
    public abstract Song getRandomSong();

    @Query("SELECT s.* FROM Song s JOIN PlaylistSongCrossRef ps ON ps.songId = s.songId WHERE s.deleted = 0 AND ps.playlistId = :playlistId ORDER BY ps.`order`")
    public abstract LiveData<List<Song>> getSongsForPlaylist(long playlistId);

    @Query("SELECT s.* FROM Song s JOIN PlaylistSongCrossRef ps ON ps.songId = s.songId WHERE s.deleted = 0 AND ps.playlistId = :playlistId ORDER BY ps.`order`")
    public abstract List<Song> getSongsForPlaylistAsList(long playlistId);

    @Query("SELECT * FROM Song s WHERE s.deleted = 0 AND s.songId = :id LIMIT 1")
    public abstract Song getSongById(long id);

    @Query("SELECT * FROM Song s WHERE s.deleted = 0 AND s.songId = :id LIMIT 1")
    public abstract LiveData<Song> getSong(long id);

    /*
     * Protected Helper Methods
     *
     */
    @Query("SELECT * FROM Song WHERE song.deleted = 0 AND (song.title like :text OR song.album like :text OR song.artist like :text) " +
            "ORDER BY CASE WHEN :asc = 1 THEN song.title END ASC, CASE WHEN :asc = 0 THEN song.title END DESC")
    protected abstract LiveData<List<Song>> getSongsSortedByTitle(String text, boolean asc);

    @Query("SELECT * FROM Song WHERE song.deleted = 0 AND (song.title like :text OR song.album like :text OR song.artist like :text) " +
            "ORDER BY CASE WHEN :asc = 1 THEN song.album END ASC, CASE WHEN :asc = 0 THEN song.album END DESC")
    protected abstract LiveData<List<Song>> getSongsSortedByAlbum(String text, boolean asc);

    @Query("SELECT * FROM Song WHERE song.deleted = 0 AND (song.title like :text OR song.album like :text OR song.artist like :text)" +
            " ORDER BY CASE WHEN :asc = 1 THEN song.artist END ASC, CASE WHEN :asc = 0 THEN song.artist END DESC")
    protected abstract LiveData<List<Song>> getSongsSortedByArtist(String text, boolean asc);

    @Query("SELECT * FROM Song WHERE song.deleted = 0 " +
            "ORDER BY CASE WHEN :asc = 1 THEN song.title END ASC, CASE WHEN :asc = 0 THEN song.title END DESC")
    protected abstract LiveData<List<Song>> getSongsSortedByTitle(boolean asc);

    @Query("SELECT * FROM Song WHERE song.deleted = 0 " +
            "ORDER BY CASE WHEN :asc = 1 THEN song.album END ASC, CASE WHEN :asc = 0 THEN song.album END DESC")
    protected abstract LiveData<List<Song>> getSongsSortedByAlbum(boolean asc);

    @Query("SELECT * FROM Song WHERE song.deleted = 0 " +
            "ORDER BY CASE WHEN :asc = 1 THEN song.artist END ASC, CASE WHEN :asc = 0 THEN song.artist END DESC")
    protected abstract LiveData<List<Song>> getSongsSortedByArtist(boolean asc);

    @Query("DELETE FROM PlaylistSongCrossRef where playlistId = :playlistId")
    protected abstract void deleteSongsFromPlaylist(long playlistId);

    public enum SortType {
        TITLE, ARTIST, ALBUM
    }
}
