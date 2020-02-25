package ch.zhaw.engineering.tbdappname.ui.directories;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.tbdappname.R;


/**
 * {@link RecyclerView.Adapter} that can display a {@link DirectoryItem} and makes a call to the
 * TODO: Replace the implementation with code for your data type.
 */
public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.ViewHolder> {

    private final List<DirectoryItem> mValues;
    private final DirectoryAdapterClickListener mListener;
    private boolean mIsRoot;

    public DirectoryAdapter(List<DirectoryItem> items, DirectoryAdapterClickListener listener, boolean isRoot) {
        mValues = items;
        mListener = listener;
        mIsRoot = isRoot;
    }


    @Override
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
            holder.mImageView.setImageResource(R.drawable.ic_up);
        } else {
            holder.mImageView.setImageResource(R.drawable.ic_directory);
        }

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                if (position == 0 && !mIsRoot) {
                    mListener.onDirectoryNavigateUp();
                } else {
                    mListener.onDirectoryNavigateDown(holder.mItem);
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final ImageView mImageView;
        public DirectoryItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = view.findViewById(R.id.directory_name);
            mImageView = view.findViewById(R.id.directory_icon);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }

    public interface DirectoryAdapterClickListener {
        void onDirectorySelected(DirectoryItem directory);

        void onDirectoryNavigateDown(DirectoryItem directory);

        void onDirectoryNavigateUp();
    }
}
