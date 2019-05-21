package com.scwen.editor.util;

import android.content.res.Resources;

/**
 * dip,px,sp相互转化
 */
public class DensityUtil {

    /**
     * dip --> px
     */
    public static int dp2px(float dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dp * density + 0.5F);
    }

    /**
     * px --> dp
     */
    public static int px2dp(float px) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return (int) (px / density + 0.5F);
    }

    /**
     * sp --> px
     */
    public static int sp2px(float sp) {
        float sd = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (sp * sd + 0.5F);
    }

    /**
     * px --> sp
     */
    public static int px2sp(float px) {
        float sd = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (px / sd + 0.5F);
    }

}