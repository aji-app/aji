package ch.zhaw.engineering.tbdappname.services.audio.filter;

import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.zhaw.engineering.tbdappname.services.audio.backend.AudioBackend;
import ch.zhaw.engineering.tbdappname.services.audio.resampling.Resampler;
import lombok.Getter;

/**
 * Applies the input to the filter in the requested sampleRate and batch size
 * Ensures that the batches are always of the requested size, padding with 0 if necessary
 * Only changes sample rate if the filter is disabled
 */
public final class FilterApplication {
    private static final String TAG = "FilterApplication";

    @Getter
    private final AudioBackend.AudioFormat outputFormat;
    private final AudioBackend.AudioFilter mFilter;
    private final AudioBackend.AudioFormat mFormat;
    private ByteBuffer resampledUnprocessedInputBuffer;

    public FilterApplication(@NonNull AudioBackend.AudioFilter filter, @NonNull AudioBackend.AudioFormat format) {
        this.mFilter = filter;
        this.mFormat = format;
        this.outputFormat = filter.getRequestedAudioFormat(format);
        this.resampledUnprocessedInputBuffer = ByteBuffer.allocate(0).order(ByteOrder.nativeOrder());
    }

    /**
     * Applies the stored filter to the input. flushing the internal cache if flush is <code>true</code>-
     * Caches left over data to be processed with the next call (or immediately if flushed)
     * @param input The buffer containing the bytes to process
     * @param flush If true, uses up all cached data
     * @return The processed bytes
     */
    @NonNull
    public ByteBuffer apply(ByteBuffer input, boolean flush) {
        byte[] inputInCorrectSampleRate = new byte[input.remaining()];
        input.get(inputInCorrectSampleRate);
        if (mFormat.getSampleRate() != outputFormat.getSampleRate()) {
            inputInCorrectSampleRate = Resampler.resample(inputInCorrectSampleRate, mFormat, outputFormat);
        }


        ByteBuffer filterInputBuffer = ByteBuffer.allocate(resampledUnprocessedInputBuffer.remaining() + inputInCorrectSampleRate.length).order(ByteOrder.nativeOrder());
        filterInputBuffer.put(resampledUnprocessedInputBuffer);
        filterInputBuffer.put(inputInCorrectSampleRate);
        filterInputBuffer.flip();

        if (!mFilter.isEnabled()) {
            // Do not apply filter if it is disabled
            // Still resample to keep AudioFormat chain fixed
            return filterInputBuffer;
        }

        int batchSize = mFilter.getRequestedFrameBatchSize() * mFormat.getBytesPerFrame();
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
        resampledUnprocessedInputBuffer = filterInputBuffer;

        output.flip();
        return output;
    }
}
