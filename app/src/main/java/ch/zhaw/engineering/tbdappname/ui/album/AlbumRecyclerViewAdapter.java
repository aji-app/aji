package ch.zhaw.engineering.tbdappname.ui.library;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.tbdappname.databinding.FragmentAlbumItemBinding;
import ch.zhaw.engineering.tbdappname.services.database.dto.AlbumDto;


public class AlbumRecyclerViewAdapter extends RecyclerView.Adapter<AlbumRecyclerViewAdapter.ViewHolder> {
    private List<AlbumDto> mAlbums;
    private AlbumArtistListFragment.AlbumArtistListFragmentListener mListener;

    public AlbumRecyclerViewAdapter(List<AlbumDto> albums, AlbumArtistListFragment.AlbumArtistListFragmentListener listener) {
        mAlbums = albums;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AlbumRecyclerViewAdapter.ViewHolder(
                FragmentAlbumItemBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.album = mAlbums.get(position);
        holder.binding.albumName.setText(holder.album.getName());
        holder.binding.albumItemPlay.setOnClickListener(v -> mListener.onAlbumPlay(holder.album.getName()));
        holder.binding.albumItemOverflow.setOnClickListener(v -> mListener.onAlbumMenu(holder.album.getName()));
        holder.itemView.setOnClickListener(v -> mListener.onAlbumSelected(holder.album.getName()));

        // TODO: display album art
    }

    @Override
    public int getItemCount() {
        return mAlbums.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final FragmentAlbumItemBinding binding;
        AlbumDto album;


        ViewHolder(FragmentAlbumItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + album.getName() + "'";
        }
    }
}
