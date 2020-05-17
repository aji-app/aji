package ch.zhaw.engineering.aji.ui.library;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentLibraryBinding;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.ui.SortResource;
import ch.zhaw.engineering.aji.ui.album.AlbumFragment;
import ch.zhaw.engineering.aji.ui.artist.ArtistFragment;
import ch.zhaw.engineering.aji.ui.menu.MenuHelper;
import ch.zhaw.engineering.aji.ui.song.SongFragment;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding mBinding;
    private LibraryFragmentStateAdapter mAdapter;
    private AppViewModel mAppViewModel;
    private SortResource mCurrentSortResource = SortResource.SONGS;
    private Menu mMenu;
    private List<MenuItem> mSongMenuItems = new ArrayList<>();
    private List<MenuItem> mDirectionMenuItems = new ArrayList<>();
    private MenuItem mSearchMenuItem;

    private ArtistFragment mArtistFragment;
    private AlbumFragment mAlbumFragment;
    private FavoriteFragment mFavoriteFragment;
    private SongFragment mSongsFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentLibraryBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAdapter = new LibraryFragmentStateAdapter(this);

        mBinding.pager.setAdapter(mAdapter);
        mBinding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Tab current = Tab.fromPosition(position);
                switch (current) {
                    case ARTISTS:
                        mCurrentSortResource = SortResource.ARTISTS;
                        toggleMenuItems(false, true, true);
                        break;
                    case ALBUMS:
                        mCurrentSortResource = SortResource.ALBUMS;
                        toggleMenuItems(false, true, true);
                        break;
                    case FAVORITES:
                        toggleMenuItems(false, false, false);
                        if (mFavoriteFragment != null) {
                            mFavoriteFragment.onShown();
                        }
                        break;
                    case SONGS:
                    default:
                        mCurrentSortResource = SortResource.SONGS;
                        toggleMenuItems(true, true, true);
                        if (showFirstSong() && mSongsFragment != null) {
                            mSongsFragment.onShown();
                        } else {
                            mAppViewModel.setOpenFirstInList(true);
                        }
                }
                if (mMenu != null) {
                    MenuHelper.setupSearchView(mCurrentSortResource, mAppViewModel, mMenu);
                }
            }
        });
        new TabLayoutMediator(mBinding.tabLayout, mBinding.pager,
                (tab, position) -> {
                    Tab current = Tab.fromPosition(position);
                    switch (current) {
                        case ARTISTS:
                            tab.setText(getResources().getString(R.string.tab_artists));
                            break;
                        case ALBUMS:
                            tab.setText(getResources().getString(R.string.tab_albums));
                            break;
                        case FAVORITES:
                            tab.setText(getResources().getString(R.string.tab_favorites));
                            break;
                        case SONGS:
                        default:
                            tab.setText(getResources().getString(R.string.tab_songs));
                    }
                }
        ).attach();
    }

    private boolean showFirstSong() {
        return mAppViewModel.isOpenFirstInList();
    }

    private void toggleMenuItems(boolean showSongMenuItems, boolean showDirectionMenuItems, boolean showSearchItem) {
        for (MenuItem item : mSongMenuItems) {
            item.setVisible(showSongMenuItems);
        }
        for (MenuItem item : mDirectionMenuItems) {
            item.setVisible(showDirectionMenuItems);
        }
        if (mSearchMenuItem != null) {
            mSearchMenuItem.setVisible(showSearchItem);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        mMenu = menu;
        super.onCreateOptionsMenu(menu, inflater);
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.filter_list_menu_song, menu);
        MenuHelper.setupSearchView(mCurrentSortResource, mAppViewModel, mMenu);
        mSongMenuItems = new ArrayList<>(4);
        mSongMenuItems.add(mMenu.findItem(R.id.song_meta_order_album));
        mSongMenuItems.add(mMenu.findItem(R.id.song_meta_order_artist));
        mSongMenuItems.add(mMenu.findItem(R.id.song_meta_order_title));
        MenuItem showHidden = mMenu.findItem(R.id.song_meta_show_hidden);
        setHideMenuTitle(showHidden, mAppViewModel.showHiddenSongs());
        mDirectionMenuItems.add(showHidden);

        mDirectionMenuItems = new ArrayList<>(3);
        mDirectionMenuItems.add(mMenu.findItem(R.id.direction_asc));
        mDirectionMenuItems.add(mMenu.findItem(R.id.direction_desc));

        mSearchMenuItem = menu.findItem(R.id.search);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.song_meta_order_album:
                mAppViewModel.changeSortType(SongDao.SortType.ALBUM);
                return true;
            case R.id.song_meta_order_artist:
                mAppViewModel.changeSortType(SongDao.SortType.ARTIST);
                return true;
            case R.id.song_meta_order_title:
                mAppViewModel.changeSortType(SongDao.SortType.TITLE);
                return true;
            case R.id.song_meta_show_hidden:
                setHideMenuTitle(item,mAppViewModel.toggleHiddenSongs() );
                return true;
            case R.id.direction_asc:
                mAppViewModel.changeSortDirection(mCurrentSortResource, true);
                return true;
            case R.id.direction_desc:
                mAppViewModel.changeSortDirection(mCurrentSortResource, false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setHideMenuTitle(@NonNull MenuItem item, boolean hiddenShown) {
        if (hiddenShown) {
            item.setTitle(R.string.hide_hidden);
        } else {
            item.setTitle(R.string.show_hidden);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            mAppViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        }
    }

    private static class LibraryFragmentStateAdapter extends FragmentStateAdapter {

        private final LibraryFragment mLibraryFragment;

        LibraryFragmentStateAdapter(@NonNull LibraryFragment fragment) {
            super(fragment);
            mLibraryFragment = fragment;
        }


        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Tab current = Tab.fromPosition(position);
            switch (current) {
                case ARTISTS:
                    ArtistFragment artistfragment = ArtistFragment.newInstance();
                    mLibraryFragment.setArtistFragment(artistfragment);
                    return artistfragment;
                case ALBUMS:
                    AlbumFragment albumFragment = AlbumFragment.newInstance();
                    mLibraryFragment.setAlbumFragment(albumFragment);
                    return albumFragment;
                case FAVORITES:
                    FavoriteFragment favoritesFragment = FavoriteFragment.newInstance();
                    mLibraryFragment.setFavoritesFragment(favoritesFragment);
                    return favoritesFragment;
                case SONGS:
                default:
                    SongFragment songFragment = SongFragment.newInstance();
                    mLibraryFragment.setSongsFragment(songFragment);
                    return songFragment;
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }

    private void setArtistFragment(ArtistFragment fragment) {
        mArtistFragment = fragment;
    }

    private void setAlbumFragment(AlbumFragment fragment) {
        mAlbumFragment = fragment;
    }

    private void setFavoritesFragment(FavoriteFragment fragment) {
        mFavoriteFragment = fragment;
    }

    private void setSongsFragment(SongFragment fragment) {
        mSongsFragment = fragment;
    }

    private enum Tab {
        SONGS(0), ALBUMS(1), ARTISTS(2), FAVORITES(3);

        public final int position;

        Tab(int position) {
            this.position = position;
        }

        public static Tab fromPosition(int position) {
            switch (position) {

                case 1:
                    return ALBUMS;
                case 2:
                    return ARTISTS;
                case 3:
                    return FAVORITES;
                case 0:
                default:
                    return SONGS;
            }
        }
    }
}
