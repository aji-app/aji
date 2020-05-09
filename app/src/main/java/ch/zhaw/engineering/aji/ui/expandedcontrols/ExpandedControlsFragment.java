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
    private AudioService.PlayState mPlaybackState;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentExpandedControlsBinding.inflate(inflater, container, false);

        mBinding.btnNext.setOnClickListener(v -> mListener.onNext());
        mBinding.btnPlaypause.setOnClickListener(v -> mListener.onPlayPause());
        mBinding.btnPrevious.setOnClickListener(v -> mListener.onPrevious());
        mBinding.btnStop.setOnClickListener(v -> mListener.onStopPlayback());

        mBinding.playbackmodeAutoqueue.setOnClickListener(v -> mListener.onToggleAutoQueue());
        mBinding.playbackmodeRepeat.setOnClickListener(v -> mListener.onChangeRepeatMode());
        mBinding.playbackmodeShuffle.setOnClickListener(v -> mListener.onToggleShuffle());

        mSeekbarBackground = mBinding.seekbar.getBackground();

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
                mPlaybackState = state;
                switch (state) {
                    case PAUSED:
                        mBinding.btnPlaypause.setImageResource(R.drawable.ic_play);
                        mBinding.seekbar.setEnabled(true);
                        enableImageView(mBinding.btnPlaypause);
                        enableImageView(mBinding.btnStop);
                        if (!isRadio) {
                            enableImageView(mBinding.btnPrevious);
                            enableImageView(mBinding.btnNext);
                        }
                        break;
                    case PLAYING:
                        mBinding.btnPlaypause.setImageResource(R.drawable.ic_pause);
                        mBinding.seekbar.setEnabled(true);
                        enableImageView(mBinding.btnPlaypause);
                        enableImageView(mBinding.btnStop);
                        if (!isRadio) {
                            enableImageView(mBinding.btnPrevious);
                            enableImageView(mBinding.btnNext);
                        }
                        break;
                    case STOPPED:
                    case INITIAL:
                        mBinding.btnPlaypause.setImageResource(R.drawable.ic_play);
                        mBinding.persistentControlsAlbumcover.setImageResource(R.drawable.ic_placeholder_image);
                        setSeekbarProgress(0);
                        disableImageView(mBinding.btnPlaypause, true);
                        disableImageView(mBinding.btnPrevious, true);
                        disableImageView(mBinding.btnNext, true);
                        disableImageView(mBinding.btnStop, true);
                        mBinding.seekbar.setEnabled(false);

                        break;
                }
            });

            mBinding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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
                    song -> mBinding.songItemFavorite.setImageResource(song.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_not_favorite)
            );

            mListener.getCurrentQueue().observe(getViewLifecycleOwner(), songs -> {
                if (songs != null && songs.size() > 0) {
                    enableImageView(mBinding.btnPlaypause);
                }
            });

            mListener.getCurrentSong().observe(getViewLifecycleOwner(), info -> {
                mBinding.songItemFavorite.setOnClickListener(v -> mListener.onToggleFavorite(info.getId()));
                mBinding.songItemOverflow.setOnClickListener(v -> mListener.onSongMenu(info.getId()));

                updateRepeatButton(mListener.getRepeatMode().getValue());
                updateShuffleButton(mListener.getShuffleEnabled().getValue());
                updateAutoQueueButton(mListener.getAutoQueueEnabled().getValue());

                if (info != null) {
                    isRadio = info.isRadio();
                    if (isRadio) {
                        mBinding.songItemFavorite.setImageResource(R.drawable.ic_not_favorite);
                    }

                    mBinding.songTitleExpanded.setText(info.getTitle());
                    mBinding.songAlbumExpanded.setText(info.getAlbum());
                    mBinding.songArtistExpanded.setText(info.getArtist());
                    mBinding.playbackmodeAutoqueue.setEnabled(true);
                    mBinding.playbackmodeRepeat.setEnabled(true);
                    mBinding.playbackmodeShuffle.setEnabled(true);

                    if (info.getAlbumPath() != null) {
                        Picasso.get().load(new File(info.getAlbumPath())).into(mBinding.persistentControlsAlbumcover);
                    } else {
                        mBinding.persistentControlsAlbumcover.setImageResource(R.drawable.ic_placeholder_image);
                    }

                    if (isRadio) {
                        mBinding.timerTotal.setText(R.string.unknown_duration);
                        mBinding.songItemFavorite.setVisibility(View.GONE);
                        mBinding.songItemOverflow.setVisibility(View.GONE);
                        mBinding.seekbar.setIndeterminate(true);
                        mBinding.seekbar.getThumb().setAlpha(0);
                        mBinding.seekbar.setBackground(null);
                        disableImageView(mBinding.btnPrevious, true);
                        disableImageView(mBinding.btnNext, true);
                        enableImageView(mBinding.btnStop);
                    } else {
                        mBinding.songItemFavorite.setVisibility(View.VISIBLE);
                        mBinding.songItemOverflow.setVisibility(View.VISIBLE);
                        mBinding.timerTotal.setText(getMillisAsTime(info.getDuration()));
                        mBinding.seekbar.setIndeterminate(false);
                        mBinding.seekbar.getThumb().setAlpha(255);
                        mBinding.seekbar.setBackground(mSeekbarBackground);
                        mBinding.seekbar.setMax((int) info.getDuration());
                        enableImageView(mBinding.btnPrevious);
                        enableImageView(mBinding.btnNext);
                        enableImageView(mBinding.btnStop);
                    }
                } else {
                    mBinding.songTitleExpanded.setText(R.string.not_playing);
                    mBinding.songAlbumExpanded.setText(null);
                    mBinding.songArtistExpanded.setText(null);
                    mBinding.songItemFavorite.setVisibility(View.GONE);
                    mBinding.songItemOverflow.setVisibility(View.GONE);
                    mBinding.timerTotal.setText(null);
                    mBinding.timerElapsed.setText(null);
                    disableImageView(mBinding.playbackmodeAutoqueue, false);
                    disableImageView(mBinding.playbackmodeShuffle, false);
                    disableImageView(mBinding.playbackmodeRepeat, false);
                    disableImageView(mBinding.btnStop, false);
                }
            });

            mListener.getRepeatMode().observe(getViewLifecycleOwner(), this::updateRepeatButton);

            mListener.getAutoQueueEnabled().observe(getViewLifecycleOwner(), this::updateAutoQueueButton);

            mListener.getShuffleEnabled().observe(getViewLifecycleOwner(), this::updateShuffleButton);

            mListener.getCurrentPosition().observe(getViewLifecycleOwner(), position -> {
                if (!mSeeking && !isRadio && AudioService.PlayState.INITIAL!= mPlaybackState && AudioService.PlayState.STOPPED != mPlaybackState) {
                    setSeekbarProgress(position.intValue());
                }
            });
        }
    }

    private void updateAutoQueueButton(Boolean enabled) {
        enabled = enabled == null ? false : enabled;
        if (enabled) {
            enableImageView(mBinding.playbackmodeAutoqueue);
        } else {
            disableImageView(mBinding.playbackmodeAutoqueue, false);
        }
    }

    private void updateShuffleButton(Boolean enabled) {
        enabled = enabled == null ? false : enabled;
        if (enabled) {
            enableImageView(mBinding.playbackmodeShuffle);
        } else {
            disableImageView(mBinding.playbackmodeShuffle, false);
        }
    }

    private void updateRepeatButton(@Nullable AudioBackend.RepeatModes mode) {
        ImageButton repeatMode = mBinding.playbackmodeRepeat;
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
        mBinding.seekbar.setProgress(progress);
        mBinding.timerElapsed.setText(getMillisAsTime(progress));
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

        void onStopPlayback();

        void onToggleShuffle();

        void onChangeRepeatMode();

        void onToggleAutoQueue();

        void seek(long position);
    }
}
