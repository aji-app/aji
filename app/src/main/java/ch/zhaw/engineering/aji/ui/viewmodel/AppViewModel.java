package ch.zhaw.engineering.aji.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.aji.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.dto.AlbumDto;
import ch.zhaw.engineering.aji.services.database.dto.ArtistDto;
import ch.zhaw.engineering.aji.services.database.dto.PlaylistWithSongCount;
import ch.zhaw.engineering.aji.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.SortResource;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public class AppViewModel extends AndroidViewModel {
    private final SongDao mSongDao;

    private final SongViewModel mSongViewModel;
    private final RadioViewModel mRadioViewModel;
    private final PlaylistViewModel mPlaylistViewModel;
    private final ArtistViewModel mArtistViewModel;
    private final AlbumViewModel mAlbumViewModel;

    @Getter
    @Setter
    private boolean mTwoPane;

    @Getter
    @Setter
    private boolean mOpenFirstInList;

    public void resetOpenFirstInList() {
        mOpenFirstInList = mTwoPane;
    }


    public AppViewModel(@NonNull Application application) {
        super(application);
        mSongDao = AppDatabase.getInstance(application).songDao();
        RadioStationDao radioStationDao = AppDatabase.getInstance(application).radioStationDao();
        PlaylistDao playlistDao = AppDatabase.getInstance(application).playlistDao();

        mSongViewModel = new SongViewModel(mSongDao);
        mArtistViewModel = new ArtistViewModel(mSongDao);
        mAlbumViewModel = new AlbumViewModel(mSongDao);
        mRadioViewModel = new RadioViewModel(radioStationDao);
        mPlaylistViewModel = new PlaylistViewModel(playlistDao);
    }

    public LiveData<List<Song>> getSongs() {
        return mSongViewModel.getFileteredSongs();
    }

    public LiveData<List<RadioStationDto>> getRadios() {
        return mRadioViewModel.getFilteredRadios();
    }

    public LiveData<List<PlaylistWithSongCount>> getAllPlaylists() {
        return mPlaylistViewModel.getFilteredPlaylists();
    }

    public void changeSortType(SongDao.SortType sortType) {
        mSongViewModel.changeSortType(sortType);
    }

    public void changeSearchText(SortResource sortResource, String searchText) {
        FilteringViewModel currentViewModel = getCurrentViewModel(sortResource);
        if (currentViewModel != null) {
            currentViewModel.changeSearchText(searchText);
        }
    }

    public void changeSortDirection(SortResource sortResource, boolean ascending) {
        FilteringViewModel currentViewModel = getCurrentViewModel(sortResource);
        if (currentViewModel != null) {
            currentViewModel.changeSortOrder(ascending);
        }
    }

    public boolean getSortDirection(SortResource sortResource) {
        FilteringViewModel currentViewModel = getCurrentViewModel(sortResource);
        if (currentViewModel != null) {
            return currentViewModel.getSortDirection();
        }
        return true;
    }

    public String getSearchString(SortResource sortResource) {
        FilteringViewModel currentViewModel = getCurrentViewModel(sortResource);
        if (currentViewModel != null) {
            return currentViewModel.getSearchString();
        }
        return "";
    }


    public LiveData<List<Song>> getSongsForPlaylist(Integer id) {
        return mSongDao.getSongsForPlaylist(id);
    }


    public LiveData<List<Song>> getFavorites() {
        return mSongDao.getFavorites();
    }

    public LiveData<List<AlbumDto>> getAlbums() {
        return mAlbumViewModel.getFilteredAlbums();
    }

    public LiveData<List<ArtistDto>> getArtists() {
        return mArtistViewModel.getFilteredArtists();
    }

    public LiveData<List<Song>> getSongsForAlbum(String album) {
        return mSongDao.getSongsForAlbum(album);
    }

    public LiveData<List<Song>> getSongsForArtist(String artist) {
        return mSongDao.getSongsForArtist(artist);
    }


    private FilteringViewModel getCurrentViewModel(SortResource sortResource) {
        switch (sortResource) {
            case ARTISTS:
                return mArtistViewModel;
            case SONGS:
                return mSongViewModel;
            case ALBUMS:
                return mAlbumViewModel;
            case PLAYLISTS:
                return mPlaylistViewModel;
            case RADIOS:
                return mRadioViewModel;
        }
        return null;
    }

    public boolean toggleHiddenSongs() {
        return mSongViewModel.toggleShowHidden();
    }

    public boolean showHiddenSongs() {
        return mSongViewModel.showHiddenSongs();
    }
}
