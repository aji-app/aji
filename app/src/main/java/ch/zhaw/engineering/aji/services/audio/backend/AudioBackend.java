package ch.zhaw.engineering.aji.services.audio.backend;

import android.content.Context;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;

/**
 * The audio backend that the AudioService uses.
 * It is used to play files in the background.
 */
public interface AudioBackend {
    /**
     * Initialize the AudioBackend
     *
     * @param context      A {@link Context}
     * @param mediaSession The {@link MediaSessionCompat} to use
     * @param listener     A {@link EventListener} to listen for events
     * @param filters       The {@link AudioFilter}s to integrate
     */
    void initialize(Context context, MediaSessionCompat mediaSession, @NonNull EventListener listener, final AudioFilter... filters);

    /**
     * Queues a {@link WebMedia} to play
     * @param media A {@link WebMedia} to queue
     */
    void queueWebMedia(@NonNull WebMedia media);

    /**
     * Queues a {@link Media} to play
     * @param media A {@link Media} to queue
     */
    void queueFile(@NonNull Media media);

    /**
     * Queues a list of {@link Media} to play
     * @param medias A {@link List<Media>} to queue
     */
    void queueFiles(@NonNull List<Media> medias);

    /**
     * Start playing
     * @param reset If true resets the current position
     */
    void play(boolean reset);

    /**
     * Pauses playback. Can be resumed by calling {@link #play(boolean)} with reset = false
     */
    void pause();

    /**
     * Stops playback. Resets the current queue.
     * Subsequent calls to {@link #play(boolean)} will not play any file if not explicitly queued again.
     */
    void stop();

    /**
     * Skips to the next {@link Media} in the queue. If there is no next media, does nothing.
     */
    void next();

    /**
     * Skip to next {@link Media} in queue
     *
     * @param media    the media to be played if there is no next {@link Media} in the queue
     * @param callback the callback is called with true if the media is playing now
     */
    void next(Media media, Callback<Boolean> callback);

    /**
     * Skips to the previous {@link Media} in the queue. If there is no previous media, does nothing.
     */
    void previous();

    /**
     * Gets the current position in the current {@link Media} that is playing
     * @param callback A {@link Callback<Long>} which will be called with the current position
     */
    void getCurrentPosition(@NonNull Callback<Long> callback);

    /**
     * Clears the current playback queue
     */
    void clear();

    /**
     * If enabled, plays songs in queue in a random order
     */
    void toggleShuffleModeEnabled();

    /**
     * Gets the current state of the shuffle mode
     * @param callback A {@link Callback<Boolean>} which will be called with the current tag
     */
    void getShuffleModeEnabled(@NonNull Callback<Boolean> callback);

    /**
     * Sets the playback mode to either "REPEAT_OFF", "REPEAT_ALL" or "REPEAT_ONE"
     * @param mode A {@link RepeatModes}
     */
    void setRepeatMode(RepeatModes mode);

    /**
     * Gets the current tag of the current {@link Media} that is playing
     * @param callback A {@link Callback<Object>} which will be called with the current tag
     */
    // TODO: Return songId (tag) and position
    void getCurrentSongInfo(@NonNull Callback<SongInfo> callback);

    /**
     * Seeks to the given position in the current song (or end of song if too far)
     * @param position the position in millis to seek to
     */
    void seekTo(long position);


    void removeSongFromQueue(Object tag);

    void removeSongFromQueueByPosition(int position);

    /**
     * Skips to the song at the provided index in the queue
     * @param index the index of the song
     */
    void skipToMedia(int index);

    interface Media {
        @Nullable
        Object getTag();

        String getPath();
    }

    interface WebMedia {
        @Nullable
        Object getTag();

        String getUrl();
    }

    @Value
    public class SongInfo {
        Object tag;
        int position;
    }

    /**
     * EventListener for playback events
     */
    interface EventListener {
        /**
         * Called when the backend starts playback
         */
        void onStartedPlaying();

        /**
         * Called when the backend stops playing due to external influences (e.g. google voice commands)
         * Not Called when {@link AudioBackend#stop()} is called
         */
        void onStoppedPlaying();

        /**
         * Called when the backend pauses playback due to external influences (e.g. google voice commands)
         * Not Called when {@link AudioBackend#pause()} is called
         */
        void onPausedPlaying();

        /**
         * Called when the backend starts playing a new song (either automatic or manual)
         */
        void onPositionDiscontinuity();

        /**
         * Called when an error happened while playing the media source with the given tag
         * @param tag The tag of the media source that failed to play
         */
        void onError(Object tag);
    }

    /**
     * Callback for the "Getters" in the AudioBackend.
     * This enables the backend to work asynchronously as well.
     * @param <T> The type of the value
     */
    interface Callback<T> {
        void receiveValue(T tag);
    }

    abstract class AudioFilter {
        @Getter
        @Setter
        private boolean enabled = false;

        public abstract int getRequestedFrameBatchSize();
        public abstract int getRequestedSampleRate();
        public abstract byte[] apply(byte[] bytes, AudioFormat format);

        public final AudioFormat getRequestedAudioFormat(AudioFormat input) {
            return new AudioFormat(getRequestedSampleRate(), input.channelCount, input.encoding, input.bytesPerFrame);
        }

        public abstract String getIdentifier();

        public abstract void modify(boolean enabled, double[] params);
    }

    /**
     * RepeatModes that the backend implements.
     */
    enum RepeatModes {
        REPEAT_OFF, REPEAT_ALL, REPEAT_ONE
    }

    /**
     * Represents an audio format of the PCM stream that is playing.
     */
    @Value
    class AudioFormat {
        int sampleRate;
        int channelCount;
        int encoding;
        int bytesPerFrame;
    }
}
