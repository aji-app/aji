package ch.zhaw.engineering.tbdappname.ui.radiostation;

import ch.zhaw.engineering.tbdappname.ui.SortingListener;

public interface RadioStationFragmentInteractionListener extends SortingListener {
    void onRadioStationSelected(long radioStationId);
    void onRadioStationPlay(long radioStationId);
    void onRadioStationEdit(long radioStationId);
    void onRadioStationDelete(long radioStationId);
    void onCreateRadioStation();
}
