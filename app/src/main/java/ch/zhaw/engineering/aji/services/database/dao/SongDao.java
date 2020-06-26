package ch.zhaw.engineering.aji.services.database.dao;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.database.dto.AlbumDto;
import ch.zhaw.engineering.aji.services.database.dto.AlbumWithSongCountDto;
import ch.zhaw.engineering.aji.services.database.dto.ArtistDto;
import ch.zhaw.engineering.aji.services.database.dto.ArtistWithAlbumCountDto;
import ch.zhaw.engineering.aji.services.database.dto.SongWithOnlyAlbumAndIds;
import ch.zhaw.engineering.aji.services.database.entity.Song;

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

    public void hideSong(long songId) {
        hideSongBySongId(songId);
    }

    @Transaction
    public void deleteSongById(long songId) {
        deleteSongsFromPlaylist(songId);
        hideSongBySongId(songId);
    }

    public LiveData<List<Song>> getSortedSongs(SortType sortType, boolean ascending, String searchText, boolean onlyHidden) {
        if (searchText == null) {
            switch (sortType) {

                case TITLE:
                    return getSongsSortedByTitle(ascending, onlyHidden);
                case ARTIST:
                    return ascending ? getSongsSortedByArtistAsc(onlyHidden) : getSongsSortedByArtistDesc(onlyHidden);
                case ALBUM:
                    return ascending ? getSongsSortedByAlbumAsc(onlyHidden) : getSongsSortedByAlbumDesc(onlyHidden);
            }
        }
        String searchQuery = "%" + searchText + "%";
        switch (sortType) {

            case TITLE:
                return getSongsSortedByTitle(searchQuery, ascending, onlyHidden);
            case ARTIST:
                return ascending ? getSongsSortedByArtistAsc(searchQuery, onlyHidden) : getSongsSortedByArtistDesc(searchQuery, onlyHidden);
            case ALBUM:
                return ascending ? getSongsSortedByAlbumAsc(searchQuery, onlyHidden) : getSongsSortedByAlbumDesc(searchQuery, onlyHidden);
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

    @Query("SELECT * FROM Song WHERE song.deleted = 0 ORDER BY random() LIMIT 1")
    public abstract Song getRandomSong();

    @Query("SELECT s.* FROM Song s JOIN PlaylistSongCrossRef ps ON ps.songId = s.songId WHERE s.deleted = 0 AND ps.playlistId = :playlistId ORDER BY ps.`order`")
    public abstract LiveData<List<Song>> getSongsForPlaylist(long playlistId);

    @Query("SELECT s.* FROM Song s JOIN PlaylistSongCrossRef ps ON ps.songId = s.songId WHERE s.deleted = 0 AND ps.playlistId = :playlistId and ps.`order` = 0 LIMIT 1")
    public abstract Song getFirstSongOfPlaylist(long playlistId);

    @Query("SELECT s.* FROM Song s JOIN PlaylistSongCrossRef ps ON ps.songId = s.songId WHERE s.deleted = 0 AND ps.playlistId = :playlistId ORDER BY ps.`order`")
    public abstract List<Song> getSongsForPlaylistAsList(long playlistId);

    @Query("SELECT * FROM Song s WHERE s.deleted = 0 AND s.songId = :id LIMIT 1")
    public abstract Song getSongById(long id);

    @Query("SELECT * FROM Song s WHERE s.songId = :id LIMIT 1")
    public abstract Song getSongByIdInclusiveHidden(long id);


    @Query("SELECT * FROM Song s WHERE s.deleted = 0 AND s.songId = :id LIMIT 1")
    public abstract LiveData<Song> getSong(long id);

    @Query("SELECT * FROM Song WHERE song.deleted = 0 AND song.favorite = 1")
    public abstract LiveData<List<Song>> getFavorites();

    @Query("SELECT * FROM Song WHERE song.deleted = 0 AND song.favorite = 1")
    public abstract List<Song> getFavoritesAsList();

    @Query("SELECT DISTINCT song.album as name, song.albumArtPath as coverPath FROM Song WHERE song.deleted = 0 ORDER BY song.album ASC")
    public abstract LiveData<List<AlbumDto>> getAlbums();

    public LiveData<List<AlbumDto>> getAlbums(String filter, boolean ascending, boolean showHidden) {
        if (filter == null) {
            return getFilteredAlbums(ascending, showHidden);
        }
        String searchQuery = "%" + filter + "%";
        return getFilteredAlbums(searchQuery, ascending, showHidden);
    }

    @Query("SELECT DISTINCT song.artist as name FROM Song WHERE song.deleted = 0 ORDER BY song.artist ASC")
    public abstract LiveData<List<ArtistDto>> getArtists();

    public LiveData<List<ArtistDto>> getArtists(String filter, boolean ascending, boolean showHidden) {
        if (filter == null) {
            return getFilteredArtists(ascending, showHidden);
        }
        String searchQuery = "%" + filter + "%";
        return getFilteredArtists(searchQuery, ascending, showHidden);
    }

    @Query("SELECT * FROM Song WHERE song.deleted = 0 AND song.album = :album ORDER BY song.trackNumber, song.title ASC")
    public abstract LiveData<List<Song>> getSongsForAlbum(String album);

    @Query("SELECT * FROM Song WHERE song.deleted = 0 AND song.album = :album ORDER BY song.trackNumber, song.title ASC")
    public abstract List<Song> getSongsForAlbumAsList(String album);

    @Query("SELECT * FROM Song WHERE song.deleted = 0 AND song.artist = :artist ORDER BY song.album, song.trackNumber, song.title ASC")
    public abstract LiveData<List<Song>> getSongsForArtist(String artist);

    @Query("SELECT * FROM Song WHERE song.deleted = 0 AND song.artist = :artist ORDER BY song.album, song.trackNumber, song.title ASC")
    public abstract List<Song> getSongsForArtistAsList(String artist);

    @Query("SELECT song.album as name, (SELECT s2.albumArtPath FROM song s2 WHERE song.album = s2.album AND s2.albumArtPath is not null) as coverPath FROM Song song " +
            "WHERE song.album = :album " +
            "GROUP BY song.album ")
    public abstract AlbumDto getAlbum(String album);

    @Query("SELECT Count(song.songID) as name FROM Song song WHERE song.album = :album ")
    public abstract int getAlbumSongCount(String album);

    @Query("SELECT * from Song where songId in (:songIds)")
    public abstract LiveData<List<Song>> getSongs(List<Long> songIds);

    public LiveData<Map<Long, Song>> getSongsById(List<Long> songIds) {
        return Transformations.map(getSongs(songIds), songs -> {
            HashMap<Long, Song> songMap = new HashMap<>(songs.size());
            for (Song song : songs) {
                songMap.put(song.getSongId(), song);
            }
            return songMap;
        });
    }

    @Query("SELECT songId, album, albumArtPath, mediaStoreSongId from Song")
    public abstract List<SongWithOnlyAlbumAndIds> getAllSongsForSync();

    @Query("DELETE from Song where songId in (:ids)")
    public abstract void deleteSongsByIds(Collection<Long> ids);

    @Query("UPDATE Song SET deleted = 0 WHERE songId = :songId")
    public abstract void unhideSong(long songId);

    @Query("UPDATE song SET deleted = 1 WHERE artist = :artist")
    public abstract void hideSongsByArtist(String artist);

    @Query("UPDATE song SET deleted = 0 WHERE artist = :artist")
    public abstract void unhideSongsByArtist(String artist);

    @Query("UPDATE song SET deleted = 1 WHERE album = :album")
    public abstract void hideSongsByAlbum(String album);

    @Query("UPDATE song SET deleted = 0 WHERE album = :album")
    public abstract void unhideSongsByAlbum(String album);

    @Query("DELETE FROM Song")
    public abstract void removeAllSongs();

    @Query("SELECT * from Song where song.album = :name ORDER BY song.trackNumber, song.title LIMIT 1")
    public abstract Song getFirstSongOfAlbum(String name);

    @Query("SELECT * from Song where song.artist= :name ORDER BY song.album, song.trackNumber, song.title LIMIT 1")
    public abstract Song getFirstSongOfArtist(String name);

    /*
     * Protected Helper Methods
     *
     */
    @Query("UPDATE song SET deleted = 1 WHERE songId = :songId")
    protected abstract void hideSongBySongId(long songId);

    @Query("SELECT DISTINCT song.artist as name FROM Song song " +
            "WHERE song.artist LIKE :text AND song.deleted = :showHidden AND song.artist IS NOT NULL " +
            "ORDER BY CASE WHEN :asc = 1 THEN song.artist END ASC, CASE WHEN :asc = 0 THEN song.artist END DESC")
    protected abstract LiveData<List<ArtistDto>> getFilteredArtists(String text, boolean asc, boolean showHidden);

    @Query("SELECT DISTINCT song.artist as name FROM Song song " +
            "WHERE song.deleted = :showHidden AND song.artist IS NOT NULL " +
            "ORDER BY CASE WHEN :asc = 1 THEN song.artist END ASC, CASE WHEN :asc = 0 THEN song.artist END DESC")
    protected abstract LiveData<List<ArtistDto>> getFilteredArtists(boolean asc, boolean showHidden);

    @Query("SELECT DISTINCT song.album as name, (SELECT s2.albumArtPath FROM song s2 WHERE song.album = s2.album AND s2.albumArtPath is not null) as coverPath FROM Song song " +
            "WHERE LOWER(song.album) LIKE LOWER(:text) AND song.deleted = :showHidden AND song.album IS NOT NULL " +
            "GROUP BY song.album " +
            "ORDER BY CASE WHEN :asc = 1 THEN song.album END ASC, CASE WHEN :asc = 0 THEN song.album END DESC")
    protected abstract LiveData<List<AlbumDto>> getFilteredAlbums(String text, boolean asc, boolean showHidden);

    @Query("SELECT DISTINCT song.album as name, (SELECT s2.albumArtPath FROM song s2 WHERE song.album = s2.album AND s2.albumArtPath is not null) as coverPath FROM Song song " +
            "WHERE song.deleted = :showHidden AND song.album IS NOT NULL " +
            "GROUP BY song.album " +
            "ORDER BY CASE WHEN :asc = 1 THEN song.album END ASC, CASE WHEN :asc = 0 THEN song.album END DESC")
    protected abstract LiveData<List<AlbumDto>> getFilteredAlbums(boolean asc, boolean showHidden);

    @Query("SELECT * FROM Song WHERE song.deleted = :deleted AND (song.title like :text OR song.album like :text OR song.artist like :text) " +
            "ORDER BY CASE WHEN :asc = 1 THEN song.title END ASC, CASE WHEN :asc = 0 THEN song.title END DESC")
    protected abstract LiveData<List<Song>> getSongsSortedByTitle(String text, boolean asc, boolean deleted);

    @Query("SELECT * FROM Song WHERE song.deleted = :deleted AND (song.title like :text OR song.album like :text OR song.artist like :text) " +
            "ORDER BY song.album, song.trackNumber ASC")
    protected abstract LiveData<List<Song>> getSongsSortedByAlbumAsc(String text, boolean deleted);

    @Query("SELECT * FROM Song WHERE song.deleted = :deleted AND (song.title like :text OR song.album like :text OR song.artist like :text) " +
            "ORDER BY song.album, song.trackNumber DESC")
    protected abstract LiveData<List<Song>> getSongsSortedByAlbumDesc(String text, boolean deleted);

    @Query("SELECT * FROM Song WHERE song.deleted = :deleted AND (song.title like :text OR song.album like :text OR song.artist like :text)" +
            " ORDER BY song.artist, song.album, song.trackNumber ASC")
    protected abstract LiveData<List<Song>> getSongsSortedByArtistAsc(String text, boolean deleted);

    @Query("SELECT * FROM Song WHERE song.deleted = :deleted AND (song.title like :text OR song.album like :text OR song.artist like :text)" +
            " ORDER BY song.artist, song.album, song.trackNumber DESC")
    protected abstract LiveData<List<Song>> getSongsSortedByArtistDesc(String text, boolean deleted);

    @Query("SELECT * FROM Song WHERE song.deleted = :deleted " +
            "ORDER BY CASE WHEN :asc = 1 THEN song.title END ASC, CASE WHEN :asc = 0 THEN song.title END DESC")
    protected abstract LiveData<List<Song>> getSongsSortedByTitle(boolean asc, boolean deleted);

    @Query("SELECT * FROM Song WHERE song.deleted = :deleted " +
            "ORDER BY song.album, song.trackNumber ASC")
    protected abstract LiveData<List<Song>> getSongsSortedByAlbumAsc(boolean deleted);

    @Query("SELECT * FROM Song WHERE song.deleted = :deleted " +
            "ORDER BY song.album, song.trackNumber DESC")
    protected abstract LiveData<List<Song>> getSongsSortedByAlbumDesc(boolean deleted);

    @Query("SELECT * FROM Song WHERE song.deleted = :deleted " +
            "ORDER BY song.artist, song.album, song.trackNumber ASC")
    protected abstract LiveData<List<Song>> getSongsSortedByArtistAsc(boolean deleted);

    @Query("SELECT * FROM Song WHERE song.deleted = :deleted " +
            "ORDER BY song.artist, song.album, song.trackNumber DESC")
    protected abstract LiveData<List<Song>> getSongsSortedByArtistDesc(boolean deleted);

    @Query("DELETE FROM PlaylistSongCrossRef where playlistId = :playlistId")
    protected abstract void deleteSongsFromPlaylist(long playlistId);
    
    @Query("SELECT DISTINCT song.album as name, (SELECT s2.albumArtPath FROM song s2 WHERE song.album = s2.album AND s2.albumArtPath is not null) as coverPath " +
            ", (SELECT COUNT(DISTINCT song.songId) FROM Song s3 WHERE song.album = s3.album) as songCount " +
            "FROM Song song " +
            "WHERE song.deleted = :showHidden AND song.album IS NOT NULL " +
            "GROUP BY song.album " +
            "ORDER BY CASE WHEN :asc = 1 THEN song.album END ASC, CASE WHEN :asc = 0 THEN song.album END DESC")
    public abstract List<AlbumWithSongCountDto> getFilteredAlbumsWithSongCount(boolean asc, boolean showHidden);

    @Query("SELECT DISTINCT song.artist as name" +
            ", (SELECT COUNT(DISTINCT s3.album) FROM Song s3 WHERE song.artist = s3.artist) as albumCount " +
            "FROM Song song " +
            "WHERE song.deleted = :showHidden AND song.artist IS NOT NULL " +
            "ORDER BY CASE WHEN :asc = 1 THEN song.artist END ASC, CASE WHEN :asc = 0 THEN song.artist END DESC")
    public abstract List<ArtistWithAlbumCountDto> getFilteredArtistsWithAlbumCounts(boolean asc, boolean showHidden);

    public enum SortType {
        TITLE, ARTIST, ALBUM
    }
}
