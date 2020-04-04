package ch.zhaw.engineering.tbdappname.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;

public final class Color {
    private Color() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @ColorInt
    public static int getColorFromAttr(Context context, @AttrRes int attr) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

}
