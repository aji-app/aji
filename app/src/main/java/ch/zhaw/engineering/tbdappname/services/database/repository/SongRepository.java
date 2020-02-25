package ch.zhaw.engineering.tbdappname.services.database.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.AppDatabase;
import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.SongDto;
import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.services.files.StorageHelper;

public class SongRepository {

    private final Context mContext;
    private final PlaylistRepository mPlaylistRepo;
    private final SongDao mSongDao;

    public List<Song> getSongList() {
        return mSongDao.getSongList();
    }

    public LiveData<List<Song>> getSongs() {
        return mSongDao.getSongsSortedByTitle(true);
    }

    public Song getRandomSong() {
        return mSongDao.getRandomSong();
    }

    public LiveData<List<Song>> getSortedSongs(SortType sortType, boolean ascending, String searchText) {

        if (searchText == null) {
            switch (sortType) {

                case TITLE:
                    return mSongDao.getSongsSortedByTitle(ascending);
                case ARTIST:
                    return mSongDao.getSongsSortedByArtist(ascending);
                case ALBUM:
                    return mSongDao.getSongsSortedByAlbum(ascending);
            }
        }
        String searchQuery = "%" + searchText + "%";
        switch (sortType) {

            case TITLE:
                return mSongDao.getSongsSortedByTitle(searchQuery, ascending);
            case ARTIST:
                return mSongDao.getSongsSortedByArtist(searchQuery, ascending);
            case ALBUM:
                return mSongDao.getSongsSortedByAlbum(searchQuery, ascending);
        }

        return new MutableLiveData<>();
    }

    public SongRepository(SongDao songDao, Context context, PlaylistRepository playlistRepo) {
        mSongDao = songDao;
        mContext = context;
        mPlaylistRepo = playlistRepo;
    }

    public static SongRepository getInstance(Context context) {
        SongDao songDao = AppDatabase.getInstance(context).songDao();
        PlaylistRepository playlistRepo = PlaylistRepository.getInstance(context);
        return new SongRepository(songDao, context, playlistRepo);
    }

    public List<Song> getSongsForPlaylist(Playlist playlist) {
        if (playlist == null) {
            return new ArrayList<>(0);
        }
        return mSongDao.getSongsForPlaylist(playlist.getPlaylistId());
    }

    public boolean exists(String filepath) {
        return mSongDao.exists(filepath);
    }

    public boolean synchronizeSong(SongDto song) {
        if (mSongDao.exists(song.getFilepath())) {
            Song storedSong = mSongDao.getSongByPath(song.getFilepath());
            if (storedSong.getAlbumArtPath() == null) {
                String artPath = StorageHelper.saveAlbumArt(mContext, song);
                storedSong.setAlbumArtPath(artPath);
            }
            if (song.getMediaStoreSongId() != null) {
                storedSong.setMediaStoreSongId(song.getMediaStoreSongId());
            }
            mSongDao.updateSong(storedSong);
            return false;
        } else {
            String artPath = StorageHelper.saveAlbumArt(mContext, song);
            Song dbSong = song.toSong(artPath);
            mSongDao.insertSong(dbSong);
            return true;
        }
    }

    public void deleteSong(Song song) {
        mPlaylistRepo.deleteSongFromPlaylists(song);
        mSongDao.deleteSong(song);
    }

    public enum SortType {
        TITLE, ARTIST, ALBUM
    }
}
