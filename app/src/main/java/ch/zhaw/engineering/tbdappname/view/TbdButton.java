package ch.zhaw.engineering.tbdappname.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

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
        attributes.recycle();
        setTextColor(textColor == 0 ? getColorFromAttr(getContext(), R.attr.colorPrimary) : textColor);
    }
}
