package ch.zhaw.engineering.tbdappname.ui.playlist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.dto.PlaylistWithSongCount;
import ch.zhaw.engineering.tbdappname.services.database.repository.PlaylistRepository;

public class PlaylistViewModel extends AndroidViewModel {
    private final PlaylistRepository mPlaylistRepository;
    private final MediatorLiveData<List<PlaylistWithSongCount>> mPlaylists;
    private boolean mAscending = true;

    private String mSearchText = null;

    public LiveData<List<PlaylistWithSongCount>> getAllPlaylists() {
        return mPlaylists;
    }

    public PlaylistViewModel(@NonNull Application application) {
        super(application);
        mPlaylistRepository = PlaylistRepository.getInstance(application);
        mPlaylists = new MediatorLiveData<>();
        mPlaylists.addSource(mPlaylistRepository.getPlaylistsWithSongCount("", mAscending), mPlaylists::setValue);
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
        mPlaylists.addSource(mPlaylistRepository.getPlaylistsWithSongCount(mSearchText, mAscending), mPlaylists::setValue);
    }
}
