package ch.zhaw.engineering.aji.ui.library;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import ch.zhaw.engineering.aji.databinding.FragmentAlbumArtistListBinding;
import ch.zhaw.engineering.aji.ui.ListFragment;
import ch.zhaw.engineering.aji.ui.album.AlbumRecyclerViewAdapter;
import ch.zhaw.engineering.aji.ui.artist.ArtistRecyclerViewAdapter;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public class AlbumArtistListFragment extends ListFragment {
    private static final String ARG_MODE = "mode";

    private Mode mMode;
    private AlbumArtistListFragmentListener mListener;
    private AppViewModel mAppViewModel;
    private FragmentAlbumArtistListBinding mBinding;

    public static AlbumArtistListFragment newArtistsInstance() {
        AlbumArtistListFragment fragment = new AlbumArtistListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MODE, Mode.ARTIST);
        fragment.setArguments(args);
        return fragment;
    }

    public static AlbumArtistListFragment newAlbumInstance() {
        AlbumArtistListFragment fragment = new AlbumArtistListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MODE, Mode.ALBUM);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_MODE)) {
            mMode = (Mode) getArguments().getSerializable(ARG_MODE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentAlbumArtistListBinding.inflate(inflater, container, false);
        mRecyclerView = mBinding.list;
        LinearLayoutManager layoutManager = new LinearLayoutManager(mRecyclerView.getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            mAppViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            mAppViewModel.getPlaceholderText().observe(getViewLifecycleOwner(), text -> {
                mBinding.songPrompt.setText(text);
            });
            if (mMode == Mode.ALBUM) {
                mAppViewModel.getAlbums().observe(getViewLifecycleOwner(), albums -> {
                    AlbumRecyclerViewAdapter adapter = new AlbumRecyclerViewAdapter(albums, mListener, mAppViewModel.showHiddenSongs());
                    getActivity().runOnUiThread(() -> {
                        mBinding.songPrompt.setVisibility(!albums.isEmpty() || mAppViewModel.isTwoPane() ? View.GONE : View.VISIBLE);
                        mRecyclerView.setAdapter(adapter);
                    });
                });
            } else if (mMode == Mode.ARTIST) {
                mAppViewModel.getArtists().observe(getViewLifecycleOwner(), artists -> {
                    ArtistRecyclerViewAdapter adapter = new ArtistRecyclerViewAdapter(artists, mListener, mAppViewModel.showHiddenSongs());
                    getActivity().runOnUiThread(() -> {
                        mBinding.songPrompt.setVisibility(!artists.isEmpty() || mAppViewModel.isTwoPane() ? View.GONE : View.VISIBLE);
                        mRecyclerView.setAdapter(adapter);
                    });
                });
            }
        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AlbumArtistListFragmentListener) {
            mListener = (AlbumArtistListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AlbumArtistListFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface AlbumArtistListFragmentListener {
        void onAlbumPlay(String album);

        void onAlbumQueue(String album);

        void onAlbumMenu(String album);

        void onAlbumSelected(String album);

        void onArtistPlay(String artist);

        void onArtistQueue(String artist);

        void onArtistMenu(String artist);

        void onArtistSelected(String artist);
    }

    private enum Mode {
        ARTIST, ALBUM
    }
}
