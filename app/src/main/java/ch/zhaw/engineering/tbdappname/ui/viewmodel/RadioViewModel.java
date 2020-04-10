package ch.zhaw.engineering.tbdappname.ui.viewmodel;

import androidx.lifecycle.LiveData;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.RadioStationDto;

public class RadioViewModel extends FilteringViewModel<RadioStationDao, List<RadioStationDto>> {

    public RadioViewModel(RadioStationDao radioStationDao) {
        super(radioStationDao);
    }

    public LiveData<List<RadioStationDto>> getRadios() {
        return mList;
    }

    @Override
    protected void update() {
        mList.addSource(mDao.getRadioStations(mAscending, mSearchText), mList::setValue);
    }
}
