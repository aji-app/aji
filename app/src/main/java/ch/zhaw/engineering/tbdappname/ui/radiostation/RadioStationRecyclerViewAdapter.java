package ch.zhaw.engineering.tbdappname.ui.radiostation;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentRadiostationItemBinding;
import ch.zhaw.engineering.tbdappname.services.database.dto.RadioStationDto;

public class RadioStationRecyclerViewAdapter extends RecyclerView.Adapter<RadioStationRecyclerViewAdapter.ViewHolder> {

    private List<RadioStationDto> mValues;
    private final RadioStationFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private final Map<Long, RadioStationDto> mDeletedRadioStations = new HashMap<>();

    /* package */ RadioStationRecyclerViewAdapter(List<RadioStationDto> items, RadioStationFragmentInteractionListener listener, Context context) {
        mValues = items;
        mListener = listener;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_radiostation_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    public void updateItems(List<RadioStationDto> newItems) {
        List<RadioStationDto> items = new ArrayList<>(newItems.size());
        for (RadioStationDto item : newItems) {
            if (!mDeletedRadioStations.containsKey(item.getId())) {
                items.add(item);
            }
        }
        mValues = items;
        notifyDataSetChanged();
    }

    public void onDismiss(int position) {
        final RadioStationDto radioToBeRemoved = mValues.get(position);
        mDeletedRadioStations.put(radioToBeRemoved.getId(), radioToBeRemoved);
        Snackbar snackbar = Snackbar
                .make(mRecyclerView, R.string.playlist_deleted, Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo, view -> {
                    mValues.add(position, radioToBeRemoved);
                    notifyItemInserted(position);
                    mRecyclerView.scrollToPosition(position);
                }).addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        if (event != DISMISS_EVENT_ACTION && mListener != null) {
                            mListener.onRadioStationDelete(radioToBeRemoved.getId());
                            mDeletedRadioStations.remove(radioToBeRemoved.getId());
                        }
                    }
                });
        snackbar.show();
        mValues.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.radio = mValues.get(position);
        View root = holder.binding.getRoot();
        Button overFlowButton = holder.binding.radiostationItemOverflow;

        holder.binding.radiostationName.setText(mValues.get(position).getName());
        holder.binding.radiostationGenres.setText(TextUtils.join(" ", mValues.get(position).getGenres()));

        holder.binding.radiostationItemPlay.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onRadioStationPlay(holder.radio.getId());
            }
        });

        holder.binding.radiostationItemOverflow.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onRadioStationEdit(holder.radio.getId());
            }
        });

        root.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onRadioStationSelected(holder.radio.getId());
            }
        });

        overFlowButton.setOnClickListener(v -> {

        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final FragmentRadiostationItemBinding binding;
        RadioStationDto radio;

        ViewHolder(View view) {
            super(view);
            binding = FragmentRadiostationItemBinding.bind(view);
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + radio.getName() + "'";
        }
    }
}
