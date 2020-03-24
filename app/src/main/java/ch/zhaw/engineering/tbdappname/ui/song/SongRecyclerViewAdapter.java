package ch.zhaw.engineering.tbdappname.ui.song;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentSongItemBinding;
import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Song} and makes a call to the
 * specified {@link SongListFragment.SongListFragmentListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder> {

    private final List<Song> mValues;
    private final SongListFragment.SongListFragmentListener mListener;
    private Context mContext;
    private Map<Integer, Playlist> mPlaylists;

    public SongRecyclerViewAdapter(List<Song> items, SongListFragment.SongListFragmentListener listener, Context context, List<Playlist> playlists) {
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
        FragmentSongItemBinding binding = FragmentSongItemBinding.inflate(LayoutInflater.from(parent.getContext()));
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_song_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.song = mValues.get(position);
        holder.binding.songTitle.setText(mValues.get(position).getTitle());
        holder.binding.songArtist.setText(mValues.get(position).getArtist());
        holder.binding.songAlbum.setText(mValues.get(position).getAlbum());

        Button overFlow = holder.binding.songItemOverflow;
        ImageButton favoriteButton = holder.binding.songItemFavorite;

        overFlow.setBackground(null);
        favoriteButton.setBackground(null);

        if (holder.song.isFavorite()) {
            favoriteButton.setImageResource(R.drawable.ic_favorite);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_not_favorite);
        }
        favoriteButton.setOnClickListener(v -> mListener.onToggleFavorite(holder.song));

        overFlow.setOnClickListener(v -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(mContext, overFlow);
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
                        mListener.onSongPlay(holder.song);
                        return true;
                    case R.id.song_menu_queue:
                        mListener.onSongQueue(holder.song);
                        return true;
                    case R.id.song_menu_edit:
                        mListener.onSongEdit(holder.song);
                        return true;
                    case R.id.song_create_playlist:
                        mListener.onCreatePlaylist();
                        return true;
                    case R.id.song_menu_delete:
                        mListener.onSongDelete(holder.song);
                        return true;
                    default:
                        Playlist selectedPlaylist = mPlaylists.get(item.getItemId());
                        if (selectedPlaylist != null) {
                            mListener.onSongAddToPlaylist(holder.song, selectedPlaylist);
                            return true;
                        }
                        return false;
                }
            });
            //displaying the popup
            popup.show();
        });

        holder.binding.getRoot().setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onSongSelected(holder.song);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public FragmentSongItemBinding binding;
        public Song song;

        public ViewHolder(View view) {
            super(view);
            this.binding = FragmentSongItemBinding.bind(view);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + song.toString() + "'";
        }
    }
}
