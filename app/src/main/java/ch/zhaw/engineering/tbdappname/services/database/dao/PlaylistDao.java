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
public abstract class PlaylistDao {
    @Query("select * from playlist ORDER BY playlist.name ASC")
    public abstract LiveData<List<Playlist>> getPlaylists();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(Playlist playlist);

    @Transaction
    @Query("SELECT * FROM Playlist")
    public abstract LiveData<List<PlaylistWithSongs>> getPlaylistsWithSongs();

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertAll(List<PlaylistSongCrossRef> refs);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(PlaylistSongCrossRef ref);

    @Query("DELETE FROM PlaylistSongCrossRef WHERE PlaylistSongCrossRef.songId == :songId")
    public abstract void deleteBySongId(long songId);

    @Query("SELECT * FROM playlist WHERE playlist.name LIKE :text " +
            "ORDER BY CASE WHEN :asc = 1 THEN playlist.name END ASC, CASE WHEN :asc = 0 THEN playlist.name END DESC")
    public abstract  LiveData<List<PlaylistWithSongs>> getSortedPlaylists(String text, boolean asc);

    @Query("SELECT * FROM playlist ORDER BY CASE WHEN :asc = 1 THEN playlist.name END ASC, CASE WHEN :asc = 0 THEN playlist.name END DESC")
    public abstract LiveData<List<PlaylistWithSongs>> getSortedPlaylists(boolean asc);

    @Transaction
    @Query("SELECT * FROM Playlist WHERE playlistId = :id")
    public abstract PlaylistWithSongs findPlaylistWithSongsById(long id);

    @Query("DELETE FROM PlaylistSongCrossRef where playlistId = :playlistId")
    public abstract void deleteSongsFromPlaylist(long playlistId);

    @Query("SELECT pl.name as name, pl.playlistId as playlistId, COUNT(ref.playlistId) as songCount FROM Playlist pl " +
            "LEFT JOIN PlaylistSongCrossRef ref ON ref.playlistId = pl.playlistId " +
            "WHERE pl.name LIKE :text " +
            "GROUP BY pl.playlistId " +
            "ORDER BY CASE WHEN :asc = 1 THEN pl.name END ASC, CASE WHEN :asc = 0 THEN pl.name END DESC")
    public abstract LiveData<List<PlaylistWithSongCount>> getPlaylistWithSongCount(String text, boolean asc);

    @Query("SELECT pl.name as name, pl.playlistId as playlistId, COUNT(ref.playlistId) as songCount FROM Playlist pl " +
            "LEFT JOIN PlaylistSongCrossRef ref ON ref.playlistId = pl.playlistId " +
            "GROUP BY pl.playlistId " +
            "ORDER BY CASE WHEN :asc = 1 THEN pl.name END ASC, CASE WHEN :asc = 0 THEN pl.name END DESC")
    public abstract LiveData<List<PlaylistWithSongCount>> getPlaylistWithSongCount(boolean asc);


    @Query("DELETE FROM Playlist WHERE playlistId = :playlistId")
    public abstract void deletePlaylistById(int playlistId);

    @Transaction
    public void deletePlaylistAndSongsByPlaylistId(int playlistId) {
        this.deleteSongsFromPlaylist(playlistId);
        this.deletePlaylistById(playlistId);
    }
}
