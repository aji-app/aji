package ch.zhaw.engineering.tbdappname.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.PlaylistWithSongCount;

public class PlaylistViewModel extends FilteringViewModel<PlaylistDao, List<PlaylistWithSongCount>> {
    public PlaylistViewModel(PlaylistDao playlistDao) {
        super(playlistDao);
        update();
    }

    public LiveData<List<PlaylistWithSongCount>> getAllPlaylists() {
        return mList;
    }


    @Override
    protected void update() {
        mList.addSource(mDao.getPlaylists(mSearchText, mAscending), mList::setValue);
    }


}
