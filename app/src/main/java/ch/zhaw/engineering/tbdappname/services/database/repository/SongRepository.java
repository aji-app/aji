package ch.zhaw.engineering.tbdappname.services.database.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.AppDatabase;
import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.SongDto;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.services.files.StorageHelper;

public class SongRepository {

    private Context mContext;
    private PlaylistRepository mPlaylistRepo;
    public final LiveData<List<Song>> allSongs;
    private final SongDao mSongDao;

    public List<Song> getSongList() {
        return mSongDao.getSongList();
    }

    public SongRepository(SongDao songDao, Context context, PlaylistRepository playlistRepo) {
        allSongs = songDao.getSongs();
        mSongDao = songDao;
        mContext = context;
        mPlaylistRepo = playlistRepo;
    }

    public static SongRepository getInstance(Context context) {
        SongDao songDao = AppDatabase.getInstance(context).songDao();
        PlaylistRepository playlistRepo = PlaylistRepository.getInstance(context);
        return new SongRepository(songDao, context, playlistRepo);
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
}
