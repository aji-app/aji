package ch.zhaw.engineering.tbdappname.ui.playlist;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentPlaylistBinding;

public class PlaylistFragment extends Fragment {
    private PlaylistFragmentListener mListener;

    public static PlaylistFragment newInstance() {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PlaylistFragmentListener) {
            mListener = (PlaylistFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PlaylistFragmentListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentPlaylistBinding binding = FragmentPlaylistBinding.inflate(inflater);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.playlist_container, PlaylistListFragment.newInstance())
                    .commitNow();
        }

        binding.fabAddPlaylist.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCreatePlaylist();
            }
        });

        return binding.getRoot();
    }

    public interface PlaylistFragmentListener {
        void onCreatePlaylist();
    }
}
