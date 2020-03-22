package ch.zhaw.engineering.tbdappname.ui.song;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.services.database.repository.SongRepository;

public class SongViewModel extends AndroidViewModel {
    private final SongRepository mSongRepository;
    private final MediatorLiveData<List<Song>> mSongs;
    private boolean mAscending = true;
    private SongRepository.SortType mSortType = SongRepository.SortType.TITLE;

    private String mSearchText = null;

    public LiveData<List<Song>> getAllSongs() {
        return mSongs;
    }

    public SongViewModel(@NonNull Application application) {
        super(application);
        mSongRepository = SongRepository.getInstance(application);
        mSongs = new MediatorLiveData<>();
        mSongs.addSource(mSongRepository.getSortedSongs(mSortType, mAscending, ""), mSongs::setValue);
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
        mSongs.addSource(mSongRepository.getSortedSongs(mSortType, mAscending, mSearchText), mSongs::setValue);
    }
}
