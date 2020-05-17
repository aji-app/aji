package ch.zhaw.engineering.aji.ui.album;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.io.File;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentAlbumDetailsBinding;
import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.dto.AlbumDto;
import ch.zhaw.engineering.aji.ui.FabCallbackListener;
import ch.zhaw.engineering.aji.ui.song.list.AlbumSongListFragment;

public class AlbumDetailsFragment extends Fragment {
    private static final String ARG_ALBUM = "album";
    private String mAlbum;
    private FragmentAlbumDetailsBinding mBinding;
    private AlbumDetailsListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAlbum = getArguments().getString(ARG_ALBUM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentAlbumDetailsBinding.inflate(inflater, container, false);
        mBinding.albumName.setText(mAlbum);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.album_songlist_container, AlbumSongListFragment.newInstance(mAlbum))
                .commit();

        return mBinding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AlbumDetailsListener) {
            mListener = (AlbumDetailsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AlbumDetailsListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        mListener.disableFab();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AsyncTask.execute(() -> {
            SongDao dao = AppDatabase.getInstance(getActivity()).songDao();
            AlbumDto album = dao.getAlbum(mAlbum);
            if (album.getCoverPath() != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Picasso.get().load(new File(album.getCoverPath())).into(mBinding.albumCover);
                });
            }
        });
    }

    public interface AlbumDetailsListener extends FabCallbackListener {
    }
}
