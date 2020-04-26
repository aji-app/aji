package ch.zhaw.engineering.aji.services.audio.exoplayer;

import com.google.android.exoplayer2.audio.AudioProcessor;

import ch.zhaw.engineering.aji.services.audio.backend.AudioBackend;

/* package */ class ExoPlayerAudioFormatHelper {
    private ExoPlayerAudioFormatHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static AudioBackend.AudioFormat fromExoplayerAudioFormat(AudioProcessor.AudioFormat format) {
        return new AudioBackend.AudioFormat(format.sampleRate, format.channelCount, format.encoding, format.bytesPerFrame);
    }
}
