package ch.zhaw.engineering.tbdappname.ui.song;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.ui.song.dummy.DummyContent.DummyItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link SongFragment.SongFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder> {

    private final List<Song> mValues;
    private final SongFragment.SongFragmentInteractionListener mListener;

    public SongRecyclerViewAdapter(List<Song> items, SongFragment.SongFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mSong = mValues.get(position);
        holder.mSongTitle.setText(mValues.get(position).getTitle());
        holder.mSongArtist.setText(mValues.get(position).getArtist());
        holder.mSongAlbum.setText(mValues.get(position).getAlbum());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onSongSelected(holder.mSong);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mSongTitle;
        public final TextView mSongArtist;
        public final TextView mSongAlbum;
        public Song mSong;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mSongTitle = view.findViewById(R.id.song_title);
            mSongArtist = view.findViewById(R.id.song_artist);
            mSongAlbum = view.findViewById(R.id.song_album);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mSong.toString() + "'";
        }
    }
}
