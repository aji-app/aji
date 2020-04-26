package ch.zhaw.engineering.aji.services.audio.exoplayer;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.audio.AudioProcessor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.zhaw.engineering.aji.services.audio.resampling.Resampler;

import static android.os.Process.THREAD_PRIORITY_URGENT_AUDIO;
import static ch.zhaw.engineering.aji.services.audio.exoplayer.ExoPlayerAudioFormatHelper.fromExoplayerAudioFormat;

public class ExoPlayerResamplingAudioProcessor implements AudioProcessor {
    private static final String TAG = "ResamplingProcessor";

    private final Handler mHandler;
    private byte[] mOutputBuffer;
    private byte[] mInputBuffer;
    private AudioFormat mPendingInputFormat;
    private AudioFormat mPendingOutputFormat;
    private AudioFormat mInputAudioFormat;
    private AudioFormat mOutputAudioFormat;
    private boolean mInputEnded;
    private boolean mResampling;
    private int mSampleRate;

    public ExoPlayerResamplingAudioProcessor(int sampleRate) {
        mSampleRate = sampleRate;
        HandlerThread thread = new HandlerThread("ResamplingAudioProcessor" + sampleRate, THREAD_PRIORITY_URGENT_AUDIO);
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    @NonNull
    @Override
    public AudioFormat configure(@NonNull AudioFormat inputAudioFormat) throws UnhandledAudioFormatException {
        mPendingInputFormat = inputAudioFormat;
        mPendingOutputFormat = new AudioFormat(mSampleRate, mPendingInputFormat.channelCount, mPendingInputFormat.encoding);
        return mPendingOutputFormat;
    }

    @Override
    public boolean isActive() {
        return mPendingInputFormat != null && mPendingOutputFormat != null && mPendingInputFormat.sampleRate != mPendingOutputFormat.sampleRate && mSampleRate != 0;
    }

    @Override
    public void queueInput(ByteBuffer buffer) {
        if (buffer.remaining() > 0 && mInputBuffer == null) {
            mInputBuffer = new byte[buffer.remaining()];
            buffer.get(mInputBuffer);
            if (mOutputBuffer == null) {
                resample();
            }
        }
    }

    private void resample() {
        mHandler.post(() -> {
            mResampling = true;
            byte[] input = mInputBuffer;
            mInputBuffer = null;
            mOutputBuffer = Resampler.resample(input, fromExoplayerAudioFormat(mInputAudioFormat), fromExoplayerAudioFormat(mOutputAudioFormat));
            mResampling = false;
        });
    }

    @Override
    public void queueEndOfStream() {
        mInputEnded = true;
    }

    @NonNull
    @Override
    public ByteBuffer getOutput() {
        if (mOutputBuffer != null && mInputBuffer != null) {
            Log.i(TAG, String.format("Outputting %d resampled bytes", mOutputBuffer.length));
            byte[] output = mOutputBuffer;
            mOutputBuffer = null;
            if (mInputBuffer != null) {
                resample();
            }
            return ByteBuffer.wrap(output);
        }
        return ByteBuffer.allocate(0).order(ByteOrder.nativeOrder());
    }

    @Override
    public boolean isEnded() {
        return mInputEnded && !mResampling && mOutputBuffer == null && mInputBuffer == null;
    }

    @Override
    public void flush() {
        mOutputBuffer = null;
        mInputBuffer = null;
        mInputEnded = false;
        mResampling = false;
        mInputAudioFormat = mPendingInputFormat;
        mOutputAudioFormat = mPendingOutputFormat;
    }

    @Override
    public void reset() {
        mOutputBuffer = null;
        mInputBuffer = null;
        mPendingInputFormat = null;
        mPendingOutputFormat = null;
        mInputAudioFormat = null;
        mOutputAudioFormat = null;
        mInputEnded = false;
        mResampling = false;
        mSampleRate = 0;
    }
}
