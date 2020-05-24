package ch.zhaw.engineering.aji.services.files.sync;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TimingLogger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.dto.SongDto;
import ch.zhaw.engineering.aji.services.database.dto.SongWithOnlyAlbumAndIds;
import ch.zhaw.engineering.aji.services.files.StorageHelper;
import lombok.Value;

public class MediaStoreSynchronizer {
    private static final String TAG = "MediaStoreSync";
    private final Context mContext;
    private final ExecutorService mExecutorService;
    private Handler mHandler;
    private BackgroundSyncTask mWaitForLotsOfUpdates = new BackgroundSyncTask();
    private final static long WAIT_TIME = 5 * 1000;
    private final static int QUERY_LIMIT = 10;
    private final List<SongDto> mCurrentlyProcessingSongs = new ArrayList<>(QUERY_LIMIT);

    public MediaStoreSynchronizer(Context context) {
        HandlerThread thread = new HandlerThread("AudioFileObserver", Process.THREAD_PRIORITY_BACKGROUND);
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
        TimingLogger logger = new TimingLogger(TAG, "synchronizeAllSongs");
        int offset = 0;
        int processed = 0;

        do {
            processed = syncSongs(offset);
            offset += processed;
            logger.addSplit(offset + " to " + (offset + QUERY_LIMIT));
        } while (processed == QUERY_LIMIT);
        logger.dumpToLog();
        deleteSongsNotInMediaStore();
    }

    private int syncSongs(int offset) {
        Log.i(TAG, "Processing songs " + offset + " to " + (offset + QUERY_LIMIT));
        String onlyAudioSelection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.buildUpon().encodedQuery("limit=" + offset + "," + QUERY_LIMIT).build()
                , null, onlyAudioSelection, null,
                MediaStore.Audio.Media._ID);
        if (cursor == null) {
            return 0;
        }
        final int processedCount = cursor.getCount();
        int position = 0;
        int size = mCurrentlyProcessingSongs.size();
        while (cursor.moveToNext()) {
            if (size > position) {
                mCurrentlyProcessingSongs.get(position).clear();
            } else {
                mCurrentlyProcessingSongs.add(new SongDto());
            }
            loadFromCursorIntoDto(cursor, mCurrentlyProcessingSongs.get(position));
            position++;
        }
        cursor.close();
        for (SongDto song : mCurrentlyProcessingSongs) {
            DatabaseSynchronizer.synchronizeSongWithDb(mContext, song);
        }
        return processedCount;
    }

    private void deleteSongsNotInMediaStore() {
        String onlyAudioSelection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media._ID }, onlyAudioSelection, null,
                MediaStore.Audio.Media._ID);
        if (cursor == null) {
            return;
        }
        Set<Long> mediaStoreIds = new HashSet<>(cursor.getCount());
        while (cursor.moveToNext()) {
            Long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
            mediaStoreIds.add(id);
        }
        cursor.close();
        Log.i(TAG, "Found " + mediaStoreIds.size() + " songs");
        SongDao songDao = AppDatabase.getInstance(mContext).songDao();
        List<SongWithOnlyAlbumAndIds> allSongs = songDao.getAllSongsForSync();
        List<SongWithOnlyAlbumAndIds> songsNotInMediaStore = new ArrayList<>();
        for (SongWithOnlyAlbumAndIds song : allSongs) {
            if (!mediaStoreIds.contains(song.getMediaStoreSongId())) {
                songsNotInMediaStore.add(song);
            }
        }
        List<Long> songsToDelete = new ArrayList<>();
        for (SongWithOnlyAlbumAndIds song : songsNotInMediaStore) {
            int albumSongCount = songDao.getAlbumSongCount(song.getAlbum());
            if (albumSongCount == 1) {
                StorageHelper.deleteAlbumArt(song.getAlbumArtPath());
            }
            songsToDelete.add(song.getSongId());
        }
        Log.i(TAG, "Deleting " + songsToDelete.size() + " songs");
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
        SongDto song = new SongDto();
        if (cursor.moveToFirst()) {
            loadFromCursorIntoDto(cursor, song);
            DatabaseSynchronizer.synchronizeSongWithDb(mContext, song);
        }
        cursor.close();
    }

    private void loadFromCursorIntoDto(Cursor cursor, SongDto song) {

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
