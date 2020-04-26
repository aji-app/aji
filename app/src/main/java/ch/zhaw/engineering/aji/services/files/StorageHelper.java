package ch.zhaw.engineering.aji.services.files;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.dto.SongDto;
import ch.zhaw.engineering.aji.services.database.entity.Song;

public class StorageHelper {

    private static final String TAG = "StorageHelper";

    public static void synchronizeMediaStoreSongs(Context context, Handler handler) {
        handler.post(() -> {
            Log.i(TAG, "Starting synchronization");
            SongDao songRepository = SongDao.getInstance(context);
            // Add new songs
            forEachSongInMediaStore(context, song -> {
                if (synchronizeSongWithDb(context, song)) {
                    Log.i(TAG, "Added song '" + song.getFilepath() + "' to Database");
                }
            });
            // Remove deleted songs
            for (Song song : songRepository.getAllSongs()) {
                if (!new File(song.getFilepath()).exists()) {
                    songRepository.deleteSongById(song.getSongId());
                    Log.i(TAG, "Deleted song '" + song.getFilepath() + "' from Database");
                }
            }

            Log.i(TAG, "Finished synchronization");
        });
    }

    private static String saveAlbumArt(Context context, SongDto song) {
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

    private static void forEachSongInMediaStore(Context context, SongDtoOperationCallback callback) {

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return;
        }

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();


            int isMusic = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));

            if (isMusic != 0) {
                SongDto song = new SongDto();

                song.setFilepath(cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));

                if (!new File(song.getFilepath()).exists()) {
                    continue;
                }

                song.setMediaStoreSongId(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
                song.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
                song.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
                song.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                song.setTrackNumber(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)));

                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(song.getFilepath());

                byte[] albumArt = mmr.getEmbeddedPicture();
                if (albumArt != null) {
                    song.albumArt = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    song.setDuration(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                } else {

                    song.setDuration(Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
                }

                mmr.release();

                if (callback != null) {
                    callback.handleSong(song);
                }
            }
        }
        cursor.close();
    }

    private interface SongDtoOperationCallback {
        void handleSong(SongDto dto);
    }
}
