package ch.zhaw.engineering.tbdappname.ui.radiostation;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;
import ch.zhaw.engineering.tbdappname.services.database.repository.PlaylistRepository;
import ch.zhaw.engineering.tbdappname.services.database.repository.RadioStationRepository;

public class RadioStationViewModel extends AndroidViewModel {
    private final RadioStationRepository mRadioStationRepository;
    private final MediatorLiveData<List<RadioStation>> radioStations;
    private boolean mAscending = true;

    private String searchText = null;

    public LiveData<List<RadioStation>> getAllRadioStations() {
        return radioStations;
    }

    public RadioStationViewModel(@NonNull Application application) {
        super(application);
        mRadioStationRepository = RadioStationRepository.getInstance(application);
        radioStations = new MediatorLiveData<>();
        radioStations.addSource(mRadioStationRepository.getRadioStations(mAscending, ""), radioStations::setValue);
    }


    public void changeSortOrder(boolean ascending) {
        mAscending = ascending;
        update();
    }

    public void changeSearchText(String text) {
        String prev = searchText;
        if (text.length() < 3) {
            searchText = null;
        } else {
            searchText = text;
        }
        if (prev == null && searchText != null || prev != null && !prev.equals(searchText)) {
            update();
        }
    }

    private void update() {
        radioStations.addSource(mRadioStationRepository.getRadioStations(mAscending, searchText), radioStations::setValue);
    }
}
