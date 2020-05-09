package ch.zhaw.engineering.aji.services.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.services.audio.backend.AudioBackend;
import ch.zhaw.engineering.aji.services.audio.exoplayer.ExoPlayerAudioBackend;
import ch.zhaw.engineering.aji.services.audio.notification.ErrorNotificationManager;
import ch.zhaw.engineering.aji.services.audio.notification.NotificationManager;
import ch.zhaw.engineering.aji.services.audio.webradio.RadioStationMetadataRunnable;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.entity.Playlist;
import ch.zhaw.engineering.aji.services.database.entity.RadioStation;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static java.util.Collections.emptyList;

public class AudioService extends LifecycleService {
    public static final String EXTRAS_COMMAND = "extra-code";
    private final static String TAG = "AudioService";

    private final AudioBackend mAudioBackend = new ExoPlayerAudioBackend();

    private final AudioServiceBinder mBinder = new AudioServiceBinder();

    private final MutableLiveData<PlayState> mCurrentState = new MutableLiveData<>(PlayState.INITIAL);
    private final MutableLiveData<Long> mCurrentPosition = new MutableLiveData<>(0L);
    private final MutableLiveData<SongInformation> mCurrentSong = new MutableLiveData<>(null);
    private final MutableLiveData<List<Long>> mCurrentQueue = new MutableLiveData<>(emptyList());
    private String mCurrentPlaylistName = null;

    private Handler mCurrentPositionTrackingThread;
    private Handler mRadioStationInfoThread;
    private NotificationManager mNotificationManager;
    private ErrorNotificationManager mErrorNotificationManager;

    private final Map<Long, Song> mCurrentSongs = new LinkedHashMap<>();
    private final Map<Long, RadioStation> mCurrentRadioStations = new HashMap<>();
    private MediaSessionCompat mMediaSession;

    private final IntentFilter mNoisyAudioIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BroadcastReceiver mNoisyAudioStreamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                // Pause the playback
                mBinder.pause();
            }
        }
    };

    private AudioBackend.RepeatModes mCurrentRepeatMode = AudioBackend.RepeatModes.REPEAT_OFF;
    private SongDao mSongRepository;
    private boolean mAutoQueueRandomTrack = false;
    private Song mAutoQueueSong;
    private boolean mTrackingPosition;
    private RadioStationMetadataRunnable mUpdateSongInfoRunnable;

    public AudioService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = new NotificationManager(this, mCurrentSong, mCurrentPosition, mCurrentState);
        mErrorNotificationManager = new ErrorNotificationManager(this);

        setupBackgroundThreads();
        setupMediaSession();

        mSongRepository = SongDao.getInstance(getApplication());
        mAudioBackend.initialize(
                this,
                mMediaSession,
                new AudioBackend.EventListener() {

                    @Override
                    public void onStartedPlaying() {
                        updateCurrentSong();
                    }

                    @Override
                    public void onStoppedPlaying() {
                        if (mAutoQueueRandomTrack && mAutoQueueSong != null) {
                            mBinder.next();
                        } else {
                            mBinder.stop();
                        }
                    }

                    @Override
                    public void onPausedPlaying() {
                        mBinder.pause();
                    }

                    @Override
                    public void onPositionDiscontinuity() {
                        updateCurrentSong();
                    }

                    @Override
                    public void onError(Object tag) {
                        if (tag instanceof Long) {
                            if (mCurrentSongs.containsKey(tag)) {
                                Song errorSong = mCurrentSongs.get(tag);
                                mErrorNotificationManager.notifyError(errorSong);
                            } else {
                                RadioStation errorStation = mCurrentRadioStations.get(tag);
                                mErrorNotificationManager.notifyError(errorStation);
                            }
                        }
                        Log.i(TAG, "ERROR");
                    }
                });

        mNotificationManager.start();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        int value = super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "AudioService.onStartCommand");
        handleStartIntent(intent);
        return value;
    }

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        super.onBind(intent);
        return mBinder;
    }

    private void setupMediaSession() {
        mMediaSession = new MediaSessionCompat(this, "AJI_APP_MEDIA_SESSION");
        mMediaSession.setActive(true);
        mMediaSession.setCallback(new AjiMediaSessionCallback(mBinder));
    }

    private void setupBackgroundThreads() {
        HandlerThread audioServicePositionTrackingThread = new HandlerThread("AudioServicePositionTrackingThread");
        audioServicePositionTrackingThread.start();
        mCurrentPositionTrackingThread = new Handler(audioServicePositionTrackingThread.getLooper());

        HandlerThread audioServiceRadioStationUpdateThread = new HandlerThread("AudioServiceRadioStationUpdateThread");
        audioServiceRadioStationUpdateThread.start();
        mRadioStationInfoThread = new Handler(audioServiceRadioStationUpdateThread.getLooper());
    }

    private void updateCurrentSong() {
        if (PlayState.STOPPED != mCurrentState.getValue()) {
            mAudioBackend.getCurrentTag(tag -> {
                if (tag != null) {
                    Song song = mCurrentSongs.get(tag);
                    if (song != null) {
                        mCurrentSong.postValue(SongInformation.fromSong(song, mCurrentPlaylistName));
                    } else {
                        RadioStation station = mCurrentRadioStations.get(tag);
                        if (station != null) {
                            SongInformation baseInfo = SongInformation.fromRadioStation(station);
                            mCurrentSong.postValue(baseInfo);
                            try {
                                mUpdateSongInfoRunnable = new RadioStationMetadataRunnable((String title, String artist, boolean hasError) -> {
                                    if (title == null || title.equals("")) {
                                        title = getApplicationContext().getResources().getString(R.string.unknown);
                                    }
                                    if (artist == null || artist.equals("")) {
                                        artist = getApplicationContext().getResources().getString(R.string.unknown);
                                    }
                                    if (mCurrentSong.getValue() != null && PlayState.PAUSED != mCurrentState.getValue()) {
                                        SongInformation nextInfo = baseInfo.toBuilder().artist(artist).title(title).build();
                                        if (!mCurrentSong.getValue().equals(nextInfo)) {
                                            mCurrentSong.postValue(nextInfo);
                                        }
                                    }
                                }, station.getUrl());

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }

    private void handleStartIntent(@Nullable Intent intent) {
        if (intent != null && intent.hasExtra(EXTRAS_COMMAND)) {
            AudioServiceCommand command = (AudioServiceCommand) intent.getSerializableExtra(EXTRAS_COMMAND);
            if (command != null) {
                Log.i(TAG, "Command: " + command.toString());
                switch (command) {
                    case PLAYPAUSE:
                        mBinder.playOrPause();
                        break;
                    case STOP:
                        mBinder.stop();
                        break;
                    case NEXT:
                        mBinder.next();
                        break;
                    case PREVIOUS:
                        mBinder.previous();
                        break;
                    case SHUTDOWN:
                        mBinder.shutdown();
                        break;
                }
            }
        }
    }

    private void resetAutoQueue() {
        mAutoQueueSong = null;
    }

    private void updateNextSong() {
        mCurrentPositionTrackingThread.post(() -> {
            mAutoQueueSong = mSongRepository.getRandomSong();
        });
    }

    private void trackPosition() {
        mRadioStationInfoThread.removeCallbacksAndMessages(null);
        if (!mTrackingPosition) {
            trackPositionDelayed();
            mTrackingPosition = true;
        }

    }

    private void trackPositionDelayed() {
        SongInformation currentSongInfo = mCurrentSong.getValue();
        if (currentSongInfo != null && currentSongInfo.isRadio() && mUpdateSongInfoRunnable != null) {
            mRadioStationInfoThread.post(mUpdateSongInfoRunnable);
        }
        mCurrentPositionTrackingThread.postDelayed(() -> {
            mAudioBackend.getCurrentPosition(position -> {
                if (!position.equals(mCurrentPosition.getValue())) {
                    mCurrentPosition.postValue(position);
                }
                trackPositionDelayed();
            });
        }, 500);

    }

    private AudioBackend.RepeatModes nextRepeatMode() {
        switch (mCurrentRepeatMode) {
            case REPEAT_ALL:
                return AudioBackend.RepeatModes.REPEAT_ONE;
            case REPEAT_ONE:
                return AudioBackend.RepeatModes.REPEAT_OFF;
            default:
                return AudioBackend.RepeatModes.REPEAT_ALL;
        }
    }

    private void playbackControlPause() {
        mCurrentState.postValue(PlayState.PAUSED);
        mAudioBackend.pause();
    }

    private void playbackControlStop() {
        mCurrentState.postValue(PlayState.STOPPED);
        mCurrentSong.postValue(null);
        mAudioBackend.stop();
        clearQueue();
        try {
            unregisterReceiver(mNoisyAudioStreamReceiver);
        } catch (IllegalArgumentException e) {
            // Was not registered
        }
    }

    private void playbackControlPauseOrPlay() {
        boolean isStopped = (PlayState.STOPPED == mCurrentState.getValue() || PlayState.INITIAL == mCurrentState.getValue());
        boolean stoppedButSongsQueued = isStopped && mCurrentQueue.getValue() != null && mCurrentQueue.getValue().size() > 0;
        if (PlayState.PAUSED == mCurrentState.getValue()) {
            mCurrentState.postValue(PlayState.PLAYING);
            mAudioBackend.play(false);
            mNotificationManager.start();
        } else if (stoppedButSongsQueued) {
            playbackControlPlay();
        } else {
            playbackControlPause();
        }
    }

    private void playbackControlPlay() {
        mCurrentState.postValue(PlayState.PLAYING);
        mAudioBackend.play(true);
        mUpdateSongInfoRunnable = null;
        trackPosition();
        registerReceiver(mNoisyAudioStreamReceiver, mNoisyAudioIntentFilter);
    }

    private void playbackControlQueueSong(Song song) {
        SongMedia media = new SongMedia(song);
        mAudioBackend.queueFile(media);
        addSongToCurrentSongs(song);
    }

    private void addSongToCurrentSongs(Song song) {
        mCurrentSongs.put(song.getSongId(), song);
        List<Long> songs = new ArrayList<>(mCurrentSongs.size());
        for (Song s : mCurrentSongs.values()) {
            songs.add(s.getSongId());
        }
        mCurrentQueue.postValue(songs);
    }

    private void playbackControlPlayRadioStation(RadioStation station) {
        clearQueue();
        mCurrentRadioStations.put(station.getId(), station);
        mAudioBackend.queueWebMedia(new RadioStationMedia(station));
        playbackControlPlay();
    }

    private void playbackControlPlayPlaylist(Playlist playlist) {
        mCurrentPositionTrackingThread.post(() -> {
            clearQueue();

            List<Song> songsForPlaylist = mSongRepository.getSongsForPlaylistAsList(playlist.getPlaylistId());
            playbackControlQueueSongs(songsForPlaylist);
            playbackControlPlay();
            mCurrentPlaylistName = playlist.getName();
        });
        registerReceiver(mNoisyAudioStreamReceiver, mNoisyAudioIntentFilter);
    }

    private void playbackControlPlaySongs(List<Song> songs) {
        clearQueue();
        mCurrentQueue.postValue(emptyList());
        playbackControlQueueSongs(songs);
        playbackControlPlay();
    }

    private void playbackControlQueueSongs(List<Song> songs) {
        for (Song song : songs) {
            playbackControlQueueSong(song);
        }
    }

    private void playbackControlPlaySong(Song song) {
        clearQueue();
        playbackControlQueueSong(song);
        playbackControlPlay();
    }

    private void playbackControlNext() {
        if (mAutoQueueRandomTrack) {

            mAudioBackend.next(new SongMedia(mAutoQueueSong), didQueueSong -> {
                if (didQueueSong) {
                    addSongToCurrentSongs(mAutoQueueSong);
                    updateCurrentSong();
                    updateNextSong();
                }
            });
        } else {
            mAudioBackend.next();
        }
        Log.i(TAG, "Next");
    }

    private void playbackControlPrevious() {
        mAudioBackend.previous();
        Log.i(TAG, "Previous");
    }

    private void playbackControlShutdown() {
        mAudioBackend.stop();
        Log.i(TAG, "Shutdown AudioService");
        mNotificationManager.cancel();
        mCurrentQueue.postValue(emptyList());
        stopSelf();
    }

    private void playbackControlToggleShuffleModeEnabled() {
        mAudioBackend.toggleShuffleModeEnabled();
    }

    private void playbackControlLoopThroughRepeatMode() {
        mCurrentRepeatMode = nextRepeatMode();
        mAudioBackend.setRepeatMode(mCurrentRepeatMode);
    }

    private boolean playbackControlToggleAutoQueue() {
        mAutoQueueRandomTrack = !mAutoQueueRandomTrack;
        if (mAutoQueueRandomTrack && mAutoQueueSong == null) {
            updateNextSong();
        }
        return mAutoQueueRandomTrack;
    }

    private void playbackControlSeekTo(long position) {
        mAudioBackend.seekTo(position);
    }

    private void playbackControlSkipToSong(long songId) {
        if (mCurrentQueue.getValue() != null) {
            int index = mCurrentQueue.getValue().indexOf(songId);
            mAudioBackend.skipToMedia(index);
        }
    }

    private void playbackControlRemoveSongFromQueue(long songId) {
        Song song = mCurrentSongs.remove(songId);
        if (song != null) {
            SongMedia media = new SongMedia(song);
            mAudioBackend.removeSongFromQueue(media);
        }
    }

    private LiveData<List<Long>> playbackControlGetQueue() {
        return mCurrentQueue;
    }

    private void clearQueue() {
        mAudioBackend.clear();
        mCurrentQueue.postValue(emptyList());
        mCurrentSongs.clear();
        mCurrentRadioStations.clear();
    }

    public class AudioServiceBinder extends Binder {

        public LiveData<List<Long>> getCurrentQueue() {
            return playbackControlGetQueue();
        }

        public void pause() {
            playbackControlPause();
        }

        public void stop() {
            playbackControlStop();
        }

        public void playOrPause() {
            playbackControlPauseOrPlay();
        }

        public void play() {
            playbackControlPlay();
        }

        public void play(RadioStation station) {
            playbackControlPlayRadioStation(station);
        }

        public void play(Playlist playlist) {
            playbackControlPlayPlaylist(playlist);
        }

        public void queue(Song song) {
            playbackControlQueueSong(song);
        }

        public void queue(Playlist playlist) {
            // TODO: Queue playlist, how to handle name?
            playbackControlPlayPlaylist(playlist);
        }

        public void play(Song song) {
            playbackControlPlaySong(song);
        }

        public void play(List<Song> songs) {
            playbackControlPlaySongs(songs);
        }

        public void queue(List<Song> songs) {
            playbackControlQueueSongs(songs);
        }

        public void next() {
            playbackControlNext();
        }

        public void previous() {
            playbackControlPrevious();
        }

        public void shutdown() {
            playbackControlShutdown();
        }

        public void toggleShuffle() {
            playbackControlToggleShuffleModeEnabled();
        }

        public void isShuffleModeEnabled(AudioBackend.Callback<Boolean> callback) {
            mAudioBackend.getShuffleModeEnabled(callback);
        }

        public void toggleRepeatMode() {
            playbackControlLoopThroughRepeatMode();
        }

        public AudioBackend.RepeatModes getRepeatMode() {
            return mCurrentRepeatMode;
        }

        public boolean toggleAutoQueue() {
            return playbackControlToggleAutoQueue();
        }

        public LiveData<PlayState> getPlayState() {
            return mCurrentState;
        }

        public LiveData<SongInformation> getCurrentSong() {
            return mCurrentSong;
        }

        public LiveData<Long> getCurrentPosition() {
            return mCurrentPosition;
        }

        public void seekTo(long position) {
            playbackControlSeekTo(position);
        }

        public void removeSongFromQueue(long songId) {
            playbackControlRemoveSongFromQueue(songId);
        }

        public void skipToSong(long songId) {
            playbackControlSkipToSong(songId);
        }
    }

    public enum PlayState {
        STOPPED, PLAYING, PAUSED, INITIAL
    }

    public enum AudioServiceCommand {
        PLAYPAUSE, STOP, NEXT, PREVIOUS, SHUTDOWN
    }

    private static class SongMedia implements AudioBackend.Media {
        private final long tag;
        private final String filepath;

        SongMedia(Song song) {
            this.tag = song.getSongId();
            this.filepath = song.getFilepath();
        }

        @Override
        public Object getTag() {
            return tag;
        }

        @Override
        public String getPath() {
            return filepath;
        }

    }

    private static class RadioStationMedia implements AudioBackend.WebMedia {
        private final long tag;
        private final String url;

        RadioStationMedia(RadioStation station) {
            this.tag = station.getId();
            this.url = station.getUrl();
        }

        @Override
        public Object getTag() {
            return tag;
        }

        @Override
        public String getUrl() {
            return url;
        }

    }

    @Value
    @Builder(toBuilder = true)
    @AllArgsConstructor
    public static class SongInformation {
        long id;
        String title;
        String artist;
        String album;
        String playlistName;
        String path;
        long duration;
        boolean isRadio;
        boolean isFavorite;
        String albumPath;

        static SongInformation fromSong(Song song, String playlistName) {
            return new SongInformation(song.getSongId(), song.getTitle(), song.getArtist(), song.getAlbum(), playlistName, song.getFilepath(), song.getDuration(), false, song.isFavorite(), song.getAlbumArtPath());
        }

        static SongInformation fromRadioStation(RadioStation station) {
            return new SongInformation(station.getId(), "", "", "", station.getName(), station.getUrl(), 0, true, false, null);
        }
    }


}
