package ch.zhaw.engineering.aji.services.files.sync;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;

public class AudioFileContentObserver extends ContentObserver {
    private static final String TAG = "AudioFileObserver";
    private final Context mContext;
    private final MediaStoreSynchronizer mSynchronizer;

    public AudioFileContentObserver(Handler handler, Context context) {
        super(handler);
        mContext = context;
        mSynchronizer = new MediaStoreSynchronizer(mContext);
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        mSynchronizer.synchronizeUri(uri);
    }

    public void register() {
        mContext.getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, this);
    }

    public void unregister() {
        mContext.getContentResolver().unregisterContentObserver(this);
    }

}
