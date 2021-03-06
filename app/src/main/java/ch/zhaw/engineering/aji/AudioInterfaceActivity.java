package ch.zhaw.engineering.aji;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.aji.services.audio.AudioService;
import ch.zhaw.engineering.aji.services.audio.backend.AudioBackend;
import ch.zhaw.engineering.aji.services.audio.notification.ErrorNotificationManager;
import ch.zhaw.engineering.aji.services.audio.notification.NotificationManager;
import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.entity.Playlist;
import ch.zhaw.engineering.aji.services.database.entity.RadioStation;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.filter.EchoFilterConfigurationFragment;
import ch.zhaw.engineering.aji.ui.filter.FilterFragment;
import ch.zhaw.engineering.aji.ui.song.list.QueueSongListFragment;
import lombok.Builder;
import lombok.Value;

import static ch.zhaw.engineering.aji.services.audio.notification.NotificationManager.SHUTDOWN_INTENT;

public abstract class AudioInterfaceActivity extends AppCompatActivity implements AudioControlListener, QueueSongListFragment.QueueListFragmentListener, FilterFragment.FilterFragmentListener, EchoFilterConfigurationFragment.EchoFilterDetailsListener, ErrorNotificationManager.AlertErrorHandler {
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
    private final MediatorLiveData<List<Song>> mCurrentQueue = new MediatorLiveData<>();
    private final MutableLiveData<AudioBackend.RepeatModes> mRepeatMode = new MutableLiveData<>(AudioBackend.RepeatModes.REPEAT_OFF);
    private LiveData<List<Song>> mStoredCurrentSongs;

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
            SongDao dao = AppDatabase.getInstance(getApplication()).songDao();
            serviceBinder.getCurrentQueue().observe(AudioInterfaceActivity.this, mapCurrentQueueToSongs(dao));
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private Observer<List<Long>> mapCurrentQueueToSongs(SongDao dao) {
        return songIds -> AsyncTask.execute(() -> {
            LiveData<List<Song>> currentQueue = Transformations.map(dao.getSongsById(songIds), songMap -> {
                List<Song> queue = new ArrayList<>(songIds.size());
                for (long id : songIds) {
                    queue.add(songMap.get(id));
                }
                return queue;
            });
            runOnUiThread(() -> {
                if (mStoredCurrentSongs != null) {
                    mCurrentQueue.removeSource(mStoredCurrentSongs);
                }
                mStoredCurrentSongs = currentQueue;
                mCurrentQueue.addSource(mStoredCurrentSongs, mCurrentQueue::postValue);
            });
        });
    }

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
            if (audioService != null) {
                audioService.setAlertErrorHandler(this);
                if (startAction != null) {
                    if (startAction.getSong() != null) {
                        if (startAction.mQueue) {
                            audioService.queue(startAction.getSong());
                        } else {
                            audioService.play(startAction.getSong());
                        }
                    } else if (startAction.getPlaylist() != null) {
                        if (startAction.mQueue) {
                            audioService.queue(startAction.getPlaylist());
                        } else {
                            audioService.play(startAction.getPlaylist());
                        }
                    } else if (startAction.getRadioStation() != null) {
                        audioService.play(startAction.getRadioStation());
                    } else if (startAction.getSongs() != null) {
                        audioService.play(startAction.getSongs());
                    }
                    startAction = null;
                }
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
    protected void onResume() {
        super.onResume();
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().setAlertErrorHandler(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().setAlertErrorHandler(null);
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
    public void removeSongFromQueue(long songId, Integer position) {
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().removeSongFromQueue(songId, position);
        }
    }

    @Override
    public void onSkipToSong(int songId) {
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().skipToSong(songId);
        }
    }

    @Override
    public void modifyEchoFilter(boolean enabled, double strength, double delay) {
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().modifyFilter(AudioService.Filter.EchoFilter, enabled, strength, delay);
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
            startAction = StartPlayingAction.builder().radioStation(radioStation).build();
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
    @NonNull
    public LiveData<Boolean> getAutoQueueEnabled() {
        return mAutoQueueEnabled;
    }

    @Override
    @NonNull
    public LiveData<Boolean> getShuffleEnabled() {
        return mShuffleEnabled;
    }

    @Override
    @NonNull
    public LiveData<AudioBackend.RepeatModes> getRepeatMode() {
        return mRepeatMode;
    }

    @Override
    public void showRadioError(String text, String title, long id, int notificationId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(R.string.details, (dialog, which) -> {
                    notificationManager.cancel(notificationId);
                    dialog.dismiss();
                    runOnUiThread(() -> {
                        navigateToRadioStationForAlert(id);
                    });
                })
                .setNegativeButton(R.string.ignore, (dialog, which) -> {
                    notificationManager.cancel(notificationId);
                    dialog.dismiss();
                });

        dialogBuilder.show();
    }

    @Override
    public void showSongError(String text, String title, long id, int notificationId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(R.string.details, (dialog, which) -> {
                    notificationManager.cancel(notificationId);
                    dialog.dismiss();
                    runOnUiThread(() -> {
                        navigateToSongDetails(id);
                    });
                })
                .setNegativeButton(R.string.ignore, (dialog, which) -> {
                    notificationManager.cancel(notificationId);
                    dialog.dismiss();
                });

        dialogBuilder.show();
    }

    final void toggleShuffle() {
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().toggleShuffle();
            mAudioService.getValue().isShuffleModeEnabled(mShuffleEnabled::postValue);
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
            mAudioService.getValue().isShuffleModeEnabled(mShuffleEnabled::postValue);
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

    final void stop() {
        if (mAudioService.getValue() != null) {
            mAudioService.getValue().stop();
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

    protected abstract void navigateToRadioStationForAlert(Long radioStationId);

    protected abstract void navigateToSongDetails(long songId);

    @Value
    @Builder
    private static class StartPlayingAction {
        @Builder.Default
        Song mSong = null;
        @Builder.Default
        Playlist mPlaylist = null;
        @Builder.Default
        RadioStation mRadioStation = null;
        @Builder.Default
        boolean mQueue = false;
        @Builder.Default
        List<Song> mSongs = null;
    }
}
