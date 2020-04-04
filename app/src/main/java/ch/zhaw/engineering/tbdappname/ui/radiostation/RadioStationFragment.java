package ch.zhaw.engineering.tbdappname.ui.radiostation;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentRadiostationBinding;

public class RadioStationFragment extends Fragment {
    private RadioStationFragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentRadiostationBinding binding = FragmentRadiostationBinding.inflate(inflater);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.radiostation_list_container, RadioStationListFragment.newInstance())
                    .commitNow();
        }

        binding.fabAddRadiostation.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCreateRadioStation();
            }
        });

        return binding.getRoot();
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.radiostation_menu, menu);
        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
        search.setMaxWidth((Resources.getSystem().getDisplayMetrics().widthPixels / 2));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mListener.onRadioStationSearchTextChanged(newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.radiostation_direction_asc:
                mListener.onRadioStationSortDirectionChanged(true);
                return true;
            case R.id.radiostation_direction_desc:
                mListener.onRadioStationSortDirectionChanged(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
