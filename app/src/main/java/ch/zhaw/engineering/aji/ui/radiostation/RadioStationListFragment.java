package ch.zhaw.engineering.aji.ui.radiostation;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;
import ch.zhaw.engineering.aji.ui.ListFragment;
import ch.zhaw.engineering.aji.util.SwipeToDeleteCallback;

public class RadioStationListFragment extends ListFragment {
    private static final String TAG = "RadioListFragment";
    private RadioStationFragmentInteractionListener mListener;
    private RadioStationRecyclerViewAdapter mAdapter;

    @SuppressWarnings("unused")
    public static RadioStationListFragment newInstance() {
        RadioStationListFragment fragment = new RadioStationListFragment();
        Bundle args = new Bundle();
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
            AppViewModel viewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            viewModel.getRadios().observe(getViewLifecycleOwner(), this::onRadiosChanged);
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

}