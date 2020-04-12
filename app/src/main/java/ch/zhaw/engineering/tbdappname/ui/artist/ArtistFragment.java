package ch.zhaw.engineering.tbdappname.ui.artist;

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
import androidx.lifecycle.ViewModelProvider;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.ui.SortResource;
import ch.zhaw.engineering.tbdappname.ui.library.AlbumArtistListFragment;
import ch.zhaw.engineering.tbdappname.ui.menu.MenuHelper;
import ch.zhaw.engineering.tbdappname.ui.viewmodel.AppViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ArtistFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArtistFragment extends Fragment {

    private AppViewModel mAppViewModel;

    public static ArtistFragment newInstance() {
        return new ArtistFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.artist_list_container, AlbumArtistListFragment.newArtistsInstance())
                .commitNow();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artist, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            mAppViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.filter_list_menu, menu);
        MenuHelper.setupSearchView(SortResource.ARTISTS, mAppViewModel, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!MenuHelper.onOptionsItemSelected(SortResource.ARTISTS, mAppViewModel, item)) {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
