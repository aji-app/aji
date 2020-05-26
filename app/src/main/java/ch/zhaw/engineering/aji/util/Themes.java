package ch.zhaw.engineering.aji.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.preference.PreferenceManager;

import ch.zhaw.engineering.aji.R;

public class Themes {
    public static final String KEY_THEME = "theme";

    private Themes() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @StyleRes
    public static int getSelectedTheme(@NonNull Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String theme = defaultSharedPreferences.getString(KEY_THEME, context.getString(R.string.default_theme));
        switch (theme) {
            // These values must match R.array.themes_list_values (in strings.xml)
            case "BA":
                return R.style.ThemeOverlay_BrownAmber;
            case "BP":
                return R.style.ThemeOverlay_BrownPink;
            case "PL":
                return R.style.ThemeOverlay_PurpleLime;
            case "PA":
                return R.style.ThemeOverlay_PurpleAmber;
            case "N":
                return R.style.ThemeOverlay_Night;
            case "DPA":
            default:
                return R.style.ThemeOverlay_DarkPurpleAmber;
        }
    }
}
