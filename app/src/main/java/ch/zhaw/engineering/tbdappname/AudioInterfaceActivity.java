package ch.zhaw.engineering.tbdappname;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import ch.zhaw.engineering.tbdappname.services.audio.AudioService;
import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import lombok.Builder;
import lombok.Value;

import static ch.zhaw.engineering.tbdappname.services.audio.NotificationManager.SHUTDOWN_INTENT;

public abstract class AudioInterfaceActivity extends AppCompatActivity {
    private static final String TAG = "AudioInterfaceActivity";
    private final static String EXTRAS_STARTED = "extras-service-started";
    private boolean mServiceStarted = false;
    protected final MutableLiveData<AudioService.AudioServiceBinder> mAudioService = new MutableLiveData<>(null);
    private boolean mBound;

    private final ServiceConnection mAudioServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mAudioService.postValue((AudioService.AudioServiceBinder) service);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private final BroadcastReceiver mShutdownBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mAudioService.getValue() != null) {
                mAudioService.getValue().shutdown();
            }
            mServiceStarted = false;
        }
    };

    private StartPlayingAction startAction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mServiceStarted = savedInstanceState.getBoolean(EXTRAS_STARTED, false);
            if (mServiceStarted) {
                bindToAudioService();
            }
        }

        mAudioService.observe(this, audioService -> {
            if (audioService != null && startAction != null) {
                if (startAction.getSong() != null) {
                    if (startAction.queue) {
                        audioService.queue(startAction.getSong());
                    } else {
                        audioService.play(startAction.getSong());
                    }
                } else if (startAction.getPlaylist() != null) {
                    if (startAction.queue) {
                        audioService.queue(startAction.getPlaylist());
                    } else {
                        audioService.play(startAction.getPlaylist());
                    }
                } else if (startAction.getRadio() != null) {
                    audioService.play(startAction.getRadio());
                }
                startAction = null;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBound && mServiceStarted) {
            bindToAudioService();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(SHUTDOWN_INTENT);
        registerReceiver(mShutdownBroadcastReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(mShutdownBroadcastReceiver);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mServiceStarted = savedInstanceState.getBoolean(EXTRAS_STARTED, false);
        if (mServiceStarted) {
            bindToAudioService();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRAS_STARTED, mServiceStarted);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mAudioServiceConnection);
        }
    }

    public void playMusic(Song song, boolean queue) {
        startService();
        if (mAudioService.getValue() != null) {
            if (queue) {
                mAudioService.getValue().queue(song);
            } else {
                mAudioService.getValue().play(song);
            }
        } else {
            startAction = StartPlayingAction.builder().song(song).queue(queue).build();
        }
        runOnUiThread(() -> {
            Toast.makeText(this, (queue ? "Queued" : "Playing") + " song: " + song.toString(), Toast.LENGTH_SHORT).show();
        });
    }

    public void playMusic(Playlist playlist, boolean queue) {
        startService();
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().play(playlist);
        } else {
            startAction = StartPlayingAction.builder().playlist(playlist).queue(queue).build();
        }
        runOnUiThread(() -> {
            Toast.makeText(this, (queue ? "Queued" : "Playing") + " playlist: " + playlist.toString(), Toast.LENGTH_SHORT).show();
        });
    }

    public void playMusic(RadioStation radioStation) {
        startService();
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().play(radioStation);
        } else {
            startAction = StartPlayingAction.builder().radio(radioStation).build();
        }
        runOnUiThread(() -> {
            Toast.makeText(this, "Playing radioStation: " + radioStation.toString(), Toast.LENGTH_SHORT).show();
        });
    }

    protected void startService() {
        if (!mServiceStarted) {
            Log.i(TAG, "Start service");
            Intent serviceIntent = new Intent(this, AudioService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
            mServiceStarted = true;
            bindToAudioService();
        }
    }

    private void bindToAudioService() {
        Intent serviceIntent = new Intent(this, AudioService.class);
        bindService(serviceIntent, mAudioServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Value
    @Builder
    private static class StartPlayingAction {
        @Builder.Default
        Song song = null;
        @Builder.Default
        Playlist playlist = null;
        @Builder.Default
        RadioStation radio = null;
        @Builder.Default
        boolean queue = false;
    }
}
