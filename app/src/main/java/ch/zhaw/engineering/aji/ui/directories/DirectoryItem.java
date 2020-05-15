package ch.zhaw.engineering.aji.ui.directories;

import java.io.File;

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
    @Getter
    int mSubDirectoryCount;
    @Getter
    int mFileCount;

    public DirectoryItem(File file) {
        this.mFile = file;
        this.mName = null;
        this.mIsDirectory = file.isDirectory();
        File[] directories = file.listFiles(File::isDirectory);
        File[] audioFiles = file.listFiles(new AudioFileFilter());
        mSubDirectoryCount = directories == null ? 0 : directories.length;
        mFileCount = audioFiles == null ? 0 : audioFiles.length;
    }

    public static DirectoryItem parentDirectory(DirectoryItem parent) {
        return new DirectoryItem(parent.mFile, "..", true, -1, -1 );
    }

    public String getName() {
        if (mName == null) {
            return mFile.getName();
        }
        return mName;
    }
}
