package ch.zhaw.engineering.aji;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import ch.zhaw.engineering.aji.databinding.ActivityMainBinding;
import ch.zhaw.engineering.aji.services.audio.AudioService;
import ch.zhaw.engineering.aji.services.audio.webradio.RadioStationImporter;
import ch.zhaw.engineering.aji.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.aji.services.files.AudioFileContentObserver;
import ch.zhaw.engineering.aji.ui.album.AlbumDetailsFragmentDirections;
import ch.zhaw.engineering.aji.ui.artist.ArtistDetailsFragmentDirections;
import ch.zhaw.engineering.aji.ui.expandedcontrols.ExpandedControlsFragment;
import ch.zhaw.engineering.aji.ui.filter.FilterFragmentDirections;
import ch.zhaw.engineering.aji.ui.preferences.PreferenceFragmentDirections;
import ch.zhaw.engineering.aji.ui.library.LibraryFragmentDirections;
import ch.zhaw.engineering.aji.ui.playlist.PlaylistDetailsFragmentDirections;
import ch.zhaw.engineering.aji.ui.playlist.PlaylistFragmentDirections;
import ch.zhaw.engineering.aji.ui.radiostation.RadioStationDetailsFragment;
import ch.zhaw.engineering.aji.ui.radiostation.RadioStationDetailsFragmentDirections;
import ch.zhaw.engineering.aji.ui.radiostation.RadioStationFragmentDirections;
import ch.zhaw.engineering.aji.ui.song.SongDetailsFragmentDirections;
import ch.zhaw.engineering.aji.util.PermissionChecker;


public class MainActivity extends FragmentInteractionActivity {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private BottomSheetBehavior bottomSheetBehavior;
    private ActivityMainBinding mBinding;
    private MutableLiveData<Boolean> mHasPermission = new MutableLiveData<>();
    private AudioFileContentObserver mAudioFileContentObserver;
    private Menu mActionBarMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));

        PermissionChecker.checkForExternalStoragePermission(this, mHasPermission);
        // TODO: Only use this if user did not disable this functionality
        HandlerThread thread = new HandlerThread("AudioFileObserver", Thread.NORM_PRIORITY);
        thread.start();
        Handler handler = new Handler(thread.getLooper());
        mAudioFileContentObserver = new AudioFileContentObserver(handler, this);
//        mAudioFileContentObserver.register();

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

        handlePotentialRadiostationDeepLink(navController);

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(mBinding.navView, navController);

        setupPersistentBottomSheet();
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }

    /**
     * Overwrite NavGraph startDestination when we deeplink to a radio station
     * That makes it so that the back button goes back to the radio station list instead of the library
     * It seems like {@link androidx.navigation.NavDeepLinkBuilder} does uses the startDestination as parent
     * instead of the actual parent of the destination when constructing deep links in {@link ch.zhaw.engineering.aji.services.audio.notification.ErrorNotificationManager}
     */
    private void handlePotentialRadiostationDeepLink(NavController navController) {
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(NavController.KEY_DEEP_LINK_INTENT)) {
            Intent navIntent = (Intent) getIntent().getExtras().get(NavController.KEY_DEEP_LINK_INTENT);
            // If we have a navIntent (which always contains the bundle for the destination)
            if (navIntent != null) {
                Bundle navArgs = navIntent.getExtras();
                if (navArgs != null) {
                    for (String key : navArgs.keySet()) {
                        Object arg = navArgs.get(key);
                        // If it is a bundle, it's the arguments for the detail view
                        if (arg instanceof Bundle) {
                            if (((Bundle) arg).containsKey(RadioStationDetailsFragment.ARG_RADIOSTATION_ID)) {
                                NavGraph graph = navController.getGraph();
                                // We want back to go to the radiostations instead of library
                                graph.setStartDestination(R.id.nav_radiostations);
                                navController.setGraph(graph);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void setupPersistentBottomSheet() {
        ImageButton persistentPlayPause = mBinding.layoutAppBarMain.persistentControls.persistentPlaypause;
        mAudioService.observe(this, service -> {
            if (service != null) {
                service.getPlayState().observe(this, state -> {
                    if (state == AudioService.PlayState.PLAYING) {
                        persistentPlayPause.setImageResource(R.drawable.ic_pause);
                    } else {
                        persistentPlayPause.setImageResource(R.drawable.ic_play);
                    }
                });

                TextView songName = mBinding.layoutAppBarMain.persistentControls.songTitle;
                TextView albumName = mBinding.layoutAppBarMain.persistentControls.songAlbum;
                TextView artistName = mBinding.layoutAppBarMain.persistentControls.songArtist;

                service.getCurrentSong().observe(this, info -> {
                    if (info != null) {
                        songName.setText(info.getTitle());
                        albumName.setText(info.getAlbum());
                        artistName.setText(info.getArtist());
                    } else {
                        songName.setText(R.string.not_playing);
                        albumName.setText(null);
                        artistName.setText(null);
                    }
                });
            }
        });
        bottomSheetBehavior = BottomSheetBehavior.from(mBinding.layoutAppBarMain.persistentControls.persistentControls);

        mBinding.layoutAppBarMain.persistentControls.persistentControls.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        Resources r = getResources();
        final float actionBarHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                48,
                r.getDisplayMetrics()
        );
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBinding.layoutAppBarMain.persistentControls.persistentControlsVisiblePart.getLayoutParams();
        final int bottomSheetPeekHeight = params.height;

        final LinearLayout bottomSheetPersistentPart = mBinding.layoutAppBarMain.persistentControls.persistentControlsVisiblePart;

        persistentPlayPause.setOnClickListener(v -> onPlayPause());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.expanded_persistent_controls_container, new ExpandedControlsFragment())
                .commit();


        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        if (getSupportActionBar() != null) {
                            for (int i = 0; i < mActionBarMenu.size(); i++) {
                                mActionBarMenu.getItem(i).setVisible(false);
                            }
                            getSupportActionBar().setTitle(R.string.now_playing);
                            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        }
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        if (getSupportActionBar() != null) {
                            for (int i = 0; i < mActionBarMenu.size(); i++) {
                                mActionBarMenu.getItem(i).setVisible(true);
                            }
                            getSupportActionBar().setTitle(getCurrentActionBarTitle());
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        }
                        break;
                    default:
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // When sliding make persistent part slowly get as small as the action bar so it gets hidden correctly
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bottomSheetPersistentPart.getLayoutParams();
                params.height = (int) (actionBarHeight + ((1 - slideOffset) * (bottomSheetPeekHeight - actionBarHeight)));
                bottomSheetPersistentPart.setLayoutParams(params);
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mActionBarMenu = menu;
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void navigateToRadioStation(Long radioStationId) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        Bundle args = radioStationId != null ? RadioStationFragmentDirections.actionNavRadiostationsToRadiostationDetails(radioStationId).getArguments() : null;
        navController.navigate(R.id.action_nav_radiostations_to_radiostation_details, args);
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
        navController.navigate(R.id.action_nav_playlists_to_playlist_details, PlaylistFragmentDirections.actionNavPlaylistsToPlaylistDetails(playlistId).getArguments());
    }

    protected String getCurrentActionBarTitle() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return navController.getCurrentDestination().getLabel().toString();
    }

    @Override
    protected void navigateToSongDetails(long songId) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        if (navController.getCurrentDestination() != null) {
            int id = navController.getCurrentDestination().getId();
            switch (id) {
                case R.id.nav_library:
                    navController.navigate(R.id.action_nav_library_to_song_details, LibraryFragmentDirections.actionNavLibraryToSongDetails(songId).getArguments());
                    break;
                case R.id.nav_playlists:
                    navController.navigate(R.id.action_nav_playlists_to_song_details, PlaylistFragmentDirections.actionNavPlaylistsToSongDetails(songId).getArguments());
                    break;
                case R.id.nav_radiostations:
                    navController.navigate(R.id.action_nav_radiostations_to_song_details, RadioStationFragmentDirections.actionNavRadiostationsToSongDetails(songId).getArguments());
                    break;
                case R.id.nav_filters:
                    navController.navigate(R.id.action_nav_filters_to_song_details, FilterFragmentDirections.actionNavFiltersToSongDetails(songId).getArguments());
                    break;
                case R.id.nav_settings:
                    navController.navigate(R.id.action_nav_settings_to_nav_song_details, PreferenceFragmentDirections.actionNavSettingsToNavSongDetails(songId).getArguments());
                    break;
                case R.id.nav_playlist_details:
                    navController.navigate(R.id.action_playlist_details_to_song_details, PlaylistDetailsFragmentDirections.actionPlaylistDetailsToSongDetails(songId).getArguments());
                    break;
                case R.id.nav_radiostation_details:
                    navController.navigate(R.id.action_nav_radiostation_details_to_nav_song_details, RadioStationDetailsFragmentDirections.actionNavRadiostationDetailsToNavSongDetails(songId).getArguments());
                    break;
                case R.id.nav_song_details:
                    navController.navigate(R.id.action_song_details_self, SongDetailsFragmentDirections.actionSongDetailsSelf(songId).getArguments(), new NavOptions.Builder().setPopUpTo(R.id.nav_song_details, true).build());
                    break;
                case R.id.nav_album_details:
                    navController.navigate(R.id.action_album_details_to_song_details, AlbumDetailsFragmentDirections.actionAlbumDetailsToSongDetails(songId).getArguments());
                    break;
                case R.id.nav_artist_details:
                    navController.navigate(R.id.action_artist_details_to_song_details, ArtistDetailsFragmentDirections.actionArtistDetailsToSongDetails(songId).getArguments());
                    break;
            }
        }
    }

    @Override
    protected void navigateToArtist(String artist) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.action_nav_library_to_artist_details, LibraryFragmentDirections.actionNavLibraryToArtistDetails(artist).getArguments());
    }

    @Override
    protected void navigateToAlbum(String album) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.action_nav_library_to_album_details, LibraryFragmentDirections.actionNavLibraryToAlbumDetails(album).getArguments());
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void showProgressSpinner(boolean show) {
        runOnUiThread(() -> {
            mBinding.progressBarHolder.setVisibility(show ? View.VISIBLE : View.GONE);
        });
    }
}
