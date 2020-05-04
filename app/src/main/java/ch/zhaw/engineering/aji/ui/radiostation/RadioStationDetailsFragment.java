package ch.zhaw.engineering.aji.ui.radiostation;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.net.MalformedURLException;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentRadioStationDetailsBinding;
import ch.zhaw.engineering.aji.services.audio.backend.AudioBackend;
import ch.zhaw.engineering.aji.services.audio.webradio.RadioStationMetadataRunnable;
import ch.zhaw.engineering.aji.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.aji.services.database.dto.RadioStationDto;

public class RadioStationDetailsFragment extends Fragment {
    private static final String ARG_RADIOSTATION_ID = "radiostation-id";

    private Long mRadioStationId;
    private FragmentRadioStationDetailsBinding mBinding;
    private RadioStationDto mRadioStation;
    private boolean mInEditMode = false;
    private RadioStationDetailsFragmentListener mListener;
    private GenreRecyclerViewAdapter mAdapter;
    private boolean mPlaylistDeleted;

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
            long id = getArguments().getLong(ARG_RADIOSTATION_ID);
            mRadioStationId = id == 0 ? null : id;
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
        if (item.getItemId() == R.id.radiostation_import) {
            mListener.onRadioStationImport();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentRadioStationDetailsBinding.inflate(inflater, container, false);
        mBinding.radiostationEdit.setOnClickListener(v -> {
            if (mInEditMode) {
                checkRadioStation(working -> {
                    if (working && getActivity() != null) {
                        getActivity().runOnUiThread(() -> setEditMode(false));
                    }
                });
            } else {
                setEditMode(true);
            }
        });

        mBinding.genreAddButton.setOnClickListener(v -> {
            if (mInEditMode) {
                mAdapter.addEmptyGenre();
            }
        });

        mBinding.fabSaveRadiostation.setOnClickListener(v -> {
            updateRadioStationData();
            checkRadioStation(working -> {
                if (working) {
                    if (mListener != null) {
                        mListener.onRadioStationSaved(mRadioStation);
                    }
                }
            });

        });

        mBinding.radiostationDelete.setOnClickListener(v -> {
            Snackbar snackbar = Snackbar
                    .make(mBinding.getRoot(), R.string.radiostation_deleted, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo, view -> {
                    }).addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            if (event != DISMISS_EVENT_ACTION && mListener != null) {
                                mPlaylistDeleted = true;
                                mListener.onRadioStationDelete(mRadioStationId);
                                mListener.onSupportNavigateUp();
                            }
                        }
                    });
            snackbar.show();
        });

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            Activity activity = getActivity();
            if (mRadioStationId != null) {
                AsyncTask.execute(() -> {
                    RadioStationDao playlistDao = RadioStationDao.getInstance(getActivity());
                    mRadioStation = playlistDao.getRadioStationById(mRadioStationId);
                    setupAdapter(activity);
                });
            } else {
                mBinding.radiostationEdit.setVisibility(View.GONE);
                mRadioStation = new RadioStationDto();
                setupAdapter(activity);
                setEditMode(true);
            }
        }
    }

    private void setEditMode(boolean editMode) {
        mInEditMode = editMode;
        mBinding.radiostationName.setEditMode(mInEditMode);
        mBinding.radiostationUrl.setEditMode(mInEditMode);
        mBinding.genreAddButton.setVisibility(mInEditMode ? View.VISIBLE : View.GONE);
        mAdapter.setEditMode(mInEditMode);
        if (!mInEditMode) {
            notifyListenerEdited();
        }
        setMenuVisibility(mInEditMode);
    }

    private void setupAdapter(@NonNull Activity activity) {
        mAdapter = new GenreRecyclerViewAdapter(mRadioStation.getGenres(), getActivity());
        syncDisplay(activity);
    }

    private void syncDisplay(@NonNull Activity activity) {
        activity.runOnUiThread(() -> {
            mBinding.radiostationName.setText(mRadioStation.getName());
            mBinding.radiostationUrl.setText(mRadioStation.getUrl());
            mBinding.genreList.setAdapter(mAdapter);
        });
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
        updateRadioStationData();
        if (mListener != null) {
            mListener.onRadioStationEdited(mRadioStation);
        }
    }

    private void updateRadioStationData() {
        if (mBinding.radiostationName.getText().length() > 0) {
            mRadioStation.setName(mBinding.radiostationName.getText().toString());
        }

        if (mBinding.radiostationUrl.getText().length() > 0) {
            mRadioStation.setUrl(mBinding.radiostationUrl.getText().toString());
        }

        mRadioStation.setGenres(mAdapter.getGenres());
    }

    private void checkRadioStation(AudioBackend.Callback<Boolean> callback) {
        try {
            mListener.showProgressSpinner(true);
            RadioStationMetadataRunnable metaCheck = new RadioStationMetadataRunnable((title, artist, hasError) -> {
                mListener.showProgressSpinner(false);
                if (hasError) {
                    showInvalidUrlAlert(callback);
                }
            }, mBinding.radiostationUrl.getText().toString());
            AsyncTask.execute(metaCheck);
        } catch (MalformedURLException e) {
            // Display alert
            mListener.showProgressSpinner(false);
            showInvalidUrlAlert(callback);
            return;
        }
    }

    private void showInvalidUrlAlert(AudioBackend.Callback<Boolean> callback) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.radiostation_url_invalid)
                    .setTitle(R.string.radiostation_url_invalid_title);
            builder.setPositiveButton(R.string.ignore, (dialog, id) -> {
                callback.receiveValue(true);
            });
            builder.setNegativeButton(R.string.fix, (dialog, id) -> {
                callback.receiveValue(false);
            });
            getActivity().runOnUiThread(() -> {
                AlertDialog dialog = builder.create();

                dialog.show();
            });
        }
    }

    public void useImportedRadioStation(RadioStationDto imported) {
        mRadioStation.setName(imported.getName());
        mRadioStation.setUrl(imported.getUrl());
        if (getActivity() != null) {
            syncDisplay(getActivity());
        }
    }

    public interface RadioStationDetailsFragmentListener {
        void onRadioStationEdited(RadioStationDto updatedRadioStation);

        void onRadioStationSaved(RadioStationDto updatedRadioStation);

        void onRadioStationDelete(long radioStationId);

        void onRadioStationImport();

        boolean onSupportNavigateUp();

        void showProgressSpinner(boolean show);
    }
}
