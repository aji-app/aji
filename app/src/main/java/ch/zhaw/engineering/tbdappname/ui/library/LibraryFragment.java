package ch.zhaw.engineering.tbdappname.ui.library;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayoutMediator;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentLibraryBinding;
import ch.zhaw.engineering.tbdappname.ui.album.AlbumFragment;
import ch.zhaw.engineering.tbdappname.ui.artist.ArtistFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongFragment;

public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding mBinding;
    private LibraryFragmentStateAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentLibraryBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAdapter = new LibraryFragmentStateAdapter(this);

        mBinding.pager.setAdapter(mAdapter);
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

    private static class LibraryFragmentStateAdapter extends FragmentStateAdapter {

        LibraryFragmentStateAdapter(@NonNull Fragment fragment) {
            super(fragment);
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
                    return SongFragment.newInstance();
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
