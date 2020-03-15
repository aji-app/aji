package ch.zhaw.engineering.tbdappname.services.audio.filter;

import android.util.Log;

import ch.zhaw.engineering.tbdappname.services.audio.backend.AudioBackend;

public class NoopFilter extends AudioBackend.AudioFilter {
    private static final String TAG = "NoopFilter";
    private int mId;
    private int mBatchSize;

    public NoopFilter(int id, int batchSize, boolean enabled) {
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
        return 22050;
    }

    @Override
    public byte[] apply(byte[] bytes, AudioBackend.AudioFormat format) {
        Log.i(TAG, String.format("Applying noop %d filter to %d bytes (%d frames) with %d bytes per sample", mId, bytes.length,  bytes.length / format.getBytesPerFrame(), format.getBytesPerFrame() / format.getChannelCount()));
        return bytes;
    }

    @Override
    public String getIdentifier() {
        return "" + mId;
    }
}
