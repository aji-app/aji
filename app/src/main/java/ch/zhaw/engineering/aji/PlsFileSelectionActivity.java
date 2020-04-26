package ch.zhaw.engineering.aji;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import ch.zhaw.engineering.aji.ui.directories.DirectoryFragment;

public class PlsFileSelectionActivity extends AppCompatActivity implements DirectoryFragment.OnDirectoryFragmentListener {
    private static final String EXTRA_FILE = "EXTRA_FILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directory_selection_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, DirectoryFragment.newInstance(1, false, "pls"))
                    .commitNow();
        }
    }

    @Override
    public void onSelectionFinished(File directory) {
        Intent response = new Intent();
        response.putExtra(EXTRA_FILE, directory.getAbsolutePath());
        setResult(RESULT_OK, response);
        finish();
    }
}
