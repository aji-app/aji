package ch.zhaw.engineering.tbdappname.ui.contextmenu;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentContextMenuBinding;
import lombok.Builder;
import lombok.Value;

public class ContextMenuFragment extends BottomSheetDialogFragment {
    public final static String TAG = "ContextMenuFragment";

    private static final String ARG_SONG_ID = "song-id";
    private long mSongId;
    private RecyclerView mRecyclerView;
    private final LiveData<List<ItemConfig>> mConfig;
    private Context mContext;

    public ContextMenuFragment(LiveData<List<ItemConfig>> config) {
        mConfig = config;
    }

    public static ContextMenuFragment newInstance(LiveData<List<ItemConfig>> config) {
        return new ContextMenuFragment(config);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist_selection, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSongId = getArguments().getLong(ARG_SONG_ID);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mConfig.observe(getViewLifecycleOwner(), config -> {
            mRecyclerView.setAdapter(new ContextMenuRecyclerViewAdapter(config, getActivity()));
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) view;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        final FragmentContextMenuBinding binding;
        ItemConfig config;

        ViewHolder(FragmentContextMenuBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @Value
    @Builder
    public static class ItemConfig<T> {
        OnItemSelectedCallback<T> callback;
        @Builder.Default
        T value = null;

        @DrawableRes
        int imageId;

        @Builder.Default
        @StringRes
        int textId = -1;

        @Builder.Default
        String text = null;

        public void callCallback() {
            callback.onItemSelected(getValue());
        }
    }

    public interface OnItemSelectedCallback<T> {
        void onItemSelected(T item);
    }

    private static class ContextMenuRecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<ItemConfig> mItems;
        private Context mContext;

        ContextMenuRecyclerViewAdapter(List<ItemConfig> items, Context context) {
            mItems = items;
            mContext = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(FragmentContextMenuBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final ItemConfig currentConfig = mItems.get(position);
            holder.itemView.setOnClickListener(v -> {
                currentConfig.callCallback();
            });
            if (currentConfig.getText() != null) {
                holder.binding.text.setText(currentConfig.getText());
                holder.binding.image.setContentDescription(currentConfig.getText());
            } else {
                holder.binding.text.setText(currentConfig.getTextId());
                holder.binding.image.setContentDescription(mContext.getResources().getString(currentConfig.getTextId()));
            }
            holder.binding.image.setImageResource(currentConfig.getImageId());
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

    }
}
