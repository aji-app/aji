package ch.zhaw.engineering.aji.ui.viewmodel;

import androidx.lifecycle.LiveData;

import java.util.List;

import ch.zhaw.engineering.aji.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.aji.services.database.dto.RadioStationDto;

/* package */ class RadioViewModel extends FilteringViewModel<RadioStationDao, List<RadioStationDto>> {

    RadioViewModel(RadioStationDao radioStationDao) {
        super(radioStationDao);
    }

    LiveData<List<RadioStationDto>> getFilteredRadios() {
        return mList;
    }

    @Override
    protected LiveData<List<RadioStationDto>> getUpdatedFilteredSource() {
        return mDao.getRadioStations(mAscending, mSearchText);
    }
}
