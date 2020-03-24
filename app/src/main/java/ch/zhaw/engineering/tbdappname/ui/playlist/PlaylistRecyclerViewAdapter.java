package ch.zhaw.engineering.tbdappname.ui.playlist;

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

import java.util.List;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.services.database.dto.PlaylistWithSongCount;
import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaylistWithSongCount} and makes a call to the
 * specified {@link PlaylistListFragment.PlaylistFragmentListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class PlaylistRecyclerViewAdapter extends RecyclerView.Adapter<PlaylistRecyclerViewAdapter.ViewHolder> {

    private final List<PlaylistWithSongCount> mValues;
    private final PlaylistListFragment.PlaylistFragmentListener mListener;
    private final Context mContext;

    public PlaylistRecyclerViewAdapter(List<PlaylistWithSongCount> items, PlaylistListFragment.PlaylistFragmentListener listener, Context context) {
        mValues = items;
        mListener = listener;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_playlist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mPlaylist = mValues.get(position);
        holder.mPlaylistName.setText(mValues.get(position).getName());
        holder.mPlaylistSongCount.setText(holder.mView.getContext().getResources().getString(R.string.song_count, holder.mPlaylist.getSongCount()));
        holder.mOverflowMenu.setBackground(null);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onPlaylistSelected(holder.mPlaylist);
                }
            }
        });

        holder.mOverflowMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(mContext, holder.mOverflowMenu);
            popup.inflate(R.menu.playlist_item_menu);

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.playlist_menu_play:
                        mListener.onPlaylistPlay(holder.mPlaylist);
                        return true;

                    case R.id.playlist_menu_queue:
                        mListener.onPlaylistQueue(holder.mPlaylist);
                        return true;

                    case R.id.playlist_menu_edit:
                        mListener.onPlaylistEdit(holder.mPlaylist);
                        return true;

                    case R.id.playlist_menu_delete:
                        mListener.onPlaylistDelete(holder.mPlaylist);
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mPlaylistName;
        final TextView mPlaylistSongCount;
        final Button mOverflowMenu;
        PlaylistWithSongCount mPlaylist;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mOverflowMenu = view.findViewById(R.id.playlist_item_overflow);
            mPlaylistSongCount = view.findViewById(R.id.playlist_count);
            mPlaylistName = view.findViewById(R.id.playlist_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mPlaylist.getName() + "'";
        }
    }
}
