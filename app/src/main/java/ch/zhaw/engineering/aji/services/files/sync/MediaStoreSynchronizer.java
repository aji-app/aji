package ch.zhaw.engineering.aji.services.files.sync;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.dto.SongDto;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.services.files.StorageHelper;
import lombok.Value;

public class MediaStoreSynchronizer {
    private static final String TAG = "MediaStoreSync";
    private final Context mContext;
    private final ExecutorService mExecutorService;
    private Handler mHandler;
    private BackgroundSyncTask mWaitForLotsOfUpdates = new BackgroundSyncTask();
    private final static long WAIT_TIME = 5 * 1000;

    public MediaStoreSynchronizer(Context context) {
        HandlerThread thread = new HandlerThread("AudioFileObserver", Thread.NORM_PRIORITY);
        thread.start();
        mHandler = new Handler(thread.getLooper());
        mContext = context;
        mExecutorService = Executors.newFixedThreadPool(1);
    }

    public void triggerAllSyncSoon() {
        if (mWaitForLotsOfUpdates.getStatus() == AsyncTask.Status.RUNNING) {
            mWaitForLotsOfUpdates.cancel(true);
        }
        mWaitForLotsOfUpdates = new BackgroundSyncTask();
        mWaitForLotsOfUpdates.executeOnExecutor(mExecutorService, new BackgroundArgs(this, mHandler));
    }

    public void synchronizeAllSongs() {
        Log.i(TAG, "Synchronizing all songs");
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
            DatabaseSynchronizer.synchronizeSongWithDb(mContext, song);
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

        List<Long> songsToDelete = new ArrayList<>();
        for (Song song : nonExistingSongs.values()) {
            StorageHelper.deleteAlbumArt(song.getAlbumArtPath());
            songsToDelete.add(song.getSongId());
        }

        songDao.deleteSongsByIds(songsToDelete);
    }

    public void synchronizeUri(Uri uri) {
        if (!uri.toString().contains(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())) {
            triggerAllSyncSoon();
            return;
        }
        Log.i(TAG, "Synchronizing single song");
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        Cursor cursor = mContext.getContentResolver().query(
                uri, null, selection, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return;
        }
        Log.i(TAG, "Synchronizing single song");
        if (cursor.moveToFirst()) {
            SongDto song = loadFromCursor(cursor);
            DatabaseSynchronizer.synchronizeSongWithDb(mContext, song);
        }
        cursor.close();
    }

    private SongDto loadFromCursor(Cursor cursor) {
        SongDto song = new SongDto();

        song.setFilepath(cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));

        song.setMediaStoreSongId(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
        song.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)).trim());
        song.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)).trim());
        song.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)).trim());
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

    @Value
    private static class BackgroundArgs {
        MediaStoreSynchronizer mSynchronizer;
        Handler mHandler;
    }
    private static class BackgroundSyncTask extends AsyncTask<BackgroundArgs, Void, Void> {

        @Override
        protected Void doInBackground(BackgroundArgs... contexts) {
            if (contexts.length == 0) {
                return null;
            }
            try {
                Log.i(TAG, "Wait for more updates");
                Thread.sleep(WAIT_TIME);
                Log.i(TAG, "Triggering Synchronization");
                contexts[0].getHandler().post(() -> {
                   contexts[0].getSynchronizer().synchronizeAllSongs();
                });
            } catch (InterruptedException e) {
                // This happens when we cancel the task
                Log.i(TAG, "More updates arrived");
            }

            return null;
        }
    }
}
