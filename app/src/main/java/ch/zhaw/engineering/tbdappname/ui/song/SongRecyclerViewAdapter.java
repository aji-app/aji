package ch.zhaw.engineering.tbdappname.ui.song;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    private Context mContext;

    public SongRecyclerViewAdapter(List<Song> items, SongFragment.SongFragmentInteractionListener listener, Context context) {
        mValues = items;
        mListener = listener;
        mContext = context;
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

        holder.mOverflowMenu.setBackgroundDrawable(null);

        holder.mOverflowMenu.setOnClickListener(v -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(mContext, holder.mOverflowMenu);
            //inflating menu from xml resource
            popup.inflate(R.menu.song_item_menu);

            //adding click listener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.song_menu_delete:
                            //handle menu1 click
                            Toast.makeText(mContext, "Delete", Toast.LENGTH_SHORT).show();
                            return true;
//                        case R.id.menu2:
//                            //handle menu2 click
//                            return true;
//                        case R.id.menu3:
//                            //handle menu3 click
//                            return true;
                        default:
                            return false;
                    }
                }
            });
            //displaying the popup
            popup.show();
        });

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
        public final Button mOverflowMenu;
        public Song mSong;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mSongTitle = view.findViewById(R.id.song_title);
            mSongArtist = view.findViewById(R.id.song_artist);
            mSongAlbum = view.findViewById(R.id.song_album);
            mOverflowMenu = view.findViewById(R.id.song_item_overflow);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mSong.toString() + "'";
        }
    }
}
