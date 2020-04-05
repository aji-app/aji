package ch.zhaw.engineering.tbdappname.ui.playlist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentPlaylistItemBinding;
import ch.zhaw.engineering.tbdappname.services.database.dto.PlaylistWithSongCount;
import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;

public class PlaylistRecyclerViewAdapter extends RecyclerView.Adapter<PlaylistRecyclerViewAdapter.ViewHolder> {

    private List<PlaylistWithSongCount> mValues;
    private final PlaylistListFragment.PlaylistFragmentListener mListener;
    private final Context mContext;
    private RecyclerView mRecyclerView;
    private Map<Integer, PlaylistWithSongCount> mDeletedPlaylists = new HashMap<>();

    /* package */ PlaylistRecyclerViewAdapter(List<PlaylistWithSongCount> items, PlaylistListFragment.PlaylistFragmentListener listener, Context context) {
        mValues = items;
        mListener = listener;
        mContext = context;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_playlist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    public void onDismiss(int position) {
        final PlaylistWithSongCount playlistToBeRemoved = mValues.get(position);
        mDeletedPlaylists.put(playlistToBeRemoved.getPlaylistId(), playlistToBeRemoved);
        Snackbar snackbar = Snackbar
                .make(mRecyclerView, R.string.playlist_deleted, Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo, view -> {
                    mValues.add(position, playlistToBeRemoved);
                    notifyItemInserted(position);
                    mRecyclerView.scrollToPosition(position);
                }).addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        if (event != DISMISS_EVENT_ACTION && mListener != null) {
                            mListener.onPlaylistDelete(playlistToBeRemoved.getPlaylistId());
                            mDeletedPlaylists.remove(playlistToBeRemoved.getPlaylistId());
                        }
                    }
                });
        snackbar.show();
        mValues.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.playlist = mValues.get(position);
        View root = holder.binding.getRoot();
        Button overFlowButton = holder.binding.playlistItemOverflow;

        holder.binding.playlistName.setText(mValues.get(position).getName());
        int songCount = holder.playlist.getSongCount();
        holder.binding.playlistSongCount.setText(root.getContext().getResources().getQuantityString(R.plurals.song_count, songCount, songCount));

        holder.binding.playlistItemPlay.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onPlaylistPlay(holder.playlist.getPlaylistId());
            }
        });

        root.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onPlaylistSelected(holder.playlist.getPlaylistId());
            }
        });

        overFlowButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(mContext, overFlowButton);
            popup.inflate(R.menu.playlist_item_menu);

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.playlist_menu_play:
                        mListener.onPlaylistPlay(holder.playlist.getPlaylistId());
                        return true;

                    case R.id.playlist_menu_queue:
                        mListener.onPlaylistQueue(holder.playlist.getPlaylistId());
                        return true;

                    case R.id.playlist_menu_edit:
                        mListener.onPlaylistEdit(holder.playlist.getPlaylistId());
                        return true;

                    case R.id.playlist_menu_delete:
                        mListener.onPlaylistDelete(holder.playlist.getPlaylistId());
                        return true;
                    default:
                        return false;
                }
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void updateItems(List<PlaylistWithSongCount> newItems) {
        List<PlaylistWithSongCount> items = new ArrayList<>(newItems.size());
        for (PlaylistWithSongCount item : newItems) {
            if (!mDeletedPlaylists.containsKey(item.getPlaylistId())) {
                items.add(item);
            }
        }
        mValues = items;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final FragmentPlaylistItemBinding binding;
        PlaylistWithSongCount playlist;

        ViewHolder(View view) {
            super(view);
            binding = FragmentPlaylistItemBinding.bind(view);
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + playlist.getName() + "'";
        }
    }
}
