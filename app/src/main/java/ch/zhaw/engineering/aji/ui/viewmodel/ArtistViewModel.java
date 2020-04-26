package ch.zhaw.engineering.aji.ui.viewmodel;

import androidx.lifecycle.LiveData;

import java.util.List;

import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.dto.ArtistDto;

/* package */  class ArtistViewModel extends FilteringViewModel<SongDao, List<ArtistDto>> {
    ArtistViewModel(SongDao playlistDao) {
        super(playlistDao);
        update();
    }

    LiveData<List<ArtistDto>> getFilteredArtists() {
        return mList;
    }

    @Override
    protected LiveData<List<ArtistDto>> getUpdatedFilteredSource() {
        return mDao.getArtists(mSearchText, mAscending);
    }
}
