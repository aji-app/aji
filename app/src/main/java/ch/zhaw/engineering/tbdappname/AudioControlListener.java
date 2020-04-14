package ch.zhaw.engineering.tbdappname;

import androidx.lifecycle.LiveData;

import ch.zhaw.engineering.tbdappname.services.audio.AudioService;
import ch.zhaw.engineering.tbdappname.services.audio.backend.AudioBackend;

public interface AudioControlListener {
    LiveData<AudioService.PlayState> getPlayState();

    LiveData<AudioService.SongInformation> getCurrentSong();

    LiveData<Long> getCurrentPosition();

    LiveData<Boolean> getAutoQueueEnabled();

    LiveData<Boolean> getShuffleEnabled();

    LiveData<AudioBackend.RepeatModes> getRepeatMode();
}
