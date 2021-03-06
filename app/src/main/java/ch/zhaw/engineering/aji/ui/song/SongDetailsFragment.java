package ch.zhaw.engineering.aji.ui.song;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.squareup.picasso.Picasso;

import java.io.File;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentSongDetailsBinding;
import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.FabCallbackListener;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public class SongDetailsFragment extends Fragment {

    private static final String ARG_SONG_ID = "song-id";
    private SongDetailsFragmentListener mListener;
    private FragmentSongDetailsBinding mBinding;

    private long mSongId;
    private LiveData<Song> mSong;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSongId = getArguments().getLong(ARG_SONG_ID);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentSongDetailsBinding.inflate(inflater, container, false);

        mBinding.deleteSong.setOnClickListener(v -> {
            mListener.onSongDelete(mSongId);
        });

        mBinding.play.setOnClickListener(v -> {
            mListener.onSongPlay(mSongId);
        });

        mBinding.queue.setOnClickListener(v -> {
            mListener.onSongQueue(mSongId);
        });

        mBinding.songItemFavorite.setOnClickListener(v -> {
            mListener.onToggleFavorite(mSongId);
        });

        mBinding.addToPlaylist.setOnClickListener(v -> {
            mListener.onSongAddToPlaylist(mSongId);
        });

        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null) {
            AppViewModel appViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            if (!appViewModel.isTwoPane()) {
                mListener.disableFab();
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SongDetailsFragmentListener) {
            mListener = (SongDetailsFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SongDetailsFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            AsyncTask.execute(() -> {
                SongDao dao = AppDatabase.getInstance(getActivity()).songDao();
                mSong = dao.getSong(mSongId);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (getActivity() != null && getView() != null) {
                            mSong.observe(getViewLifecycleOwner(), song -> {
                                if (song != null) {
                                    mBinding.songTitle.setText(song.getTitle());
                                    mBinding.songArtist.setText(song.getArtist());
                                    mBinding.songAlbum.setText(song.getAlbum());
                                    mBinding.songItemFavorite.setImageResource(song.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_not_favorite);
                                    if (song.getAlbumArtPath() != null) {
                                        Picasso.get().load(new File(song.getAlbumArtPath())).into(mBinding.albumCover);
                                    } else {
                                        mBinding.albumCover.setImageResource(R.drawable.ic_placeholder_image);
                                    }
                                } else {
                                    mListener.onSupportNavigateUp();
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    public interface SongDetailsFragmentListener extends FabCallbackListener {

        void onSongAddToPlaylist(long songId);

        void onSongQueue(long songId);

        void onSongPlay(long songId);

        void onSongDelete(long songId);

        void onToggleFavorite(long songId);

        boolean onSupportNavigateUp();
    }
}
