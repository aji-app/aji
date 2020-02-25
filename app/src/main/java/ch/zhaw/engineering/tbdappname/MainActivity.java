package ch.zhaw.engineering.tbdappname;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.AppDatabase;
import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.services.files.AudioFileContentObserver;
import ch.zhaw.engineering.tbdappname.services.files.AudioFileScanner;
import ch.zhaw.engineering.tbdappname.services.files.StorageHelper;
import ch.zhaw.engineering.tbdappname.util.PermissionChecker;

import static ch.zhaw.engineering.tbdappname.DirectorySelectionActivity.EXTRA_FILE;
import static ch.zhaw.engineering.tbdappname.services.files.AudioFileScanner.EXTRA_SCRAPE_ROOT_FOLDER;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_DIRECTOY_SELECT = 1;

    private MutableLiveData<Boolean> mHasPermission = new MutableLiveData<>(false);
    private AudioFileContentObserver mAudioFileContentObserver = new AudioFileContentObserver(new Handler(), this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: Only use this if user did not disable this functionality
        mAudioFileContentObserver.register();

        // TODO: Only sync on startup if user did not disable this functionality
//        mAudioFileContentObserver.onChange(false);

        PermissionChecker.checkForExternalStoragePermission(this, mHasPermission);

        SongDao songDao = AppDatabase.getInstance(this).songDao();

        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            if (mHasPermission.getValue() != null && mHasPermission.getValue()) {
                StorageHelper.synchronizeMediaStoreSongs(this);
            }
        });

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(v -> {
            AsyncTask.execute(() -> {
                LiveData<List<Song>> songs = songDao.getSongs();
                runOnUiThread(() -> {
                    songs.observe(MainActivity.this, list -> {
                        Toast.makeText(this, "We've got " + list.size() + " Songs", Toast.LENGTH_SHORT).show();
                    });
                });

            });
        });

        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(v -> {
            Intent directorySelect = new Intent(this, DirectorySelectionActivity.class);
            startActivityForResult(directorySelect, REQUEST_CODE_DIRECTOY_SELECT);
        });

        Button button4 = findViewById(R.id.button4);
        button4.setOnClickListener(v -> {
            Intent intent = new Intent(this, SongListActivity.class);
            startActivity(intent);
        });

        Button button5 = findViewById(R.id.button5);
        button5.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlaylistListActivity.class);
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
    }
}
