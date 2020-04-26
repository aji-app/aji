package ch.zhaw.engineering.aji;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import ch.zhaw.engineering.aji.services.audio.AudioService;
import ch.zhaw.engineering.aji.services.audio.backend.AudioBackend;
import ch.zhaw.engineering.aji.services.database.entity.Playlist;
import ch.zhaw.engineering.aji.services.database.entity.RadioStation;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.song.list.QueueSongListFragment;
import lombok.Builder;
import lombok.Value;

import static ch.zhaw.engineering.aji.services.audio.NotificationManager.SHUTDOWN_INTENT;
import static java.util.Collections.emptyList;

public abstract class AudioInterfaceActivity extends AppCompatActivity implements AudioControlListener, QueueSongListFragment.QueueListFragmentListener {
    private static final String TAG = "AudioInterfaceActivity";
    private final static String EXTRAS_STARTED = "extras-service-started";
    private boolean mServiceStarted = false;
    final MutableLiveData<AudioService.AudioServiceBinder> mAudioService = new MutableLiveData<>(null);
    private boolean mBound;

    private final MutableLiveData<AudioService.PlayState> mCurrentState = new MutableLiveData<>(AudioService.PlayState.INITIAL);
    private final MutableLiveData<Long> mCurrentPosition = new MutableLiveData<>(0L);
    private final MutableLiveData<AudioService.SongInformation> mCurrentSong = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> mShuffleEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> mAutoQueueEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<List<Song>> mCurrentQueue = new MutableLiveData<>(emptyList());
    private final MutableLiveData<AudioBackend.RepeatModes> mRepeatMode = new MutableLiveData<>(AudioBackend.RepeatModes.REPEAT_OFF);


    private final ServiceConnection mAudioServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AudioService.AudioServiceBinder serviceBinder = (AudioService.AudioServiceBinder) service;
            mAudioService.postValue(serviceBinder);
            serviceBinder.getCurrentSong().observe(AudioInterfaceActivity.this, mCurrentSong::setValue);
            serviceBinder.getPlayState().observe(AudioInterfaceActivity.this, mCurrentState::setValue);
            serviceBinder.getCurrentPosition().observe(AudioInterfaceActivity.this, mCurrentPosition::setValue);
            serviceBinder.getCurrentQueue().observe(AudioInterfaceActivity.this, mCurrentQueue::setValue);
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
                } else if (startAction.getSongs() != null) {
                    audioService.play(startAction.getSongs());
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
        if (!mServiceStarted) {
            startService();
        }
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

    @Override
    public void removeSongFromQueue(long songId) {
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().removeSongFromQueue(songId);
        }
    }

    final void playMusic(Song song, boolean queue) {
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
        Log.i(TAG, (queue ? "Queued" : "Playing") + " song: " + song.toString());
    }

    final void playMusic(Playlist playlist, boolean queue) {
        startService();
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().play(playlist);
        } else {
            startAction = StartPlayingAction.builder().playlist(playlist).queue(queue).build();
        }
        Log.i(TAG, (queue ? "Queued" : "Playing") + " playlist: " + playlist.toString());
    }

    final void playMusic(RadioStation radioStation) {
        startService();
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().play(radioStation);
        } else {
            startAction = StartPlayingAction.builder().radio(radioStation).build();
        }
        Log.i(TAG, "Playing radioStation: " + radioStation.toString());
    }

    final void playMusic(List<Song> songs, boolean queue) {
        startService();
        if (mAudioService.getValue() != null) {
            if (queue) {
                mAudioService.getValue().queue(songs);
            } else {
                mAudioService.getValue().play(songs);

            }
        } else {
            startAction = StartPlayingAction.builder().songs(songs).build();
        }
        Log.i(TAG, "Playing songs: " + songs.size());
    }

    @Override
    public LiveData<AudioService.PlayState> getPlayState() {
        return mCurrentState;
    }

    @Override
    public LiveData<AudioService.SongInformation> getCurrentSong() {
        return mCurrentSong;
    }

    @Override
    public LiveData<Long> getCurrentPosition() {
        return mCurrentPosition;
    }

    @Override
    public LiveData<Boolean> getAutoQueueEnabled() {
        return mAutoQueueEnabled;
    }

    @Override
    public LiveData<Boolean> getShuffleEnabled() {
        return mShuffleEnabled;
    }

    @Override
    public LiveData<AudioBackend.RepeatModes> getRepeatMode() {
        return mRepeatMode;
    }


    final void toggleShuffle() {
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().toggleShuffle();
            mShuffleEnabled.postValue(mAudioService.getValue().isShuffleModeEnabled());
        }
        Log.i(TAG, "onToggleShuffle: " + mShuffleEnabled.getValue());
    }

    final void toggleAutoQueue() {
        if (mAudioService.getValue() != null) {
            mAutoQueueEnabled.postValue(mAudioService.getValue().toggleAutoQueue());
        }
        Log.i(TAG, "onToggleAutoQueue: " + mAutoQueueEnabled.getValue());
    }

    final void seekTo(long position) {
        if (mAudioService.getValue() != null) {
           mAudioService.getValue().seekTo(position);
        }
    }

    final void toggleRepeatMode() {
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().toggleRepeatMode();
            mRepeatMode.postValue(mAudioService.getValue().getRepeatMode());
        }
        Log.i(TAG, "onChangeRepeatMode: " + mRepeatMode.getValue());
    }

    final void next() {
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().next();
        }
    }

    final void previous() {
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().previous();
        }
    }

    final void playPause() {
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().playOrPause();
        }
    }

    private void startService() {
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

    public LiveData<List<Song>> getCurrentQueue() {
        return mCurrentQueue;
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
        @Builder.Default
        List<Song> songs = null;
    }
}