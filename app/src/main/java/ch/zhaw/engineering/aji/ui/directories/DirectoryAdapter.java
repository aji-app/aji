package ch.zhaw.engineering.aji.ui.directories;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentDirectoryBinding;


/**
 * {@link RecyclerView.Adapter} that can display a {@link DirectoryItem} and makes a call to the
 * TODO: Replace the implementation with code for your data type.
 */
public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.ViewHolder> {

    private final List<DirectoryItem> mValues;
    private final DirectoryAdapterClickListener mListener;
    private final boolean mIsRoot;
    private Context mContext;

    public DirectoryAdapter(List<DirectoryItem> items, DirectoryAdapterClickListener listener, boolean isRoot, Context context) {
        mValues = items;
        mListener = listener;
        mIsRoot = isRoot;
        mContext = context;
    }


    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_directory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.binding.directoryName.setText(mValues.get(position).getName());
        if (holder.mItem.getFileCount() >= 0 && holder.mItem.getSubDirectoryCount() >= 0) {
            holder.binding.containingDirectories.setText(mContext.getResources().getQuantityString(R.plurals.directories, holder.mItem.getSubDirectoryCount(), holder.mItem.getSubDirectoryCount()));
            holder.binding.containingFiles.setText(mContext.getResources().getQuantityString(R.plurals.files, holder.mItem.getFileCount(), holder.mItem.getFileCount()));
        }

        holder.binding.addDirectory.setVisibility(View.VISIBLE);
        holder.binding.contentContainer.setVisibility(View.VISIBLE);
        if (!mIsRoot && position == 0) {
            Drawable drawableLeft = holder.itemView.getContext().getResources().getDrawable(R.drawable.ic_up);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(holder.binding.directoryName, drawableLeft, null, null, null);
            holder.binding.contentContainer.setVisibility(View.GONE);
            holder.binding.addDirectory.setVisibility(View.GONE);
        } else if (holder.mItem.isDirectory()) {
            Drawable drawableLeft = holder.itemView.getContext().getResources().getDrawable(R.drawable.ic_directory);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(holder.binding.directoryName, drawableLeft, null, null, null);
        } else {
            Drawable drawableLeft = holder.itemView.getContext().getResources().getDrawable(R.drawable.ic_file);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(holder.binding.directoryName, drawableLeft, null, null, null);
            holder.binding.contentContainer.setVisibility(View.GONE);
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            if (null != mListener) {
                if (position == 0 && !mIsRoot) {
                    mListener.onDirectoryNavigateUp();
                } else if (holder.mItem.isDirectory()) {
                    mListener.onDirectoryNavigateDown(holder.mItem);
                } else {
                    mListener.onFileSelected(holder.mItem);
                }
            }
        });
        holder.binding.getRoot().setOnLongClickListener(v -> {
            mListener.onDirectorySelected(holder.mItem);
            return true;
        });
        holder.binding.addDirectory.setOnClickListener(v -> {
            mListener.onDirectorySelected(holder.mItem);
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        DirectoryItem mItem;
        FragmentDirectoryBinding binding;

        public ViewHolder(View view) {
            super(view);
            binding = FragmentDirectoryBinding.bind(view);
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + binding.directoryName.getText() + "'";
        }
    }

    public interface DirectoryAdapterClickListener {
        void onDirectorySelected(DirectoryItem directory);

        void onDirectoryNavigateDown(DirectoryItem directory);

        void onDirectoryNavigateUp();

        void onFileSelected(DirectoryItem file);
    }
}
