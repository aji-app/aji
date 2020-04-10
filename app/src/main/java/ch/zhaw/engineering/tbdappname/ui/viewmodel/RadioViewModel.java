package ch.zhaw.engineering.tbdappname.ui.viewmodel;

import androidx.lifecycle.LiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.RadioStationDto;

public class RadioViewModel extends FilteringViewModel<RadioStationDao, List<RadioStationDto>> {

    public RadioViewModel(RadioStationDao radioStationDao) {
        super(radioStationDao);
    }

    public LiveData<List<RadioStationDto>> getFilteredRadios() {
        return mList;
    }

    @Override
    protected LiveData<List<RadioStationDto>> getUpdatedFilteredSource() {
        return mDao.getRadioStations(mAscending, mSearchText);
    }
}
