package ch.zhaw.engineering.tbdappname.ui.viewmodel;

import androidx.lifecycle.MediatorLiveData;

public abstract class FilteringViewModel<T, TListType> {
    protected final T mDao;
    protected final MediatorLiveData<TListType> mList;
    protected boolean mAscending = true;
    protected String mSearchText;

    protected FilteringViewModel(T dao) {
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

    protected abstract void update();
}
