package ch.zhaw.engineering.aji.services.database.dao;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.database.dto.PlaylistWithSongCount;
import ch.zhaw.engineering.aji.services.database.entity.Playlist;
import ch.zhaw.engineering.aji.services.database.entity.PlaylistSongCrossRef;

@Dao
public abstract class PlaylistDao {
    /*
     * Public API
     *
     */
    public static PlaylistDao getInstance(Context context) {
        return AppDatabase.getInstance(context).playlistDao();
    }

    public LiveData<List<PlaylistWithSongCount>> getPlaylists() {
        return getPlaylists(null, true);
    }

    @Query("SELECT * FROM Playlist WHERE playlistId = :playlistId LIMIT 1")
    public abstract Playlist getPlaylistById(int playlistId);

    public LiveData<List<PlaylistWithSongCount>> getPlaylists(String filter, boolean ascending) {
        if (filter == null) {
            return getPlaylistWithSongCount(ascending);
        }
        String searchQuery = "%" + filter + "%";
        return getPlaylistWithSongCount(searchQuery, ascending);
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(Playlist playlist);

    @Query("DELETE FROM PlaylistSongCrossRef WHERE PlaylistSongCrossRef.songId == :songId")
    public abstract void deleteBySongId(long songId);

    @Transaction
    public void deletePlaylist(int playlistId) {
        this.deleteSongsFromPlaylist(playlistId);
        this.deletePlaylistByPlaylistId(playlistId);
    }

    @Transaction
    public void reorderSongsInPlaylist(List<Long> songIds, int playlistId) {
        List<PlaylistSongCrossRef> songs = new ArrayList<>();
        for (int position = 0; position < songIds.size(); position++) {
            PlaylistSongCrossRef playlistSongCrossRef = new PlaylistSongCrossRef(playlistId, songIds.get(position), position);
            songs.add(playlistSongCrossRef);
        }
        deleteSongsFromPlaylist(playlistId);
        insertAll(songs);
    }


    @Transaction
    public void modifyPlaylist(List<Long> songIds, int playlistId) {
        this.deleteSongsFromPlaylist(playlistId);
        List<PlaylistSongCrossRef> songs = new ArrayList<>(songIds.size());
        for (int i = 0; i < songIds.size(); i++) {
            songs.add(new PlaylistSongCrossRef(playlistId, songIds.get(i), i));
        }
        insertAll(songs);
    }

    @Update
    public abstract void update(Playlist playlist);

    @Query("SELECT * from Playlist where name = :name")
    public abstract Playlist getPlaylistByName(String name);


    @Transaction
    public void removeAllPlaylists() {
        deleteAllPlaylistEntries();
        deleteAllPlaylists();
    }

    /*
     * Internal Helper Methods
     *
     */

    @Query("DELETE FROM PlaylistSongCrossRef")
    protected abstract void deleteAllPlaylistEntries();

    @Query("DELETE FROM Playlist")
    protected abstract void deleteAllPlaylists();

    @Transaction
    public void addSongToPlaylist(long songId, int playlistId) {
        PlaylistWithSongCount playlistwithSongs = getPlaylistWithSongCount(playlistId);
        PlaylistSongCrossRef playlistSongCrossRef = new PlaylistSongCrossRef(playlistId, songId, playlistwithSongs.getSongCount());
        insert(playlistSongCrossRef);
    }

    @Query("SELECT pl.* FROM Playlist pl " +
            "LEFT JOIN PlaylistSongCrossRef ref ON ref.playlistId = pl.playlistId " +
            "WHERE pl.playlistId NOT IN " +
            "(SELECT ref2.playlistId FROM PlaylistSongCrossRef ref2 WHERE ref2.songId == :songId) " +
            "GROUP BY pl.playlistId " +
            "ORDER BY  pl.name ASC")
    public abstract LiveData<List<Playlist>> getPlaylistsWhereSongCanBeAdded(long songId);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void insertAll(List<PlaylistSongCrossRef> refs);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void insert(PlaylistSongCrossRef ref);

    @Query("DELETE FROM PlaylistSongCrossRef where playlistId = :playlistId")
    protected abstract void deleteSongsFromPlaylist(long playlistId);

    @Query("DELETE FROM PlaylistSongCrossRef where playlistId = :playlistId AND songId = :songId")
    public abstract void deleteSongFromPlaylist(long songId, int playlistId);

    @Query("SELECT pl.name as name, pl.playlistId as playlistId, " +
            "(SELECT COUNT(r.playlistId) FROM PlaylistSongCrossRef r JOIN Song s ON s.songId = r.songId AND r.playlistId = pl.playlistId WHERE s.deleted = 0) as songCount " +
            "FROM Playlist pl " +
            "WHERE pl.name LIKE :text " +
            "GROUP BY pl.playlistId " +
            "ORDER BY CASE WHEN :asc = 1 THEN pl.name END ASC, CASE WHEN :asc = 0 THEN pl.name END DESC")
    protected abstract LiveData<List<PlaylistWithSongCount>> getPlaylistWithSongCount(String text, boolean asc);

    @Query("SELECT pl.name as name, pl.playlistId as playlistId, " +
            "(SELECT COUNT(r.playlistId) FROM PlaylistSongCrossRef r JOIN Song s ON s.songId = r.songId AND r.playlistId = pl.playlistId WHERE s.deleted = 0) as songCount " +
            "FROM Playlist pl " +
            "GROUP BY pl.playlistId " +
            "ORDER BY CASE WHEN :asc = 1 THEN pl.name END ASC, CASE WHEN :asc = 0 THEN pl.name END DESC")
    protected abstract LiveData<List<PlaylistWithSongCount>> getPlaylistWithSongCount(boolean asc);

    @Query("SELECT pl.name as name, pl.playlistId as playlistId, " +
            "(SELECT COUNT(r.playlistId) FROM PlaylistSongCrossRef r JOIN Song s ON s.songId = r.songId AND r.playlistId = pl.playlistId WHERE s.deleted = 0) as songCount " +
            "FROM Playlist pl " +
            "WHERE pl.playlistId = :playlistId " +
            "GROUP BY pl.playlistId " +
            "LIMIT 1")
    protected abstract PlaylistWithSongCount getPlaylistWithSongCount(int playlistId);

    @Query("DELETE FROM Playlist WHERE playlistId = :playlistId")
    protected abstract void deletePlaylistByPlaylistId(int playlistId);
}
