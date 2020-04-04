package ch.zhaw.engineering.tbdappname.ui.radiostation;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ch.zhaw.engineering.tbdappname.databinding.FragmentRadioStationDetailsBinding;
import ch.zhaw.engineering.tbdappname.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.RadioStationDto;

public class RadioStationDetailsFragment extends Fragment {
    private static final String ARG_RADIOSTATION_ID = "radiostation-id";

    private Long mRadioStationId;
    private FragmentRadioStationDetailsBinding mBinding;
    private RadioStationDto mRadioStation;
    private boolean mInEditMode = false;
    private RadioStationDetailsFragmentListener mListener;

    public static RadioStationDetailsFragment newInstance(long radioStationId) {
        RadioStationDetailsFragment fragment = new RadioStationDetailsFragment();
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
        mBinding = FragmentRadioStationDetailsBinding.inflate(inflater);
        mBinding.radiostationEdit.setOnClickListener(v -> {
            mInEditMode = !mInEditMode;
            mBinding.radiostationName.setEditMode(mInEditMode);
            mBinding.radiostationUrl.setEditMode(mInEditMode);
            if (!mInEditMode) {
                notifyListenerEdited();
            }
        });
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            if (mRadioStationId != null) {
                AsyncTask.execute(() -> {
                    RadioStationDao playlistDao = RadioStationDao.getInstance(getActivity());
                    mRadioStation = playlistDao.getRadioStationById(mRadioStationId);

                    getActivity().runOnUiThread(() -> {
                        mBinding.radiostationName.setText(mRadioStation.getName());
                        mBinding.radiostationUrl.setText(mRadioStation.getUrl());
                    });
                });
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof RadioStationDetailsFragmentListener) {
            mListener = (RadioStationDetailsFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PlaylistDetailsFragmentListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        notifyListenerEdited();
        mListener = null;
    }

    private void notifyListenerEdited() {
        if (mBinding.radiostationName.getText().length() > 0) {
            mRadioStation.setName(mBinding.radiostationName.getText().toString());
        }

        if (mBinding.radiostationUrl.getText().length() > 0) {
            mRadioStation.setUrl(mBinding.radiostationUrl.getText().toString());
        }

        // TODO: Genres

        if (mListener != null) {
            mListener.onRadioStationEdit(mRadioStation);
        }
    }

    public interface RadioStationDetailsFragmentListener {
        void onRadioStationEdit(RadioStationDto updatedRadioStation);
    }
}
