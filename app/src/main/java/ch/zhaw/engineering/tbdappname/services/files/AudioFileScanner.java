package ch.zhaw.engineering.tbdappname.services.files;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.io.File;

import ch.zhaw.engineering.tbdappname.services.database.dto.SongDto;
import ch.zhaw.engineering.tbdappname.services.database.repository.SongRepository;

public class AudioFileScanner extends JobIntentService {

    public static final String TAG = "AudioFileScanner";
    public static final String EXTRA_SCRAPE_ROOT_FOLDER = "scrape-root-folder";

    private SongRepository mSongRepository;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, AudioFileScanner.class, 2, intent);
    }

    public AudioFileScanner() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSongRepository = SongRepository.getInstance(this);

    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (intent.hasExtra(EXTRA_SCRAPE_ROOT_FOLDER)) {
            String rootFolder = intent.getStringExtra(EXTRA_SCRAPE_ROOT_FOLDER);
            if (rootFolder != null) {
                walk(new File(rootFolder), mSongRepository::synchronizeSong);
            }
        }
    }

    private void walk(File root, AudioFileScannedCallback callback) {
        File[] folders = root.listFiles(File::isDirectory);
        File[] audioFiles = root.listFiles(new AudioFileFilter());

        if (audioFiles != null) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            for (File f : audioFiles) {
                Uri uri = Uri.fromFile(f);
                if (mSongRepository.exists(uri.getPath())) {
                    continue;
                }
                mmr.setDataSource(getApplicationContext(), uri);

                SongDto song = new SongDto();

                song.setFilepath(uri.getPath());
                song.setArtist(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                song.setTitle(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                song.setAlbum(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
                song.setDuration(Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
                song.setTrackNumber(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));

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

    private interface AudioFileScannedCallback {
        void handleSong(SongDto dto);
    }
}
