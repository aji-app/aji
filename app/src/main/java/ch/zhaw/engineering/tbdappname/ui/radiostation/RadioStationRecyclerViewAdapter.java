package ch.zhaw.engineering.tbdappname.ui.radiostation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentRadiostationItemBinding;
import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;

public class RadioStationRecyclerViewAdapter extends RecyclerView.Adapter<RadioStationRecyclerViewAdapter.ViewHolder> {

    private final List<RadioStation> mValues;
    private final RadioStationFragmentInteractionListener mListener;
    private final Context mContext;
    private RecyclerView mRecyclerView;

    /* package */ RadioStationRecyclerViewAdapter(List<RadioStation> items, RadioStationFragmentInteractionListener listener, Context context) {
        mValues = items;
        mListener = listener;
        mContext = context;
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

    public void onDismiss(int position) {
        final RadioStation radioToBeRemoved = mValues.get(position);
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
        holder.binding.radiostationGenres.setText(mValues.get(position).getGenres());

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
        RadioStation radio;

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
