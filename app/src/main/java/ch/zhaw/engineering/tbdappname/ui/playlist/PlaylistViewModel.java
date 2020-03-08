package ch.zhaw.engineering.tbdappname.ui.playlist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.entity.PlaylistWithSongs;
import ch.zhaw.engineering.tbdappname.services.database.repository.PlaylistRepository;

public class PlaylistViewModel extends AndroidViewModel {
    private final PlaylistRepository mPlaylistRepository;
    private final MediatorLiveData<List<PlaylistWithSongs>> playlists;
    private boolean mAscending = true;

    private String searchText = null;

    public LiveData<List<PlaylistWithSongs>> getAllPlaylists() {
        return playlists;
    }

    public PlaylistViewModel(@NonNull Application application) {
        super(application);
        mPlaylistRepository = PlaylistRepository.getInstance(application);
        playlists = new MediatorLiveData<>();
        playlists.addSource(mPlaylistRepository.getSortedPlaylists(mAscending, ""), playlists::setValue);
    }


    public void changeSortOrder(boolean ascending) {
        mAscending = ascending;
        update();
    }

    public void changeSearchText(String text) {
        String prev = searchText;
        if (text.length() < 3) {
            searchText = null;
        } else {
            searchText = text;
        }
        if (prev == null && searchText != null || prev != null && !prev.equals(searchText)) {
            update();
        }
    }

    private void update() {
        playlists.addSource(mPlaylistRepository.getSortedPlaylists(mAscending, searchText), playlists::setValue);
    }
}
