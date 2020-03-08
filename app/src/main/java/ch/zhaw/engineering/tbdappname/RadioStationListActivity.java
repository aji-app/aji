package ch.zhaw.engineering.tbdappname;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;
import ch.zhaw.engineering.tbdappname.ui.radiostation.RadioStationAdapter;
import ch.zhaw.engineering.tbdappname.ui.radiostation.RadioStationViewModel;

import static ch.zhaw.engineering.tbdappname.AddOrEditRadioStationActivity.EXTRA_RADIO_STATION_ID;

public class RadioStationListActivity extends AudioInterfaceActivity {
    private static final String TAG = "RadioStationList";

    private RadioStationViewModel mRadioStationViewModel;
    private boolean mSortAsc = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radiostation_list);

        RecyclerView recyclerView = findViewById(R.id.radiostation_list);
        mRadioStationViewModel = new RadioStationViewModel(this.getApplication());

        TextView noData = findViewById(R.id.no_radiostations);

        mRadioStationViewModel.getAllRadioStations().observe(this, radioStations -> {
            if (radioStations.isEmpty()) {
                noData.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                noData.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            runOnUiThread(() -> {
                RadioStationAdapter adapter = new RadioStationAdapter(radioStations, new RadioStationAdapter.PlaylistListInteractionListener() {
                    @Override
                    public void onRadioStationClick(RadioStation station) {
                        playMusic(station);
                    }

                    @Override
                    public void onRadioStationLongClick(RadioStation station) {
                        Intent intent = new Intent(RadioStationListActivity.this, AddOrEditRadioStationActivity.class);
//                        // TODO: For some weird reason, the direct reference is null (in the debugger and also when using)
                        String extra = EXTRA_RADIO_STATION_ID;
//                        if (extra == null) {
//                            Log.e(TAG, "IMPORTED STATIC VALUE IS NULL");
//                            extra = "EXTRA_RADIOSTATION_ID";
//                        }
                        intent.putExtra(extra, station.getId());
                        startActivity(intent);
                    }
                }, this);
                recyclerView.setAdapter(adapter);
            });

        });

        ImageButton sortDirection = findViewById(R.id.radiostations_sort_direction);
        sortDirection.setOnClickListener(v -> {
            mSortAsc = !mSortAsc;
            if (mSortAsc) {
                sortDirection.setImageResource(R.drawable.ic_up);
            } else {
                sortDirection.setImageResource(R.drawable.ic_down);
            }
            mRadioStationViewModel.changeSortOrder(mSortAsc);
        });

        SearchView searchView = findViewById(R.id.filter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                mRadioStationViewModel.changeSearchText(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mRadioStationViewModel.changeSearchText(newText);
                return true;
            }
        });

        FloatingActionButton fab = findViewById(R.id.add_radioStation);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddOrEditRadioStationActivity.class);
            startActivity(intent);
        });
    }
}
