package ch.zhaw.engineering.aji.ui.viewmodel;

import androidx.lifecycle.LiveData;

import java.util.List;

import ch.zhaw.engineering.aji.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.aji.services.database.dto.PlaylistWithSongCount;

/* package */  class PlaylistViewModel extends FilteringViewModel<PlaylistDao, List<PlaylistWithSongCount>> {
    PlaylistViewModel(PlaylistDao playlistDao) {
        super(playlistDao);
        update();
    }

    LiveData<List<PlaylistWithSongCount>> getFilteredPlaylists() {
        return mList;
    }

    @Override
    protected LiveData<List<PlaylistWithSongCount>> getUpdatedFilteredSource() {
        return mDao.getPlaylists(mSearchText, mAscending);
    }
}
