package ch.zhaw.engineering.aji;

import androidx.lifecycle.LiveData;

import ch.zhaw.engineering.aji.services.audio.AudioService;
import ch.zhaw.engineering.aji.services.audio.backend.AudioBackend;

public interface AudioControlListener {
    LiveData<AudioService.PlayState> getPlayState();

    LiveData<AudioService.SongInformation> getCurrentSong();

    LiveData<Long> getCurrentPosition();

    LiveData<Boolean> getAutoQueueEnabled();

    LiveData<Boolean> getShuffleEnabled();

    LiveData<AudioBackend.RepeatModes> getRepeatMode();
}
