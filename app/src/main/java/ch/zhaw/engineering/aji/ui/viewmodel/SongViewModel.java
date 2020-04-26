package ch.zhaw.engineering.aji.ui.viewmodel;

import androidx.lifecycle.LiveData;

import java.util.List;

import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.entity.Song;

/* package */ class SongViewModel extends FilteringViewModel<SongDao, List<Song>> {
    private SongDao.SortType mSortType;

    SongViewModel(SongDao songDao) {
        super(songDao);
    }

    void changeSortType(SongDao.SortType sortType) {
        mSortType = sortType;
        update();
    }

    LiveData<List<Song>> getFileteredSongs() {
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
