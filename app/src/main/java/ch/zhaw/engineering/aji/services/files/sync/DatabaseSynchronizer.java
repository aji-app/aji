package ch.zhaw.engineering.aji.services.files.sync;

import android.content.Context;

import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.dto.SongDto;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.services.files.StorageHelper;

public class DatabaseSynchronizer {
    public static void synchronizeSongWithDb(Context context, SongDto song) {
        SongDao songDao = SongDao.getInstance(context);
        if (songDao.exists(song.getFilepath())) {
            Song storedSong = songDao.getSongByPath(song.getFilepath());
            if (storedSong.getAlbumArtPath() == null) {
                String artPath = StorageHelper.saveAlbumArt(context, song);
                storedSong.setAlbumArtPath(artPath);
            }
            if (song.getMediaStoreSongId() != null) {
                storedSong.setMediaStoreSongId(song.getMediaStoreSongId());
            }
            songDao.updateSong(storedSong);
        } else {
            String artPath = StorageHelper.saveAlbumArt(context, song);
            Song dbSong = song.toSong(artPath);
            songDao.insertSong(dbSong);
        }
    }
}
