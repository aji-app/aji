package ch.zhaw.engineering.tbdappname;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.exoplayer2.Player;

import ch.zhaw.engineering.tbdappname.services.audio.AudioService;
import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;

public class CurrentlyPlayingActivity extends AudioInterfaceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currently_playing);

        ImageButton shuffle = findViewById(R.id.shuffle);
        shuffle.setEnabled(false);
        mAudioService.observe(this, service -> shuffle.setEnabled(true));


        shuffle.setOnClickListener(v -> {
            AudioService.AudioServiceBinder service = mAudioService.getValue();
            if (service == null) {
                return;
            }
            service.toggleShuffle();
            if (service.isShuffleModeEnabled()) {
                shuffle.setImageResource(R.drawable.ic_shuffle_on);
            } else {
                shuffle.setImageResource(R.drawable.ic_shuffle_off);
            }
        });

        ImageButton repeatMode = findViewById(R.id.repeat);

        repeatMode.setOnClickListener(v -> {
            AudioService.AudioServiceBinder service = mAudioService.getValue();
            if (service == null) {
                return;
            }
            service.toggleRepeatMode();
            switch (service.getRepeatMode()) {
                case REPEAT_ALL:
                    repeatMode.setImageResource(R.drawable.ic_repeat_all);
                    break;
                case REPEAT_ONE:
                    repeatMode.setImageResource(R.drawable.ic_repeat_one);
                    break;
                default:
                    repeatMode.setImageResource(R.drawable.ic_repeat_off);
                    break;
            }
        });

        Button autoQueue = findViewById(R.id.auto_queue);
        autoQueue.setOnClickListener(v -> {
            AudioService.AudioServiceBinder service = mAudioService.getValue();
            if (service == null) {
                return;
            }
            if (service.toggleAutoQueue()) {
                autoQueue.setText("Auto-Queue On");
            } else {

                autoQueue.setText("Auto-Queue Off");
            }
        });

        Button webRadioTest = findViewById(R.id.web_radio_test);
        webRadioTest.setOnClickListener(v -> {
            AudioService.AudioServiceBinder service = mAudioService.getValue();
            if (service == null) {
                return;
            }
            service.play(new RadioStation(1, "Fake Radio Station", "http://us5.internet-radio.com:8267/stream"));
        });

        startService();

    }
}
