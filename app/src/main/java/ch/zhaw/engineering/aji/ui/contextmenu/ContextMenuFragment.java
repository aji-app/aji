package ch.zhaw.engineering.aji.ui.contextmenu;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentContextMenuBinding;
import lombok.Builder;
import lombok.Value;

public class ContextMenuFragment extends BottomSheetDialogFragment {
    public final static String TAG = "ContextMenuFragment";

    private RecyclerView mRecyclerView;
    private final LiveData<List<ItemConfig>> mConfig;

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
        return inflater.inflate(R.layout.fragment_context_menu_list, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Ensure that fragment is expanded by default
        Dialog dialog =  super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(v -> {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior behaviour = BottomSheetBehavior.from(bottomSheet);
            behaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        return dialog;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mConfig.observe(getViewLifecycleOwner(), config -> {
            mRecyclerView.setAdapter(new ContextMenuRecyclerViewAdapter(config, getActivity(), this));
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
        OnItemSelectedCallback<T> mCallback;

        @Builder.Default
        T mValue = null;

        @DrawableRes
        int mImageId;

        @Builder.Default
        @StringRes
        int mTextId = -1;

        @Builder.Default
        String mText = null;

        void callCallback() {
            mCallback.onItemSelected(getValue());
        }
    }

    public interface OnItemSelectedCallback<T> {
        void onItemSelected(T item);
    }

    private static class ContextMenuRecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<ItemConfig> mItems;
        private Context mContext;
        private ContextMenuFragment mFragment;

        ContextMenuRecyclerViewAdapter(List<ItemConfig> items, Context context, ContextMenuFragment fragment) {
            mItems = items;
            mContext = context;
            mFragment = fragment;
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
                mFragment.dismiss();
            });
            if (currentConfig.getText() != null) {
                holder.binding.text.setText(currentConfig.getText());
            } else {
                holder.binding.text.setText(currentConfig.getTextId());
            }
            Drawable drawableLeft = holder.itemView.getContext().getResources().getDrawable(currentConfig.getImageId());
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(holder.binding.text, drawableLeft, null, null, null);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

    }
}
