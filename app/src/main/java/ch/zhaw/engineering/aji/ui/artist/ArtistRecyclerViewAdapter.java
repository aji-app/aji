package ch.zhaw.engineering.aji.ui.artist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.aji.databinding.FragmentArtistItemBinding;
import ch.zhaw.engineering.aji.services.database.dto.ArtistDto;
import ch.zhaw.engineering.aji.ui.library.AlbumArtistListFragment;


public class ArtistRecyclerViewAdapter extends RecyclerView.Adapter<ArtistRecyclerViewAdapter.ViewHolder> {
    private List<ArtistDto> mArtists;
    private final AlbumArtistListFragment.AlbumArtistListFragmentListener mListener;
    private boolean mShowHidden;
    private RecyclerView mRecyclerView;

    public ArtistRecyclerViewAdapter(List<ArtistDto> artists, AlbumArtistListFragment.AlbumArtistListFragmentListener listener, boolean showHidden, RecyclerView recyclerView) {
        mArtists = artists;
        mListener = listener;
        mShowHidden = showHidden;
        mRecyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ArtistRecyclerViewAdapter.ViewHolder(
                FragmentArtistItemBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.artist = mArtists.get(position);
        holder.binding.artistName.setText(holder.artist.getName());
        holder.binding.artistItemPlay.setOnClickListener(v -> mListener.onArtistPlay(holder.artist.getName()));
        holder.binding.artistItemOverflow.setOnClickListener(v -> mListener.onArtistMenu(holder.artist.getName()));

        holder.itemView.setOnClickListener(v -> {
            if (mShowHidden) {
                mListener.onAlbumMenu(holder.artist.getName());
            } else {
                mListener.onArtistSelected(holder.artist.getName());
            }
        });

        holder.binding.artistItemPlay.setVisibility(mShowHidden ? View.GONE : View.VISIBLE);
    }

    public void updateArtists(List<ArtistDto> artists) {
        int position = mRecyclerView.getVerticalScrollbarPosition();
        mArtists = artists;
        notifyDataSetChanged();
        mRecyclerView.setVerticalScrollbarPosition(position);
    }

    @Override
    public int getItemCount() {
        return mArtists.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final FragmentArtistItemBinding binding;
        ArtistDto artist;


        ViewHolder(FragmentArtistItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + artist.getName() + "'";
        }
    }
}
