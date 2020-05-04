package ch.zhaw.engineering.aji.ui.expandedcontrols;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.squareup.picasso.Picasso;

import java.io.File;

import ch.zhaw.engineering.aji.AudioControlListener;
import ch.zhaw.engineering.aji.FragmentInteractionActivity;
import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentExpandedControlsBinding;
import ch.zhaw.engineering.aji.services.audio.AudioService;
import ch.zhaw.engineering.aji.services.audio.backend.AudioBackend;
import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.song.list.QueueSongListFragment;
import lombok.AllArgsConstructor;

import static ch.zhaw.engineering.aji.util.Color.getColorFromAttr;
import static ch.zhaw.engineering.aji.util.Duration.getMillisAsTime;

public class ExpandedControlsFragment extends Fragment {

    private static final String TAG = "ExpandedControls";
    private FragmentExpandedControlsBinding mBinding;
    private ExpandedControlsFragmentListener mListener;
    private boolean mSeeking = false;
    private boolean isRadio;

    private Drawable mSeekbarBackground;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentExpandedControlsBinding.inflate(inflater, container, false);

        mBinding.persistentControlsButtons.btnNext.setOnClickListener(v -> mListener.onNext());
        mBinding.persistentControlsButtons.btnPlaypause.setOnClickListener(v -> mListener.onPlayPause());
        mBinding.persistentControlsButtons.btnPrevious.setOnClickListener(v -> mListener.onPrevious());

        mBinding.persistentControlsPlaybackmodes.playbackmodeAutoqueue.setOnClickListener(v -> mListener.onToggleAutoQueue());
        mBinding.persistentControlsPlaybackmodes.playbackmodeRepeat.setOnClickListener(v -> mListener.onChangeRepeatMode());
        mBinding.persistentControlsPlaybackmodes.playbackmodeShuffle.setOnClickListener(v -> mListener.onToggleShuffle());

        mSeekbarBackground = mBinding.persistentControlsSeebar.seekbar.getBackground();

        getChildFragmentManager().beginTransaction()
                .replace(R.id.current_queue_container, QueueSongListFragment.newInstance())
                .commit();

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
                switch (state) {
                    case PAUSED:
                        mBinding.persistentControlsButtons.btnPlaypause.setImageResource(R.drawable.ic_play);
                        mBinding.persistentControlsSeebar.seekbar.setEnabled(true);
                        enableImageView(mBinding.persistentControlsButtons.btnPlaypause);
                        if (!isRadio) {
                            enableImageView(mBinding.persistentControlsButtons.btnPrevious);
                            enableImageView(mBinding.persistentControlsButtons.btnNext);
                        }
                        break;
                    case PLAYING:
                        mBinding.persistentControlsButtons.btnPlaypause.setImageResource(R.drawable.ic_pause);
                        mBinding.persistentControlsSeebar.seekbar.setEnabled(true);
                        enableImageView(mBinding.persistentControlsButtons.btnPlaypause);
                        if (!isRadio) {
                            enableImageView(mBinding.persistentControlsButtons.btnPrevious);
                            enableImageView(mBinding.persistentControlsButtons.btnNext);
                        }
                        break;
                    case STOPPED:
                    case INITIAL:
                        mBinding.persistentControlsButtons.btnPlaypause.setImageResource(R.drawable.ic_play);
                        disableImageView(mBinding.persistentControlsButtons.btnPlaypause, true);
                        disableImageView(mBinding.persistentControlsButtons.btnPrevious, true);
                        disableImageView(mBinding.persistentControlsButtons.btnNext, true);
                        mBinding.persistentControlsSeebar.seekbar.setEnabled(false);

                        break;
                }
            });

            mBinding.persistentControlsSeebar.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        setSeekbarProgress(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mSeeking = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (!isRadio) {
                        mListener.seek(seekBar.getProgress());
                    }
                    mSeeking = false;
                }
            });

            SongDao songDao = AppDatabase.getInstance(getActivity()).songDao();
            Transformations.switchMap(mListener.getCurrentSong(),
                    info -> info == null || info.isRadio() ? null : songDao.getSong(info.getId())
            ).observe(getViewLifecycleOwner(),
                    song -> mBinding.persistentControlsSonginfo.songItemFavorite.setImageResource(song.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_not_favorite)
            );

            mListener.getCurrentSong().observe(getViewLifecycleOwner(), info -> {
                mBinding.persistentControlsSonginfo.songItemFavorite.setOnClickListener(v -> mListener.onToggleFavorite(info.getId()));
                mBinding.persistentControlsSonginfo.songItemOverflow.setOnClickListener(v -> mListener.onSongMenu(info.getId()));

                updateRepeatButton(mListener.getRepeatMode().getValue());
                updateShuffleButton(mListener.getShuffleEnabled().getValue());
                updateAutoQueueButton(mListener.getAutoQueueEnabled().getValue());

                if (info != null) {
                    isRadio = info.isRadio();
                    if (isRadio) {
                        mBinding.persistentControlsSonginfo.songItemFavorite.setImageResource(R.drawable.ic_not_favorite);
                    }

                    mBinding.persistentControlsSonginfo.songTitleExpanded.setText(info.getTitle());
                    mBinding.persistentControlsSonginfo.songAlbumExpanded.setText(info.getAlbum());
                    mBinding.persistentControlsSonginfo.songArtistExpanded.setText(info.getArtist());
                    mBinding.persistentControlsPlaybackmodes.playbackmodeAutoqueue.setEnabled(true);
                    mBinding.persistentControlsPlaybackmodes.playbackmodeRepeat.setEnabled(true);
                    mBinding.persistentControlsPlaybackmodes.playbackmodeShuffle.setEnabled(true);

                    if (info.getAlbumPath() != null) {
                        Picasso.get().load(new File(info.getAlbumPath())).into(mBinding.persistentControlsSonginfo.persistentControlsAlbumcover);
                    } else {
                        mBinding.persistentControlsSonginfo.persistentControlsAlbumcover.setImageResource(R.drawable.ic_placeholder_image);
                    }

                    if (isRadio) {
                        mBinding.persistentControlsSeebar.timerTotal.setText(R.string.unknown_duration);
                        mBinding.persistentControlsSonginfo.songItemFavorite.setVisibility(View.GONE);
                        mBinding.persistentControlsSonginfo.songItemOverflow.setVisibility(View.GONE);
                        mBinding.persistentControlsSeebar.seekbar.setIndeterminate(true);
                        mBinding.persistentControlsSeebar.seekbar.getThumb().setAlpha(0);
                        mBinding.persistentControlsSeebar.seekbar.setBackground(null);
                        disableImageView(mBinding.persistentControlsButtons.btnPrevious, true);
                        disableImageView(mBinding.persistentControlsButtons.btnNext, true);
                    } else {
                        mBinding.persistentControlsSonginfo.songItemFavorite.setVisibility(View.VISIBLE);
                        mBinding.persistentControlsSonginfo.songItemOverflow.setVisibility(View.VISIBLE);
                        mBinding.persistentControlsSeebar.timerTotal.setText(getMillisAsTime(info.getDuration()));
                        mBinding.persistentControlsSeebar.seekbar.setIndeterminate(false);
                        mBinding.persistentControlsSeebar.seekbar.getThumb().setAlpha(255);
                        mBinding.persistentControlsSeebar.seekbar.setBackground(mSeekbarBackground);
                        mBinding.persistentControlsSeebar.seekbar.setMax((int) info.getDuration());
                        enableImageView(mBinding.persistentControlsButtons.btnPrevious);
                        enableImageView(mBinding.persistentControlsButtons.btnNext);
                    }
                } else {
                    mBinding.persistentControlsSonginfo.songTitleExpanded.setText(R.string.not_playing);
                    mBinding.persistentControlsSonginfo.songAlbumExpanded.setText(null);
                    mBinding.persistentControlsSonginfo.songArtistExpanded.setText(null);
                    mBinding.persistentControlsSonginfo.songItemFavorite.setVisibility(View.GONE);
                    mBinding.persistentControlsSonginfo.songItemOverflow.setVisibility(View.GONE);
                    mBinding.persistentControlsSeebar.timerTotal.setText(null);
                    mBinding.persistentControlsSeebar.timerElapsed.setText(null);
                    disableImageView(mBinding.persistentControlsPlaybackmodes.playbackmodeAutoqueue, false);
                    disableImageView(mBinding.persistentControlsPlaybackmodes.playbackmodeShuffle, false);
                    disableImageView(mBinding.persistentControlsPlaybackmodes.playbackmodeRepeat, false);
                }
            });

            mListener.getRepeatMode().observe(getViewLifecycleOwner(), this::updateRepeatButton);

            mListener.getAutoQueueEnabled().observe(getViewLifecycleOwner(), this::updateAutoQueueButton);

            mListener.getShuffleEnabled().observe(getViewLifecycleOwner(), this::updateShuffleButton);

            mListener.getCurrentPosition().observe(getViewLifecycleOwner(), position -> {
                if (!mSeeking && !isRadio) {
                    setSeekbarProgress(position.intValue());
                }
            });
        }
    }

    private void updateAutoQueueButton(Boolean enabled) {
        enabled = enabled == null ? false : enabled;
        if (enabled) {
            enableImageView(mBinding.persistentControlsPlaybackmodes.playbackmodeAutoqueue);
        } else {
            disableImageView(mBinding.persistentControlsPlaybackmodes.playbackmodeAutoqueue, false);
        }
    }

    private void updateShuffleButton(Boolean enabled) {
        enabled = enabled == null ? false : enabled;
        if (enabled) {
            enableImageView(mBinding.persistentControlsPlaybackmodes.playbackmodeShuffle);
        } else {
            disableImageView(mBinding.persistentControlsPlaybackmodes.playbackmodeShuffle, false);
        }
    }

    private void updateRepeatButton(@Nullable AudioBackend.RepeatModes mode) {
        ImageButton repeatMode = mBinding.persistentControlsPlaybackmodes.playbackmodeRepeat;
        if (mode == null) {
            mode = AudioBackend.RepeatModes.REPEAT_OFF;
        }
        switch (mode) {
            case REPEAT_ALL:
                enableImageView(repeatMode);
                repeatMode.setImageResource(R.drawable.ic_repeat);
                break;
            case REPEAT_ONE:
                enableImageView(repeatMode);
                repeatMode.setImageResource(R.drawable.ic_repeat_one);
                break;
            case REPEAT_OFF:
                disableImageView(repeatMode, false);
                repeatMode.setImageResource(R.drawable.ic_repeat);
                break;
        }
    }

    private void setSeekbarProgress(int progress) {
        mBinding.persistentControlsSeebar.seekbar.setProgress(progress);
        mBinding.persistentControlsSeebar.timerElapsed.setText(getMillisAsTime(progress));
    }

    private void disableImageView(ImageView imageView, boolean disable) {
        if (getContext() != null) {
            ImageViewCompat.setImageTintList(
                    imageView,
                    ColorStateList.valueOf(getColorFromAttr(getContext(), R.attr.disabledColor)));
        }
        if (disable) {
            imageView.setEnabled(false);
        }
    }

    private void enableImageView(ImageView imageView) {
        if (getContext() != null) {
            ImageViewCompat.setImageTintList(
                    imageView,
                    ColorStateList.valueOf(getColorFromAttr(getContext(), R.attr.colorPrimary)));
        }
        imageView.setEnabled(true);
    }

    public interface ExpandedControlsFragmentListener extends AudioControlListener {
        void onToggleFavorite(long songId);

        void onSongMenu(long songId, FragmentInteractionActivity.ContextMenuItem... additionalItems);

        void onPlayPause();

        void onNext();

        void onPrevious();

        void onToggleShuffle();

        void onChangeRepeatMode();

        void onToggleAutoQueue();

        void seek(long position);
    }

    @AllArgsConstructor
    private static class SongInfo {
        AudioService.SongInformation info;
        LiveData<Song> song;
        LiveData<RadioStationDto> radioStation;
    }
}
