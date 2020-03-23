package ch.zhaw.engineering.tbdappname.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.ui.song.SongRecyclerViewAdapter;
import ch.zhaw.engineering.tbdappname.ui.song.SongViewModel;

public class TbdListFragment extends Fragment {

    protected RecyclerView mRecyclerView;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                ((LinearLayoutManager) mRecyclerView.getLayoutManager()).getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.divider));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }
}
