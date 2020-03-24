package ch.zhaw.engineering.tbdappname.ui.playlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentPlaylistItemBinding;
import ch.zhaw.engineering.tbdappname.services.database.dto.PlaylistWithSongCount;

public class PlaylistRecyclerViewAdapter extends RecyclerView.Adapter<PlaylistRecyclerViewAdapter.ViewHolder> {

    private final List<PlaylistWithSongCount> mValues;
    private final PlaylistListFragment.PlaylistFragmentListener mListener;
    private final Context mContext;

    /* package */ PlaylistRecyclerViewAdapter(List<PlaylistWithSongCount> items, PlaylistListFragment.PlaylistFragmentListener listener, Context context) {
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
        holder.playlist = mValues.get(position);
        View root = holder.binding.getRoot();
        Button overFlowButton = holder.binding.playlistItemOverflow;

        holder.binding.playlistName.setText(mValues.get(position).getName());
        holder.binding.playlistSongCount.setText(root.getContext().getResources().getString(R.string.song_count, holder.playlist.getSongCount()));
        overFlowButton.setBackground(null);

        root.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onPlaylistSelected(holder.playlist);
            }
        });

        overFlowButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(mContext, overFlowButton);
            popup.inflate(R.menu.playlist_item_menu);

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.playlist_menu_play:
                        mListener.onPlaylistPlay(holder.playlist);
                        return true;

                    case R.id.playlist_menu_queue:
                        mListener.onPlaylistQueue(holder.playlist);
                        return true;

                    case R.id.playlist_menu_edit:
                        mListener.onPlaylistEdit(holder.playlist);
                        return true;

                    case R.id.playlist_menu_delete:
                        mListener.onPlaylistDelete(holder.playlist);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        FragmentPlaylistItemBinding binding;
        PlaylistWithSongCount playlist;

        ViewHolder(View view) {
            super(view);
            binding = FragmentPlaylistItemBinding.bind(view);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + playlist.getName() + "'";
        }
    }
}
