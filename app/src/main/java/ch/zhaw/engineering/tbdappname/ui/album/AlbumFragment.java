package ch.zhaw.engineering.tbdappname.ui.album;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.ui.SortingListener;
import ch.zhaw.engineering.tbdappname.ui.library.AlbumArtistListFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlbumFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlbumFragment extends Fragment {
    private SortingListener mListener;

    public static AlbumFragment newInstance() {
        return new AlbumFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.album_list_container, AlbumArtistListFragment.newAlbumInstance())
                .commitNow();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_album, container, false);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SortingListener) {
            mListener = (SortingListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SortingListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.filter_list_menu, menu);
        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
        search.setMaxWidth((Resources.getSystem().getDisplayMetrics().widthPixels / 2));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mListener.onSearchTextChanged(SortingListener.SortResource.ALBUMS, newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.direction_asc:
                mListener.onSortDirectionChanged(SortingListener.SortResource.ALBUMS, true);
                return true;
            case R.id.direction_desc:
                mListener.onSortDirectionChanged(SortingListener.SortResource.ALBUMS, false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
