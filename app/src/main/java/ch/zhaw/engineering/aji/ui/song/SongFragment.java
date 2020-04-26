package ch.zhaw.engineering.aji.ui.song;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.ui.SortResource;
import ch.zhaw.engineering.aji.ui.menu.MenuHelper;
import ch.zhaw.engineering.aji.ui.song.list.AllSongsListFragment;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SongFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SongFragment extends Fragment {

    private AppViewModel mAppViewModel;

    @SuppressWarnings("unused")
    public static SongFragment newInstance() {
        SongFragment fragment = new SongFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.bottom_container, AllSongsListFragment.newInstance())
                    .commitNow();
        }
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_song, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.filter_list_menu_song, menu);
        MenuHelper.setupSearchView(SortResource.SONGS, mAppViewModel, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!MenuHelper.onOptionsItemSelected(SortResource.SONGS, mAppViewModel, item)) {
            switch (item.getItemId()) {

                case R.id.song_meta_order_album:
                    mAppViewModel.changeSortType(SongDao.SortType.ALBUM);
                    return true;
                case R.id.song_meta_order_artist:
                    mAppViewModel.changeSortType(SongDao.SortType.ARTIST);
                    return true;
                case R.id.song_meta_order_title:
                    mAppViewModel.changeSortType(SongDao.SortType.TITLE);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        return true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            mAppViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        }
    }
}