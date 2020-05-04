package ch.zhaw.engineering.aji.services.audio.webradio;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class RadioStationMetadataRunnable implements Runnable {
        private final IcyStreamMeta streamMeta;
        private final Listener mListener;

        public RadioStationMetadataRunnable(Listener listener, String url) throws MalformedURLException {
            mListener = listener;
            this.streamMeta = new IcyStreamMeta(new URL(url));
        }

        @Override
        public void run() {
            try {
                streamMeta.refreshMeta();
                mListener.onSongInformationChanged(streamMeta.getTitle(), streamMeta.getArtist(), false);
            } catch (IOException e) {
                mListener.onSongInformationChanged(null, null, true);
                        // nothing
            }
        }

        public interface Listener {
            void onSongInformationChanged(String title, String artist, boolean hasError);
        }
    }