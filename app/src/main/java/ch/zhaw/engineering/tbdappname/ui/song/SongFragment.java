package ch.zhaw.engineering.tbdappname.ui.song;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.ui.SortingListener;
import ch.zhaw.engineering.tbdappname.ui.song.list.AllSongsListFragment;
import ch.zhaw.engineering.tbdappname.ui.song.list.SongListFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SongFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SongFragment extends Fragment {

    private SortingListener mListener;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.song_meta_menu, menu);
        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
        search.setMaxWidth((Resources.getSystem().getDisplayMetrics().widthPixels / 2));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mListener.onSearchTextChanged(SortingListener.SortResource.SONGS, newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.song_meta_order_album:
                mListener.onSongSortTypeChanged(SongDao.SortType.ALBUM);
                return true;
            case R.id.song_meta_order_artist:
                mListener.onSongSortTypeChanged(SongDao.SortType.ARTIST);
                return true;
            case R.id.song_meta_order_title:
                mListener.onSongSortTypeChanged(SongDao.SortType.TITLE);
                return true;
            case R.id.song_meta_direction_asc:
                mListener.onSortDirectionChanged(SortingListener.SortResource.SONGS,true);
                return true;
            case R.id.song_meta_direction_desc:
                mListener.onSortDirectionChanged(SortingListener.SortResource.SONGS,false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SortingListener) {
            mListener = (SortingListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SongFragmentListener");
        }
    }
}
