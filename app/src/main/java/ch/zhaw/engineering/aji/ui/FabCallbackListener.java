package ch.zhaw.engineering.aji.ui;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import ch.zhaw.engineering.aji.MainActivity;

public interface FabCallbackListener {
    void disableFab();
    void configureFab(@NonNull MainActivity.FabCallback fabCallback, @DrawableRes int icon);
}
