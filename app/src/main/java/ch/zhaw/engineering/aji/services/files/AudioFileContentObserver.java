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
    private final Context mContext;

    public AudioFileContentObserver(Handler handler, Context context) {
        super(handler);
        mContext = context;
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.i(TAG, "Some audio file changed");
        StorageHelper.synchronizeSong(mContext, uri);
    }

    public void register() {
        mContext.getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, this);
    }

    public void unregister() {
        mContext.getContentResolver().unregisterContentObserver(this);
    }

}
