package ch.zhaw.engineering.tbdappname.ui.playlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.services.database.entity.PlaylistWithSongs;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private final List<PlaylistWithSongs> mValues;
    private PlaylistListInteractionListener mPlaylistListInteractionListener;

    public PlaylistAdapter(List<PlaylistWithSongs> items, PlaylistListInteractionListener playlistListInteractionListener) {
        mValues = items;
        mPlaylistListInteractionListener = playlistListInteractionListener;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return mValues.get(position).playlist.getPlaylistId();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        PlaylistWithSongs playlist = mValues.get(position);
        holder.bind(playlist);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mSongName;
        public PlaylistWithSongs mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.item_number);
            mSongName = view.findViewById(R.id.playlist_name);
        }

        public void bind(PlaylistWithSongs playlist) {
            mItem = playlist;
            mIdView.setText(mItem.playlist.getName());
            mSongName.setText(mItem.songs.size() + " Songs");

            mView.setOnClickListener(v -> {
                if (null != mPlaylistListInteractionListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mPlaylistListInteractionListener.onPlaylistClick(mItem);
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mSongName.getText() + "'";
        }
    }

    public interface PlaylistListInteractionListener {
        // TODO: Update argument type and name
        void onPlaylistClick(PlaylistWithSongs item);
    }
}
