package ch.zhaw.engineering.aji;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import ch.zhaw.engineering.aji.services.audio.AudioService;
import ch.zhaw.engineering.aji.services.audio.backend.AudioBackend;

public interface AudioControlListener {
    LiveData<AudioService.PlayState> getPlayState();

    LiveData<AudioService.SongInformation> getCurrentSong();

    LiveData<Long> getCurrentPosition();

    @NonNull
    LiveData<Boolean> getAutoQueueEnabled();

    @NonNull
    LiveData<Boolean> getShuffleEnabled();

    @NonNull
    LiveData<AudioBackend.RepeatModes> getRepeatMode();
}
