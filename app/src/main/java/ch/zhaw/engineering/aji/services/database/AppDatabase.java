package ch.zhaw.engineering.aji.services.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import ch.zhaw.engineering.aji.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.aji.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.entity.Playlist;
import ch.zhaw.engineering.aji.services.database.entity.PlaylistSongCrossRef;
import ch.zhaw.engineering.aji.services.database.entity.RadioStation;
import ch.zhaw.engineering.aji.services.database.entity.Song;

@Database(entities = {Song.class, Playlist.class, PlaylistSongCrossRef.class, RadioStation.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract SongDao songDao();

    public abstract PlaylistDao playlistDao();

    public abstract RadioStationDao radioStationDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context,
                    AppDatabase.class, "musicDatabase")
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .build();
        }
        return INSTANCE;
    }

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `RadioStation` ( `id` INTEGER PRIMARY KEY NOT NULL, `name` TEXT UNIQUE NOT NULL, `url` TEXT UNIQUE NOT NULL)");
            database.execSQL("CREATE UNIQUE INDEX index_RadioStation_url on RadioStation (url)");
            database.execSQL("CREATE UNIQUE INDEX index_RadioStation_name on RadioStation (name)");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE RadioStation ADD COLUMN genres TEXT NOT NULL DEFAULT ''");
        }
    };
}
