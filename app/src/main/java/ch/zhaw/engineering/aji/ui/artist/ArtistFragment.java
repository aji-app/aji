package ch.zhaw.engineering.aji.ui.artist;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.ui.FabCallbackListener;
import ch.zhaw.engineering.aji.ui.library.AlbumArtistListFragment;

public class ArtistFragment extends Fragment {

    private ArtistFragmentListener mListener;

    public static ArtistFragment newInstance() {
        return new ArtistFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.artist_list_container, AlbumArtistListFragment.newArtistsInstance())
                .commitNow();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artist, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ArtistFragmentListener) {
            mListener = (ArtistFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SongFragmentListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        configureFab();
    }

    private void configureFab() {
        if (mListener != null) {
            mListener.disableFab();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface ArtistFragmentListener extends FabCallbackListener {
    }
}
