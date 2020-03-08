package ch.zhaw.engineering.tbdappname;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.services.database.repository.RadioStationRepository;
import ch.zhaw.engineering.tbdappname.services.files.WebRadioPlsParser;

import static ch.zhaw.engineering.tbdappname.DirectorySelectionActivity.EXTRA_FILE;

public class AddOrEditRadioStationActivity extends AppCompatActivity {
    public static final String EXTRA_RADIO_STATION_ID = "EXTRA_RADIOSTATION_ID";
    private static final int REQUEST_CODE_PLS_SELECT = 2;

    private RadioStationRepository mRadioStationRepository;
    private boolean isEdit = false;
    private RadioStation mRadioStation = new RadioStation();
    private Handler mHandler;
    private EditText mRadioStationName;
    private EditText mRadioStationUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_edit_radio_station);
        mRadioStationRepository = RadioStationRepository.getInstance(this);

        HandlerThread mHandlerThread = new HandlerThread("BackgroundFragmentThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        Button button7 = findViewById(R.id.select_pls_file);
        button7.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlsFileSelectionActivity.class);
            startActivityForResult(intent, REQUEST_CODE_PLS_SELECT);
        });

        FloatingActionButton fab = findViewById(R.id.save_button);
        mRadioStationName = findViewById(R.id.radiostation_name);
        mRadioStationUrl = findViewById(R.id.radiostation_url);

        fab.setEnabled(false);
        fab.setOnClickListener(v -> {
            mHandler.post(() -> {
                mRadioStation.setName(mRadioStationName.getText().toString());
                mRadioStation.setUrl(mRadioStationUrl.getText().toString());
                if (isEdit) {
                    mRadioStationRepository.update(mRadioStation);
                } else {
                    mRadioStationRepository.insert(mRadioStation);
                }
            });
            finish();
        });

        if (getIntent() != null) {
            long id = getIntent().getLongExtra(EXTRA_RADIO_STATION_ID, 0);
            if (id != 0) {
                isEdit = true;
                mHandler.post(() -> {
                    mRadioStation = mRadioStationRepository.findById(id);
                    runOnUiThread(() -> {
                        mRadioStationName.setText(mRadioStation.getName());
                        mRadioStationUrl.setText(mRadioStation.getUrl());
                    });
                });
            }
        }

        mRadioStationName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && mRadioStationUrl.getText().length() > 0) {
                    fab.setEnabled(true);
                } else {
                    fab.setEnabled(false);
                }
            }
        });

        mRadioStationUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && mRadioStationName.getText().length() > 0) {
                    fab.setEnabled(true);
                } else {
                    fab.setEnabled(false);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PLS_SELECT) {
            if (data != null && data.hasExtra(EXTRA_FILE)) {
                String path = data.getStringExtra(EXTRA_FILE);
                AsyncTask.execute(() -> {
                    mRadioStation = WebRadioPlsParser.parseSingleRadioStationFromPlsFile(path);
                    runOnUiThread(() -> {
                        mRadioStationName.setText(mRadioStation.getName());
                        mRadioStationUrl.setText(mRadioStation.getUrl());
                    });
                });
            }
        }
    }
}
