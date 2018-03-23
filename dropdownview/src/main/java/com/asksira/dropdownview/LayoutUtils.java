package com.asksira.dropdownview;

import android.content.res.Resources;
import android.util.TypedValue;

public class LayoutUtils {

    public static int pixelFromDp (Resources r, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

}
