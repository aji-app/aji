package ch.zhaw.engineering.tbdappname.ui.song;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentSongItemBinding;
import ch.zhaw.engineering.tbdappname.services.database.dto.PlaylistWithSongCount;
import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.util.SwipeToDeleteCallback;

public class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private static final String TAG = "SongRecyclerViewAdapter";

    private final List<Song> mValues;
    private final SongListFragment.SongListFragmentListener mListener;
    private final OnTouchCallbacks mDragStartListener;
    private final Context mContext;
    @Nullable
    private final Integer mPlaylistId;
    private Map<Integer, Playlist> mPlaylists;
    private final Mode mMode;
    private RecyclerView mRecyclerView;
    private boolean mEditMode = false;

    /* package */ SongRecyclerViewAdapter(List<Song> items, SongListFragment.SongListFragmentListener listener, Context context, @NonNull Integer playlistId, OnTouchCallbacks dragListener) {
        this(items, listener, context, null, playlistId, true, dragListener);
    }

    /* package */ SongRecyclerViewAdapter(List<Song> items, SongListFragment.SongListFragmentListener listener, Context context, @Nullable List<PlaylistWithSongCount> playlists) {
        this(items, listener, context, playlists, null, false, null);
    }

    private SongRecyclerViewAdapter(List<Song> items, SongListFragment.SongListFragmentListener listener, Context context, @Nullable List<PlaylistWithSongCount> playlists, @Nullable Integer playlistId, boolean enableDrag, OnTouchCallbacks dragListener) {
        mValues = items;
        mListener = listener;
        mContext = context;
        mPlaylistId = playlistId;
        mDragStartListener = dragListener;
        mMode = enableDrag && playlistId != null ? Mode.PLAYLIST : Mode.ALL_SONGS;
        if (playlists != null) {
            mPlaylists = new HashMap<>(playlists.size());
            for (Playlist pl : playlists) {
                mPlaylists.put(pl.getPlaylistId(), pl);
            }
        }
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_song_item, parent, false);
        return new ViewHolder(view);
    }

    public void setEditMode(boolean editMode) {
        if (mMode == Mode.PLAYLIST) {
            if (mEditMode != editMode) {
                notifyDataSetChanged();
            }
            mEditMode = editMode;
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.song = mValues.get(position);
        holder.binding.songTitle.setText(mValues.get(position).getTitle());
        holder.binding.songArtist.setText(mValues.get(position).getArtist());
        holder.binding.songAlbum.setText(mValues.get(position).getAlbum());

        Button overFlow = holder.binding.songItemOverflow;
        ImageButton favoriteButton = holder.binding.songItemFavorite;
        ImageButton dragHandle = holder.binding.songItemDraghandle;
        if (mEditMode) {
            overFlow.setVisibility(View.GONE);
            favoriteButton.setVisibility(View.GONE);
            dragHandle.setVisibility(View.VISIBLE);
            dragHandle.setBackground(null);

            dragHandle.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mDragStartListener != null) {
                            mDragStartListener.onStartDrag(holder);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        break;
                    default:
                        break;
                }
                return true;
            });
        } else {
            dragHandle.setVisibility(View.GONE);
            overFlow.setVisibility(View.VISIBLE);
            favoriteButton.setVisibility(View.VISIBLE);
            overFlow.setBackground(null);
            favoriteButton.setBackground(null);

            if (holder.song.isFavorite()) {
                favoriteButton.setImageResource(R.drawable.ic_favorite);
            } else {
                favoriteButton.setImageResource(R.drawable.ic_not_favorite);
            }
            favoriteButton.setOnClickListener(v -> mListener.onToggleFavorite(holder.song.getSongId()));

            overFlow.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onSongMenu(holder.song.getSongId());
                }
//                //creating a popup menu
//                PopupMenu popup = new PopupMenu(mContext, overFlow);
//                //inflating menu from xml resource
//                popup.inflate(R.menu.song_item_menu);
//
//                MenuItem addToPlaylist = popup.getMenu().findItem(R.id.song_menu_add_to_playlist);
//                if (mPlaylists == null) {
//                    addToPlaylist.setVisible(false);
//                } else {
//                    SubMenu playlistMenu = addToPlaylist.getSubMenu();
//                    for (Playlist playlist : mPlaylists.values()) {
//                        playlistMenu.add(0, playlist.getPlaylistId(), Menu.NONE, playlist.getName()).setIcon(R.drawable.ic_menu_playlist);
//                    }
//                }
//
//                //adding click listener
//                popup.setOnMenuItemClickListener(item -> {
//                    switch (item.getItemId()) {
//
//                        case R.id.song_menu_play:
//                            mListener.onSongPlay(holder.song.getSongId());
//                            return true;
//                        case R.id.song_menu_queue:
//                            mListener.onSongQueue(holder.song.getSongId());
//                            return true;
//                        case R.id.song_menu_edit:
//                            mListener.onSongEdit(holder.song.getSongId());
//                            return true;
//                        case R.id.song_create_playlist:
//                            mListener.onCreatePlaylist();
//                            return true;
//                        case R.id.song_menu_delete:
//                            mListener.onSongDelete(holder.song.getSongId());
//                            return true;
//                        default:
//                            Playlist selectedPlaylist = mPlaylists.get(item.getItemId());
//                            if (selectedPlaylist != null) {
//                                mListener.onSongAddToPlaylist(holder.song.getSongId(), selectedPlaylist.getPlaylistId());
//                                return true;
//                            }
//                            return false;
//                    }
//                });
//                //displaying the popup
//                popup.show();
            });
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onSongSelected(holder.song.getSongId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (mMode == Mode.PLAYLIST) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mValues, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mValues, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    @Override
    public void onItemDismiss(int position) {
        // TODO: After reorder dismissing break somehow
        if (mMode == Mode.PLAYLIST) {
            final Song songToBeRemoved = mValues.get(position);
            Log.i(TAG, "Removing " + position + ": " + songToBeRemoved.getTitle());
            Snackbar snackbar = Snackbar
                    .make(mRecyclerView, R.string.song_removed_playlist, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo, view -> {
                        mValues.add(position, songToBeRemoved);
                        notifyItemInserted(position);
                        Log.i(TAG, "Restoring " + position + ": " + songToBeRemoved.getTitle());
                        mRecyclerView.scrollToPosition(position);
                    });
            snackbar.show();
            mValues.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Pair<Integer, List<Long>> getModifiedPlaylist() {
        List<Long> songIds = new ArrayList<>(mValues.size());
        for (Song song : mValues) {
            songIds.add(song.getSongId());
        }
        return new Pair<>(mPlaylistId, songIds);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final FragmentSongItemBinding binding;
        Song song;

        ViewHolder(View view) {
            super(view);
            this.binding = FragmentSongItemBinding.bind(view);
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + song.toString() + "'";
        }
    }

    /* package */ static class SimpleItemTouchHelperCallback extends SwipeToDeleteCallback {
        private final ItemTouchHelperAdapter mAdapter;

        public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter, Context context) {
            super(context);
            mAdapter = adapter;
        }

        @Override
        public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder.getAdapterPosition(),
                    target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }
    }

    private enum Mode {
        ALL_SONGS, PLAYLIST
    }

    public interface OnTouchCallbacks {

        /**
         * Called when a view is requesting a start of a drag.
         *
         * @param viewHolder The holder of the view to drag.
         */
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }
}
