package ch.zhaw.engineering.aji.services.files;

import java.io.File;
import java.io.FileFilter;

/**
 * Inspired by https://stackoverflow.com/a/11015925
 */
public class SupportedFileTypeFilter implements FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isHidden() || !f.canRead()) {
            return false;
        }

        if (f.isDirectory()) {
            return false;
        }
        return checkFileExtension(f);
    }

    private boolean checkFileExtension(File f) {
        String ext = getFileExtension(f);
        if (ext == null) return false;
        try {
            SupportedFileFormat.valueOf(ext.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            //Not known enum value
            return false;
        }
    }

    private String getFileExtension(File f) {
        return getFileExtension(f.getName());
    }

    private String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        } else
            return null;
    }

    /**
     * Files formats currently supported by Library
     */
    public enum SupportedFileFormat {
        AAC,
        OGG,
        MP3,
        WAV,
        MP4
    }

}