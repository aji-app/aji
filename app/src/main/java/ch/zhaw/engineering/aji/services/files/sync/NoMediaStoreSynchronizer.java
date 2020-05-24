package ch.zhaw.engineering.aji.services.files.sync;

import android.content.Context;
import android.util.Log;

import java.io.File;

import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.services.files.StorageHelper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NoMediaStoreSynchronizer {
    private static final String TAG = "NoMediaStoreSync";
    private final Context mContext;

    public void synchronizeDeletedSongs() {
        SongDao dao = AppDatabase.getInstance(mContext).songDao();
        for (Song song : dao.getAllSongs()) {
            if (!new File(song.getFilepath()).exists()) {
                int albumSongCount = dao.getAlbumSongCount(song.getAlbum());
                if (albumSongCount == 1) {
                    StorageHelper.deleteAlbumArt(song.getAlbumArtPath());
                }
                dao.deleteSongById(song.getSongId());
                Log.i(TAG, "Deleted song '" + song.getFilepath() + "' from Database");
            }
        }
    }
}
