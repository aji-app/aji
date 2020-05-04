package ch.zhaw.engineering.aji;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;

import ch.zhaw.engineering.aji.services.audio.AudioService;
import ch.zhaw.engineering.aji.services.audio.backend.AudioBackend;
import ch.zhaw.engineering.aji.services.database.entity.Song;

public interface AudioControlListener {
    LiveData<AudioService.PlayState> getPlayState();

    LiveData<AudioService.SongInformation> getCurrentSong();

    LiveData<Long> getCurrentPosition();


    LiveData<List<Song>> getCurrentQueue();

    @NonNull
    LiveData<Boolean> getAutoQueueEnabled();

    @NonNull
    LiveData<Boolean> getShuffleEnabled();

    @NonNull
    LiveData<AudioBackend.RepeatModes> getRepeatMode();
}
