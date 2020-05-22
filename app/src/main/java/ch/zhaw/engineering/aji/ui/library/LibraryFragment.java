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
    private List<MenuItem> mGeneralMenuItems = new ArrayList<>();
    private MenuItem mSearchMenuItem;
    private boolean mShowSongMenuItems;
    private boolean mShowDirectionMenuItems;
    private boolean mShowSearchItem;

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
                        setMenuItemStates(false, true, true);
                        break;
                    case ALBUMS:
                        mCurrentSortResource = SortResource.ALBUMS;
                        setMenuItemStates(false, true, true);
                        break;
                    case FAVORITES:
                        setMenuItemStates(false, false, false);
                        break;
                    case SONGS:
                    default:
                        mCurrentSortResource = SortResource.SONGS;
                        setMenuItemStates(true, true, true);
                }
                toggleMenuItems();
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

    private void setMenuItemStates(boolean showSongMenuItems, boolean showDirectionMenuItems, boolean showSearchItem) {
        mShowSongMenuItems = showSongMenuItems;
        mShowDirectionMenuItems = showDirectionMenuItems;
        mShowSearchItem = showSearchItem;
    }

    private void toggleMenuItems() {
        if (mMenu != null) {
            MenuHelper.setupSearchView(mCurrentSortResource, mAppViewModel, mMenu);
        }
        for (MenuItem item : mSongMenuItems) {
            item.setVisible(mShowSongMenuItems);
        }
        for (MenuItem item : mGeneralMenuItems) {
            item.setVisible(mShowDirectionMenuItems);
        }
        if (mSearchMenuItem != null) {
            mSearchMenuItem.setVisible(mShowSearchItem);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        inflater.inflate(R.menu.filter_list_menu_song, menu);
        mSongMenuItems = new ArrayList<>(4);
        mSongMenuItems.add(mMenu.findItem(R.id.song_meta_order_album));
        mSongMenuItems.add(mMenu.findItem(R.id.song_meta_order_artist));
        mSongMenuItems.add(mMenu.findItem(R.id.song_meta_order_title));
        MenuItem showHidden = mMenu.findItem(R.id.song_meta_show_hidden);
        setHideMenuTitle(showHidden, mAppViewModel.showHiddenSongs());
        mGeneralMenuItems.add(showHidden);

        mGeneralMenuItems = new ArrayList<>(3);
        mGeneralMenuItems.add(mMenu.findItem(R.id.direction_asc));
        mGeneralMenuItems.add(mMenu.findItem(R.id.direction_desc));
        mGeneralMenuItems.add(showHidden);

        mSearchMenuItem = menu.findItem(R.id.search);
        toggleMenuItems();
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
                setHideMenuTitle(item, mAppViewModel.toggleHiddenSongs());
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
                    return ArtistFragment.newInstance();
                case ALBUMS:
                    return AlbumFragment.newInstance();
                case FAVORITES:
                    return FavoriteFragment.newInstance();
                case SONGS:
                default:
                    SongFragment songFragment = SongFragment.newInstance();
                    return songFragment;
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
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
