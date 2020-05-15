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

import com.google.android.material.snackbar.Snackbar;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentPlaylistDetailsBinding;
import ch.zhaw.engineering.aji.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.aji.services.database.entity.Playlist;
import ch.zhaw.engineering.aji.ui.FabCallbackListener;
import ch.zhaw.engineering.aji.ui.song.list.PlaylistSongListFragment;


public class PlaylistDetailsFragment extends Fragment {
    private static final String ARG_PLAYLIST_ID = "playlist-id";

    private int mPlaylistId;
    private Playlist mPlaylist;
    private PlaylistDetailsFragmentListener mListener;
    private FragmentPlaylistDetailsBinding mBinding;
    private boolean mInEditMode = false;
    private PlaylistSongListFragment mSongListFragment;
    private boolean mPlaylistDeleted;

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

    private void setEditMode(boolean editMode) {
        mInEditMode = editMode;
        mBinding.playlistNameEdittext.setEditMode(mInEditMode);
        mSongListFragment.setEditMode(mInEditMode);
        if (mInEditMode) {
            mListener.configureFab(u -> setEditMode(false), R.drawable.ic_save);
        } else {
            mListener.disableFab();
            saveEditedPlaylist();
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentPlaylistDetailsBinding.inflate(inflater, container, false);
        mBinding.playlistEdit.setOnClickListener(v -> setEditMode(!mInEditMode));

        mBinding.playlistDelete.setOnClickListener(v -> {
            Snackbar snackbar = Snackbar
                    .make(mBinding.getRoot(), R.string.playlist_deleted, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo, view -> {
                    }).addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            if (event != DISMISS_EVENT_ACTION && mListener != null) {
                                mPlaylistDeleted = true;
                                mSongListFragment.setPlaylistDeleted(true);
                                mListener.onPlaylistDelete(mPlaylistId);
                                mListener.onSupportNavigateUp();
                            }
                        }
                    });
            snackbar.show();
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
            mListener.disableFab();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PlaylistDetailsFragmentListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mListener != null) {
            mListener.disableFab();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        saveEditedPlaylist();
        mListener = null;
    }

    private void saveEditedPlaylist() {
        if (mListener != null) {
            if (mBinding.playlistNameEdittext.getText().length() > 0 && !mPlaylistDeleted) {
                mListener.onPlaylistNameChanged(mPlaylistId, mBinding.playlistNameEdittext.getText().toString());
            }
        }
    }

    public interface PlaylistDetailsFragmentListener extends FabCallbackListener {
        void onPlaylistNameChanged(int playlistId, String newName);

        void onPlaylistDelete(int playlistId);

        boolean onSupportNavigateUp();
    }
}
