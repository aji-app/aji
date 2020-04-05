package ch.zhaw.engineering.tbdappname.ui.radiostation;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ch.zhaw.engineering.tbdappname.R;
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
    private GenreRecyclerViewAdapter mAdapter;

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
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.radiostation_edit_menu, menu);
        setMenuVisibility(mInEditMode);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.radiostation_import:
                // TODO
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
            mBinding.genreAddButton.setVisibility(mInEditMode ? View.VISIBLE : View.GONE);
            mAdapter.setEditMode(mInEditMode);
            if (!mInEditMode) {
                notifyListenerEdited();
            }
            setMenuVisibility(mInEditMode);
        });

        mBinding.genreAddButton.setOnClickListener(v -> {
            if (mInEditMode) {
                mAdapter.addEmptyGenre();
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

                    mAdapter = new GenreRecyclerViewAdapter(mRadioStation.getGenres(), getActivity());

                    getActivity().runOnUiThread(() -> {
                        mBinding.radiostationName.setText(mRadioStation.getName());
                        mBinding.radiostationUrl.setText(mRadioStation.getUrl());
                        mBinding.genreList.setAdapter(mAdapter);
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

        mRadioStation.setGenres(mAdapter.getGenres());

        if (mListener != null) {
            mListener.onRadioStationEdit(mRadioStation);
        }
    }

    public interface RadioStationDetailsFragmentListener {
        void onRadioStationEdit(RadioStationDto updatedRadioStation);
    }
}
