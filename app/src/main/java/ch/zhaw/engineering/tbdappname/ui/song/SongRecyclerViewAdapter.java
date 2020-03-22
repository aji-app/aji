package ch.zhaw.engineering.tbdappname.ui.song;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Song} and makes a call to the
 * specified {@link SongFragment.SongFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder> {

    private final List<Song> mValues;
    private final SongFragment.SongFragmentInteractionListener mListener;
    private Context mContext;
    private Map<Integer, Playlist> mPlaylists;

    public SongRecyclerViewAdapter(List<Song> items, SongFragment.SongFragmentInteractionListener listener, Context context, List<Playlist> playlists) {
        mValues = items;
        mListener = listener;
        mContext = context;
        mPlaylists = new HashMap<>(playlists.size());
        for (Playlist playlist : playlists) {
            mPlaylists.put(playlist.getPlaylistId(), playlist);
        }
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

        holder.mOverflowMenu.setBackground(null);

        holder.mOverflowMenu.setOnClickListener(v -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(mContext, holder.mOverflowMenu);
            //inflating menu from xml resource
            popup.inflate(R.menu.song_item_menu);

            SubMenu playlistMenu = popup.getMenu().findItem(R.id.song_menu_add_to_playlist).getSubMenu();
            for (Playlist playlist : mPlaylists.values()) {
                playlistMenu.add(0, playlist.getPlaylistId(), Menu.NONE, playlist.getName()).setIcon(R.drawable.ic_playlist);
            }

            //adding click listener
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {

                    case R.id.song_menu_play:
                        mListener.onSongPlay(holder.mSong);
                        return true;
                    case R.id.song_menu_queue:
                        mListener.onSongQueue(holder.mSong);
                        return true;
                    case R.id.song_menu_edit:
                        mListener.onSongEdit(holder.mSong);
                        return true;
                    case R.id.song_create_playlist:
                        mListener.onCreatePlaylist();
                        return true;
                    case R.id.song_menu_delete:
                        mListener.onSongDelete(holder.mSong);
                        return true;
                    default:
                        Playlist selectedPlaylist = mPlaylists.get(item.getItemId());
                        if (selectedPlaylist != null) {
                            mListener.onSongAddToPlaylist(holder.mSong, selectedPlaylist);
                            return true;
                        }
                        return false;
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
