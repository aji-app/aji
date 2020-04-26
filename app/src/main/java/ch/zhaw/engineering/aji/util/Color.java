package ch.zhaw.engineering.aji.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;

import ch.zhaw.engineering.aji.R;

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


    @ColorInt
    public static int getPrimaryColor(Context context, boolean inverted) {
        if (inverted) {
            return Color.getColorFromAttr(context, R.attr.primaryColorInverted);
        }
        return Color.getColorFromAttr(context, R.attr.colorPrimary);
    }

    @ColorInt
    public static int getPrimaryTextColor(Context context, boolean inverted) {
        if (inverted) {
            return Color.getColorFromAttr(context, R.attr.primaryTextColorInverted);
        }
        return Color.getColorFromAttr(context, R.attr.primaryTextColor);
    }

    @ColorInt
    public static int getSecondaryTextColor(Context context, boolean inverted) {
        if (inverted) {
            return Color.getColorFromAttr(context, R.attr.secondaryTextColorInverted);
        }
        return Color.getColorFromAttr(context, R.attr.secondaryTextColor);
    }

    public static int getBackgroundColor(Context context, boolean inverted) {
        if (inverted) {
            return Color.getColorFromAttr(context, R.attr.backgroundColorInverted);
        }
        return Color.getColorFromAttr(context, R.attr.backgroundColorApp);
    }
}
