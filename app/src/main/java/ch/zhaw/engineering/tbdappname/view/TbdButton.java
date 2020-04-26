package ch.zhaw.engineering.tbdappname.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.widget.TextViewCompat;

import ch.zhaw.engineering.tbdappname.R;

import static ch.zhaw.engineering.tbdappname.util.Color.getColorFromAttr;

public class TbdButton extends AppCompatButton {

    public TbdButton(Context context) {
        super(context);
        init(null, 0);
    }

    public TbdButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TbdButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        setBackground(null);
        final TypedArray attributes = getContext().obtainStyledAttributes(
                attrs, R.styleable.TbdButton, defStyle, 0);
        int textColor = attributes.getColor(R.styleable.TbdButton_android_textColor, 0);
        int background = attributes.getColor(R.styleable.TbdButton_android_background, 0);
        int tint = attributes.getColor(R.styleable.TbdButton_drawableTint, 0);
        attributes.recycle();
        setTextColor(textColor == 0 ? getColorFromAttr(getContext(), R.attr.colorPrimary) : textColor);
        if (background != 0) {
            setBackgroundColor(background);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && tint != 0) {
            Drawable[] drawables = TextViewCompat.getCompoundDrawablesRelative(this);
            if (drawables[0] != null) {  // left drawable
                drawables[0].setColorFilter(tint, PorterDuff.Mode.SRC_IN);
            }
        }
    }
}
