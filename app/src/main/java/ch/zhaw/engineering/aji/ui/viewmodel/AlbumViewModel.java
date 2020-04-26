package ch.zhaw.engineering.aji.ui.viewmodel;

import androidx.lifecycle.LiveData;

import java.util.List;

import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.dto.AlbumDto;

/* package */  class AlbumViewModel extends FilteringViewModel<SongDao, List<AlbumDto>> {
    AlbumViewModel(SongDao playlistDao) {
        super(playlistDao);
        update();
    }

    LiveData<List<AlbumDto>> getFilteredAlbums() {
        return mList;
    }

    @Override
    protected LiveData<List<AlbumDto>> getUpdatedFilteredSource() {
        return mDao.getAlbums(mSearchText, mAscending);
    }
}
