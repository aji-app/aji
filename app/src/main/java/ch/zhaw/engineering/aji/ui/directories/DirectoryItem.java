package ch.zhaw.engineering.aji.ui.directories;

import android.os.AsyncTask;

import java.io.File;
import java.util.concurrent.ExecutorService;

import ch.zhaw.engineering.aji.services.audio.backend.AudioBackend;
import ch.zhaw.engineering.aji.services.files.AudioFileFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

@Value
@AllArgsConstructor
public class DirectoryItem {
    File mFile;
    String mName;
    boolean mIsDirectory;

    public DirectoryItem(File file) {
        mFile = file;
        mName = null;
        mIsDirectory = file.isDirectory();
    }

    private int countFiles(File file) {
        File[] files = file.listFiles(new AudioFileFilter(true));
        int count = 0;
        if (files != null) {

            for (File f : files) {
                if (f.isDirectory()) {
                    count += countFiles(f);
                } else {
                    count++;
                }
            }
        }
        return count;
    }

    public static DirectoryItem parentDirectory(DirectoryItem parent) {
        return new DirectoryItem(parent.mFile, "..", true);
    }

    public void getFileCount(ExecutorService executor, AudioBackend.Callback<Integer> callback) {
        callback.receiveValue(null);
        executor.execute(() -> {
            callback.receiveValue(countFiles(mFile));
        });
    }

    public String getName() {
        if (mName == null) {
            return mFile.getName();
        }
        return mName;
    }
}
