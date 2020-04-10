package ch.zhaw.engineering.tbdappname.ui.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;

public class SongViewModel extends FilteringViewModel<SongDao, List<Song>> {
    private static final String TAG = "SongViewModel";

    private SongDao.SortType mSortType;

    public SongViewModel(SongDao songDao) {
        super(songDao);
    }

    public void changeSortType(SongDao.SortType sortType) {
        mSortType = sortType;
        update();
    }

    public LiveData<List<Song>> getFileteredSongs() {
        return mList;
    }


    @Override
    protected LiveData<List<Song>> getUpdatedFilteredSource() {
        if (mSortType == null) {
            mSortType = SongDao.SortType.TITLE;
        }
        return mDao.getSortedSongs(mSortType, mAscending, mSearchText);
    }
}
