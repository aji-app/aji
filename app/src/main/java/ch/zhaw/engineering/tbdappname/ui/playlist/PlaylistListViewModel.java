package ch.zhaw.engineering.tbdappname.ui.playlist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.PlaylistWithSongCount;

public class PlaylistListViewModel extends AndroidViewModel {
    private final PlaylistDao mPlaylistDao;
    private final MediatorLiveData<List<PlaylistWithSongCount>> mPlaylists;
    private boolean mAscending = true;

    private String mSearchText = null;

    public LiveData<List<PlaylistWithSongCount>> getAllPlaylists() {
        return mPlaylists;
    }

    public PlaylistListViewModel(@NonNull Application application) {
        super(application);
        mPlaylistDao = PlaylistDao.getInstance(application);
        mPlaylists = new MediatorLiveData<>();
        mPlaylists.addSource(mPlaylistDao.getPlaylists("", mAscending), mPlaylists::setValue);
    }

    public void changeSortOrder(boolean ascending) {
        mAscending = ascending;
        update();
    }

    public void changeSearchText(String text) {
        String prev = mSearchText;
        if (text.length() < 3) {
            mSearchText = null;
        } else {
            mSearchText = text;
        }
        if (prev == null && mSearchText != null || prev != null && !prev.equals(mSearchText)) {
            update();
        }
    }

    private void update() {
        mPlaylists.addSource(mPlaylistDao.getPlaylists(mSearchText, mAscending), mPlaylists::setValue);
    }
}
