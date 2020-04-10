package ch.zhaw.engineering.tbdappname.ui.song;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentSongDetailsBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongDetailsFragment extends Fragment {

    private SongDetailsFragmentListener mListener;
    private FragmentSongDetailsBinding mBinding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding = FragmentSongDetailsBinding.inflate(inflater);
        // Inflate the layout for this fragment
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface SongDetailsFragmentListener {

        void onSongAddToPlaylist(long songId, int playlistId);

        void onSongQueue(long songId);

        void onSongDelete(long songId);

        void onCreatePlaylist();

        void onToggleFavorite(long songId);
    }
}
