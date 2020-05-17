package ch.zhaw.engineering.aji.ui.preferences.licenses;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.ui.ListFragment;
import ch.zhaw.engineering.aji.ui.preferences.licenses.data.Licenses;
import ch.zhaw.engineering.aji.ui.preferences.licenses.data.Licenses.LicenseInformation;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public class LicenseInformationFragment extends ListFragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private LicenseListFragmentListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LicenseInformationFragment() {
    }

    public static LicenseInformationFragment newInstance(int columnCount) {
        LicenseInformationFragment fragment = new LicenseInformationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_license_information_list, container, false);

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
            List<LicenseInformation> items = Licenses.ITEMS;
            Collections.sort(items, (i1, i2) -> {
                Context context = getActivity();
                return context.getString(i1.getLibraryName()).compareTo(context.getString(i2.getLibraryName()));
            });
            AppViewModel appViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            if (appViewModel.isTwoPane() && items.size() > 0) {
                mListener.onLicenseSelected(items.get(0));
            }

            mRecyclerView.setAdapter(new LicenseInformationRecyclerViewAdapter(items, mListener, getContext()));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LicenseListFragmentListener) {
            mListener = (LicenseListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface LicenseListFragmentListener {
        void onLicenseSelected(LicenseInformation item);

        void onLibraryUrlClicked(LicenseInformation item);
    }
}
