package ch.zhaw.engineering.tbdappname.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

/* package */ abstract class FilteringViewModel<T, TListType> {
    final T mDao;
    final MediatorLiveData<TListType> mList;
    private LiveData<TListType> mLastSource;
    boolean mAscending = true;
    String mSearchText;

    FilteringViewModel(T dao) {
        mDao = dao;
        mList = new MediatorLiveData<>();
        update();
    }

    void changeSortOrder(boolean ascending) {
        mAscending = ascending;
        update();
    }

    void changeSearchText(String text) {
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

    void update() {
        if (mLastSource != null) {
            mList.removeSource(mLastSource);
        }
        mLastSource = getUpdatedFilteredSource();
        mList.addSource(mLastSource, mList::setValue);
    }

    protected abstract LiveData<TListType> getUpdatedFilteredSource();

    public boolean getSortDirection() {
        return mAscending;
    }

    public String getSearchString() {
        return mSearchText;
    }
}
