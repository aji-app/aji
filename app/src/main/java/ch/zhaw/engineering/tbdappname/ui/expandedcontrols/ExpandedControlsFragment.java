package ch.zhaw.engineering.tbdappname.ui.expandedcontrols;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

import ch.zhaw.engineering.tbdappname.AudioControlListener;
import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentExpandedControlsBinding;
import ch.zhaw.engineering.tbdappname.services.audio.AudioService;
import ch.zhaw.engineering.tbdappname.ui.song.list.SongListFragment;

import static ch.zhaw.engineering.tbdappname.util.Color.getColorFromAttr;
import static ch.zhaw.engineering.tbdappname.util.Duration.getMillisAsTime;

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
                        mBinding.persistentControlsSonginfo.songItemOverflow.setVisibility(View.GONE);
                        mBinding.persistentControlsSeebar.seekbar.setIndeterminate(true);
                        mBinding.persistentControlsSeebar.seekbar.getThumb().setAlpha(0);
                        disableImageView(mBinding.persistentControlsButtons.btnPrevious);
                        disableImageView(mBinding.persistentControlsButtons.btnNext);
                    } else {
                        mBinding.persistentControlsSonginfo.songItemFavorite.setVisibility(View.VISIBLE);
                        mBinding.persistentControlsSonginfo.songItemOverflow.setVisibility(View.VISIBLE);
                        mBinding.persistentControlsSeebar.timerTotal.setText(getMillisAsTime(info.getDuration()));
                        mBinding.persistentControlsSeebar.seekbar.setIndeterminate(false);
                        mBinding.persistentControlsSeebar.seekbar.getThumb().setAlpha(255);
                        enableImageView(mBinding.persistentControlsButtons.btnPrevious);
                        enableImageView(mBinding.persistentControlsButtons.btnNext);
                    }
                }
            });

            mListener.getRepeatMode().observe(getViewLifecycleOwner(), mode -> {
                ImageButton repeatMode = mBinding.persistentControlsPlaybackmodes.playbackmodeRepeat;
                switch (mode) {
                    case REPEAT_OFF:
                        disableImageView(repeatMode);
                        repeatMode.setImageResource(R.drawable.ic_repeat);
                        break;
                    case REPEAT_ALL:
                        enableImageView(repeatMode);
                        repeatMode.setImageResource(R.drawable.ic_repeat);
                        break;
                    case REPEAT_ONE:
                        enableImageView(repeatMode);
                        repeatMode.setImageResource(R.drawable.ic_repeat_one);
                        break;
                }
            });

            mListener.getAutoQueueEnabled().observe(getViewLifecycleOwner(), enabled -> {
                if (enabled) {
                    enableImageView(mBinding.persistentControlsPlaybackmodes.playbackmodeAutoqueue);
                } else {
                    disableImageView(mBinding.persistentControlsPlaybackmodes.playbackmodeAutoqueue);
                }
            });

            mListener.getShuffleEnabled().observe(getViewLifecycleOwner(), enabled -> {
                if (enabled) {
                    enableImageView(mBinding.persistentControlsPlaybackmodes.playbackmodeShuffle);
                } else {
                    disableImageView(mBinding.persistentControlsPlaybackmodes.playbackmodeShuffle);
                }
            });

            mListener.getCurrentPosition().observe(getViewLifecycleOwner(), position -> {
                mBinding.persistentControlsSeebar.timerElapsed.setText(getMillisAsTime(position));
                mBinding.persistentControlsSeebar.seekbar.setProgress((int) (position / 100000));
            });
        }
    }

    private void disableImageView(ImageView imageView) {
        if (getContext() != null) {
            ImageViewCompat.setImageTintList(
                    imageView,
                    ColorStateList.valueOf(getColorFromAttr(getContext(), R.attr.disabledColor)));
            imageView.setEnabled(false);
        }
    }

    private void enableImageView(ImageView imageView) {
        if (getContext() != null) {
            ImageViewCompat.setImageTintList(
                    imageView,
                    ColorStateList.valueOf(getColorFromAttr(getContext(), R.attr.colorPrimary)));
            imageView.setEnabled(true);
        }
    }

    public interface ExpandedControlsFragmentListener extends AudioControlListener {
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
