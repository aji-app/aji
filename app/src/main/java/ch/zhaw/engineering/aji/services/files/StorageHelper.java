package ch.zhaw.engineering.aji.services.files;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.dto.SongDto;
import ch.zhaw.engineering.aji.services.database.entity.Song;

public class StorageHelper {

    private static final String TAG = "StorageHelper";

    public static String saveAlbumArt(Context context, SongDto song) {
        if (song.getAlbumArt() == null) {
            return null;
        }
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("albumart", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }
        String filenameIdentifier = song.getMediaStoreSongId() != null ? song.getMediaStoreSongId().toString() : UUID.randomUUID().toString();
        File albumArtPath = new File(directory, "albumart_" + filenameIdentifier + ".png");

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(albumArtPath);
            song.getAlbumArt().compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return albumArtPath.getAbsolutePath();
    }

    public static boolean synchronizeSongWithDb(Context context, SongDto song) {
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
            return false;
        } else {
            String artPath = StorageHelper.saveAlbumArt(context, song);
            Song dbSong = song.toSong(artPath);
            songDao.insertSong(dbSong);
            return true;
        }
    }

    public static void synchronizeSong(Context context, Uri uri) {
       MediaStoreSynchronizer synchronizer = new MediaStoreSynchronizer(context);
       synchronizer.synchronizeUri(uri);
    }
}
