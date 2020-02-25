package ch.zhaw.engineering.tbdappname.services.database.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.AppDatabase;
import ch.zhaw.engineering.tbdappname.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.tbdappname.services.database.entity.PlaylistSongCrossRef;
import ch.zhaw.engineering.tbdappname.services.database.entity.PlaylistWithSongs;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;


public class PlaylistRepository {

    public final LiveData<List<PlaylistWithSongs>> allPlaylists;
    private final PlaylistDao mPlaylistDao;

    public PlaylistRepository(PlaylistDao playlistDao) {
        allPlaylists = playlistDao.getPlaylistsWithSongs();
        mPlaylistDao = playlistDao;
    }

    public static PlaylistRepository getInstance(Context context) {
        PlaylistDao songDao = AppDatabase.getInstance(context).playlistDao();
        return new PlaylistRepository(songDao);
    }

    public void update(PlaylistWithSongs playlist) {
        // TODO
//        mPlaylistDao.update(playlist.playlist);
//        mPlaylistDao.deleteSongsFromPlaylist(playlist.playlist.getPlaylistId());
//        List<PlaylistSongCrossRef> songs = playlist.songs.stream().map(song -> new PlaylistSongCrossRef(playlist.playlist.getPlaylistId(), song.getSongId())).collect(Collectors.toList());
//        mPlaylistDao.insertAll(songs);
    }

    public void deleteSongFromPlaylists(Song song) {
        mPlaylistDao.deleteBySongId(song.getSongId());
    }

    public void insert(PlaylistWithSongs playlist) {
        long createdPlaylist = mPlaylistDao.insert(playlist.playlist);
        List<PlaylistSongCrossRef> songs = new ArrayList<>();
        for (int position = 0; position < playlist.songs.size(); position++) {
            Song song = playlist.songs.get(position);
            PlaylistSongCrossRef playlistSongCrossRef = new PlaylistSongCrossRef(createdPlaylist, song.getSongId(), position);
            songs.add(playlistSongCrossRef);
        }
        mPlaylistDao.insertAll(songs);
    }
}
