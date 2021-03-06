package ch.zhaw.engineering.aji.services.audio.exoplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;

import ch.zhaw.engineering.aji.services.audio.backend.AudioBackend;

import static android.os.Process.THREAD_PRIORITY_URGENT_AUDIO;


public class ExoPlayerAudioBackend implements AudioBackend {
    private static final String TAG = "ExoPlayerAudioBackend";
    private SimpleExoPlayer mPlayer;
    private Handler mAudioHandler;
    private HandlerThread mHandlerThread;
    private Context mContext;
    private EventListener mListener;
    private ConcatenatingMediaSource mConcatenatingMediaSource = new ConcatenatingMediaSource();

    @Override
    public void initialize(Context context, MediaSessionCompat mediaSession, @NonNull EventListener listener, final AudioFilter... filters) {
        mContext = context;
        mListener = listener;
        mHandlerThread = new HandlerThread("ExoplayerBackendHandler", THREAD_PRIORITY_URGENT_AUDIO);
        if (mHandlerThread.getState() != Thread.State.RUNNABLE) {
            mHandlerThread.start();
        }
        mAudioHandler = new Handler(mHandlerThread.getLooper());

        if (mListener == null) {
            throw new IllegalStateException("EventListener is null");
        }

        mAudioHandler.post(() -> {
            mPlayer = new SimpleExoPlayer.Builder(mContext
                    , new DefaultRenderersFactory(/* context= */ mContext) {
                @Override
                @NonNull
                protected AudioProcessor[] buildAudioProcessors() {
                    return new AudioProcessor[]{
                            new ExoPlayerBatchingAudioProcessor(),
                            new ExoPlayerFilterApplicationAudioProcessor(filters),
                    };
                }
            }
            ).setLooper(mHandlerThread.getLooper())
                    .build();

            MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);
            mediaSessionConnector.setPlayer(mPlayer);
            mediaSessionConnector.setControlDispatcher(new ControlDispatcher() {
                @Override
                public boolean dispatchSetPlayWhenReady(@NonNull Player player, boolean playWhenReady) {
                    if (!playWhenReady) {
                        mListener.onPausedPlaying();
                        return true;
                    } else {
                        mListener.onStartedPlaying();
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
                    mListener.onStoppedPlaying();
                    return false;
                }
            });


            mPlayer.addListener(new Player.EventListener() {

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                        mListener.onStoppedPlaying();
                    }
                    if (playbackState == Player.STATE_READY) {
                        mListener.onStartedPlaying();
                    }
                }

                @Override
                public void onPositionDiscontinuity(int reason) {
                    if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION || reason == Player.DISCONTINUITY_REASON_SEEK) {
                        mListener.onPositionDiscontinuity();
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    mListener.onError(mPlayer.getCurrentTag());
                }
            });
        });
    }

    @Override
    public void queueWebMedia(@NonNull WebMedia media) {
        mConcatenatingMediaSource.addMediaSource(GetMediaSource(media));
    }

    @Override
    public void queueFile(@NonNull Media media) {
        queueMedia(media);
    }

    @Override
    public void queueFiles(@NonNull List<Media> medias) {
        for (Media media : medias) {
            queueMedia(media);
        }
    }

    @Override
    public void play(boolean reset) {
        mAudioHandler.post(() -> {
            if (reset) {
                mPlayer.prepare(mConcatenatingMediaSource, true, true);
            }
            mPlayer.setPlayWhenReady(true);
        });
    }

    @Override
    public void pause() {
        mAudioHandler.post(() -> mPlayer.setPlayWhenReady(false));
    }

    @Override
    public void stop() {
        mAudioHandler.post(() -> {
            mPlayer.stop();
            mConcatenatingMediaSource = new ConcatenatingMediaSource();
        });
    }

    @Override
    public void next() {
        mAudioHandler.post(() -> mPlayer.next());
    }

    @Override
    public void next(Media media, Callback<Boolean> callback) {
        mAudioHandler.post(() -> {
            if (mPlayer.getNextWindowIndex() == -1) {
                queueMedia(media);
                mAudioHandler.postDelayed(() -> {
                    if (mPlayer.getShuffleModeEnabled()) {
                        mPlayer.setShuffleModeEnabled(false);

                        mPlayer.seekToDefaultPosition(mConcatenatingMediaSource.getSize() - 1);
                    } else {
                        mPlayer.next();
                    }
                    callback.receiveValue(true);
                }, 100);
            } else {
                mPlayer.next();
                callback.receiveValue(false);
            }
        });
    }

    @Override
    public void previous() {
        mAudioHandler.post(() -> mPlayer.previous());
    }

    @Override
    public void getCurrentPosition(@NonNull Callback<Long> callback) {
        if (mPlayer != null) {
            mAudioHandler.post(() -> callback.receiveValue(mPlayer.getCurrentPosition()));
        } else {
            callback.receiveValue(0L);
        }
    }

    @Override
    public void clear() {
        mConcatenatingMediaSource = new ConcatenatingMediaSource();
    }

    @Override
    public void toggleShuffleModeEnabled() {
        mAudioHandler.post(() -> mPlayer.setShuffleModeEnabled(!mPlayer.getShuffleModeEnabled()));
    }

    @Override
    public void getShuffleModeEnabled(@NonNull Callback<Boolean> callback) {
        mAudioHandler.post(() -> callback.receiveValue(mPlayer.getShuffleModeEnabled()));
    }

    @Override
    public void setRepeatMode(RepeatModes mode) {
        switch (mode) {
            case REPEAT_OFF:
                mAudioHandler.post(() -> mPlayer.setRepeatMode(Player.REPEAT_MODE_OFF));
                break;
            case REPEAT_ALL:
                mAudioHandler.post(() -> mPlayer.setRepeatMode(Player.REPEAT_MODE_ALL));
                break;
            case REPEAT_ONE:
                mAudioHandler.post(() -> mPlayer.setRepeatMode(Player.REPEAT_MODE_ONE));
                break;
        }
    }

    @Override
    public void getCurrentSongInfo(@NonNull Callback<SongInfo> callback) {
        if (mPlayer != null) {
            mAudioHandler.post(() -> callback.receiveValue(new SongInfo(mPlayer.getCurrentTag(), getCurrentSongIndex(mPlayer.getCurrentTag()))));
        } else {
            callback.receiveValue(null);
        }
    }

    @Override
    public void seekTo(long position) {
        if (mPlayer != null) {
            mAudioHandler.post(() -> {
                mPlayer.seekTo(position);
            });
        }
    }

    private int getCurrentSongIndex(Object tag) {
        for (int index = 0; index < mConcatenatingMediaSource.getSize(); index++) {
            MediaSource source = mConcatenatingMediaSource.getMediaSource(index);
            if (source.getTag() == tag) {
               return index;
            }
        }
        return -1;
    }

    @Override
    public void removeSongFromQueue(Object tag) {
        for (int index = 0; index < mConcatenatingMediaSource.getSize(); index++) {
            MediaSource source = mConcatenatingMediaSource.getMediaSource(index);
            if (source.getTag() == tag) {
                mConcatenatingMediaSource.removeMediaSource(index);
            }
        }
    }

    @Override
    public void removeSongFromQueueByPosition(int position) {
        mConcatenatingMediaSource.removeMediaSource(position);
    }

    @Override
    public void skipToMedia(int index) {
        if (mPlayer != null) {
            mAudioHandler.post(() -> {
                mPlayer.seekToDefaultPosition(index);
            });
        }
    }

    private void queueMedia(Media media) {
        mConcatenatingMediaSource.addMediaSource(GetMediaSource(media));
    }

    private MediaSource GetMediaSource(WebMedia media) {
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(mContext, "Aji"), 500, 500, true);
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .setTag(media.getTag())
                .createMediaSource(Uri.parse(media.getUrl()));
    }

    private MediaSource GetMediaSource(Media media) {
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext,
                Util.getUserAgent(mContext, "Aji"));

        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .setTag(media.getTag())
                .createMediaSource(Uri.parse(media.getPath()));
    }
}
