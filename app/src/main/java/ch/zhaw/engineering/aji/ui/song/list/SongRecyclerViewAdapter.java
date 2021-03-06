package ch.zhaw.engineering.aji.ui.song.list;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.util.Pair;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentSongItemBinding;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.util.Color;
import ch.zhaw.engineering.aji.util.SwipeToDeleteCallback;

public class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private static final String TAG = "SongRecyclerViewAdapter";

    private List<Song> mValues;
    private final SongListFragment.SongListFragmentListener mListener;
    private final OnTouchCallbacks mDragStartListener;
    @Nullable
    private final Integer mPlaylistId;
    private final Mode mMode;
    private RecyclerView mRecyclerView;
    private boolean mEditMode = false;
    private Long mHighlightedSongId;
    private Integer mHighlightedSongPosition;
    private boolean mShowFavoriteButton;

    /* package */ SongRecyclerViewAdapter(List<Song> items, SongListFragment.SongListFragmentListener listener, @NonNull Integer playlistId, OnTouchCallbacks dragListener) {
        this(items, listener, playlistId, dragListener != null, dragListener, true);
    }

    /* package */ SongRecyclerViewAdapter(List<Song> items, SongListFragment.SongListFragmentListener listener, OnTouchCallbacks dragListener) {
        this(items, listener, null, dragListener != null, dragListener, true);
    }

    /* package */ SongRecyclerViewAdapter(List<Song> items, SongListFragment.SongListFragmentListener listener, boolean showFavoriteButton) {
        this(items, listener, null, false, null, showFavoriteButton);
    }
    /* package */ SongRecyclerViewAdapter(List<Song> items, SongListFragment.SongListFragmentListener listener) {
        this(items, listener, null, false, null, true);
    }

    private SongRecyclerViewAdapter(List<Song> items, SongListFragment.SongListFragmentListener listener, @Nullable Integer playlistId, boolean enableDrag, OnTouchCallbacks dragListener, boolean showFavoriteButton) {
        mValues = items;
        mListener = listener;
        mPlaylistId = playlistId;
        mDragStartListener = dragListener;
        mMode = enableDrag && playlistId != null ? Mode.PLAYLIST : Mode.ALL_SONGS;
        mShowFavoriteButton = showFavoriteButton;
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

    public void setShowFavoriteButton(boolean showFavoriteButton) {
        mShowFavoriteButton = showFavoriteButton;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.song = mValues.get(position);
        if (holder.song == null) {
            return;
        }
        holder.binding.songTitle.setText(mValues.get(position).getTitle());
        holder.binding.songArtist.setText(mValues.get(position).getArtist());
        holder.binding.songAlbum.setText(mValues.get(position).getAlbum());

        Button overFlow = holder.binding.songItemOverflow;
        ImageButton favoriteButton = holder.binding.songItemFavorite;
        ImageButton dragHandle = holder.binding.songItemDraghandle;

        boolean highlightSong = mHighlightedSongId != null && holder.song.getSongId() == mHighlightedSongId;
        boolean highlightCurrentSongInstance = mHighlightedSongPosition == null || position == mHighlightedSongPosition;

        holder.setInverted(highlightSong && highlightCurrentSongInstance);


        if (mEditMode) {
            overFlow.setVisibility(View.GONE);
            favoriteButton.setVisibility(View.GONE);
            dragHandle.setVisibility(View.VISIBLE);

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

            if (holder.song.isFavorite()) {
                favoriteButton.setImageResource(R.drawable.ic_favorite);
            } else {
                favoriteButton.setImageResource(R.drawable.ic_not_favorite);
            }
            favoriteButton.setOnClickListener(v -> mListener.onToggleFavorite(holder.song.getSongId()));
            favoriteButton.setVisibility(mShowFavoriteButton ? View.VISIBLE : View.GONE);

            overFlow.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onSongMenu(holder.song.getSongId(), position);
                }
            });
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onSongSelected(holder.song.getSongId(), position);
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
        if (mDragStartListener != null) {
            mDragStartListener.onItemDismiss(position);
        }
    }

    public void dismissWithoutSnackbar(int position, @NonNull OnItemDismissedCallback callback) {
        final Song songToBeRemoved = mValues.get(position);
        callback.onItemDismissedCompletely(songToBeRemoved);
        mValues.remove(position);
        notifyItemRemoved(position);
    }

    public void dismissWithSnackbar(int position, @StringRes int text, OnItemDismissedCallback callback) {
        final Song songToBeRemoved = mValues.get(position);
        Log.i(TAG, "Removing " + position + ": " + songToBeRemoved.getTitle());
        Snackbar snackbar = Snackbar
                .make(mRecyclerView, text, Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo, view -> {
                    mValues.add(position, songToBeRemoved);
                    notifyItemInserted(position);
                    Log.i(TAG, "Restoring " + position + ": " + songToBeRemoved.getTitle());
                    mRecyclerView.scrollToPosition(position);
                }).addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        if (callback != null) {
                            callback.onItemDismissedCompletely(songToBeRemoved);
                        }
                    }
                });
        snackbar.show();
        mValues.remove(position);
        notifyItemRemoved(position);
    }

    public Pair<Integer, List<Long>> getModifiedPlaylist() {
        List<Long> songIds = new ArrayList<>(mValues.size());
        for (Song song : mValues) {
            songIds.add(song.getSongId());
        }
        return new Pair<>(mPlaylistId, songIds);
    }

    public void setHighlighted(Long songId, Integer position) {
        mHighlightedSongId = songId;
        mHighlightedSongPosition = position;
        notifyDataSetChanged();
    }

    public void setSongs(List<Song> songs) {
        int position = mRecyclerView.getVerticalScrollbarPosition();
        mValues = songs;
        notifyDataSetChanged();
        mRecyclerView.setVerticalScrollbarPosition(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final FragmentSongItemBinding binding;
        Song song;

        ViewHolder(View view) {
            super(view);
            this.binding = FragmentSongItemBinding.bind(view);
        }

        void setInverted(boolean inverted) {
            Context context = itemView.getContext();
            int primaryColor = Color.getPrimaryColor(context, inverted);
            int primaryTextColor = Color.getPrimaryTextColor(context, inverted);
            int secondaryTextColor = Color.getSecondaryTextColor(context, inverted);
            int backgroundColor = Color.getBackgroundColor(context, inverted);

            itemView.setBackgroundColor(backgroundColor);
            binding.songItemOverflow.setTextColor(primaryColor);
            binding.songTitle.setTextColor(primaryTextColor);
            binding.songAlbum.setTextColor(secondaryTextColor);
            binding.songArtist.setTextColor(secondaryTextColor);
            ImageViewCompat.setImageTintList(binding.songItemFavorite, ColorStateList.valueOf(primaryColor));
            ImageViewCompat.setImageTintList(binding.songItemDraghandle, ColorStateList.valueOf(primaryColor));
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + song.toString() + "'";
        }
    }

    /* package */ static class SimpleItemTouchHelperCallback extends SwipeToDeleteCallback {
        private final ItemTouchHelperAdapter mAdapter;
        private final int mDragFlags;
        private final int mSwipeFlags;

        public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter, Context context, int dragFlags, int swipeFlags) {
            super(context);
            mAdapter = adapter;
            mDragFlags = dragFlags;
            mSwipeFlags = swipeFlags;
        }

        public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter, Context context) {
            this(adapter, context, ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START);
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
            return makeMovementFlags(mDragFlags, mSwipeFlags);
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

    public interface OnItemDismissedCallback {
        void onItemDismissedCompletely(Song song);
    }

    public interface OnTouchCallbacks {

        /**
         * Called when a view is requesting a start of a drag.
         *
         * @param viewHolder The holder of the view to drag.
         */
        void onStartDrag(RecyclerView.ViewHolder viewHolder);

        void onItemDismiss(int position);
    }
}
