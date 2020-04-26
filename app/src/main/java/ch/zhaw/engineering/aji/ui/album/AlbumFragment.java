package ch.zhaw.engineering.aji.ui.album;

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
import ch.zhaw.engineering.aji.ui.SortResource;
import ch.zhaw.engineering.aji.ui.library.AlbumArtistListFragment;
import ch.zhaw.engineering.aji.ui.menu.MenuHelper;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlbumFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlbumFragment extends Fragment {
    private AppViewModel mAppViewModel;

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
        MenuHelper.setupSearchView(SortResource.ALBUMS, mAppViewModel, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!MenuHelper.onOptionsItemSelected(SortResource.ALBUMS, mAppViewModel, item)) {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
