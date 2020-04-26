package ch.zhaw.engineering.tbdappname.services.audio;

import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AjiMediaSessionCallback extends MediaSessionCompat.Callback {
    private final static String TAG = "AjiMediaSessionCallback";
    private final AudioService.AudioServiceBinder mBinder;

    @Override
    public void onPause() {
        Log.i(TAG, "Media Session on Pause");
        super.onPause();
        mBinder.pause();
    }

    @Override
    public void onPlay() {
        Log.i(TAG, "Media Session on Play");
        super.onPlay();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "Media Session on Stop");
        super.onStop();
        mBinder.stop();
    }
}
