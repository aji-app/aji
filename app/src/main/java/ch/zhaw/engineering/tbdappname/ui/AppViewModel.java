package ch.zhaw.engineering.tbdappname.ui;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.AppDatabase;
import ch.zhaw.engineering.tbdappname.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;

public class AppViewModel extends AndroidViewModel {
    private static final String TAG = "AppViewModel";

    private final SongDao mSongDao;
    private final MediatorLiveData<List<Song>> mSongs = new MediatorLiveData<>();
    private LiveData<List<Song>> mLastSource;

    private boolean mSongsAscending = true;
    private SongDao.SortType mSortTypeSongs = SongDao.SortType.TITLE;
    private String mSongSearchText;


    private final RadioStationDao mRadioStationDao;
    private final MediatorLiveData<List<RadioStationDto>> mRadios = new MediatorLiveData<>();
    private boolean mRadioAscending = true;
    private String mRadioSearchText;


    public AppViewModel(@NonNull Application application) {
        super(application);
        mSongDao = AppDatabase.getInstance(application).songDao();
        mRadioStationDao = AppDatabase.getInstance(application).radioStationDao();
        updateSongsSource();
        updateRadioSource();
    }

    public void changeSongSearchText(String text) {
        String prev = mSongSearchText;
        if (text.length() < 3) {
            mSongSearchText = null;
        } else {
            mSongSearchText = text;
        }
        if (prev == null && mSongSearchText != null || prev != null && !prev.equals(mSongSearchText)) {
            updateSongsSource();
        }
    }

    public void changeSongSortType(SongDao.SortType sortType) {
        mSortTypeSongs = sortType;
        updateSongsSource();
    }

    public void changeSongSortOrder(boolean ascending) {
        mSongsAscending = ascending;
        updateSongsSource();
    }


    public LiveData<List<Song>> getSongsForPlaylist(Integer id) {
        return mSongDao.getSongsForPlaylist(id);
    }

    public LiveData<List<Song>> getSongs() {
        return mSongs;
    }

    public LiveData<List<RadioStationDto>> getRadios() {
        return mRadios;
    }

    public void changeRadioSortOrder(boolean ascending) {
        mRadioAscending = ascending;
        updateRadioSource();
    }

    public void changeRadioSearchText(String text) {
        String prev = mRadioSearchText;
        if (text.length() < 3) {
            mRadioSearchText = null;
        } else {
            mRadioSearchText = text;
        }
        if (prev == null && mRadioSearchText != null || prev != null && !prev.equals(mRadioSearchText)) {
            updateRadioSource();
        }
    }

    private void updateRadioSource() {
        mRadios.addSource(mRadioStationDao.getRadioStations(mRadioAscending, mRadioSearchText), mRadios::setValue);
    }

    private void updateSongsSource() {
        if (mLastSource != null) {
            mSongs.removeSource(mLastSource);
        }
        mLastSource = mSongDao.getSortedSongs(mSortTypeSongs, mSongsAscending, mSongSearchText);
        mSongs.addSource(mLastSource, value -> {
            Log.i(TAG, "Updating songs in songs list: " + (value.size() > 0 ? value.get(0).getTitle() : " no songs"));
            mSongs.setValue(value);
        });
    }
}
