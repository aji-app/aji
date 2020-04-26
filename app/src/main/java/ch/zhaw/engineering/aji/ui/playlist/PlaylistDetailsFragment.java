package ch.zhaw.engineering.aji.ui.playlist;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentPlaylistDetailsBinding;
import ch.zhaw.engineering.aji.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.aji.services.database.entity.Playlist;
import ch.zhaw.engineering.aji.ui.song.list.PlaylistSongListFragment;
import ch.zhaw.engineering.aji.ui.song.list.SongListFragment;


public class PlaylistDetailsFragment extends Fragment {
    private static final String ARG_PLAYLIST_ID = "playlist-id";

    private int mPlaylistId;
    private Playlist mPlaylist;
    private PlaylistDetailsFragmentListener mListener;
    private FragmentPlaylistDetailsBinding mBinding;
    private boolean mInEditMode = false;
    private SongListFragment mSongListFragment;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param playlistId Playlist ID
     * @return A new instance of fragment PlaylistDetailsFragment.
     */
    public static PlaylistDetailsFragment newInstance(int playlistId) {
        PlaylistDetailsFragment fragment = new PlaylistDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PLAYLIST_ID, playlistId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlaylistId = getArguments().getInt(ARG_PLAYLIST_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentPlaylistDetailsBinding.inflate(inflater, container, false);
        mBinding.playlistEdit.setOnClickListener(v -> {
            mInEditMode = !mInEditMode;
            mBinding.playlistNameEdittext.setEditMode(mInEditMode);
            mSongListFragment.setEditMode(mInEditMode);
            if (!mInEditMode) {
                notifyListenerNameUpdate();
            }
        });

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            AsyncTask.execute(() -> {
                PlaylistDao playlistDao = PlaylistDao.getInstance(getActivity());
                mPlaylist = playlistDao.getPlaylistById(mPlaylistId);

                getActivity().runOnUiThread(() -> {
//                    mBinding.playlistName.setText(mPlaylist.getName());
//                    mBinding.playlistNameEdit.setText(mPlaylist.getName());
                    mBinding.playlistNameEdittext.setText(mPlaylist.getName());
                    mSongListFragment = PlaylistSongListFragment.newInstance(mPlaylistId);
                    getChildFragmentManager().beginTransaction()
                            .replace(R.id.songlist_container, mSongListFragment)
                            .commitNow();
                });
            });
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PlaylistDetailsFragmentListener) {
            mListener = (PlaylistDetailsFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PlaylistDetailsFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        notifyListenerNameUpdate();
        mListener = null;
    }

    private void notifyListenerNameUpdate() {
        if (mBinding.playlistNameEdittext.getText().length() > 0 && mListener != null) {
            mListener.onPlaylistNameChanged(mPlaylistId, mBinding.playlistNameEdittext.getText().toString());
        }
    }

    public interface PlaylistDetailsFragmentListener {
        void onPlaylistNameChanged(int playlistId, String newName);
    }
}