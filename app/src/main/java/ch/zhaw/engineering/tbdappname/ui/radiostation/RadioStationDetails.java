package ch.zhaw.engineering.tbdappname.ui.radiostation;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.zhaw.engineering.tbdappname.R;

public class RadioStationDetails extends Fragment {
    private static final String ARG_RADIOSTATION_ID = "radiostation-id";

    private long mRadioStationId;

    public static RadioStationDetails newInstance(long radioStationId) {
        RadioStationDetails fragment = new RadioStationDetails();
        Bundle args = new Bundle();
        args.putLong(ARG_RADIOSTATION_ID, radioStationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRadioStationId = getArguments().getLong(ARG_RADIOSTATION_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_radio_station_details, container, false);
    }
}
