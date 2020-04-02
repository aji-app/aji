package ch.zhaw.engineering.tbdappname.services.audio.exoplayer;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.audio.AudioProcessor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ExoPlayerBatchingAudioProcessor implements AudioProcessor {
    private byte[] mInputBuffer = null;
    private byte[] mOutputBuffer = null;
    private int mInputPosition = 0;
    private boolean mInputEnded = false;
    private int mInputBufferSize;
    private final int mBatchSize;

    public ExoPlayerBatchingAudioProcessor() {
        this(0);
    }

    public ExoPlayerBatchingAudioProcessor(int batchSize) {
        mBatchSize = batchSize;
    }

    @NonNull
    @Override
    public AudioFormat configure(@NonNull AudioFormat inputAudioFormat) throws UnhandledAudioFormatException {
        if (mBatchSize > 0) {
            // Grab the number of requested frames
            mInputBufferSize = mBatchSize * inputAudioFormat.bytesPerFrame;
        } else {
            // 2.5s Buffer
            mInputBufferSize = 5 * inputAudioFormat.sampleRate * inputAudioFormat.bytesPerFrame / inputAudioFormat.channelCount;
        }
        return inputAudioFormat;
    }

    @Override
    public boolean isActive() {
        return mInputBufferSize > 0;
    }

    @Override
    public void queueInput(@NonNull ByteBuffer buffer) {
        if (mInputPosition < mInputBuffer.length) {
            byte[] buf = new byte[Math.min(buffer.remaining(), mInputBuffer.length - mInputPosition)];
            buffer.get(buf);
            System.arraycopy(buf, 0, mInputBuffer, mInputPosition, buf.length);
            mInputPosition += buf.length;
        }
        if ((mInputPosition == mInputBuffer.length || mInputEnded) && mOutputBuffer == null) {
            mOutputBuffer = mInputBuffer;
            mInputBuffer = new byte[mInputPosition];
            mInputPosition = 0;
        }
    }

    @Override
    public void queueEndOfStream() {
        mInputEnded = true;
    }

    @NonNull
    @Override
    public ByteBuffer getOutput() {
        if ((mInputPosition == mInputBuffer.length || mInputEnded) && mOutputBuffer != null) {
            ByteBuffer output = ByteBuffer.wrap(mOutputBuffer);
            mOutputBuffer = null;
            return output;
        }
        return ByteBuffer.allocate(0).order(ByteOrder.nativeOrder());
    }

    @Override
    public boolean isEnded() {
        return mOutputBuffer == null && mInputPosition == 0 && mInputEnded;
    }

    @Override
    public void flush() {
        mInputBuffer = new byte[mInputBufferSize];
        mOutputBuffer = null;
        mInputPosition = 0;
        mInputEnded = false;
    }

    @Override
    public void reset() {
        mInputBuffer = null;
        mOutputBuffer = null;
        mInputEnded = false;
        mInputPosition = 0;
        mInputBufferSize = 0;
    }
}
