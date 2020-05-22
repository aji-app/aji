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

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentRadiostationBinding;
import ch.zhaw.engineering.aji.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.aji.ui.SortResource;
import ch.zhaw.engineering.aji.ui.TabletAwareFragment;
import ch.zhaw.engineering.aji.ui.menu.MenuHelper;

import static ch.zhaw.engineering.aji.services.audio.notification.ErrorNotificationManager.EXTRA_RADIOSTATION_ID;

public class RadioStationFragment extends TabletAwareFragment {
    private RadioStationListFragment.RadioStationFragmentInteractionListener mListener;
    private RadioStationDto mTopRadio;
    private Long mRadioStationId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected void showDetails() {
        if (mRadioStationId == null && mTopRadio != null) {
            mListener.onRadioStationSelected(mTopRadio.getId());
        } else {
            mListener.showEmptyDetails();
        }
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
            mRadioStationId = null;
            if (getArguments() != null && getArguments().containsKey(EXTRA_RADIOSTATION_ID)) {
                mRadioStationId = getArguments().getLong(EXTRA_RADIOSTATION_ID);
            }
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.radiostation_list_container, RadioStationListFragment.newInstance(mRadioStationId))
                    .commitNow();
        }

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            mAppViewModel.getRadios().observe(getViewLifecycleOwner(), radios -> {
                String searchText = mAppViewModel.getSearchString(SortResource.RADIOS);
                if (searchText != null && !searchText.equals("")) {
                    mAppViewModel.setPlaceholderText(R.string.search_no_result);
                } else {
                    mAppViewModel.setPlaceholderText(R.string.no_radios_prompt);
                }
                if (radios.size() > 0) {
                    mTopRadio = radios.get(0);
                } else {
                    mTopRadio = null;
                }
                triggerTabletLogic();
            });
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
