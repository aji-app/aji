package ch.zhaw.engineering.aji.services.files;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.io.File;

import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.dto.SongDto;
import ch.zhaw.engineering.aji.services.files.sync.DatabaseSynchronizer;
import ch.zhaw.engineering.aji.util.FileNameParser;

public class AudioFileScanner extends JobIntentService {

    public static final String TAG = "AudioFileScanner";
    public static final String EXTRA_SCRAPE_ROOT_FOLDER = "scrape-root-folder";

    private SongDao mSongDao;
    private FileNameParser mFileNameParser;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, AudioFileScanner.class, 2, intent);
    }

    public AudioFileScanner() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSongDao = SongDao.getInstance(this);
        mFileNameParser = new FileNameParser(this);

    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (intent.hasExtra(EXTRA_SCRAPE_ROOT_FOLDER)) {
            String rootFolder = intent.getStringExtra(EXTRA_SCRAPE_ROOT_FOLDER);
            if (rootFolder != null) {
                walk(new File(rootFolder), songDto -> {
                    DatabaseSynchronizer.synchronizeSongWithDb(this, songDto);
                });
            }
        }
    }

    private void walk(File root, AudioFileScannedCallback callback) {
        File[] folders = root.listFiles(File::isDirectory);
        File[] audioFiles = root.listFiles(new SupportedFileTypeFilter());

        if (audioFiles != null) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            for (File f : audioFiles) {
                Uri uri = Uri.fromFile(f);
                if (mSongDao.exists(uri.getPath())) {
                    continue;
                }
                mmr.setDataSource(getApplicationContext(), uri);

                SongDto song = new SongDto();

                song.setFilepath(uri.getPath());
                song.setArtist(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).trim());
                song.setTitle(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).trim());
                song.setAlbum(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM).trim());
                song.setDuration(Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
                song.setTrackNumber(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER).trim());

                if (song.getTitle() == null) {
                    // No or not enough Metadata
                    parseFileName(f.getName(), song);
                }

                byte[] albumArt = mmr.getEmbeddedPicture();
                if (albumArt != null) {
                    song.albumArt = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                }
                callback.handleSong(song);
            }
        }
        if (folders != null) {
            for (File folder : folders) {
                walk(folder, callback);
            }
        }
    }

    private void parseFileName(String filename, SongDto song) {
        FileNameParser.ParsedFileName parsedFileName = mFileNameParser.parseFileName(filename);
        if (song.getArtist() == null) {
            song.setArtist(parsedFileName.getArtist().trim());
        }
        if (song.getAlbum() == null) {
            song.setAlbum(parsedFileName.getAlbum().trim());
        }
        if (song.getTitle() == null) {
            song.setTitle(parsedFileName.getTitle().trim());
        }
    }

    private interface AudioFileScannedCallback {
        void handleSong(SongDto dto);
    }
}
