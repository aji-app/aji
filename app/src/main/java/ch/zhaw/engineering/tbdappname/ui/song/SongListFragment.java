package ch.zhaw.engineering.tbdappname.ui.song;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.ui.TbdListFragment;

/**
 * A fragment representing a list of Songs.
 * <p/>
 * Activities containing this fragment MUST implement the {@link SongListFragmentListener}
 * interface.
 */
public class SongListFragment extends TbdListFragment {
    private static final String TAG = "SongListFragment";
    private SongListFragmentListener mListener;
    private SongViewModel mSongViewModel;

    @SuppressWarnings("unused")
    public static SongListFragment newInstance() {
        SongListFragment fragment = new SongListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_list, container, false);

        // Set the adapter
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
        mSongViewModel = new ViewModelProvider(getActivity()).get(SongViewModel.class);
        mSongViewModel.getSongsAndPlaylists().observe(getViewLifecycleOwner(), songsAndPlaylists -> {
            Log.i(TAG, "Updating songs for song fragment");
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    mRecyclerView.setAdapter(new SongRecyclerViewAdapter(songsAndPlaylists.getSongs(), mListener, getActivity(), songsAndPlaylists.getPlaylists()));
                });
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SongListFragmentListener) {
            mListener = (SongListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface SongListFragmentListener {
        void onSongSelected(Song song);

        void onSongPlay(Song song);

        void onSongQueue(Song song);

        void onSongEdit(Song song);

        void onSongAddToPlaylist(Song song, Playlist playlist);

        void onSongDelete(Song song);

        void onCreatePlaylist();

        void onToggleFavorite(Song song);
    }
}
