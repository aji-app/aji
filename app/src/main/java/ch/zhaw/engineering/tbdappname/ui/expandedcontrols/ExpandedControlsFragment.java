package ch.zhaw.engineering.tbdappname.ui.expandedcontrols;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentExpandedControlsBinding;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistFragment;
import ch.zhaw.engineering.tbdappname.ui.song.list.SongListFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExpandedControlsFragment extends Fragment {

    private FragmentExpandedControlsBinding mBinding;
    private ExpandedControlsFragmentListener mListener;

    public ExpandedControlsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentExpandedControlsBinding.inflate(inflater, container, false);

        mBinding.persistentControlsButtons.btnNext.setOnClickListener(v -> mListener.onNext());
        mBinding.persistentControlsButtons.btnPlaypause.setOnClickListener(v -> mListener.onPlayPause());
        mBinding.persistentControlsButtons.btnPrevious.setOnClickListener(v -> mListener.onPrevious());

        mBinding.persistentControlsPlaybackmodes.playbackmodeAutoqueue.setOnClickListener(v -> mListener.onToggleAutoQueue());
        mBinding.persistentControlsPlaybackmodes.playbackmodeRepeat.setOnClickListener(v -> mListener.onChangeRepeatMode());
        mBinding.persistentControlsPlaybackmodes.playbackmodeShuffle.setOnClickListener(v -> mListener.onToggleShuffle());

        mBinding.persistentControlsSonginfo.songItemFavorite.setOnClickListener(v -> mListener.onToggleFavorite(1 /* TODO: Current Song ID */));
        mBinding.persistentControlsSonginfo.songItemOverflow.setOnClickListener(v -> mListener.onSongMenu(1 /* TODO: Current Song ID */, SongListFragment.SongSelectionOrigin.EXPANDED_CONTROLS));

        return mBinding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ExpandedControlsFragmentListener) {
            mListener = (ExpandedControlsFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ExpandedControlsFragmentListener");
        }
    }

    public interface ExpandedControlsFragmentListener {
        void onToggleFavorite(long songId);

        void onSongMenu(long songId, SongListFragment.SongSelectionOrigin origin);

        void onPlayPause();

        void onNext();

        void onPrevious();

        void onToggleShuffle();

        void onChangeRepeatMode();

        void onToggleAutoQueue();
    }
}
