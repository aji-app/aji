package ch.zhaw.engineering.aji;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import ch.zhaw.engineering.aji.ui.directories.DirectoryFragment;

public class DirectorySelectionActivity extends AppCompatActivity implements DirectoryFragment.OnDirectoryFragmentListener {
    public static final String EXTRA_FILE = "EXTRA_FILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directory_selection_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, DirectoryFragment.newInstance(1, false))
                    .commitNow();
        }
    }

    @Override
    public void disableFab() {

    }

    @Override
    public void configureFab(@NonNull MainActivity.FabCallback fabCallback, int icon) {

    }

    @Override
    public void onSelectionFinished(File directory) {
        Intent response = new Intent();
        response.putExtra(EXTRA_FILE, directory.getAbsolutePath());
        setResult(RESULT_OK, response);
        finish();
    }
}
