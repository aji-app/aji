package ch.zhaw.engineering.aji.services.files.sync;

import android.content.Context;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.Setter;

public class SynchronizerControl {
    @Setter
    private boolean mMediaStore;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SynchronizerControl(boolean useMediaStore) {
        mMediaStore = useMediaStore;
    }

    public void synchronizeSongsPeriodically(Context context) {
        if (mMediaStore) {
            final MediaStoreSynchronizer synchronizer = new MediaStoreSynchronizer(context);
            scheduler.scheduleAtFixedRate(synchronizer::synchronizeAllSongs, 0,15, TimeUnit.MINUTES );
        } else {
            final NoMediaStoreSynchronizer synchronizer = new NoMediaStoreSynchronizer(context);
            scheduler.scheduleAtFixedRate(synchronizer::synchronizeDeletedSongs, 0,15, TimeUnit.MINUTES );
        }
    }
}