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
import ch.zhaw.engineering.aji.services.audio.filter.EchoFilter;
import ch.zhaw.engineering.aji.services.audio.notification.ErrorNotificationManager;
import ch.zhaw.engineering.aji.services.audio.notification.NotificationManager;
import ch.zhaw.engineering.aji.services.audio.webradio.RadioStationMetadataRunnable;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.entity.Playlist;
import ch.zhaw.engineering.aji.services.database.entity.RadioStation;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.util.PreferenceHelper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static ch.zhaw.engineering.aji.ui.filter.EchoFilterConfigurationFragment.DELAY_DEFAULT;
import static ch.zhaw.engineering.aji.ui.filter.EchoFilterConfigurationFragment.DELAY_KEY;
import static ch.zhaw.engineering.aji.ui.filter.EchoFilterConfigurationFragment.STRENGTH_DEFAULT;
import static ch.zhaw.engineering.aji.ui.filter.EchoFilterConfigurationFragment.STRENGTH_KEY;

public class AudioService extends LifecycleService {
    public static final String EXTRAS_COMMAND = "extra-code";
    private final static String TAG = "AjiAudioService";

    private final AudioBackend mAudioBackend = new ExoPlayerAudioBackend();

    private final AudioServiceBinder mBinder = new AudioServiceBinder();

    private final MutableLiveData<PlayState> mCurrentState = new MutableLiveData<>(PlayState.INITIAL);
    private final MutableLiveData<Long> mCurrentPosition = new MutableLiveData<>(0L);
    private final MutableLiveData<SongInformation> mCurrentSong = new MutableLiveData<>(null);
    private final MutableLiveData<List<Long>> mCurrentQueue = new MutableLiveData<>(new ArrayList<>(0));
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
    private Map<Filter, AudioBackend.AudioFilter> filters = new HashMap<>();

    public AudioService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = new NotificationManager(this, mCurrentSong, mCurrentPosition, mCurrentState);
        mErrorNotificationManager = new ErrorNotificationManager(this);

        setupBackgroundThreads();
        setupMediaSession();

        PreferenceHelper preferenceHelper = new PreferenceHelper(this);
        double delay = preferenceHelper.getFilterValue(Filter.EchoFilter, DELAY_KEY, DELAY_DEFAULT);
        double strength = preferenceHelper.getFilterValue(Filter.EchoFilter, STRENGTH_KEY, STRENGTH_DEFAULT);

        EchoFilter filter = new EchoFilter(1, preferenceHelper.isFilterEnbaled(Filter.EchoFilter), strength, delay);
        filters.put(Filter.EchoFilter, filter);
        AudioBackend.AudioFilter[] filters = new AudioBackend.AudioFilter[]{
                filter
        };

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
                        if (tag instanceof SongTag) {
                            SongTag actualTag = (SongTag) tag;
                            if (mCurrentSongs.containsKey(actualTag.getSongId())) {
                                Song errorSong = mCurrentSongs.get(actualTag.getSongId());
                                mErrorNotificationManager.notifyError(errorSong);
                            }
                        } else {
                            RadioStation errorStation = mCurrentRadioStations.get(tag);
                            mErrorNotificationManager.notifyError(errorStation);
                        }

                        Log.i(TAG, "ERROR");
                    }
                }
                , filters
        );

        mNotificationManager.start();
    }

    private void modifyFilter(Filter filter, boolean enabled, double... params) {
        if (filters.containsKey(filter)) {
            filters.get(filter).modify(enabled, params);
        }
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
            mAudioBackend.getCurrentSongInfo(songInfo -> {
                Object tag = songInfo.getTag();
                if (tag instanceof SongTag) {
                    SongTag actualTag = (SongTag) tag;
                    Song song = mCurrentSongs.get(actualTag.getSongId());
                    if (song != null) {
                        mCurrentSong.postValue(SongInformation.fromSong(song, mCurrentPlaylistName, songInfo.getPosition()));
                    }
                } else if (tag != null) {
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
        SongMedia media = new SongMedia(song, mCurrentQueue.getValue().size());
        mAudioBackend.queueFile(media);
        addSongToCurrentSongs(media.getTag(), song);
    }

    private void addSongToCurrentSongs(SongTag tag, Song song) {
        mCurrentSongs.put(song.getSongId(), song);
        List<Long> currentQueue = mCurrentQueue.getValue();
        currentQueue.add(song.getSongId());
        mCurrentQueue.postValue(currentQueue);
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
        mCurrentQueue.postValue(new ArrayList<>());
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
            SongMedia songMedia = new SongMedia(mAutoQueueSong, mCurrentQueue.getValue().size());
            mAudioBackend.next(songMedia, didQueueSong -> {
                if (didQueueSong) {
                    addSongToCurrentSongs(songMedia.getTag(), mAutoQueueSong);
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
        mCurrentQueue.postValue(new ArrayList<>());
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

    private void playbackControlSkipToSongAtPosition(int position) {
        if (mCurrentQueue.getValue() != null) {
            mAudioBackend.skipToMedia(position);
        }
    }

    private void playbackControlRemoveSongFromQueue(long songId, Integer position) {
        Song song;
        if (position == null) {
            song = mCurrentSongs.remove(songId);
        } else {
            song = mCurrentSongs.get(songId);
        }
        if (song != null) {
            if (position != null) {
                List<Long> currentQueue = mCurrentQueue.getValue();
                mAudioBackend.removeSongFromQueueByPosition(position);
                if (currentQueue != null) {
                    currentQueue.remove((int) position);
                }
            } else {
                List<Long> currentQueue = mCurrentQueue.getValue();
                List<Long> updatedQueue = new ArrayList<>();
                List<Integer> positionsToRemove = new ArrayList<>();
                if (currentQueue != null) {
                    for (int i = 0; i < currentQueue.size(); i++) {
                        long id = currentQueue.get(i);
                        if (id != songId) {
                            updatedQueue.add(id);
                        } else {
                            positionsToRemove.add(i);
                        }
                    }
                }
                mCurrentQueue.postValue(updatedQueue);
                for (int i = positionsToRemove.size() - 1; i >= 0; i--) {
                    mAudioBackend.removeSongFromQueueByPosition(positionsToRemove.get(i));
                }
            }
            updateCurrentSong();
        }
    }

    private LiveData<List<Long>> playbackControlGetQueue() {
        return mCurrentQueue;
    }

    private void clearQueue() {
        mAudioBackend.clear();
        mCurrentQueue.postValue(new ArrayList<>());
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

        public void removeSongFromQueue(long songId, Integer position) {
            playbackControlRemoveSongFromQueue(songId, position);
        }

        public void skipToSong(int position) {
            playbackControlSkipToSongAtPosition(position);
        }

        public void modifyFilter(Filter filter, boolean enabled, double... parameters) {
            AudioService.this.modifyFilter(filter, enabled, parameters);
        }

        public void setAlertErrorHandler(ErrorNotificationManager.AlertErrorHandler errorHandler) {
            mErrorNotificationManager.setErrorHandler(errorHandler);
        }
    }

    public enum PlayState {
        STOPPED, PLAYING, PAUSED, INITIAL
    }

    public enum AudioServiceCommand {
        PLAYPAUSE, STOP, NEXT, PREVIOUS, SHUTDOWN
    }

    private static class SongMedia implements AudioBackend.Media {
        private final SongTag tag;
        private final String filepath;

        SongMedia(Song song, int position) {
            this.tag = new SongTag(song.getSongId(), position);
            this.filepath = song.getFilepath();
        }

        @Override
        @NonNull
        public SongTag getTag() {
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
        int positionInQueue;

        static SongInformation fromSong(Song song, String playlistName, int positionInQueue) {
            return new SongInformation(song.getSongId(), song.getTitle(), song.getArtist(), song.getAlbum(), playlistName, song.getFilepath(), song.getDuration(), false, song.isFavorite(), song.getAlbumArtPath(), positionInQueue);
        }

        static SongInformation fromRadioStation(RadioStation station) {
            return new SongInformation(station.getId(), "", "", "", station.getName(), station.getUrl(), 0, true, false, null, 0);
        }
    }

    @Value
    private static class SongTag {
        long songId;
        int position;
    }

    public enum Filter {
        EchoFilter
    }

}
