package ch.zhaw.engineering.tbdappname.ui.library;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.ui.viewmodel.AppViewModel;
import ch.zhaw.engineering.tbdappname.ui.TbdListFragment;
import ch.zhaw.engineering.tbdappname.ui.album.AlbumRecyclerViewAdapter;
import ch.zhaw.engineering.tbdappname.ui.artist.ArtistRecyclerViewAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumArtistListFragment extends TbdListFragment {
    private static final String ARG_MODE = "mode";

    private Mode mMode;
    private AlbumArtistListFragmentListener mListener;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_artist_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            mRecyclerView.setLayoutManager(layoutManager);
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            final AppViewModel appViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            if (mMode == Mode.ALBUM) {
                appViewModel.getAlbums().observe(getViewLifecycleOwner(), albums -> {
                    AlbumRecyclerViewAdapter adapter = new AlbumRecyclerViewAdapter(albums, mListener);
                    getActivity().runOnUiThread(() -> mRecyclerView.setAdapter(adapter));
                });
            } else if (mMode == Mode.ARTIST) {
                appViewModel.getArtists().observe(getViewLifecycleOwner(), artists -> {
                    ArtistRecyclerViewAdapter adapter = new ArtistRecyclerViewAdapter(artists, mListener);
                    getActivity().runOnUiThread(() -> mRecyclerView.setAdapter(adapter));
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

        void onAlbumMenu(String album);

        void onAlbumSelected(String album);

        void onArtistPlay(String artist);

        void onArtistMenu(String artist);

        void onArtistSelected(String artist);
    }

    private enum Mode {
        ARTIST, ALBUM
    }
}
