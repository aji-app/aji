package ch.zhaw.engineering.tbdappname;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.ui.song.SongFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongViewModel;
import ch.zhaw.engineering.tbdappname.ui.test.TestFragment;

public class TestActivity extends AppCompatActivity implements  SongFragment.SongFragmentInteractionListener {

    private SongViewModel mSongViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.bottom_container, SongFragment.newInstance())
                    .commitNow();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_container, TestFragment.newInstance())
                    .commitNow();
        }

        mSongViewModel = new ViewModelProvider(this).get(SongViewModel.class);
    }

    @Override
    public void onSongSelected(Song item) {

    }

    @Override
    public void onSongOverflowMenu(Song item) {

    }
}
