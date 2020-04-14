package ch.zhaw.engineering.tbdappname.ui.artist;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.tbdappname.databinding.FragmentArtistItemBinding;
import ch.zhaw.engineering.tbdappname.services.database.dto.ArtistDto;
import ch.zhaw.engineering.tbdappname.ui.library.AlbumArtistListFragment;


public class ArtistRecyclerViewAdapter extends RecyclerView.Adapter<ArtistRecyclerViewAdapter.ViewHolder> {
    private List<ArtistDto> mAlbums;
    private AlbumArtistListFragment.AlbumArtistListFragmentListener mListener;

    public ArtistRecyclerViewAdapter(List<ArtistDto> albums, AlbumArtistListFragment.AlbumArtistListFragmentListener listener) {
        mAlbums = albums;
        mListener = listener;
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
        holder.artist = mAlbums.get(position);
        holder.binding.artistName.setText(holder.artist.getName());
        holder.binding.artistItemPlay.setOnClickListener(v -> mListener.onArtistPlay(holder.artist.getName()));
        holder.binding.artistItemOverflow.setOnClickListener(v -> mListener.onArtistMenu(holder.artist.getName()));
        holder.itemView.setOnClickListener(v -> mListener.onArtistSelected(holder.artist.getName()));
    }

    @Override
    public int getItemCount() {
        return mAlbums.size();
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
