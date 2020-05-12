package ch.zhaw.engineering.aji.ui.radiostation;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.services.audio.AudioService;
import ch.zhaw.engineering.aji.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;
import ch.zhaw.engineering.aji.ui.ListFragment;
import ch.zhaw.engineering.aji.util.SwipeToDeleteCallback;

import static ch.zhaw.engineering.aji.services.audio.notification.ErrorNotificationManager.EXTRA_RADIOSTATION_ID;

public class RadioStationListFragment extends ListFragment {
    private static final String TAG = "RadioListFragment";
    private RadioStationFragmentInteractionListener mListener;
    private RadioStationRecyclerViewAdapter mAdapter;
    private Long mPlayingRadioId;

    @SuppressWarnings("unused")
    public static RadioStationListFragment newInstance(Long radioStationId) {
        RadioStationListFragment fragment = new RadioStationListFragment();
        Bundle args = new Bundle();
        if (radioStationId != null) {
            args.putLong(EXTRA_RADIOSTATION_ID, radioStationId);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_radiostation_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            mRecyclerView.setLayoutManager(layoutManager);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            boolean hasRadioStationIdExtra = getArguments() != null && getArguments().containsKey(EXTRA_RADIOSTATION_ID);
            AppViewModel appViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            appViewModel.getRadios().observe(getViewLifecycleOwner(), radios -> {
                if (appViewModel.isTwoPane() && radios != null && radios.size() > 0) {
                    if (!hasRadioStationIdExtra) {
                        mListener.onRadioStationSelected(radios.get(0).getId());
                    } else {
                        appViewModel.setOpenFirstInList(true);
                    }
                }

                this.onRadiosChanged(radios);
            });
            mListener.getCurrentSong().observe(getViewLifecycleOwner(), song -> {
                if (song != null && song.isRadio()) {
                    if (mAdapter != null) {
                        mAdapter.setHighlighted(song.getId());
                    } else {
                        mPlayingRadioId = song.getId();
                    }
                }
            });
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof RadioStationFragmentInteractionListener) {
            mListener = (RadioStationFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement RadioStationFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void onRadiosChanged(List<RadioStationDto> radios) {
        Log.i(TAG, "Updating radios for playlist list fragment");
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (mAdapter == null) {
                    mAdapter = new RadioStationRecyclerViewAdapter(radios, mListener, getActivity());
                    if (mPlayingRadioId != null) {
                        mAdapter.setHighlighted(mPlayingRadioId);
                        mPlayingRadioId = null;
                    }
                } else {
                    mAdapter.updateItems(radios);
                }
                ItemTouchHelper.Callback callback =
                        new SwipeToDeleteCallback(getActivity()) {
                            @Override
                            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                mAdapter.onDismiss(viewHolder.getAdapterPosition());
                            }
                        };
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
                itemTouchHelper.attachToRecyclerView(mRecyclerView);
                mRecyclerView.setAdapter(mAdapter);
            });
        }
    }
    public interface RadioStationFragmentInteractionListener {
        void onRadioStationSelected(long radioStationId);
        void onRadioStationPlay(long radioStationId);
        void onRadioStationMenu(long radioStationId);
        void onRadioStationDelete(long radioStationId);
        void onCreateRadioStation();
        LiveData<AudioService.SongInformation> getCurrentSong();
    }
}
