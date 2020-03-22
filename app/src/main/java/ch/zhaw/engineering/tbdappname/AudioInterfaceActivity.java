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
                if (startAction.song != null) {
                    if (startAction.queue) {
                        audioService.queue(startAction.song);
                    } else {
                        audioService.play(startAction.song);
                    }
                } else if (startAction.playlist != null) {
                    audioService.play(startAction.playlist);
                } else if (startAction.station != null) {
                    audioService.play(startAction.station);
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
            startAction = new StartPlayingAction(song, null, null, queue);
        }
        Toast.makeText(this, (queue ? "Queued" : "Playing") + " song: " + song.toString(), Toast.LENGTH_SHORT).show();
    }

    public void playMusic(Playlist playlist) {
        startService();
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().play(playlist);
        } else {
            startAction = new StartPlayingAction(null, playlist, null, false);
        }
        Toast.makeText(this, "Playling playlist: " + playlist.toString(), Toast.LENGTH_SHORT).show();
    }

    public void playMusic(RadioStation radioStation) {
        startService();
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().play(radioStation);
        } else {
            startAction = new StartPlayingAction(null, null, radioStation, false);
        }
        Toast.makeText(this, "Playling radioStation: " + radioStation.toString(), Toast.LENGTH_SHORT).show();
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
    private static class StartPlayingAction {
        Song song;
        Playlist playlist;
        RadioStation station;
        boolean queue;
    }
}
