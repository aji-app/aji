package ch.zhaw.engineering.tbdappname.ui.song;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentSongMetaBinding;
import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;

public class SongMetaFragment extends Fragment {

    private SongMetaFragmentListener mListener;
    private FragmentSongMetaBinding mBinding;

    public static SongMetaFragment newInstance() {
        return new SongMetaFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentSongMetaBinding.inflate(inflater, container, false);
        mBinding.songFilter.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                mListener.onSearchTextChanged(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mListener.onSearchTextChanged(newText);
                return true;
            }
        });

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            PopupMenu popup = new PopupMenu(getActivity(), mBinding.songMetaMenu);
            //inflating menu from xml resource
            popup.inflate(R.menu.song_meta_menu);
            //adding click listener

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {

                    case R.id.song_meta_order_album:
                        mListener.onSortTypeChanged(SongDao.SortType.ALBUM);
                        return true;
                    case R.id.song_meta_order_artist:
                        mListener.onSortTypeChanged(SongDao.SortType.ARTIST);
                        return true;
                    case R.id.song_meta_order_title:
                        mListener.onSortTypeChanged(SongDao.SortType.TITLE);
                        return true;
                    case R.id.song_meta_direction_asc:
                        mListener.onSortDirectionChanged(true);
                        return true;
                    case R.id.song_meta_direction_desc:
                        mListener.onSortDirectionChanged(false);
                        return true;
                    default:
                        return false;
                }
            });

            mBinding.songMetaMenu.setOnClickListener(v -> popup.show());
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SongListFragment.SongListFragmentListener) {
            mListener = (SongMetaFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SongMetaFragmentListener");
        }
    }

    public interface SongMetaFragmentListener {

        void onSortTypeChanged(SongDao.SortType sortType);

        void onSearchTextChanged(String text);

        void onSortDirectionChanged(boolean ascending);
    }
}
