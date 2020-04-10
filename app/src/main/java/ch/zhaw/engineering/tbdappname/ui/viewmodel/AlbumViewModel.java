package ch.zhaw.engineering.tbdappname.ui.viewmodel;

import androidx.lifecycle.LiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.AlbumDto;
import ch.zhaw.engineering.tbdappname.services.database.dto.ArtistDto;

public class AlbumViewModel extends FilteringViewModel<SongDao, List<AlbumDto>> {
    public AlbumViewModel(SongDao playlistDao) {
        super(playlistDao);
        update();
    }

    public LiveData<List<AlbumDto>> getFilteredAlbums() {
        return mList;
    }

    @Override
    protected LiveData<List<AlbumDto>> getUpdatedFilteredSource() {
        return mDao.getAlbums(mSearchText, mAscending);
    }
}
