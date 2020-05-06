package ch.zhaw.engineering.aji.util;

import android.view.View;
import android.view.ViewGroup;

public class Margins {
    public static int setBottomMargin(View v, int bottom) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            int old = p.bottomMargin;
            p.setMargins(p.leftMargin, p.topMargin, p.rightMargin, bottom);
            v.requestLayout();
            return old;
        }
        return 0;
    }
}
