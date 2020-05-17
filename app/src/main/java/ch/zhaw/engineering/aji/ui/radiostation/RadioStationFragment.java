package ch.zhaw.engineering.aji.ui.radiostation;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentRadiostationBinding;
import ch.zhaw.engineering.aji.ui.SortResource;
import ch.zhaw.engineering.aji.ui.menu.MenuHelper;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

import static ch.zhaw.engineering.aji.services.audio.notification.ErrorNotificationManager.EXTRA_RADIOSTATION_ID;

public class RadioStationFragment extends Fragment {
    private RadioStationListFragment.RadioStationFragmentInteractionListener mListener;
    private AppViewModel mAppViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof RadioStationListFragment.RadioStationFragmentInteractionListener) {
            mListener = (RadioStationListFragment.RadioStationFragmentInteractionListener) context;
            setupFab();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement RadioStationFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setupFab();
    }

    private void setupFab() {
        if (mListener != null) {
            mListener.configureFab(v -> {
                if (mListener != null) {
                    mListener.onCreateRadioStation();
                }
            }, R.drawable.ic_add);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentRadiostationBinding binding = FragmentRadiostationBinding.inflate(inflater, container, false);

        if (savedInstanceState == null) {
            Long radioStationId = null;
            if (getArguments() != null && getArguments().containsKey(EXTRA_RADIOSTATION_ID)) {
                radioStationId = getArguments().getLong(EXTRA_RADIOSTATION_ID);
            }
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.radiostation_list_container, RadioStationListFragment.newInstance(radioStationId))
                    .commitNow();
        }



        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            mAppViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.filter_list_menu, menu);
        MenuHelper.setupSearchView(SortResource.RADIOS, mAppViewModel, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!MenuHelper.onOptionsItemSelected(SortResource.RADIOS, mAppViewModel, item)) {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
