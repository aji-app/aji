package ch.zhaw.engineering.tbdappname.services.audio.exoplayer;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.audio.AudioProcessor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.zhaw.engineering.tbdappname.services.audio.backend.AudioBackend;
import ch.zhaw.engineering.tbdappname.services.audio.filter.FilterApplication;
import ch.zhaw.engineering.tbdappname.services.audio.resampling.Resampler;

import static android.os.Process.THREAD_PRIORITY_URGENT_AUDIO;
import static ch.zhaw.engineering.tbdappname.services.audio.exoplayer.ExoPlayerAudioFormatHelper.fromExoplayerAudioFormat;

/**
 * Applies all given filters to the input stream
 */
public class ExoPlayerFilterApplicationAudioProcessor implements AudioProcessor {
    private static final String TAG = "FilterResampleProcessor";

    private final Handler mHandler;
    private byte[] mOutputBuffer;
    private byte[] mInputBuffer;
    private AudioProcessor.AudioFormat mPendingInputFormat;
    private AudioProcessor.AudioFormat mInputAudioFormat;
    private boolean mInputEnded;
    private boolean mRunningFilters;
    private AudioBackend.AudioFilter[] mFilters;
    private FilterApplication[] mFilterApplications;

    public ExoPlayerFilterApplicationAudioProcessor(AudioBackend.AudioFilter... filters) {
        mFilters = filters;
        mFilterApplications = new FilterApplication[mFilters.length];
        HandlerThread thread = new HandlerThread("FilterResampleProcessor", THREAD_PRIORITY_URGENT_AUDIO);
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    @NonNull
    @Override
    public AudioProcessor.AudioFormat configure(@NonNull AudioProcessor.AudioFormat inputAudioFormat) throws UnhandledAudioFormatException {
        mPendingInputFormat = inputAudioFormat;
        return mPendingInputFormat;
    }

    @Override
    public boolean isActive() {
        return mPendingInputFormat != null && mFilters.length > 0;
    }

    @Override
    public void queueInput(ByteBuffer buffer) {
        if (buffer.remaining() > 0 && mInputBuffer == null) {
            Log.i(TAG, String.format("Reading %d bytes from Input", buffer.remaining()));
            mInputBuffer = new byte[buffer.remaining()];
            buffer.get(mInputBuffer);
            if (mOutputBuffer == null) {
                applyFilters();
            }
        }
    }

    private void applyFilters() {
        if (mOutputBuffer == null && !mRunningFilters) {
            mHandler.post(() -> {
                mRunningFilters = true;
                ByteBuffer input = ByteBuffer.wrap(mInputBuffer);
                mInputBuffer = null;

                ByteBuffer nextFilterInput = input;
                boolean flushFilters = mInputEnded;
                for (FilterApplication application : mFilterApplications) {
                    nextFilterInput = application.apply(nextFilterInput, flushFilters);
                }
                byte[] res = new byte[nextFilterInput.remaining()];
                nextFilterInput.get(res);

                AudioBackend.AudioFormat lastFilterFormat = mFilterApplications[mFilterApplications.length - 1].getOutputFormat();

                if (lastFilterFormat.getSampleRate() != mInputAudioFormat.sampleRate) {
                    res = Resampler.resample(res, lastFilterFormat, fromExoplayerAudioFormat(mInputAudioFormat));
                }

                mOutputBuffer = res;
                mRunningFilters = false;
            });
        }
    }

    @Override
    public void queueEndOfStream() {
        mInputEnded = true;
    }

    @NonNull
    @Override
    public ByteBuffer getOutput() {
        if (mOutputBuffer != null && (mInputBuffer != null || mInputEnded)) {
            Log.i(TAG, String.format("Outputting %d bytes", mOutputBuffer.length));
            byte[] output = mOutputBuffer;
            mOutputBuffer = null;
            if (mInputBuffer != null) {
                applyFilters();
            }
            return ByteBuffer.wrap(output);
        }

        return ByteBuffer.allocate(0).order(ByteOrder.nativeOrder());
    }

    @Override
    public boolean isEnded() {
        return mInputEnded && !mRunningFilters && mOutputBuffer == null && mInputBuffer == null;
    }

    @Override
    public void flush() {
        mOutputBuffer = null;
        mInputBuffer = null;
        mInputEnded = false;
        mRunningFilters = false;
        mInputAudioFormat = mPendingInputFormat;
        AudioBackend.AudioFormat currentFilterInputFormat = fromExoplayerAudioFormat(mInputAudioFormat);
        for (int i = 0; i < mFilters.length; i++) {
            mFilterApplications[i] = new FilterApplication(mFilters[i], currentFilterInputFormat);
            currentFilterInputFormat = mFilterApplications[i].getOutputFormat();
        }
    }

    @Override
    public void reset() {
        mOutputBuffer = null;
        mInputBuffer = null;
        mPendingInputFormat = null;
        mInputAudioFormat = null;
        mInputEnded = false;
        mRunningFilters = false;
    }
}
