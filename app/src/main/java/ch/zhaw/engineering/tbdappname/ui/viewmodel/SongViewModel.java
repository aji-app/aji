package ch.zhaw.engineering.tbdappname.ui.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;

public class SongViewModel extends FilteringViewModel<SongDao, List<Song>> {
    private static final String TAG = "SongViewModel";

    private LiveData<List<Song>> mLastSongSource;
    private SongDao.SortType mSortType;

    public SongViewModel(SongDao songDao) {
        super(songDao);
    }

    public void changeSortType(SongDao.SortType sortType) {
        mSortType = sortType;
        update();
    }

    public LiveData<List<Song>> getSongs() {
        return mList;
    }

    @Override
    protected void update() {
        if (mLastSongSource != null) {
            mList.removeSource(mLastSongSource);
        }
        if (mSortType == null) {
            mSortType = SongDao.SortType.TITLE;
        }
        mLastSongSource = mDao.getSortedSongs(mSortType, mAscending, mSearchText);
        mList.addSource(mLastSongSource, value -> {
            Log.i(TAG, "Updating songs in songs list: " + (value.size() > 0 ? value.get(0).getTitle() : " no songs"));
            mList.setValue(value);
        });
    }
}
