package ch.zhaw.engineering.aji.services.audio.resampling;


import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ch.zhaw.engineering.aji.services.audio.backend.AudioBackend;

public final class Resampler {
    private static final String TAG = "Resampler";

    private Resampler() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static byte[] resample(byte[] input, AudioBackend.AudioFormat inputFormat, AudioBackend.AudioFormat outputFormat) {
        Log.i(TAG, String.format("Resampling: length : %d bytes, %g secs from %d to %d",
                input.length,
                (double) input.length / (inputFormat.getBytesPerFrame() / inputFormat.getChannelCount()) / inputFormat.getChannelCount() / inputFormat.getSampleRate(),
                inputFormat.getSampleRate(),
                outputFormat.getSampleRate()));
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            long start = System.currentTimeMillis();
            new SSRC(in, out,
                    inputFormat.getSampleRate(),
                    outputFormat.getSampleRate(),
                    inputFormat.getBytesPerFrame() / inputFormat.getChannelCount(),
                    outputFormat.getBytesPerFrame() / outputFormat.getChannelCount(),
                    inputFormat.getChannelCount(),
                    input.length,
                    0,
                    0,
                    true
            );
            Log.i(TAG, String.format("Took %gs to resample %d bytes from %d to %d",
                    (((double) (System.currentTimeMillis() - start)) / 1000),
                    input.length,
                    inputFormat.getSampleRate(),
                    outputFormat.getSampleRate()));
            return out.toByteArray();
        } catch (IOException | IllegalArgumentException e) {
            // TODO: Last batch often fails due to too short array of samples :(
            Log.e(TAG, "Failed to resample", e);
        }
        return new byte[input.length];
    }
}
