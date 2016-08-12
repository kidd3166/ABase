package com.ouj.library.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.ouj.library.R;

/**
 * Created by liqi on 2016-2-22.
 */
public class TintableImageView extends ImageView {


    public TintableImageView(Context context) {
        super(context);
    }


    public TintableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public TintableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setTintColor(@ColorInt int color) {
        super.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }


    public void setTintColorResource(@ColorRes int colorResource) {
        super.setColorFilter(getContext().getResources().getColor(colorResource), PorterDuff.Mode.SRC_IN);
    }


}
