package ch.zhaw.engineering.tbdappname.services.database.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.AppDatabase;
import ch.zhaw.engineering.tbdappname.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.PlaylistWithSongCount;
import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.PlaylistSongCrossRef;
import ch.zhaw.engineering.tbdappname.services.database.entity.PlaylistWithSongs;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;


public class PlaylistRepository {
    private final PlaylistDao mPlaylistDao;

    public PlaylistRepository(PlaylistDao playlistDao) {
        mPlaylistDao = playlistDao;
    }

    public static PlaylistRepository getInstance(Context context) {
        PlaylistDao songDao = AppDatabase.getInstance(context).playlistDao();
        return new PlaylistRepository(songDao);
    }

    public LiveData<List<Playlist>> getAllPlaylists() {
        return mPlaylistDao.getPlaylists();
    }

    public LiveData<List<PlaylistWithSongs>> getSortedPlaylists(boolean ascending, String searchText) {

        if (searchText == null) {
           return mPlaylistDao.getSortedPlaylists(ascending);
        }
        String searchQuery = "%" + searchText + "%";
        return mPlaylistDao.getSortedPlaylists(searchQuery, ascending);
    }

    public void addSongToPlaylist(Song song, Playlist playlist) {
        PlaylistWithSongs playlistwithSongs = mPlaylistDao.findPlaylistWithSongsById(playlist.getPlaylistId());
        PlaylistSongCrossRef playlistSongCrossRef = new PlaylistSongCrossRef(playlist.getPlaylistId(), song.getSongId(), playlistwithSongs.songs.size());
        mPlaylistDao.insert(playlistSongCrossRef);
    }

    public PlaylistWithSongs findPlaylistById(long id) {
        return mPlaylistDao.findPlaylistWithSongsById(id);
    }

    public void update(PlaylistWithSongs playlist) {
        // TODO: Improve
        mPlaylistDao.deleteSongsFromPlaylist(playlist.playlist.getPlaylistId());
        insert(playlist);
    }

    public void deleteSongFromPlaylists(Song song) {
        mPlaylistDao.deleteBySongId(song.getSongId());
    }

    public LiveData<List<PlaylistWithSongCount>> getPlaylistsWithSongCount(String searchText, boolean ascending) {
        if (searchText == null) {
            return mPlaylistDao.getPlaylistWithSongCount(ascending);
        }
        String searchQuery = "%" + searchText + "%";
        return mPlaylistDao.getPlaylistWithSongCount(searchQuery, ascending);
    }

    public void insert(Playlist playlist) {
        mPlaylistDao.insert(playlist);
    }


    public void insert(PlaylistWithSongs playlist) {
        long createdPlaylist = playlist.playlist.getPlaylistId();
        if (playlist.playlist.getPlaylistId() == 0) {
            createdPlaylist = mPlaylistDao.insert(playlist.playlist);
        }
        List<PlaylistSongCrossRef> songs = new ArrayList<>();
        for (int position = 0; position < playlist.songs.size(); position++) {
            Song song = playlist.songs.get(position);
            PlaylistSongCrossRef playlistSongCrossRef = new PlaylistSongCrossRef(createdPlaylist, song.getSongId(), position);
            songs.add(playlistSongCrossRef);
        }
        mPlaylistDao.insertAll(songs);
    }

    public void deletePlaylistById(int playlistId) {
        mPlaylistDao.deletePlaylistAndSongsByPlaylistId(playlistId);
    }
}
