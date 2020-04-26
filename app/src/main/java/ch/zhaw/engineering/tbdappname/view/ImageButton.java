package ch.zhaw.engineering.tbdappname.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.widget.ImageViewCompat;

import ch.zhaw.engineering.tbdappname.R;

import static ch.zhaw.engineering.tbdappname.util.Color.getColorFromAttr;

public class ImageButton extends AppCompatImageButton {

    public ImageButton(Context context) {
        super(context);
        init(null, 0);
    }

    public ImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        setBackground(null);
        final TypedArray attributes = getContext().obtainStyledAttributes(
                attrs, R.styleable.ImageButton, defStyle, 0);
        ColorStateList tint = attributes.getColorStateList(R.styleable.ImageButton_android_tint);
        attributes.recycle();
        ImageViewCompat.setImageTintList(this, tint == null ? ColorStateList.valueOf(getColorFromAttr(getContext(), R.attr.colorPrimary)) : tint);
    }

    public void setColor(@ColorInt int color) {
        ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color));
    }
}
