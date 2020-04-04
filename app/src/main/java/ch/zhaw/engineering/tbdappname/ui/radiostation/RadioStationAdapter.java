package ch.zhaw.engineering.tbdappname.ui.radiostation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;

public class RadioStationAdapter extends RecyclerView.Adapter<RadioStationAdapter.ViewHolder> {

    private final List<RadioStationDto> mValues;
    private final PlaylistListInteractionListener mPlaylistListInteractionListener;
    private final Context mContext;

    public RadioStationAdapter(List<RadioStationDto> items, PlaylistListInteractionListener playlistListInteractionListener, Context context) {
        mValues = items;
        mPlaylistListInteractionListener = playlistListInteractionListener;
        mContext = context;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_radiostation_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return mValues.get(position).getId();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        RadioStationDto radioStation = mValues.get(position);
        holder.bind(radioStation);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mRadioName;
        public RadioStationDto mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mRadioName = view.findViewById(R.id.radiostation_name);
        }

        public void bind(RadioStationDto playlist) {
            mItem = playlist;
            mRadioName.setText(mItem.getName());

            mView.setOnClickListener(v -> {
                if (null != mPlaylistListInteractionListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
//                    mPlaylistListInteractionListener.onRadioStationClick(mItem);
                }
            });
            mView.setOnLongClickListener(v -> {
                if (null != mPlaylistListInteractionListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
//                    mPlaylistListInteractionListener.onRadioStationLongClick(mItem);
                    return true;
                }
                return false;
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mRadioName.getText() + "'";
        }
    }

    public interface PlaylistListInteractionListener {
        // TODO: Update argument type and name
        void onRadioStationClick(RadioStation playlist);
        void onRadioStationLongClick(RadioStation playlist);
    }
}
