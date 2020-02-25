package ch.zhaw.engineering.tbdappname.ui.songs;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.services.database.repository.SongRepository;

public class SongViewModel extends AndroidViewModel {
    private final SongRepository songRepository;
    private final MediatorLiveData<List<Song>> songs;
    private boolean mAscending = true;
    private SongRepository.SortType mSortType = SongRepository.SortType.TITLE;

    private String searchText = null;

    public LiveData<List<Song>> getAllSongs() {
        return songs;
    }

    public SongViewModel(@NonNull Application application) {
        super(application);
        songRepository = SongRepository.getInstance(application);
        songs = new MediatorLiveData<>();
        songs.addSource(songRepository.getSortedSongs(mSortType, mAscending, ""), songs::setValue);
    }

    public void changeSortType(SongRepository.SortType sortType) {
        mSortType = sortType;
        update();
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
        songs.addSource(songRepository.getSortedSongs(mSortType, mAscending, searchText), value -> songs.setValue(value));
    }
}
