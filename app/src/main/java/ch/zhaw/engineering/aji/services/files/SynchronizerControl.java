package ch.zhaw.engineering.aji.services.files;

import android.content.Context;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.Setter;

public class SynchronizerControl {
    @Setter
    private boolean mMediaStore;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SynchronizerControl() {
        mMediaStore = true;
    }

    public void synchronizeSongsPeriodically(Context context) {
        if (mMediaStore) {
            final MediaStoreSynchronizer synchronizer = new MediaStoreSynchronizer(context);
            scheduler.scheduleAtFixedRate(synchronizer::synchronizeAllSongs, 0,15, TimeUnit.MINUTES );
        }
    }
}
