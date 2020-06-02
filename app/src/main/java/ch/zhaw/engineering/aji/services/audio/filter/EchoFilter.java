package ch.zhaw.engineering.aji.services.audio.filter;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.zhaw.engineering.aji.services.audio.backend.AudioBackend;

public class EchoFilter extends AudioBackend.AudioFilter {
    private static final String TAG = "EchoFilter";
    public static final int SAMPLE_RATE = 44100;
    public static final int ASSUMED_CHANNEL_COUNT = 2;
    private final int mId;
    private final int mBatchSize = 2 * ASSUMED_CHANNEL_COUNT * SAMPLE_RATE;
    private short[] mEcho;
    private int position = 0;
    private double mEchoStrength;
    private double mEchoInSeconds;
    private boolean mApplyEcho = false;


    public EchoFilter(int id, boolean enabled) {
        mId = id;
        setEnabled(enabled);
        setEchoInSeconds(1.0);
        setEchoStrength(0.4);
    }

    public void setEchoStrength(double strength) {
        mEchoStrength = Math.max(0.0, Math.min(strength, 1.0));
    }

    public void setEchoInSeconds(double seconds) {
        seconds = Math.max(0.0, Math.min(seconds, 2.0));
        mEchoInSeconds = seconds;
        int echoInFrames = (int) (SAMPLE_RATE * seconds * ASSUMED_CHANNEL_COUNT);
        mEcho = new short[echoInFrames];
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
        if (format.getBytesPerFrame() / format.getChannelCount() != 2) {
            Log.w(TAG, "Audio sample has a bigger bit depth than 2 bytes. Filter is now disabled");
            setEnabled(false);
            // TODO: Feedback to user
            return bytes;
        }
        Log.i(TAG, String.format("Applying echo filter (%.2fs, %.2f strength) to %d bytes (%d frames) with %d bytes per sample",
                mEchoInSeconds, mEchoStrength, bytes.length, bytes.length / format.getBytesPerFrame(), format.getBytesPerFrame() / format.getChannelCount()));
        short[] bytesAsShort = bytesToShort(bytes);
        short[] out = bytesAsShort.clone();

        for (int i = 0; i < bytesAsShort.length; i++) {
            if (mApplyEcho) {
                double value = (bytesAsShort[i] + mEchoStrength * mEcho[position]);
                out[i] = (short) (value / (1 + mEchoStrength));
            }
            mEcho[position] = bytesAsShort[i];
            position++;
            if (position == mEcho.length) {
                position = 0;
                mApplyEcho = true;
            }
        }

        return shortsToBytes(out);
    }

    private short[] bytesToShort(byte[] bytes) {
        int length = bytes.length;
        if (bytes.length % 2 != 0) {
            length += 1;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(length).order(ByteOrder.nativeOrder());
        byteBuffer.put(bytes);
        if (bytes.length % 2 != 0) {
            byteBuffer.put((byte) 0);
        }
        byteBuffer.flip();
        short[] shortArray = new short[length / 2];
        byteBuffer.asShortBuffer().get(shortArray);
        return shortArray;
    }

    private byte[] shortsToBytes(short[] shorts) {
        int length = shorts.length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(length * 2).order(ByteOrder.nativeOrder());
        for (short s : shorts) {
            byteBuffer.putShort(s);
        }
        byteBuffer.flip();
        byte[] byteArray = new byte[length * 2];
        byteBuffer.get(byteArray);
        return byteArray;
    }

    @Override
    public String getIdentifier() {
        return "" + mId;
    }
}
