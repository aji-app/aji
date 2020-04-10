package ch.zhaw.engineering.tbdappname.ui.song;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentSongDetailsBinding;
import ch.zhaw.engineering.tbdappname.services.database.AppDatabase;
import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;

/**
 * A simple {@link Fragment} subclass.
 */
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding = FragmentSongDetailsBinding.inflate(inflater, container, false);

        mBinding.deleteSong.setOnClickListener(v -> {
            mListener.onSongDelete(mSongId);
        });

        mBinding.songItemFavorite.setOnClickListener(v -> {
            mListener.onToggleFavorite(mSongId);
        });

        return mBinding.getRoot();
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            AsyncTask.execute(() -> {
                SongDao dao = AppDatabase.getInstance(getActivity()).songDao();
                mSong = dao.getSong(mSongId);
                getActivity().runOnUiThread(() -> {
                    mSong.observe(getViewLifecycleOwner(), song -> {
                        mBinding.songTitle.setText(song.getTitle());
                        mBinding.songArtist.setText(song.getArtist());
                        mBinding.songAlbum.setText(song.getAlbum());
                        mBinding.songItemFavorite.setImageResource(song.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_not_favorite);
                    });
                });
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface SongDetailsFragmentListener {

        void onSongAddToPlaylist(long songId, int playlistId);

        void onSongQueue(long songId);

        void onSongDelete(long songId);

        void onToggleFavorite(long songId);
    }
}
