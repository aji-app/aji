package ch.zhaw.engineering.tbdappname.ui.directories;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class DirectoryItem {
    File file;
    String name;
    boolean isDirectory;

    public DirectoryItem(File file) {
        this.file = file;
        this.name = null;
        this.isDirectory = file.isDirectory();
    }

    public static DirectoryItem parentDirectory(DirectoryItem parent) {
        return new DirectoryItem(parent.file, "..", true);
    }

    public String getName() {
        if (name == null) {
            return file.getName();
        }
        return name;
    }
}
