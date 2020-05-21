package ch.zhaw.engineering.aji.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public abstract class TabletAwareFragment extends Fragment {

    protected AppViewModel mAppViewModel;
    private boolean mInBackground = true;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            mAppViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mInBackground = false;
        triggerTabletLogic();
    }


    @Override
    public void onPause() {
        super.onPause();
        mInBackground = true;
    }

    protected void triggerTabletLogic() {
        if (mAppViewModel.isTwoPane() && !mInBackground) {
            showDetails();
        }
    }

    protected abstract void showDetails();
}
