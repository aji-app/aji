package ch.zhaw.engineering.aji.ui.playlist;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentPlaylistItemBinding;
import ch.zhaw.engineering.aji.databinding.FragmentPlaylistSelectionCreatePlaylistItemBinding;
import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.aji.services.database.entity.Playlist;

public class PlaylistSelectionFragment extends BottomSheetDialogFragment {
    public final static String TAG = "PlaylistSelectionFragment";

    private static final String ARG_SONG_ID = "song-id";
    private PlaylistSelectionListener mListener;
    private long mSongId;
    private RecyclerView mRecyclerView;

    public static PlaylistSelectionFragment newInstance(long songId) {
        final PlaylistSelectionFragment fragment = new PlaylistSelectionFragment();
        final Bundle args = new Bundle();
        args.putLong(ARG_SONG_ID, songId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_context_menu_list, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSongId = getArguments().getLong(ARG_SONG_ID);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) view;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PlaylistSelectionListener) {
            mListener = (PlaylistSelectionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PlaylistSelectionListener");
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            PlaylistDao dao = AppDatabase.getInstance(getActivity()).playlistDao();
            dao.getPlaylistsWhereSongCanBeAdded(mSongId).observe(getViewLifecycleOwner(), playlists -> {
                getActivity().runOnUiThread(() -> {
                    mRecyclerView.setAdapter(new PlaylistSelectionAdapter(playlists, mListener, mSongId));
                });
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private static abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {
        final T binding;

        BaseViewHolder(@NonNull View itemView, T binding) {
            super(itemView);
            this.binding = binding;
        }
    }

    private static class ViewHolder extends BaseViewHolder<FragmentPlaylistItemBinding> {
        ViewHolder(View view) {
            super(view, FragmentPlaylistItemBinding.bind(view));
            binding.playlistItemPlay.setVisibility(View.GONE);
            binding.playlistItemOverflow.setVisibility(View.GONE);
            binding.playlistSongCount.setVisibility(View.GONE);
        }
    }

    private static class CreatePlaylistViewHolder extends BaseViewHolder<FragmentPlaylistSelectionCreatePlaylistItemBinding> {
        CreatePlaylistViewHolder(View view) {
            super(view, FragmentPlaylistSelectionCreatePlaylistItemBinding.bind(view));
        }
    }

    private static class PlaylistSelectionAdapter extends RecyclerView.Adapter<BaseViewHolder> {

        private PlaylistSelectionListener mListener;
        private long mSongId;
        private List<Playlist> mPlaylists;

        PlaylistSelectionAdapter(List<Playlist> playlists, PlaylistSelectionListener listener, long songId) {
            mPlaylists = playlists;
            mListener = listener;
            mSongId = songId;
        }

        @NonNull
        @Override
        public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 1) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_playlist_item, parent, false);
                return new ViewHolder(view);
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_playlist_selection_create_playlist_item, parent, false);
            return new CreatePlaylistViewHolder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            }
            return 1;
        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {
            if (holder.getItemViewType() == 0) {
                CreatePlaylistViewHolder playlistViewHolder = (CreatePlaylistViewHolder) holder;
                playlistViewHolder.binding.createPlaylist.setOnClickListener(v -> {
                    mListener.onCreatePlaylist();
                });
            } else {
                ViewHolder playlistViewHolder = (ViewHolder) holder;
                holder.itemView.setOnClickListener(v -> {
                    mListener.onSongAddToPlaylist(mSongId, mPlaylists.get(position - 1).getPlaylistId());
                });
                playlistViewHolder.binding.playlistName.setText(mPlaylists.get(position - 1).getName());
            }
        }

        @Override
        public int getItemCount() {
            return mPlaylists.size() + 1;
        }

    }

    public interface PlaylistSelectionListener {
        void onSongAddToPlaylist(long songId, int playlistId);

        void onCreatePlaylist();
    }

}
