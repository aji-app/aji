package ch.zhaw.engineering.tbdappname.ui.playlist;

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
import ch.zhaw.engineering.tbdappname.databinding.FragmentPlaylistBinding;
import ch.zhaw.engineering.tbdappname.ui.SortResource;
import ch.zhaw.engineering.tbdappname.ui.menu.MenuHelper;
import ch.zhaw.engineering.tbdappname.ui.viewmodel.AppViewModel;

public class PlaylistFragment extends Fragment {
    private PlaylistFragmentListener mListener;
    private AppViewModel mAppViewModel;

    public static PlaylistFragment newInstance() {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PlaylistFragmentListener) {
            mListener = (PlaylistFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PlaylistFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentPlaylistBinding binding = FragmentPlaylistBinding.inflate(inflater, container, false);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.playlist_container, PlaylistListFragment.newInstance())
                    .commitNow();
        }

        binding.fabAddPlaylist.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCreatePlaylist();
            }
        });

        return binding.getRoot();
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
        MenuHelper.setupSearchView(SortResource.PLAYLISTS, mAppViewModel, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!MenuHelper.onOptionsItemSelected(SortResource.PLAYLISTS, mAppViewModel, item)) {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public interface PlaylistFragmentListener {
        void onCreatePlaylist();
    }
}
