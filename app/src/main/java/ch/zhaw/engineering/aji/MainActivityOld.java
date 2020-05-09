package ch.zhaw.engineering.aji;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.aji.services.audio.webradio.RadioStationImporter;
import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.services.files.sync.AudioFileContentObserver;
import ch.zhaw.engineering.aji.services.files.AudioFileScanner;
import ch.zhaw.engineering.aji.util.PermissionChecker;

import static ch.zhaw.engineering.aji.DirectorySelectionActivity.EXTRA_FILE;
import static ch.zhaw.engineering.aji.services.files.AudioFileScanner.EXTRA_SCRAPE_ROOT_FOLDER;

public class MainActivityOld extends AppCompatActivity {
    private static final String TAG = "MainActivityOld";
    private static final int REQUEST_CODE_DIRECTOY_SELECT = 1;

    private final MutableLiveData<Boolean> mHasPermission = new MutableLiveData<>(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_old);

        PermissionChecker.checkForExternalStoragePermission(this, mHasPermission);

        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(v -> {
            Intent directorySelect = new Intent(this, DirectorySelectionActivity.class);
            startActivityForResult(directorySelect, REQUEST_CODE_DIRECTOY_SELECT);
        });

        Button button9 = findViewById(R.id.button9);
        int songCount = 20;
        button9.setOnClickListener(v -> {
            AsyncTask.execute(() -> {
                SongDao dao = AppDatabase.getInstance(this).songDao();
                List<Song> fakeSongs = new ArrayList<>(songCount);
                for (int i = 100; i < songCount + 100; i++) {
                    fakeSongs.add(Song.builder()
                            .title("Song Nr. " + i)
                            .artist("Fake Band " + (int)i / 2)
                            .album("Make it Fake 2")
                            .filepath("bubu" + i)
                            .duration((long) (1000 * (i + 1) + Math.random() * 500))
                            .rating((int) (Math.random() * songCount))
                            .deleted(false)
                            .build());
                }

                long[] ids = dao.insertSongs(fakeSongs);
            });
        });

        Button button11 = findViewById(R.id.button11);
        button11.setOnClickListener(v -> {
            PlaylistDao dao = AppDatabase.getInstance(this).playlistDao();
            dao.getPlaylists().observe(this, playlists -> {
                if (playlists.size() == 0) {
                    return;
                }
                int id = playlists.get(0).getPlaylistId();
                AsyncTask.execute(() -> {
                    for (int i = 0; i < songCount; i++) {
                        dao.addSongToPlaylist(i + 1, id);
                    }
                });
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DIRECTOY_SELECT) {
            if (data != null && data.hasExtra(EXTRA_FILE)) {
                String path = data.getStringExtra(EXTRA_FILE);
                Intent scrapeFiles = new Intent();
                scrapeFiles.putExtra(EXTRA_SCRAPE_ROOT_FOLDER, path);
                AudioFileScanner.enqueueWork(this, scrapeFiles);
            }
        }
    }
}
