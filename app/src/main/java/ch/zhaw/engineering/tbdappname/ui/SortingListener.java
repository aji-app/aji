package ch.zhaw.engineering.tbdappname.ui;

import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;

public interface SortingListener {

    void onSongSortTypeChanged(SongDao.SortType sortType);

    void onSearchTextChanged(SortResource sortResource, String text);

    void onSortDirectionChanged(SortResource sortResource, boolean ascending);

    enum SortResource {
        ARTISTS, SONGS, ALBUMS, PLAYLISTS, RADIOS
    }
}
