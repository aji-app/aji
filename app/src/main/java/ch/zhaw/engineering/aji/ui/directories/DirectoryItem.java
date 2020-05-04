package ch.zhaw.engineering.aji.ui.directories;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class DirectoryItem {
    File mFile;
    String mName;
    boolean mIsDirectory;

    public DirectoryItem(File file) {
        this.mFile = file;
        this.mName = null;
        this.mIsDirectory = file.isDirectory();
    }

    public static DirectoryItem parentDirectory(DirectoryItem parent) {
        return new DirectoryItem(parent.mFile, "..", true);
    }

    public String getName() {
        if (mName == null) {
            return mFile.getName();
        }
        return mName;
    }
}
