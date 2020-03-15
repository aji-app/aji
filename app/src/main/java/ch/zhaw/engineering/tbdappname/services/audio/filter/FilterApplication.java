package ch.zhaw.engineering.tbdappname.services.audio.filter;

import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.zhaw.engineering.tbdappname.services.audio.backend.AudioBackend;
import ch.zhaw.engineering.tbdappname.services.audio.resampling.Resampler;
import lombok.Getter;
import lombok.Value;

/**
 * Applies the input to the filter in the requested sampleRate and batch size
 * Ensures that the batches are always of the requested size, padding with 0 if necessary
 * Only changes sample rate if the filter is disabled
 */
public final class FilterApplication {
    private static final String TAG = "FilterApplication";

    @Getter
    private AudioBackend.AudioFormat outputFormat;
    private final AudioBackend.AudioFilter mFilter;
    private ByteBuffer mResampledUnprocessedInputBuffer;
    private AudioBackend.AudioFormat mUnprocessedAudioFormat;

    public FilterApplication(@NonNull AudioBackend.AudioFilter filter) {
        this.mFilter = filter;
        this.mResampledUnprocessedInputBuffer = ByteBuffer.allocate(0).order(ByteOrder.nativeOrder());
    }

    /**
     * Applies the stored filter to the input. flushing the internal cache if flush is <code>true</code>-
     * Caches left over data to be processed with the next call (or immediately if flushed)
     * @param input The buffer containing the bytes to process
     * @param flush If true, uses up all cached data
     * @return The processed bytes
     */
    @NonNull
    public Result apply(ByteBuffer input, boolean flush, @NonNull AudioBackend.AudioFormat inputFormat) {
        AudioBackend.AudioFormat outputFormat = mFilter.getRequestedAudioFormat(inputFormat);
        if (!mFilter.isEnabled()) {
            // Check if we have unprocessed data and resample if necesary
            if (mResampledUnprocessedInputBuffer.remaining() > 0) {
                byte[] leftOver = new byte[mResampledUnprocessedInputBuffer.remaining()];
                mResampledUnprocessedInputBuffer.get(leftOver);
                if (mUnprocessedAudioFormat.getSampleRate() != inputFormat.getSampleRate()) {
                    leftOver = Resampler.resample(leftOver, mUnprocessedAudioFormat, inputFormat);
                }
                ByteBuffer output = ByteBuffer.allocate(input.remaining() + leftOver.length);
                output.put(leftOver);
                output.put(input);
                output.flip();
                return new Result(output, inputFormat);
            }
            return new Result(input, inputFormat);
        }

        byte[] inputInCorrectSampleRate = new byte[input.remaining()];
        input.get(inputInCorrectSampleRate);

        if (inputFormat.getSampleRate() != outputFormat.getSampleRate()) {
            inputInCorrectSampleRate = Resampler.resample(inputInCorrectSampleRate, inputFormat, outputFormat);
        }


        ByteBuffer filterInputBuffer = ByteBuffer.allocate(mResampledUnprocessedInputBuffer.remaining() + inputInCorrectSampleRate.length).order(ByteOrder.nativeOrder());
        filterInputBuffer.put(mResampledUnprocessedInputBuffer);
        filterInputBuffer.put(inputInCorrectSampleRate);
        filterInputBuffer.flip();

        int batchSize = mFilter.getRequestedFrameBatchSize() * outputFormat.getBytesPerFrame();
        int batches = filterInputBuffer.remaining() / batchSize;

        boolean needsExtraBatch = flush && (filterInputBuffer.remaining() % batchSize > 0);

        Log.i(TAG, String.format("Applying %d bytes to Filter %s in %d batches", filterInputBuffer.remaining(), mFilter.getIdentifier(), batches));
        // If we have to flush, we may need 1 batch more
        ByteBuffer output = ByteBuffer.allocate((needsExtraBatch ? batches + 1 : batches) * batchSize).order(ByteOrder.nativeOrder());

        for (int batch = 0; batch < batches; batch++) {
            byte[] batchContent = new byte[Math.min(batchSize, filterInputBuffer.remaining())];
            filterInputBuffer.get(batchContent);
            byte[] batchResult = mFilter.apply(batchContent, outputFormat);
            output.put(batchResult);
        }
        if (needsExtraBatch) {
            // Last batch is padded with zeros (= no sound)
            Log.i(TAG, String.format("Padding last flush batch with %d bytes", batchSize - filterInputBuffer.remaining()));
            byte[] batchContent = new byte[batchSize];
            filterInputBuffer.get(batchContent, 0, filterInputBuffer.remaining());
            byte[] batchResult = mFilter.apply(batchContent, outputFormat);
            output.put(batchResult);
        }

        Log.i(TAG, String.format("Did not apply %d bytes to Filter %s", filterInputBuffer.remaining(), mFilter.getIdentifier()));
        mResampledUnprocessedInputBuffer = filterInputBuffer;
        mUnprocessedAudioFormat = outputFormat;

        output.flip();
        return new Result(output, outputFormat);
    }

    @Value
    public static class Result {
        ByteBuffer output;
        AudioBackend.AudioFormat format;
    }
}
