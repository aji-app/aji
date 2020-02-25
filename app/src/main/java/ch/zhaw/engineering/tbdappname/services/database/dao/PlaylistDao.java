package ch.zhaw.engineering.tbdappname.services.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.PlaylistSongCrossRef;
import ch.zhaw.engineering.tbdappname.services.database.entity.PlaylistWithSongs;

@Dao
public interface PlaylistDao {
    @Query("select * from playlist")
    LiveData<List<Playlist>> getPlaylists();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Playlist playlist);

    @Transaction
    @Query("SELECT * FROM Playlist")
    LiveData<List<PlaylistWithSongs>> getPlaylistsWithSongs();

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<PlaylistSongCrossRef> refs);

    @Query("DELETE FROM PlaylistSongCrossRef WHERE PlaylistSongCrossRef.songId == :songId")
    void deleteBySongId(long songId);

    @Query("SELECT * FROM playlist WHERE playlist.name LIKE :text " +
            "ORDER BY CASE WHEN :asc = 1 THEN playlist.name END ASC, CASE WHEN :asc = 0 THEN playlist.name END DESC")
    LiveData<List<PlaylistWithSongs>> getSortedPlaylists(String text, boolean asc);

    @Query("SELECT * FROM playlist ORDER BY CASE WHEN :asc = 1 THEN playlist.name END ASC, CASE WHEN :asc = 0 THEN playlist.name END DESC")
    LiveData<List<PlaylistWithSongs>> getSortedPlaylists(boolean asc);

    @Transaction
    @Query("SELECT * FROM Playlist WHERE playlistId = :id")
    PlaylistWithSongs findPlaylistWithSongsById(long id);

    @Query("DELETE FROM PlaylistSongCrossRef where playlistId = :playlistId")
    void deleteSongsFromPlaylist(long playlistId);
}
