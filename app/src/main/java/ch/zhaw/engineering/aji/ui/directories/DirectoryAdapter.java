package ch.zhaw.engineering.aji.ui.directories;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentDirectoryBinding;


/**
 * {@link RecyclerView.Adapter} that can display a {@link DirectoryItem} and makes a call to the
 */
public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.ViewHolder> {

    private final List<DirectoryItem> mValues;
    private final DirectoryAdapterClickListener mListener;
    private final boolean mIsRoot;
    private final ExecutorService mExecutorService;
    private Activity mContext;
    private boolean mAudioFiles;
    private boolean mSelectFilesOnly;

    public DirectoryAdapter(List<DirectoryItem> items, DirectoryAdapterClickListener listener, boolean isRoot, Activity context, boolean audioFiles, boolean selectFilesOnly) {
        mValues = items;
        mListener = listener;
        mIsRoot = isRoot;
        mContext = context;
        mAudioFiles = audioFiles;
        mSelectFilesOnly = selectFilesOnly;
        mExecutorService = Executors.newCachedThreadPool();
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

        holder.binding.addDirectory.setVisibility(View.VISIBLE);
        holder.binding.containingFiles.setVisibility(View.VISIBLE);
        if (!mIsRoot && position == 0) {
            Drawable drawableLeft = holder.itemView.getContext().getResources().getDrawable(R.drawable.ic_up);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(holder.binding.directoryName, drawableLeft, null, null, null);
            holder.binding.containingFiles.setVisibility(View.GONE);
            holder.binding.addDirectory.setVisibility(View.GONE);
        } else if (holder.mItem.isDirectory()) {
            if (mSelectFilesOnly) {
                holder.binding.addDirectory.setVisibility(View.GONE);
            }
            Drawable drawableLeft = holder.itemView.getContext().getResources().getDrawable(R.drawable.ic_directory);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(holder.binding.directoryName, drawableLeft, null, null, null);
            holder.mItem.getFileCount(mExecutorService, i -> {
                mContext.runOnUiThread(() -> {
                    holder.binding.containingFiles.setVisibility(i == null ? View.GONE : View.VISIBLE);
                    if (i != null) {
                        holder.binding.containingFiles.setText(mContext.getResources().getQuantityString(mAudioFiles ? R.plurals.audio_files : R.plurals.files, i, i));
                    }
                });
            });
        } else {
            Drawable drawableLeft = holder.itemView.getContext().getResources().getDrawable(R.drawable.ic_file);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(holder.binding.directoryName, drawableLeft, null, null, null);
            holder.binding.containingFiles.setVisibility(View.GONE);
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
