package ch.zhaw.engineering.aji.services.audio.filter;

import android.util.Log;

import ch.zhaw.engineering.aji.services.audio.backend.AudioBackend;

public class EchoFilter extends AudioBackend.AudioFilter {
    private static final String TAG = "PhaserFilter";
    private final int mId;
    private final int mBatchSize;
    public static final int SAMPLE_RATE = 44100;

    public EchoFilter(int id, int batchSize, boolean enabled) {
        mId = id;
        mBatchSize = batchSize;
        setEnabled(enabled);
    }

    @Override
    public int getRequestedFrameBatchSize() {
        return mBatchSize;
    }

    @Override
    public int getRequestedSampleRate() {
        return SAMPLE_RATE;
    }

    @Override
    public byte[] apply(byte[] bytes, AudioBackend.AudioFormat format) {

        byte[] temp = bytes.clone();
        int N = mBatchSize / 4;
        for (int n = N + 1; n < bytes.length; n++) {
            bytes[n] = (byte) (temp[n] + temp[n - N]);
        }

        Log.i(TAG, String.format("Applying noop %d filter to %d bytes (%d frames) with %d bytes per sample", mId, bytes.length, bytes.length / format.getBytesPerFrame(), format.getBytesPerFrame() / format.getChannelCount()));
        return bytes;
    }

    @Override
    public String getIdentifier() {
        return "" + mId;
    }
}
