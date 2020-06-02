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
    private double mDelay;
    private boolean mApplyEcho = false;


    public EchoFilter(int id, boolean enabled, double strength, double delay) {
        mId = id;
        modify(enabled, strength, delay);
    }

    public void setEchoStrength(double strength) {
        mEchoStrength = Math.max(0.0, Math.min(strength, 1.0));
    }

    public void setDelay(double seconds) {
        seconds = Math.max(0.0, Math.min(seconds, 2.0));
        mDelay = seconds;
        int echoInFrames = (int) (SAMPLE_RATE * seconds * ASSUMED_CHANNEL_COUNT);
        if (!mApplyEcho) {
            mEcho = new short[echoInFrames];
            position = 0;
            return;
        }
        if (echoInFrames == mEcho.length) {
            return;
        }
        // Modify echo buffer length to keep existing echo samples if possible
        if (echoInFrames > mEcho.length) {
            short[] echo = new short[echoInFrames];
            int shift = echoInFrames - mEcho.length;
            int lengthPart1 = mEcho.length - position;
            if (shift % 2 != 0) {
                shift -= 1;
            }
            if (lengthPart1 % 2 != 0) {
                lengthPart1 -= 1;
            }

            System.arraycopy(mEcho, position, echo, shift, lengthPart1);
            System.arraycopy(mEcho, 0, echo, shift + lengthPart1, position);
            mEcho = echo;
            position = 0;
        } else {
            short[] echo = new short[echoInFrames];
            int lengthPart1 = Math.max(0, mEcho.length - position);
            lengthPart1 = Math.min(lengthPart1, echoInFrames);
            if (lengthPart1 % 2 != 0) {
                lengthPart1 -= 1;
            }
            System.arraycopy(mEcho, position, echo, 0, lengthPart1);
            if (lengthPart1 < echoInFrames) {
                int lengthPart2 = echoInFrames - lengthPart1;
                System.arraycopy(mEcho, 0, echo, lengthPart1, lengthPart2);
            }
            mEcho = echo;
            position = 0;
        }
    }

    @Override
    public void modify(boolean enabled, double... params) {
        setEnabled(enabled);
        if (params.length == 2) {
            setEchoStrength(params[0]);
            setDelay(params[1]);
        }
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
                mDelay, mEchoStrength, bytes.length, bytes.length / format.getBytesPerFrame(), format.getBytesPerFrame() / format.getChannelCount()));
        short[] bytesAsShort = bytesToShort(bytes);
        short[] out = bytesAsShort.clone();

        for (int i = 0; i < bytesAsShort.length; i++) {
            if (mApplyEcho) {
                double value = (bytesAsShort[i] + mEchoStrength * mEcho[position]);
                out[i] = (short) (value / (1 + mEchoStrength));
            } else {
                out[i] = (short) ((double) out[i] / (1 + mEchoStrength));
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
