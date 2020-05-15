package ch.zhaw.engineering.aji;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;

import ch.zhaw.engineering.aji.databinding.ActivityMainBinding;
import ch.zhaw.engineering.aji.services.audio.AudioService;
import ch.zhaw.engineering.aji.services.audio.webradio.RadioStationImporter;
import ch.zhaw.engineering.aji.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.aji.services.files.AudioFileScanner;
import ch.zhaw.engineering.aji.services.files.sync.AudioFileContentObserver;
import ch.zhaw.engineering.aji.services.files.sync.SynchronizerControl;
import ch.zhaw.engineering.aji.ui.album.AlbumDetailsFragmentDirections;
import ch.zhaw.engineering.aji.ui.artist.ArtistDetailsFragmentDirections;
import ch.zhaw.engineering.aji.ui.directories.DirectoryFragment;
import ch.zhaw.engineering.aji.ui.expandedcontrols.ExpandedControlsFragment;
import ch.zhaw.engineering.aji.ui.filter.FilterFragmentDirections;
import ch.zhaw.engineering.aji.ui.library.LibraryFragmentDirections;
import ch.zhaw.engineering.aji.ui.playlist.PlaylistDetailsFragmentDirections;
import ch.zhaw.engineering.aji.ui.playlist.PlaylistFragmentDirections;
import ch.zhaw.engineering.aji.ui.preferences.PreferenceFragment;
import ch.zhaw.engineering.aji.ui.preferences.PreferenceFragmentDirections;
import ch.zhaw.engineering.aji.ui.preferences.licenses.LicenseInformationFragment;
import ch.zhaw.engineering.aji.ui.preferences.licenses.LicenseInformationFragmentDirections;
import ch.zhaw.engineering.aji.ui.preferences.licenses.data.Licenses;
import ch.zhaw.engineering.aji.ui.radiostation.RadioStationDetailsFragment;
import ch.zhaw.engineering.aji.ui.radiostation.RadioStationDetailsFragmentDirections;
import ch.zhaw.engineering.aji.ui.radiostation.RadioStationFragmentDirections;
import ch.zhaw.engineering.aji.ui.song.SongDetailsFragmentDirections;
import ch.zhaw.engineering.aji.ui.song.SongFragment;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;
import ch.zhaw.engineering.aji.util.PermissionChecker;
import ch.zhaw.engineering.aji.util.PreferenceHelper;

import static ch.zhaw.engineering.aji.DirectorySelectionActivity.EXTRA_FILE;
import static ch.zhaw.engineering.aji.services.audio.notification.ErrorNotificationManager.EXTRA_NOTIFICATION_ID;
import static ch.zhaw.engineering.aji.services.audio.notification.ErrorNotificationManager.EXTRA_RADIOSTATION_ID;
import static ch.zhaw.engineering.aji.services.audio.notification.ErrorNotificationManager.EXTRA_SONG_ID;
import static ch.zhaw.engineering.aji.services.files.AudioFileScanner.EXTRA_SCRAPE_ROOT_FOLDER;
import static ch.zhaw.engineering.aji.util.Margins.setBottomMargin;


public class MainActivity extends FragmentInteractionActivity implements PreferenceFragment.PreferenceListener, LicenseInformationFragment.LicenseListFragmentListener, SongFragment.SongFragmentListener, DirectoryFragment.OnDirectoryFragmentListener {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private BottomSheetBehavior bottomSheetBehavior;
    private ActivityMainBinding mBinding;
    private MutableLiveData<Boolean> mHasPermission = new MutableLiveData<>();
    private AudioFileContentObserver mAudioFileContentObserver;
    private Menu mActionBarMenu;
    private int mainContentMarginBottom;
    private SynchronizerControl mSynchronizerControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));

        PermissionChecker.checkForExternalStoragePermission(this, mHasPermission);

        mHasPermission.observe(this, hasPermission -> {
//            if (hasPermission) {
//                PreferenceHelper preferenceHelper = new PreferenceHelper(this);
//                boolean useMediaStore = preferenceHelper.isMediaStoreEnabled();
//                if (useMediaStore) {
//                    setupMediaStoreIntegration();
//                }
//
//                mSynchronizerControl = new SynchronizerControl(useMediaStore);
//                mSynchronizerControl.synchronizeSongsPeriodically(this);
//                preferenceHelper.observeMediaStoreSetting(enabled -> {
//                    mSynchronizerControl.setMediaStore(enabled);
//                    if (enabled) {
//                        setupMediaStoreIntegration();
//                    } else {
//                        if (mAudioFileContentObserver != null) {
//                            mAudioFileContentObserver.unregister();
//                            mAudioFileContentObserver = null;
//                        }
//                    }
//                });
//            }
        });


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

        setupPersistentBottomSheet();

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            switch (destination.getId()) {
                case R.id.nav_license_details:
                case R.id.nav_settings:
                case R.id.nav_licenses:
                case R.id.nav_about:
                    mainContentMarginBottom = setBottomMargin(mBinding.layoutAppBarMain.layoutContentMain.layoutContentMain, 0);
                    mBinding.layoutAppBarMain.persistentControls.persistentControls.setVisibility(View.GONE);
                    break;
                default:
                    setBottomMargin(mBinding.layoutAppBarMain.layoutContentMain.layoutContentMain, mainContentMarginBottom);
                    mBinding.layoutAppBarMain.persistentControls.persistentControls.setVisibility(View.VISIBLE);
                    break;
            }
        });
        try {
            Navigation.findNavController(this, R.id.nav_details_fragment);
            mAppViewModel.setTwoPane(true);
            mAppViewModel.setOpenFirstInList(true);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // We're not on a landscape tablet
        }

        handleStartIntent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAudioFileContentObserver != null) {
            mAudioFileContentObserver.unregister();
            mAudioFileContentObserver = null;
        }
    }

    @Override
    public void onOpenAbout() {
        if (mAppViewModel.isTwoPane()) {
            NavController navController = Navigation.findNavController(this, R.id.nav_details_fragment);
            navController.navigate(R.id.nav_about);
            return;
        }
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.nav_about);
    }

    @Override
    public void onShowOpenSourceLicenses() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.nav_licenses);
    }

    private void handleStartIntent() {
        if (getIntent() != null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                if (extras.containsKey(EXTRA_NOTIFICATION_ID)) {
                    NotificationManagerCompat.from(this).cancel(extras.getInt(EXTRA_NOTIFICATION_ID));
                }
                if (extras.containsKey(EXTRA_SONG_ID)) {
                    navigateToSongDetails(extras.getLong(EXTRA_SONG_ID));
                }
                if (extras.containsKey(EXTRA_RADIOSTATION_ID)) {
                    navigateToRadioStationFromLibrary(extras.getLong(EXTRA_RADIOSTATION_ID));
                }
                AppViewModel appViewModel = new ViewModelProvider(this).get(AppViewModel.class);
                appViewModel.setOpenFirstInList(false);
            }
        }
    }

    private void setupMediaStoreIntegration() {
        if (mAudioFileContentObserver == null) {
            HandlerThread thread = new HandlerThread("AudioFileObserver", Thread.NORM_PRIORITY);
            thread.start();
            Handler handler = new Handler(thread.getLooper());
            mAudioFileContentObserver = new AudioFileContentObserver(handler, this);
            mAudioFileContentObserver.register();
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
    public void onAddSongsButtonClick() {
        if (mAppViewModel.isTwoPane()) {
            NavController navController = Navigation.findNavController(this, R.id.nav_details_fragment);
            navController.navigate(R.id.nav_directory);
            return;
        }
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.nav_directory);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mActionBarMenu = menu;
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void navigateToRadioStation(Long radioStationId) {
        if (mAppViewModel.isTwoPane()) {
            NavController navController = Navigation.findNavController(this, R.id.nav_details_fragment);
            Bundle args = radioStationId != null ? RadioStationFragmentDirections.actionNavRadiostationsToRadiostationDetails(radioStationId).getArguments() : null;
            navController.navigate(R.id.nav_radiostation_details, args);
            return;
        }
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        Bundle args = radioStationId != null ? RadioStationFragmentDirections.actionNavRadiostationsToRadiostationDetails(radioStationId).getArguments() : null;
        navController.navigate(R.id.action_nav_radiostations_to_radiostation_details, args);

    }

    private void navigateToRadioStationFromLibrary(Long radioStationId) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        Bundle args = new Bundle();
        args.putLong(EXTRA_RADIOSTATION_ID, radioStationId);
        navController.navigate(R.id.nav_radiostations, args);
        navigateToRadioStation(radioStationId);
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
        if (mAppViewModel.isTwoPane()) {
            NavController navController = Navigation.findNavController(this, R.id.nav_details_fragment);
            navController.navigate(R.id.nav_song_details, LibraryFragmentDirections.actionNavLibraryToSongDetails(songId).getArguments());
            return;
        }
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
                    navController.navigate(R.id.action_nav_album_details_to_nav_song_details, AlbumDetailsFragmentDirections.actionNavAlbumDetailsToNavSongDetails(songId).getArguments());
                    break;
                case R.id.nav_artist_details:
                    navController.navigate(R.id.action_nav_artist_details_to_nav_song_details, ArtistDetailsFragmentDirections.actionNavArtistDetailsToNavSongDetails(songId).getArguments());
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
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();

    }

    @Override
    public void onLicenseSelected(Licenses.LicenseInformation item) {
        if (mAppViewModel.isTwoPane()) {
            NavController navController = Navigation.findNavController(this, R.id.nav_details_fragment);
            navController.navigate(R.id.nav_license_details, LicenseInformationFragmentDirections.actionNavLicensesToLicenseDetails(item.getLicense()).getArguments());
            return;
        }
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.action_nav_licenses_to_license_details, LicenseInformationFragmentDirections.actionNavLicensesToLicenseDetails(item.getLicense()).getArguments());
    }

    @Override
    public void onLibraryUrlClicked(Licenses.LicenseInformation item) {
        Uri url = Uri.parse(getString(item.getUrl()));
        Intent intent = new Intent(Intent.ACTION_VIEW, url);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void showProgressSpinner(boolean show) {
        runOnUiThread(() -> {
            mBinding.progressBarHolder.setVisibility(show ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onSelectionFinished(File file) {
        Intent scrapeFiles = new Intent();
        scrapeFiles.putExtra(EXTRA_SCRAPE_ROOT_FOLDER, file.getAbsolutePath());
        AudioFileScanner.enqueueWork(this, scrapeFiles);
        if (file.isDirectory()) {
            Toast.makeText(this, getString(R.string.scanning_directory, file.getName()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getString(R.string.add_file, file.getName()), Toast.LENGTH_LONG).show();
        }
    }
}
