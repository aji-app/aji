package ch.zhaw.engineering.aji.ui.directories;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.services.files.AudioFileFilter;
import ch.zhaw.engineering.aji.util.PermissionChecker;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnDirectoryFragmentListener}
 * interface.
 */
public class DirectoryFragment extends Fragment implements DirectoryAdapter.DirectoryAdapterClickListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_MULTI_SELECT = "multi-select";
    private static final String ARG_SHOW_FILE_EXTENSIONS = "show-files-extensions";

    private int mColumnCount = 1;
    private boolean mMultiSelect = false;
    private OnDirectoryFragmentListener mListener;
    private final MutableLiveData<Boolean> mHasPermission = new MutableLiveData<>(false);
    private final Deque<DirectoryItem> mNavigationStack = new ArrayDeque<>();
    private String[] mFileExtensions;

    private RecyclerView mRecyclerView;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DirectoryFragment() {
    }

    @SuppressWarnings("unused")
    public static DirectoryFragment newInstance(int columnCount, boolean multiSelect, String... fileExtensionsToShow) {
        DirectoryFragment fragment = new DirectoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putBoolean(ARG_MULTI_SELECT, multiSelect);
        args.putStringArray(ARG_SHOW_FILE_EXTENSIONS, fileExtensionsToShow);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mMultiSelect = getArguments().getBoolean(ARG_MULTI_SELECT);
            mFileExtensions = getArguments().getStringArray(ARG_SHOW_FILE_EXTENSIONS);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_directory_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            final Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            mHasPermission.observe(getViewLifecycleOwner(), hP -> {
                if (hP) {
                    loadCurrentDirectories();
                }
            });
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PermissionChecker.checkForExternalStoragePermission(getActivity(), mHasPermission);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDirectoryFragmentListener) {
            mListener = (OnDirectoryFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDirectoryFragmentListener");
        }
    }

    private void loadCurrentDirectories() {
        if (getActivity() != null) {
            AsyncTask.execute(() -> {
                if (mNavigationStack.isEmpty()) {
                    mNavigationStack.push(new DirectoryItem(Environment.getExternalStorageDirectory(), mFileExtensions));
                }
                List<DirectoryItem> directories = new ArrayList<>();
                File currentRoot = mNavigationStack.peek().getFile();
                File[] currentFiles = mFileExtensions == null ? currentRoot.listFiles(new AudioFileFilter(true)) : currentRoot.listFiles(dir -> {
                    if (dir.isDirectory()) {
                        return true;
                    }
                    for (String ext : mFileExtensions) {
                        if (dir.getName().endsWith(ext)) {
                            return true;
                        }
                    }
                    return false;
                });
                for (File file : currentFiles) {
                    DirectoryItem directoryItem = new DirectoryItem(file, mFileExtensions);
                    directories.add(directoryItem);
                }
                Collections.sort(directories, (d1, d2) -> {
                    if (d1.isDirectory() == d2.isDirectory()){
                        return d1.getName().compareTo(d2.getName());
                    }
                    return d1.isDirectory() ? -1 : 1;
                });
                List<DirectoryItem> dirs = new ArrayList<>(directories.size() + 1);
                boolean isRoot = mNavigationStack.size() == 1;
                if (!isRoot) {
                    dirs.add(DirectoryItem.parentDirectory(mNavigationStack.peek()));
                }
                dirs.addAll(directories);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> mRecyclerView.setAdapter(new DirectoryAdapter(dirs, this, isRoot, getActivity(), mFileExtensions == null)));
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDirectorySelected(DirectoryItem directory) {
        if (!mMultiSelect) {
            mListener.onSelectionFinished(directory.getFile());
        }
    }

    @Override
    public void onDirectoryNavigateDown(DirectoryItem directory) {
        mNavigationStack.push(directory);
        loadCurrentDirectories();
    }

    @Override
    public void onDirectoryNavigateUp() {
        mNavigationStack.pop();
        loadCurrentDirectories();
    }

    @Override
    public void onFileSelected(DirectoryItem file) {
        mListener.onSelectionFinished(file.getFile());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnDirectoryFragmentListener {
        // TODO: Update argument type and name
        void onSelectionFinished(File directory);
    }
}
