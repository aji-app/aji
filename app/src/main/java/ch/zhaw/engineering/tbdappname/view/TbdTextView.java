package ch.zhaw.engineering.tbdappname.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.TextViewCompat;

import ch.zhaw.engineering.tbdappname.R;

import static ch.zhaw.engineering.tbdappname.util.Color.getColorFromAttr;

public class TbdTextView extends AppCompatTextView {

    public TbdTextView(Context context) {
        super(context);
        init(null, 0);
    }

    public TbdTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TbdTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray attributes = getContext().obtainStyledAttributes(
                attrs, R.styleable.TbdTextView, defStyle, 0);
        int tint = attributes.getColor(R.styleable.TbdTextView_drawableTint, 0);
        attributes.recycle();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && tint != 0) {
            Drawable[] drawables = TextViewCompat.getCompoundDrawablesRelative(this);
            for (Drawable drawable : drawables) {
                if (drawable != null) {
                    drawable.setColorFilter(tint, PorterDuff.Mode.SRC_IN);
                }
            }
        }
    }
}
