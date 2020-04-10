package ch.zhaw.engineering.tbdappname.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.AppDatabase;
import ch.zhaw.engineering.tbdappname.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.tbdappname.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.AlbumDto;
import ch.zhaw.engineering.tbdappname.services.database.dto.ArtistDto;
import ch.zhaw.engineering.tbdappname.services.database.dto.PlaylistWithSongCount;
import ch.zhaw.engineering.tbdappname.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.ui.SortingListener;

public class AppViewModel extends AndroidViewModel {
    private final SongDao mSongDao;

    private final SongViewModel mSongViewModel;
    private final RadioViewModel mRadioViewModel;
    private final PlaylistViewModel mPlaylistViewModel;


    public AppViewModel(@NonNull Application application) {
        super(application);
        mSongDao = AppDatabase.getInstance(application).songDao();
        RadioStationDao radioStationDao = AppDatabase.getInstance(application).radioStationDao();
        PlaylistDao playlistDao = AppDatabase.getInstance(application).playlistDao();
        mSongViewModel = new SongViewModel(mSongDao);
        mRadioViewModel = new RadioViewModel(radioStationDao);
        mPlaylistViewModel = new PlaylistViewModel(playlistDao);
    }


    public LiveData<List<Song>> getSongs() {
        return mSongViewModel.getSongs();
    }

    public LiveData<List<RadioStationDto>> getRadios() {
        return mRadioViewModel.getRadios();
    }

    public LiveData<List<PlaylistWithSongCount>> getAllPlaylists() {
        return mPlaylistViewModel.getAllPlaylists();
    }

    public void changeSortType(SongDao.SortType sortType) {
        mSongViewModel.changeSortType(sortType);
    }

    public void changeSearchText(SortingListener.SortResource sortResource, String searchText) {
        FilteringViewModel currentViewModel = getCurrentViewModel(sortResource);
        if (currentViewModel != null) {
            currentViewModel.changeSearchText(searchText);
        }
    }

    public void changeSortDirection(SortingListener.SortResource sortResource, boolean ascending) {
        FilteringViewModel currentViewModel = getCurrentViewModel(sortResource);
        if (currentViewModel != null) {
            currentViewModel.changeSortOrder(ascending);
        }
    }


    public LiveData<List<Song>> getSongsForPlaylist(Integer id) {
        return mSongDao.getSongsForPlaylist(id);
    }


    public LiveData<List<Song>> getFavorites() {
        return mSongDao.getFavorites();
    }

    public LiveData<List<AlbumDto>> getAlbums() {
        return mSongDao.getAlbums();
    }

    public LiveData<List<ArtistDto>> getArtists() {
        return mSongDao.getArtists();
    }

    public LiveData<List<Song>> getSongsForAlbum(String album) {
        return mSongDao.getSongsForAlbum(album);
    }

    public LiveData<List<Song>> getSongsForArtist(String artist) {
        return mSongDao.getSongsForArtist(artist);
    }


    private FilteringViewModel getCurrentViewModel(SortingListener.SortResource sortResource) {
        switch (sortResource) {
            case ARTISTS:
                return null;
            case SONGS:
                return mSongViewModel;
            case ALBUMS:
                return null;
            case PLAYLISTS:
                return mPlaylistViewModel;
            case RADIOS:
                return mRadioViewModel;
        }
        return null;
    }

}
