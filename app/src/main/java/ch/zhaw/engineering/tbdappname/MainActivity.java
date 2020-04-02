package ch.zhaw.engineering.tbdappname;

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

import ch.zhaw.engineering.tbdappname.services.audio.webradio.RadioStationImporter;
import ch.zhaw.engineering.tbdappname.services.database.AppDatabase;
import ch.zhaw.engineering.tbdappname.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.services.files.AudioFileContentObserver;
import ch.zhaw.engineering.tbdappname.services.files.AudioFileScanner;
import ch.zhaw.engineering.tbdappname.services.files.StorageHelper;
import ch.zhaw.engineering.tbdappname.services.files.WebRadioPlsParser;
import ch.zhaw.engineering.tbdappname.util.PermissionChecker;

import static ch.zhaw.engineering.tbdappname.DirectorySelectionActivity.EXTRA_FILE;
import static ch.zhaw.engineering.tbdappname.services.files.AudioFileScanner.EXTRA_SCRAPE_ROOT_FOLDER;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_DIRECTOY_SELECT = 1;
    private static final int REQUEST_CODE_PLS_SELECT = 2;

    private final MutableLiveData<Boolean> mHasPermission = new MutableLiveData<>(false);
    private final AudioFileContentObserver mAudioFileContentObserver = new AudioFileContentObserver(new Handler(), this);
    private RadioStationDao mRadioStationDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: Only use this if user did not disable this functionality
        mAudioFileContentObserver.register();

        // TODO: Only sync on startup if user did not disable this functionality
//        mAudioFileContentObserver.onChange(false);

        PermissionChecker.checkForExternalStoragePermission(this, mHasPermission);

        RadioStationImporter.loadDefaultRadioStations(this);
        mRadioStationDao = AppDatabase.getInstance(this).radioStationDao();

        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            if (mHasPermission.getValue() != null && mHasPermission.getValue()) {
                StorageHelper.synchronizeMediaStoreSongs(this);
            }
        });

        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(v -> {
            Intent directorySelect = new Intent(this, DirectorySelectionActivity.class);
            startActivityForResult(directorySelect, REQUEST_CODE_DIRECTOY_SELECT);
        });

        Button button6 = findViewById(R.id.button6);
        button6.setOnClickListener(v -> {
            Intent intent = new Intent(this, CurrentlyPlayingActivity.class);
            startActivity(intent);
        });

        Button button7 = findViewById(R.id.button7);
        button7.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlsFileSelectionActivity.class);
            startActivityForResult(intent, REQUEST_CODE_PLS_SELECT);
        });

        Button button8 = findViewById(R.id.button8);
        button8.setOnClickListener(v -> {
            Intent intent = new Intent(this, RadioStationListActivity.class);
            startActivity(intent);
        });

        Button button9 = findViewById(R.id.button9);
        button9.setOnClickListener(v -> {
            AsyncTask.execute(() -> {
                SongDao dao = AppDatabase.getInstance(this).songDao();
                List<Song> fakeSongs = new ArrayList<>(10);
                for (int i = 0; i < 10; i++) {
                    fakeSongs.add(Song.builder()
                            .title("Song Nr. " + i)
                            .artist("Fake Band " + i)
                            .album("Make it Fake " + i)
                            .filepath("bubu" + i)
                            .duration((long) (1000 * (i + 1) + Math.random() * 500))
                            .rating((int) (Math.random() * 10))
                            .deleted(false)
                            .build());
                }

                long[] ids = dao.insertSongs(fakeSongs);
            });
        });

        Button button10 = findViewById(R.id.button10);
        button10.setOnClickListener(v -> {
            Intent intent = new Intent(this, TestActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioFileContentObserver.unregister();
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
        if (requestCode == REQUEST_CODE_PLS_SELECT) {
            if (data != null && data.hasExtra(EXTRA_FILE)) {
                String path = data.getStringExtra(EXTRA_FILE);
                AsyncTask.execute(() -> {
                    RadioStation station = WebRadioPlsParser.parseSingleRadioStationFromPlsFile(path);
                    if (station.getName() != null && station.getUrl() != null) {
                        mRadioStationDao.insertRadioStation(station);
                    }
                });
            }
        }
    }
}
