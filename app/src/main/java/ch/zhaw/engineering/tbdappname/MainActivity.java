package ch.zhaw.engineering.tbdappname;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import ch.zhaw.engineering.tbdappname.databinding.ActivityMainBinding;
import ch.zhaw.engineering.tbdappname.services.audio.webradio.RadioStationImporter;
import ch.zhaw.engineering.tbdappname.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.tbdappname.services.files.AudioFileContentObserver;
import ch.zhaw.engineering.tbdappname.ui.expandedcontrols.ExpandedControlsFragment;
import ch.zhaw.engineering.tbdappname.ui.library.LibraryFragmentDirections;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistFragmentDirections;
import ch.zhaw.engineering.tbdappname.ui.radiostation.RadioStationDetailsFragment;
import ch.zhaw.engineering.tbdappname.ui.radiostation.RadioStationFragmentDirections;
import ch.zhaw.engineering.tbdappname.util.PermissionChecker;


public class MainActivity extends FragmentInteractionActivity {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private BottomSheetBehavior bottomSheetBehavior;
    private ActivityMainBinding mBinding;
    private MutableLiveData<Boolean> mHasPermission = new MutableLiveData<>();
    private final AudioFileContentObserver mAudioFileContentObserver = new AudioFileContentObserver(new Handler(), this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));

        PermissionChecker.checkForExternalStoragePermission(this, mHasPermission);
        // TODO: Only use this if user did not disable this functionality
        mAudioFileContentObserver.register();

        // TODO: Only sync on startup if user did not disable this functionality
//        mAudioFileContentObserver.onChange(false);
        RadioStationImporter.loadDefaultRadioStations(this);

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

        mBinding.layoutAppBarMain.persistentControls.persistentControls.setOnClickListener(v -> {
        });

        mBinding.layoutAppBarMain.persistentControls.persistentPlaypause.setOnClickListener(v -> onPlayPause());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.expanded_persistent_controls_container, new ExpandedControlsFragment())
                .commit();

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    @Override
    protected void navigateToRadioStation(Long radioStationId) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        Bundle args = radioStationId != null ? RadioStationFragmentDirections.actionNavRadiostationsToRadioStationDetails(radioStationId).getArguments() : null;
        navController.navigate(R.id.action_nav_radiostations_to_radioStationDetails, args);
    }

    protected void radioStationImported(RadioStationDto imported) {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null && navHostFragment.getChildFragmentManager().getFragments().size() > 0) {

            Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
            if (currentFragment instanceof RadioStationDetailsFragment) {
                ((RadioStationDetailsFragment) currentFragment).useImportedRadioStation(imported);
            }
        }
    }

    @Override
    protected void navigateToPlaylist(int playlistId) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.action_nav_playlists_to_playlistDetailsFragment, PlaylistFragmentDirections.actionNavPlaylistsToPlaylistDetailsFragment(playlistId).getArguments());
    }

    @Override
    protected void navigateToSong(long songId) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.action_nav_library_to_songDetailsFragment, LibraryFragmentDirections.actionNavLibraryToSongDetailsFragment(songId).getArguments());
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
