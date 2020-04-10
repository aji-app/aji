package ch.zhaw.engineering.tbdappname.ui.viewmodel;

import androidx.lifecycle.LiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.ArtistDto;

public class ArtistViewModel extends FilteringViewModel<SongDao, List<ArtistDto>> {
    public ArtistViewModel(SongDao playlistDao) {
        super(playlistDao);
        update();
    }

    public LiveData<List<ArtistDto>> getFilteredArtists() {
        return mList;
    }

    @Override
    protected LiveData<List<ArtistDto>> getUpdatedFilteredSource() {
        return mDao.getArtists(mSearchText, mAscending);
    }
}
