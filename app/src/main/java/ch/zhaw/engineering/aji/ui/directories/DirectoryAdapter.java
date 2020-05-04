package ch.zhaw.engineering.aji.ui.directories;

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


/**
 * {@link RecyclerView.Adapter} that can display a {@link DirectoryItem} and makes a call to the
 * TODO: Replace the implementation with code for your data type.
 */
public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.ViewHolder> {

    private final List<DirectoryItem> mValues;
    private final DirectoryAdapterClickListener mListener;
    private final boolean mIsRoot;

    public DirectoryAdapter(List<DirectoryItem> items, DirectoryAdapterClickListener listener, boolean isRoot) {
        mValues = items;
        mListener = listener;
        mIsRoot = isRoot;
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
        holder.mNameView.setText(mValues.get(position).getName());

        if (!mIsRoot && position == 0) {
            Drawable drawableLeft = holder.itemView.getContext().getResources().getDrawable(R.drawable.ic_up);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(holder.mNameView, drawableLeft, null, null, null);
        } else if (holder.mItem.isDirectory()) {
            Drawable drawableLeft = holder.itemView.getContext().getResources().getDrawable(R.drawable.ic_directory);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(holder.mNameView, drawableLeft, null, null, null);
        } else {
            Drawable drawableLeft = holder.itemView.getContext().getResources().getDrawable(R.drawable.ic_file);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(holder.mNameView, drawableLeft, null, null, null);
        }

        holder.mView.setOnClickListener(v -> {
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
        holder.mView.setOnLongClickListener(v -> {
            mListener.onDirectorySelected(holder.mItem);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mNameView;
        DirectoryItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = view.findViewById(R.id.directory_name);
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }

    public interface DirectoryAdapterClickListener {
        void onDirectorySelected(DirectoryItem directory);

        void onDirectoryNavigateDown(DirectoryItem directory);

        void onDirectoryNavigateUp();

        void onFileSelected(DirectoryItem file);
    }
}
