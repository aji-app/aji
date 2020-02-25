package ch.zhaw.engineering.tbdappname.ui.songs;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import lombok.RequiredArgsConstructor;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> implements ItemHolder.ItemTouchHelperAdapter {

    private List<Song> mValues;
    private final List<Song> mAllValues;
    private List<Long> mSongsInPlaylist;
    private final Dictionary<Long, Song> songsById;
    private boolean mEnableSelection;
    private SongListInteractionListener mSongListInteractionListener;
    public SelectionTracker<Long> selectionTracker;
    private boolean mIsDragEnabled = false;

    public SongAdapter(List<Song> items, List<Song> songsInPlaylist, boolean enableSelection, SongListInteractionListener songListInteractionListener) {
        mValues = items;
        mAllValues = items;
        mEnableSelection = enableSelection;
        mSongListInteractionListener = songListInteractionListener;
        songsById = new Hashtable<>();
        mSongsInPlaylist = new ArrayList<>();
        for (Song song : items) {
            songsById.put(song.getSongId(), song);
        }
        for (Song song : songsInPlaylist) {
            mSongsInPlaylist.add(song.getSongId());
        }

        setHasStableIds(true);
    }

    public boolean isDragEnabled() {
        return mIsDragEnabled;
    }

    public void setDragEnabled(boolean dragEnabled) {
        mIsDragEnabled = dragEnabled;
        if (dragEnabled) {
            syncSelectedSongs();
            List<Song> selectedValues = new ArrayList<>(mSongsInPlaylist.size());
            for (long id : mSongsInPlaylist) {
                selectedValues.add(songsById.get(id));
            }

            mValues = selectedValues;
        } else {
            List<Long> newlyOrderedSongs = new ArrayList<>(mValues.size());
            for (Song song : mValues) {
                newlyOrderedSongs.add(song.getSongId());
            }
            mValues = mAllValues;
            mSongsInPlaylist = newlyOrderedSongs;

        }

        notifyDataSetChanged();
    }

    public List<Song> getSongsInOrder() {
        if (mIsDragEnabled) {
            return getSelectedValues();
        }
        syncSelectedSongs();
        List<Song> selectedValues = new ArrayList<>(mSongsInPlaylist.size());
        for (long id : mSongsInPlaylist) {
            selectedValues.add(songsById.get(id));
        }
        return selectedValues;
    }

    private void syncSelectedSongs() {
        // Very ugly sync code
        // TODO: Improve this reordering thing
        List<Song> songs = getSelectedValues();
        List<Long> selectedSongIds = new ArrayList<>();
        for (Song song : songs) {
            if (!mSongsInPlaylist.contains(song.getSongId())) {
                mSongsInPlaylist.add(song.getSongId());
            }
            selectedSongIds.add(song.getSongId());
        }
        for (Iterator<Long> it = mSongsInPlaylist.iterator(); it.hasNext(); ) {
            Long id = it.next();
            if (!selectedSongIds.contains(id)) {
                it.remove();
            }
        }
    }

    private List<Song> getSelectedValues() {
        List<Song> selectedValues = new ArrayList<>(selectionTracker.getSelection().size());
        for (Song song : mValues) {
            if (selectionTracker.isSelected(song.getSongId())) {
                selectedValues.add(song);
            }
        }
        return selectedValues;
    }

    @Override
    public void onItemDismiss(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
    }


    @Override
    public void onItemMove(int fromPosition, int toPosition) {
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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        Song song = mValues.get(position);
        return song.getSongId();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Song song = mValues.get(position);
        holder.bind(song, mEnableSelection && selectionTracker.isSelected((long) song.getSongId()));
        holder.mDragHandle.setOnTouchListener((v, event) -> {
            if (!mIsDragEnabled) {
                return false;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mSongListInteractionListener.onStartDrag(holder);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mSongName;
        public final TextView mSongAlbum;
        public final ImageView mDragHandle;
        public Song mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.item_number);
            mSongName = view.findViewById(R.id.song_name);
            mSongAlbum = view.findViewById(R.id.song_album);
            mDragHandle = view.findViewById(R.id.song_drag_handle);
        }

        public void bind(Song song, boolean isSelected) {
            mItem = song;
            mIdView.setText(mItem.getArtist());
            mSongName.setText(mItem.getTitle());
            mSongAlbum.setText(mItem.getAlbum());

            mView.setActivated(isSelected);

            mView.setOnClickListener(v -> {
                if (null != mSongListInteractionListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mSongListInteractionListener.onSongClick(mItem);
                }
            });

            mView.setOnLongClickListener(v -> {
                if (null != mSongListInteractionListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mSongListInteractionListener.onSongLongClick(mItem);
                    return true;
                }
                return false;
            });

            if (mIsDragEnabled) {
                mDragHandle.setVisibility(View.VISIBLE);
            } else {
                mDragHandle.setVisibility(View.GONE);
            }
        }

        public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
            return new ItemDetailsLookup.ItemDetails<Long>() {

                @Override
                public int getPosition() {
                    return getAdapterPosition();
                }

                @Nullable
                @Override
                public Long getSelectionKey() {
                    return (long) mItem.getSongId();
                }
            };
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mSongName.getText() + "'";
        }
    }

    @RequiredArgsConstructor
    public static class SongItemDetailsLookup extends ItemDetailsLookup<Long> {
        private final RecyclerView mRecyclerView;

        @Nullable
        @Override
        public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                ViewHolder viewHolder = (ViewHolder) mRecyclerView.getChildViewHolder(view);
                return viewHolder.getItemDetails();
            }
            return null;
        }
    }

    public interface SongListInteractionListener {
        void onSongClick(Song song);

        void onSongLongClick(Song song);

        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }
}
