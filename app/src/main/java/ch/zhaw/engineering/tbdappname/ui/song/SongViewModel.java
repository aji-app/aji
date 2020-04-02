package ch.zhaw.engineering.tbdappname.ui.song;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.PlaylistWithSongCount;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import lombok.Value;

public class SongViewModel extends AndroidViewModel {
    private static final String TAG = "SongViewModel";
    private final SongDao mSongDao;
    private final MediatorLiveData<List<Song>> mSongs;

    private final MediatorLiveData<SongsAndPlaylists> mSongsAndPlaylists = new MediatorLiveData<>();

    private boolean mAscending = true;
    private SongDao.SortType mSortType = SongDao.SortType.TITLE;

    private String mSearchText = null;
    private LiveData<List<Song>> mLastSource;

    public LiveData<List<Song>> getSongs() {
        return mSongs;
    }

    public LiveData<List<Song>> getSongsForPlaylist(Integer id) {
        return mSongDao.getSongsForPlaylist(id);
    }

    public LiveData<SongsAndPlaylists> getSongsAndPlaylists() {
        return mSongsAndPlaylists;
    }

    public SongViewModel(@NonNull Application application) {
        super(application);
        mSongDao = SongDao.getInstance(application);
        mSongs = new MediatorLiveData<>();
        PlaylistDao playlistDao = PlaylistDao.getInstance(application);

        mSongsAndPlaylists.addSource(mSongs, songs -> {
            Log.i(TAG, "Updating songs in combined list: " + (songs.size() > 0 ? songs.get(0).getTitle() : " no songs"));
            SongsAndPlaylists currentValue = mSongsAndPlaylists.getValue();
            mSongsAndPlaylists.setValue(new SongsAndPlaylists(songs, currentValue == null ? new ArrayList<>() : currentValue.playlists));
        });

        mSongsAndPlaylists.addSource(playlistDao.getPlaylists(), playlists -> {
            Log.i(TAG, "Updating playlists");
            SongsAndPlaylists currentValue = mSongsAndPlaylists.getValue();
            mSongsAndPlaylists.setValue(new SongsAndPlaylists(currentValue == null ? new ArrayList<>() : currentValue.songs, playlists));
        });
        updateSource();
    }

    public void changeSortType(SongDao.SortType sortType) {
        mSortType = sortType;
        updateSource();
    }

    public void changeSortOrder(boolean ascending) {
        mAscending = ascending;
        updateSource();
    }

    public void changeSearchText(String text) {
        String prev = mSearchText;
        if (text.length() < 3) {
            mSearchText = null;
        } else {
            mSearchText = text;
        }
        if (prev == null && mSearchText != null || prev != null && !prev.equals(mSearchText)) {
            updateSource();
        }
    }

    private void updateSource() {
        if (mLastSource != null) {
            mSongs.removeSource(mLastSource);
        }
        mLastSource = mSongDao.getSortedSongs(mSortType, mAscending, mSearchText);
        mSongs.addSource(mLastSource, value -> {
            Log.i(TAG, "Updating songs in songs list: " + (value.size() > 0 ? value.get(0).getTitle() : " no songs"));
            mSongs.setValue(value);
        });
    }

    @Value
    public static class SongsAndPlaylists {
        List<Song> songs;
        List<PlaylistWithSongCount> playlists;
    }
}
