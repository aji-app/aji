package ch.zhaw.engineering.tbdappname.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.CustomEdittextviewBinding;

/**
 * TODO: document your custom view class.
 */
public class EditTextView extends LinearLayout {
    private boolean mEditMode;
    private CustomEdittextviewBinding mBinding;

    public EditTextView(Context context) {
        super(context);
        init(null, 0);
    }

    public EditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public EditTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        View view = inflate(getContext(), R.layout.custom_edittextview, this);
        mBinding = CustomEdittextviewBinding.bind(view);

        final TypedArray attributes = getContext().obtainStyledAttributes(
                attrs, R.styleable.EditTextView, defStyle, 0);
        mEditMode = attributes.getBoolean(
                R.styleable.EditTextView_editMode, false);
        String text = attributes.getString(R.styleable.EditTextView_text);
        String hint = attributes.getString(R.styleable.EditTextView_android_hint);
        int inputType = attributes.getInt(R.styleable.EditTextView_android_inputType, EditorInfo.TYPE_NULL);
        float textSize = attributes.getDimensionPixelSize(R.styleable.EditTextView_android_textSize, 0);

        attributes.recycle();

        mBinding.editText.setText(text);
        mBinding.editText.setHint(hint);
        if (textSize != 0) {
            mBinding.editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            mBinding.textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }
        if (inputType != EditorInfo.TYPE_NULL) {
            mBinding.editText.setInputType(inputType);
        }

        mBinding.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.textView.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void setEditMode(boolean editMode) {
        mEditMode = editMode;

        mBinding.textView.setVisibility(!mEditMode ? View.VISIBLE : View.GONE);
        mBinding.editText.setVisibility(mEditMode ? View.VISIBLE : View.GONE);
    }

    public boolean getEditMode() {
        return mEditMode;
    }

    public void setText(String text) {
        mBinding.editText.setText(text);
        mBinding.textView.setText(text);
    }

    public CharSequence getText() {
        return mBinding.textView.getText();
    }
}
