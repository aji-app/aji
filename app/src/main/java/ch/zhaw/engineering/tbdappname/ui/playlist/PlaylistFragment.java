package ch.zhaw.engineering.tbdappname.ui.playlist;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentPlaylistBinding;

public class PlaylistFragment extends Fragment {
    private PlaylistFragmentListener mListener;
    private FragmentPlaylistBinding mBinding;

    public static PlaylistFragment newInstance() {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PlaylistFragmentListener) {
            mListener = (PlaylistFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PlaylistFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentPlaylistBinding.inflate(inflater);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.playlist_container, PlaylistListFragment.newInstance())
                    .commitNow();
        }

        mBinding.fabAddPlaylist.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCreatePlaylist();
            }
        });

        return mBinding.getRoot();
    }

    public interface PlaylistFragmentListener {
        void onCreatePlaylist();
    }
}
