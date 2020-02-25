package ch.zhaw.engineering.tbdappname.services.audio;

import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.Player;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TbdControlDispatcher implements ControlDispatcher {

    private final AudioService.AudioServiceBinder mBinder;

    @Override
    public boolean dispatchSetPlayWhenReady(Player player, boolean playWhenReady) {
        if (!playWhenReady) {
            mBinder.pause();
            return true;
        } else {
            mBinder.playOrPause();
        }
        return false;
    }

    @Override
    public boolean dispatchSeekTo(Player player, int windowIndex, long positionMs) {
        return false;
    }

    @Override
    public boolean dispatchSetRepeatMode(Player player, int repeatMode) {
        return false;
    }

    @Override
    public boolean dispatchSetShuffleModeEnabled(Player player, boolean shuffleModeEnabled) {
        return false;
    }

    @Override
    public boolean dispatchStop(Player player, boolean reset) {
        mBinder.stop();

        return false;
    }
}
