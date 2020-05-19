package ch.zhaw.engineering.aji.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public abstract class TabletAwareFragment extends Fragment {

    protected AppViewModel mAppViewModel;

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
        triggerTabletLogic();
    }

    protected void triggerTabletLogic() {
        if (mAppViewModel.isTwoPane()) {
            showDetails();
        }
    }

    protected abstract void showDetails();
}
