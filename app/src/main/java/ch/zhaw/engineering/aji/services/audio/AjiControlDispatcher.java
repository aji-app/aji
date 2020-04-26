package ch.zhaw.engineering.aji.services.audio;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.Player;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AjiControlDispatcher implements ControlDispatcher {

    private final AudioService.AudioServiceBinder mBinder;

    @Override
    public boolean dispatchSetPlayWhenReady(@NonNull Player player, boolean playWhenReady) {
        if (!playWhenReady) {
            mBinder.pause();
            return true;
        } else {
            mBinder.playOrPause();
        }
        return false;
    }

    @Override
    public boolean dispatchSeekTo(@NonNull Player player, int windowIndex, long positionMs) {
        return false;
    }

    @Override
    public boolean dispatchSetRepeatMode(@NonNull Player player, int repeatMode) {
        return false;
    }

    @Override
    public boolean dispatchSetShuffleModeEnabled(@NonNull Player player, boolean shuffleModeEnabled) {
        return false;
    }

    @Override
    public boolean dispatchStop(@NonNull Player player, boolean reset) {
        mBinder.stop();

        return false;
    }
}
