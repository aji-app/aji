package ch.zhaw.engineering.aji.ui.radiostation;

public interface RadioStationFragmentInteractionListener {
    void onRadioStationSelected(long radioStationId);
    void onRadioStationPlay(long radioStationId);
    void onRadioStationMenu(long radioStationId);
    void onRadioStationDelete(long radioStationId);
    void onCreateRadioStation();
}
