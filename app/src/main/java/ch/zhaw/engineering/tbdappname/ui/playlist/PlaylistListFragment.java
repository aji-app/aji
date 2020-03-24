package ch.zhaw.engineering.tbdappname.ui.playlist;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.services.database.dto.PlaylistWithSongCount;
import ch.zhaw.engineering.tbdappname.ui.TbdListFragment;

public class PlaylistListFragment extends TbdListFragment {
    private static final String TAG = "PlaylistListFragment";
    private PlaylistFragmentListener mListener;

    @SuppressWarnings("unused")
    public static PlaylistListFragment newInstance() {
        PlaylistListFragment fragment = new PlaylistListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_list, container, false);

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
        if (getActivity() != null) {
            PlaylistViewModel playlistViewModel = new ViewModelProvider(getActivity()).get(PlaylistViewModel.class);
            playlistViewModel.getAllPlaylists().observe(getViewLifecycleOwner(), this::onPlaylistsChanged);
        }
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

    private void onPlaylistsChanged(List<PlaylistWithSongCount> playlists) {
        Log.i(TAG, "Updating playlists for playlist list fragment");
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                mRecyclerView.setAdapter(new PlaylistRecyclerViewAdapter(playlists, mListener, getActivity()));
            });
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface PlaylistFragmentListener {
        void onPlaylistSelected(PlaylistWithSongCount playlist);
        void onPlaylistEdit(PlaylistWithSongCount playlist);
        void onPlaylistPlay(PlaylistWithSongCount playlist);
        void onPlaylistQueue(PlaylistWithSongCount playlist);
        void onPlaylistDelete(PlaylistWithSongCount playlist);

    }
}
