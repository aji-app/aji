package ch.zhaw.engineering.tbdappname.services.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.dto.PlaylistWithSongCount;
import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.PlaylistSongCrossRef;
import ch.zhaw.engineering.tbdappname.services.database.entity.PlaylistWithSongs;

@Dao
public interface PlaylistDao {
    @Query("select * from playlist ORDER BY playlist.name ASC")
    LiveData<List<Playlist>> getPlaylists();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Playlist playlist);

    @Transaction
    @Query("SELECT * FROM Playlist")
    LiveData<List<PlaylistWithSongs>> getPlaylistsWithSongs();

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<PlaylistSongCrossRef> refs);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(PlaylistSongCrossRef ref);

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

    @Query("SELECT pl.name as name, pl.playlistId as playlistId, COUNT(ref.playlistId) as songCount FROM Playlist pl " +
            "LEFT JOIN PlaylistSongCrossRef ref ON ref.playlistId = pl.playlistId " +
            "WHERE pl.name LIKE :text " +
            "GROUP BY pl.playlistId " +
            "ORDER BY CASE WHEN :asc = 1 THEN pl.name END ASC, CASE WHEN :asc = 0 THEN pl.name END DESC")
    LiveData<List<PlaylistWithSongCount>> getPlaylistWithSongCount(String text, boolean asc);

    @Query("SELECT pl.name as name, pl.playlistId as playlistId, COUNT(ref.playlistId) as songCount FROM Playlist pl " +
            "LEFT JOIN PlaylistSongCrossRef ref ON ref.playlistId = pl.playlistId " +
            "GROUP BY pl.playlistId " +
            "ORDER BY CASE WHEN :asc = 1 THEN pl.name END ASC, CASE WHEN :asc = 0 THEN pl.name END DESC")
    LiveData<List<PlaylistWithSongCount>> getPlaylistWithSongCount(boolean asc);


}
