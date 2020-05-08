package ch.zhaw.engineering.aji.services.files.sync;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.dto.SongDto;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.services.files.StorageHelper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MediaStoreSynchronizer {
    private static final String TAG = "MediaStoreSync";
    private final Context mContext;

    public void synchronizeAllSongs() {
        String onlyAudioSelection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, onlyAudioSelection, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return;
        }
        List<SongDto> songs = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            SongDto song = loadFromCursor(cursor);
            songs.add(song);
        }
        cursor.close();
        List<Long> mediaStoreIds = new ArrayList<>(songs.size());
        for (SongDto song : songs) {
            synchronizeSongWithDb(song);
            mediaStoreIds.add(song.getMediaStoreSongId());
        }

        SongDao songDao = AppDatabase.getInstance(mContext).songDao();
        List<Song> potentiallyDeletedSongs = songDao.getSongsNotMatchingMediaStoreIds(mediaStoreIds);
        Set<String> nonExistingMediaStoreIds = new HashSet<>();
        Map<Long, Song> nonExistingSongs = new HashMap<>();
        for (Song song : potentiallyDeletedSongs) {
            if (song.getMediaStoreSongId() != null) {
                nonExistingMediaStoreIds.add(song.getMediaStoreSongId().toString());
                nonExistingSongs.put(song.getMediaStoreSongId(), song);
            }
        }
        String selection = onlyAudioSelection + " AND " + MediaStore.Audio.Media._ID + " IN (?)";
        cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                selection,
                new String[]{TextUtils.join(",", nonExistingMediaStoreIds)},
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return;
        }
        while (cursor.moveToNext()) {
            Long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
            nonExistingSongs.remove(id);

        }

        for (Song song : nonExistingSongs.values()) {
            StorageHelper.deleteAlbumArt(song.getAlbumArtPath());
        }

        songDao.deleteSongsByMediaStoreIds(nonExistingSongs.keySet());
    }

    public void synchronizeUri(Uri uri) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        Cursor cursor = mContext.getContentResolver().query(
                uri, null, selection, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return;
        }
        if (cursor.moveToFirst()) {
            SongDto song = loadFromCursor(cursor);
            synchronizeSongWithDb(song);
        }
        cursor.close();
    }

    private void synchronizeSongWithDb(SongDto song) {
        SongDao songDao = SongDao.getInstance(mContext);
        if (songDao.exists(song.getFilepath())) {
            Song storedSong = songDao.getSongByPath(song.getFilepath());
            if (storedSong.getAlbumArtPath() == null) {
                String artPath = StorageHelper.saveAlbumArt(mContext, song);
                storedSong.setAlbumArtPath(artPath);
            }
            if (song.getMediaStoreSongId() != null) {
                storedSong.setMediaStoreSongId(song.getMediaStoreSongId());
            }
            songDao.updateSong(storedSong);
        } else {
            String artPath = StorageHelper.saveAlbumArt(mContext, song);
            Song dbSong = song.toSong(artPath);
            songDao.insertSong(dbSong);
        }
    }


    private SongDto loadFromCursor(Cursor cursor) {
        SongDto song = new SongDto();

        song.setFilepath(cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));

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
            try {
                song.setDuration(Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
            } catch (NumberFormatException e) {
                song.setDuration(0);
            }
        }

        mmr.release();

        return song;

    }
}
