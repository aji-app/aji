package ch.zhaw.engineering.tbdappname.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ch.zhaw.engineering.tbdappname.R;

public class ListFragment extends Fragment {

    protected RecyclerView mRecyclerView;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null && mRecyclerView.getLayoutManager() != null) {
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                    ((LinearLayoutManager) mRecyclerView.getLayoutManager()).getOrientation());
            Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.divider);
            if (drawable != null) {
                dividerItemDecoration.setDrawable(drawable);
                mRecyclerView.addItemDecoration(dividerItemDecoration);
            }
        }
    }
}
