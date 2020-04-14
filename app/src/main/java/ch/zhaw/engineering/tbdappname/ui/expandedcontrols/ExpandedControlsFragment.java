package ch.zhaw.engineering.tbdappname.ui.expandedcontrols;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentExpandedControlsBinding;
import ch.zhaw.engineering.tbdappname.services.audio.AudioService;
import ch.zhaw.engineering.tbdappname.ui.song.list.SongListFragment;

import static ch.zhaw.engineering.tbdappname.util.Duration.getMillisAsTime;
import static ch.zhaw.engineering.tbdappname.util.Duration.getPositionDurationString;

public class ExpandedControlsFragment extends Fragment {

    private FragmentExpandedControlsBinding mBinding;
    private ExpandedControlsFragmentListener mListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            mListener.getPlayState().observe(getViewLifecycleOwner(), state -> {
                if (AudioService.PlayState.PLAYING == state) {
                    mBinding.persistentControlsButtons.btnPlaypause.setImageResource(R.drawable.ic_pause);
                } else {
                    mBinding.persistentControlsButtons.btnPlaypause.setImageResource(R.drawable.ic_play);
                }
            });
            mListener.getCurrentSong().observe(getViewLifecycleOwner(), info -> {
                if (info != null) {
                    mBinding.persistentControlsSonginfo.songTitleExpanded.setText(info.getTitle());
                    mBinding.persistentControlsSonginfo.songAlbumExpanded.setText(info.getAlbum());
                    mBinding.persistentControlsSonginfo.songArtistExpanded.setText(info.getArtist());
                    if (info.isRadio()) {
                        mBinding.persistentControlsSeebar.timerTotal.setText(R.string.unknown_duration);
                        mBinding.persistentControlsSonginfo.songItemFavorite.setVisibility(View.GONE);
                        mBinding.persistentControlsSeebar.seekbar.setIndeterminate(true);
                        mBinding.persistentControlsSeebar.seekbar.getThumb().setAlpha(0);
                    } else {
                        mBinding.persistentControlsSonginfo.songItemFavorite.setVisibility(View.VISIBLE);
                        mBinding.persistentControlsSeebar.timerTotal.setText(getMillisAsTime(info.getDuration()));
                        mBinding.persistentControlsSeebar.seekbar.setIndeterminate(false);
                        mBinding.persistentControlsSeebar.seekbar.getThumb().setAlpha(255);
                    }
                }
            });

            mListener.getCurrentPosition().observe(getViewLifecycleOwner(), position -> {
                mBinding.persistentControlsSeebar.timerElapsed.setText(getMillisAsTime(position));
                mBinding.persistentControlsSeebar.seekbar.setProgress((int) (position / 100000));
            });
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

        LiveData<AudioService.PlayState> getPlayState();

        LiveData<AudioService.SongInformation> getCurrentSong();

        LiveData<Long> getCurrentPosition();
    }
}
