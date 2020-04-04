package ch.zhaw.engineering.tbdappname.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import ch.zhaw.engineering.tbdappname.R;

import static ch.zhaw.engineering.tbdappname.util.Color.getColorFromAttr;

public class TbdImageButton extends AppCompatImageButton {

    public TbdImageButton(Context context) {
        super(context);
        init(null, 0);
    }

    public TbdImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TbdImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        setBackground(null);
        final TypedArray attributes = getContext().obtainStyledAttributes(
                attrs, R.styleable.TbdImageButton, defStyle, 0);
        ColorStateList tint = attributes.getColorStateList(R.styleable.TbdImageButton_android_tint);
        attributes.recycle();
        ImageViewCompat.setImageTintList(this, tint == null ? ColorStateList.valueOf(getColorFromAttr(getContext(), R.attr.colorPrimary)) : tint);
    }
}
