package ch.zhaw.engineering.tbdappname.ui.song;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.services.database.repository.PlaylistRepository;
import ch.zhaw.engineering.tbdappname.services.database.repository.SongRepository;
import lombok.Value;

public class SongViewModel extends AndroidViewModel {
    private final SongRepository mSongRepository;
    private final PlaylistRepository mPlaylistRepository;
    private final MediatorLiveData<List<Song>> mSongs;

    private final MediatorLiveData<SongsAndPlaylists> mSongsAndPlaylists = new MediatorLiveData<>();

    private boolean mAscending = true;
    private SongRepository.SortType mSortType = SongRepository.SortType.TITLE;

    private String mSearchText = null;

    public LiveData<List<Song>> getSongs() {
        return mSongs;
    }

    public LiveData<List<Playlist>> getPlaylists() {
        return mPlaylistRepository.getAllPlaylists();
    }

    public LiveData<SongsAndPlaylists> getSongsAndPlaylists() {
        return mSongsAndPlaylists;
    }

    public SongViewModel(@NonNull Application application) {
        super(application);
        mSongRepository = SongRepository.getInstance(application);
        mPlaylistRepository = PlaylistRepository.getInstance(application);
        mSongs = new MediatorLiveData<>();
        mSongs.addSource(mSongRepository.getSortedSongs(mSortType, mAscending, ""), mSongs::setValue);

        mSongsAndPlaylists.addSource(mSongs, songs -> {
            SongsAndPlaylists currentValue = mSongsAndPlaylists.getValue();
            mSongsAndPlaylists.setValue(new SongsAndPlaylists(songs, currentValue == null ? new ArrayList<>() : currentValue.playlists));
        });

        mSongsAndPlaylists.addSource(mPlaylistRepository.getAllPlaylists(), playlists -> {
            SongsAndPlaylists currentValue = mSongsAndPlaylists.getValue();
            mSongsAndPlaylists.setValue(new SongsAndPlaylists(currentValue == null ? new ArrayList<>() : currentValue.songs, playlists));
        });
    }

    public void changeSortType(SongRepository.SortType sortType) {
        mSortType = sortType;
        update();
    }

    public void changeSortOrder(boolean ascending) {
        mAscending = ascending;
        update();
    }

    public void changeSearchText(String text) {
        String prev = mSearchText;
        if (text.length() < 3) {
            mSearchText = null;
        } else {
            mSearchText = text;
        }
        if (prev == null && mSearchText != null || prev != null && !prev.equals(mSearchText)) {
            update();
        }
    }

    private void update() {
        mSongs.addSource(mSongRepository.getSortedSongs(mSortType, mAscending, mSearchText), mSongs::setValue);
    }

    @Value
    public static class SongsAndPlaylists {
        List<Song> songs;
        List<Playlist> playlists;
    }
}
