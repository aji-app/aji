package ch.zhaw.engineering.aji.services.files;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Value;

public class AudioFileContentObserver extends ContentObserver {
    private static final String TAG = "AudioFileObserver";
    private final ExecutorService mExecutorService;
    private Handler mHandler;
    private final Context mContext;
    private final static long WAIT_TIME = 15 * 1000;

    private BackgroundSyncTask mWaitForLotsOfUpdates = new BackgroundSyncTask();

    public AudioFileContentObserver(Handler handler, Context context) {
        super(handler);
        mHandler = handler;
        mContext = context;
        mExecutorService = Executors.newFixedThreadPool(1);
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.i(TAG, "Some audio file changed");
        if (mWaitForLotsOfUpdates.getStatus() == AsyncTask.Status.RUNNING) {
            mWaitForLotsOfUpdates.cancel(true);
        }
        mWaitForLotsOfUpdates = new BackgroundSyncTask();
        mWaitForLotsOfUpdates.executeOnExecutor(mExecutorService, new BackgroundArgs(mContext, mHandler));
    }

    public void register() {
        mContext.getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, this);
    }

    public void unregister() {
        mContext.getContentResolver().unregisterContentObserver(this);
    }

    @Value
    private static class BackgroundArgs {
        Context context;
        Handler handler;
    }
    private static class BackgroundSyncTask extends AsyncTask<BackgroundArgs, Void, Void> {

        @Override
        protected Void doInBackground(BackgroundArgs... contexts) {
            if (contexts.length == 0) {
                return null;
            }
            try {
                Log.i(TAG, "Wait for more updates");
                Thread.sleep(WAIT_TIME);
                Log.i(TAG, "Triggering Synchronization");
                StorageHelper.synchronizeMediaStoreSongs(contexts[0].context, contexts[0].handler);
            } catch (InterruptedException e) {
                // This happens when we cancel the task
                Log.i(TAG, "More updates arrived");
            }

            return null;
        }
    }
}
