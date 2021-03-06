package ch.zhaw.engineering.aji.services.files;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

import ch.zhaw.engineering.aji.services.database.dto.SongDto;

public class StorageHelper {

    private static final String TAG = "StorageHelper";
    private static final String FOLDER = "albumart";

    public static String saveAlbumArt(Context context, SongDto song) {
        if (song.getAlbumArt() == null) {
            return null;
        }
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(FOLDER, Context.MODE_PRIVATE);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                return null;
            }
        }
        // java strings are annoying, this regex is: \s|[\/]
        String filenameIdentifier = song.getAlbum().replaceAll("\\s|[\\\\/]", "_");
        File albumArtPath = new File(directory, getAlbumArtPath(filenameIdentifier));

        if (albumArtPath.exists()) {
            // We already stored this album art
            return albumArtPath.getAbsolutePath();
        }
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(albumArtPath);
            song.getAlbumArt().compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return albumArtPath.getAbsolutePath();
    }

    public static void deleteAlbumArt(String albumArtPath) {
        if (albumArtPath == null) {
            return;
        }
        File albumArt = new File(albumArtPath);
        try {
            boolean deleted = albumArt.delete();
            Log.i(TAG, "Deleted ablum ? " + deleted);
        } catch (SecurityException e) {
            // Should not happen because we delete files in internal app storage
        }
    }

    private static String getAlbumArtPath(String identifier) {
        return FOLDER + "_" + identifier + ".png";
    }

    public static void deleteAllAlbumArt(Context context) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(FOLDER, Context.MODE_PRIVATE);
        deleteDirectory(directory);
    }
    private static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}
