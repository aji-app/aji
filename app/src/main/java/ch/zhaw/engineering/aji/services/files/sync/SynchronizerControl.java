package ch.zhaw.engineering.aji.services.files.sync;

import android.content.Context;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.Setter;

public class SynchronizerControl {
    private boolean mMediaStore;
    private Context mContext;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> mFuture;

    public SynchronizerControl(boolean useMediaStore, Context context) {
        mMediaStore = useMediaStore;
        mContext = context;
        synchronizeSongsPeriodically(mContext);
    }

    public void setMediaStore(boolean useMediaStore) {
        if (useMediaStore != mMediaStore && mFuture != null) {
            mMediaStore = useMediaStore;
            mFuture.cancel(true);
            synchronizeSongsPeriodically(mContext);
        }
    }

    private void synchronizeSongsPeriodically(Context context) {
        if (mMediaStore) {
            final MediaStoreSynchronizer synchronizer = new MediaStoreSynchronizer(context);
            mFuture = scheduler.scheduleAtFixedRate(synchronizer::synchronizeAllSongs, 0, 15, TimeUnit.MINUTES);
        } else {
            final NoMediaStoreSynchronizer synchronizer = new NoMediaStoreSynchronizer(context);
            mFuture = scheduler.scheduleAtFixedRate(synchronizer::synchronizeDeletedSongs, 0,15, TimeUnit.MINUTES );
        }
    }
}
