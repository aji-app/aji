package ch.zhaw.engineering.tbdappname;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import ch.zhaw.engineering.tbdappname.databinding.ActivityMainBinding;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistFragmentDirections;
import ch.zhaw.engineering.tbdappname.ui.radiostation.RadioStationFragmentDirections;


public class MainActivity extends FragmentInteractionActivity {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private BottomSheetBehavior bottomSheetBehavior;
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.layoutAppBarMain.toolbar);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_library, R.id.nav_playlists, R.id.nav_radiostations, R.id.nav_filters)
                .setDrawerLayout(mBinding.drawerLayout)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(mBinding.navView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
        });

        bottomSheetBehavior = BottomSheetBehavior.from(mBinding.layoutAppBarMain.persistentControls.persistentControls);

        mBinding.layoutAppBarMain.persistentControls.extraSpace.setMinimumHeight((Resources.getSystem().getDisplayMetrics().heightPixels) / 2);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    @Override
    protected void navigateToRadioStation(long radioStationId) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.action_nav_radiostations_to_radioStationDetails, RadioStationFragmentDirections.actionNavRadiostationsToRadioStationDetails(radioStationId).getArguments());
    }

    @Override
    protected void navigateToPlaylist(int playlistId) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.action_nav_playlists_to_playlistDetailsFragment, PlaylistFragmentDirections.actionNavPlaylistsToPlaylistDetailsFragment(playlistId).getArguments());
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
