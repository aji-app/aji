package ch.zhaw.engineering.aji;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;

import ch.zhaw.engineering.aji.databinding.ActivityMainBinding;
import ch.zhaw.engineering.aji.services.audio.AudioService;
import ch.zhaw.engineering.aji.services.audio.webradio.RadioStationImporter;
import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.files.AudioFileScanner;
import ch.zhaw.engineering.aji.services.files.StorageHelper;
import ch.zhaw.engineering.aji.services.files.WebRadioPlsParser;
import ch.zhaw.engineering.aji.services.files.sync.AudioFileContentObserver;
import ch.zhaw.engineering.aji.services.files.sync.SynchronizerControl;
import ch.zhaw.engineering.aji.ui.album.AlbumDetailsFragment;
import ch.zhaw.engineering.aji.ui.album.AlbumDetailsFragmentDirections;
import ch.zhaw.engineering.aji.ui.artist.ArtistDetailsFragment;
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
import ch.zhaw.engineering.aji.ui.radiostation.RadioStationDetailsFragmentDirections;
import ch.zhaw.engineering.aji.ui.radiostation.RadioStationFragmentDirections;
import ch.zhaw.engineering.aji.ui.song.SongDetailsFragmentDirections;
import ch.zhaw.engineering.aji.ui.song.SongFragment;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;
import ch.zhaw.engineering.aji.util.PermissionChecker;
import ch.zhaw.engineering.aji.util.PreferenceHelper;
import ch.zhaw.engineering.aji.util.Themes;

import static ch.zhaw.engineering.aji.services.audio.notification.ErrorNotificationManager.EXTRA_NOTIFICATION_ID;
import static ch.zhaw.engineering.aji.services.audio.notification.ErrorNotificationManager.EXTRA_RADIOSTATION_ID;
import static ch.zhaw.engineering.aji.services.audio.notification.ErrorNotificationManager.EXTRA_SONG_ID;
import static ch.zhaw.engineering.aji.services.files.AudioFileScanner.EXTRA_SCRAPE_ROOT_FOLDER;
import static ch.zhaw.engineering.aji.ui.directories.DirectoryFragment.ARG_SELECT_FILES_ONLY;
import static ch.zhaw.engineering.aji.util.Margins.setBottomMargin;


public class MainActivity extends FragmentInteractionActivity implements PreferenceFragment.PreferenceListener, LicenseInformationFragment.LicenseListFragmentListener, SongFragment.SongFragmentListener,
        DirectoryFragment.OnDirectoryFragmentListener, AlbumDetailsFragment.AlbumDetailsListener, ArtistDetailsFragment.ArtistDetailsListener {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private BottomSheetBehavior bottomSheetBehavior;
    private ActivityMainBinding mBinding;
    private MutableLiveData<Boolean> mHasPermission = new MutableLiveData<>();
    private AudioFileContentObserver mAudioFileContentObserver;
    private Menu mActionBarMenu;
    private int mainContentMarginBottom;
    private SynchronizerControl mSynchronizerControl;
    private FabCallback mFabCallback;
    @DrawableRes
    private int mFabIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int theme = Themes.getSelectedTheme(this);
        getTheme().applyStyle(theme, true);

        mBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        if (mFabCallback != null) {
            configureFab(mFabCallback, mFabIcon);
        } else {
            disableFab();
        }

        PermissionChecker.checkForExternalStoragePermission(this, mHasPermission);

        mHasPermission.observe(this, hasPermission -> {
            if (hasPermission) {
                PreferenceHelper preferenceHelper = new PreferenceHelper(this);
                boolean useMediaStore = preferenceHelper.isMediaStoreEnabled();
                if (useMediaStore) {
                    setupMediaStoreIntegration();
                }

                mSynchronizerControl = new SynchronizerControl(useMediaStore, this);
                preferenceHelper.observeMediaStoreSetting(enabled -> {
                    mSynchronizerControl.setMediaStore(enabled);
                    if (enabled) {
                        setupMediaStoreIntegration();
                    } else {
                        if (mAudioFileContentObserver != null) {
                            mAudioFileContentObserver.unregister();
                            mAudioFileContentObserver = null;
                        }
                    }
                });
            }
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

        mBinding.layoutAppBarMain.layoutContentMain.fab.setOnClickListener(v -> {
            if (mFabCallback != null) {
                mFabCallback.onClick(v);
            }
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            switch (destination.getId()) {
                case R.id.nav_license_details:
                case R.id.nav_settings:
                case R.id.nav_licenses:
                case R.id.nav_about:
                    mainContentMarginBottom = setBottomMargin(mBinding.layoutAppBarMain.layoutContentMain.layoutContentMain, 0);
                    mBinding.layoutAppBarMain.persistentControls.persistentControls.setVisibility(View.GONE);
                    break;
                case R.id.nav_directory:
                    ActionBar supportActionBar = getSupportActionBar();
                    if (supportActionBar != null) {
                        if (arguments != null && arguments.containsKey(ARG_SELECT_FILES_ONLY)) {
                            supportActionBar.setTitle(R.string.menu_select_file);
                        } else {
                            supportActionBar.setTitle(R.string.menu_directory_selection);
                        }
                    }
                default:
                    setBottomMargin(mBinding.layoutAppBarMain.layoutContentMain.layoutContentMain, mainContentMarginBottom);
                    mBinding.layoutAppBarMain.persistentControls.persistentControls.setVisibility(View.VISIBLE);
                    break;
            }
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });


        handleStartIntent(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            Navigation.findNavController(this, R.id.nav_details_fragment);
            mAppViewModel.setTwoPane(true);
            mAppViewModel.setOpenFirstInList(true);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // We're not on a landscape tablet
            mAppViewModel.setTwoPane(false);
        }
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
    protected void onPause() {
        super.onPause();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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

    @Override
    public void themeChanged() {
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void cleanupDatabase() {
        AsyncTask.execute(() -> {
            AppDatabase database = AppDatabase.getInstance(this);
            database.playlistDao().removeAllPlaylists();
            database.songDao().removeAllSongs();
            StorageHelper.deleteAllAlbumArt(this);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleStartIntent(intent);
    }

    private void handleStartIntent(Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
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
            HandlerThread thread = new HandlerThread("AudioFileObserver", Process.THREAD_PRIORITY_BACKGROUND);
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

        Log.i(TAG, "Bottom sheet is collapsed ? " + (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED));

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
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    case BottomSheetBehavior.STATE_HIDDEN:
                    case BottomSheetBehavior.STATE_SETTLING:
                    default:
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

    private void navigateToRadioStationFromLibrary(Long radioStationId) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        Bundle args = new Bundle();
        args.putLong(EXTRA_RADIOSTATION_ID, radioStationId);
        navController.navigate(R.id.nav_radiostations, args);
        navigateToRadioStation(radioStationId);
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
        if (navController.getCurrentDestination() != null) {
            int id = navController.getCurrentDestination().getId();
            if (id == R.id.nav_radiostation_details) {
                navController.navigate(R.id.action_nav_radiostation_details_self, args, new NavOptions.Builder().setPopUpTo(R.id.nav_radiostation_details, true).build());
            } else {
                navController.navigate(R.id.action_nav_radiostations_to_radiostation_details, args);
            }
        }
    }

    @Override
    protected void navigateToRadioStationForAlert(Long radioStationId) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        Bundle args = radioStationId != null ? RadioStationFragmentDirections.actionNavRadiostationsToRadiostationDetails(radioStationId).getArguments() : null;
        if (mAppViewModel.isTwoPane()) {
            NavController navController = Navigation.findNavController(this, R.id.nav_details_fragment);
            navController.navigate(R.id.nav_radiostation_details, args);
            return;
        }
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        if (navController.getCurrentDestination() != null) {
            int currentDestinationId = navController.getCurrentDestination().getId();
            switch (currentDestinationId) {
                case R.id.nav_library:
                    navController.navigate(R.id.action_nav_library_to_nav_radiostation_details, args);
                    break;
                case R.id.nav_playlists:
                    navController.navigate(R.id.action_nav_playlists_to_nav_radiostation_details, args);
                    break;
                case R.id.nav_radiostations:
                    navController.navigate(R.id.action_nav_radiostations_to_radiostation_details, args, new NavOptions.Builder().setPopUpTo(R.id.nav_radiostations, false).build());
                    break;
                case R.id.nav_filters:
                    navController.navigate(R.id.action_nav_filters_to_nav_radiostation_details, args);
                    break;
                case R.id.nav_settings:
                    navController.navigate(R.id.action_nav_settings_to_nav_radiostation_details, args);
                    break;
                case R.id.nav_playlist_details:
                    navController.navigate(R.id.action_nav_playlist_details_to_nav_radiostation_details, args);
                    break;
                case R.id.nav_radiostation_details:
                    navController.navigate(R.id.action_nav_radiostation_details_self, args, new NavOptions.Builder().setPopUpTo(R.id.nav_radiostation_details, true).build());
                    break;
                case R.id.nav_song_details:
                    navController.navigate(R.id.action_nav_song_details_to_nav_radiostation_details, args);
                    break;
                case R.id.nav_album_details:
                    navController.navigate(R.id.action_nav_album_details_to_nav_radiostation_details, args);
                    break;
                case R.id.nav_artist_details:
                    navController.navigate(R.id.action_nav_artist_details_to_nav_radiostation_details, args);
                    break;
                case R.id.nav_about:
                    navController.navigate(R.id.action_nav_about_to_nav_radiostation_details, args);
                    break;
                case R.id.nav_licenses:
                    navController.navigate(R.id.action_nav_licenses_to_nav_radiostation_details, args);
                    break;
                case R.id.nav_license_details:
                    navController.navigate(R.id.action_nav_license_details_to_nav_radiostation_details, args);
                    break;
                case R.id.nav_directory:
                    navController.navigate(R.id.action_nav_directory_to_nav_radiostation_details, args);
                    break;
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
        NavDestination currentDestination = navController.getCurrentDestination();
        if (currentDestination != null && currentDestination.getLabel() != null) {
            return currentDestination.getLabel().toString();
        }
        return "";
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
        Bundle args = LibraryFragmentDirections.actionNavLibraryToSongDetails(songId).getArguments();
        if (navController.getCurrentDestination() != null) {
            int id = navController.getCurrentDestination().getId();
            switch (id) {
                case R.id.nav_library:
                    navController.navigate(R.id.action_nav_library_to_song_details, args);
                    break;
                case R.id.nav_playlists:
                    navController.navigate(R.id.action_nav_playlists_to_song_details, args);
                    break;
                case R.id.nav_radiostations:
                    navController.navigate(R.id.action_nav_radiostations_to_song_details, args);
                    break;
                case R.id.nav_filters:
                    navController.navigate(R.id.action_nav_filters_to_song_details, args);
                    break;
                case R.id.nav_settings:
                    navController.navigate(R.id.action_nav_settings_to_nav_song_details, args);
                    break;
                case R.id.nav_playlist_details:
                    navController.navigate(R.id.action_playlist_details_to_song_details, args);
                    break;
                case R.id.nav_radiostation_details:
                    navController.navigate(R.id.action_nav_radiostation_details_to_nav_song_details, args);
                    break;
                case R.id.nav_song_details:
                    navController.navigate(R.id.action_song_details_self, args, new NavOptions.Builder().setPopUpTo(R.id.nav_song_details, true).build());
                    break;
                case R.id.nav_album_details:
                    navController.navigate(R.id.action_nav_album_details_to_nav_song_details, args);
                    break;
                case R.id.nav_artist_details:
                    navController.navigate(R.id.action_nav_artist_details_to_nav_song_details, args);
                    break;
                case R.id.nav_about:
                    navController.navigate(R.id.action_nav_about_to_nav_song_details, args);
                    break;
                case R.id.nav_licenses:
                    navController.navigate(R.id.action_nav_licenses_to_nav_song_details, args);
                    break;
                case R.id.nav_license_details:
                    navController.navigate(R.id.action_nav_license_details_to_nav_song_details, args);
                    break;
                case R.id.nav_directory:
                    navController.navigate(R.id.action_nav_directory_to_nav_song_details, args);
            }
        }
    }

    @Override
    protected void navigateToFilterDetails(AudioService.Filter filter) {
        switch (filter) {
            case EchoFilter:
                if (mAppViewModel.isTwoPane()) {
                    NavController navController = Navigation.findNavController(this, R.id.nav_details_fragment);
                    navController.navigate(R.id.nav_echo_filter_details, FilterFragmentDirections.actionNavFiltersToNavEchoFiltrDetails().getArguments());
                    return;
                }
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                navController.navigate(R.id.nav_echo_filter_details, FilterFragmentDirections.actionNavFiltersToNavEchoFiltrDetails().getArguments());
                break;
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
        mAppViewModel.setOpenFirstInList(true);
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
    public void onRadioStationImport() {
        Bundle args = RadioStationDetailsFragmentDirections.actionNavRadiostationDetailsToNavDirectory(new String[]{".pls"}, true).getArguments();
        if (mAppViewModel.isTwoPane()) {
            NavController navController = Navigation.findNavController(this, R.id.nav_details_fragment);
            navController.navigate(R.id.nav_directory, args);
            return;
        }
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.nav_directory, args);
    }

    @Override
    public void onSelectionFinished(File file) {
        if (file.isDirectory() || !file.getName().endsWith(".pls")) {
            Intent scrapeFiles = new Intent();
            scrapeFiles.putExtra(EXTRA_SCRAPE_ROOT_FOLDER, file.getAbsolutePath());
            AudioFileScanner.enqueueWork(this, scrapeFiles);
            if (file.isDirectory()) {
                Toast.makeText(this, getString(R.string.scanning_directory, file.getName()), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.add_file, file.getName()), Toast.LENGTH_LONG).show();
            }
        } else {
            mAppViewModel.setImportedRadioStation(WebRadioPlsParser.parseSingleRadioStationFromPlsFile(file.getPath()));
            if (!mAppViewModel.isTwoPane()) {
                onSupportNavigateUp();
            } else {
                onCreateRadioStation();
            }
        }
    }

    @Override
    public void configureFab(@NonNull FabCallback fabCallback, @DrawableRes int icon) {
        mFabCallback = fabCallback;
        mFabIcon = icon;
        if (mBinding != null) {
            mBinding.layoutAppBarMain.layoutContentMain.fab.setVisibility(View.VISIBLE);
            mBinding.layoutAppBarMain.layoutContentMain.fab.setImageResource(icon);
        }
    }

    @Override
    public void disableFab() {
        mFabCallback = null;
        if (mBinding != null) {
            mBinding.layoutAppBarMain.layoutContentMain.fab.setVisibility(View.GONE);
        }
    }

    @Override
    public void showEmptyDetails() {
        if (mAppViewModel.isTwoPane()) {
            NavController navController = Navigation.findNavController(this, R.id.nav_details_fragment);
            navController.navigate(R.id.nav_placeholder_details);
        }
    }

    public interface FabCallback {
        void onClick(View view);
    }
}
