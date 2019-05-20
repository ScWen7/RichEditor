package com.scwen.editor.span;

import android.graphics.Paint;
import android.text.style.LineHeightSpan;

/**
 * Created by scwen on 2019/4/20.
 * QQ ：811733738
 * 作用：
 */
public class HeightSpan implements LineHeightSpan {
    private final int mHeight;

    public HeightSpan(float height) {
        this.mHeight = (int) Math.ceil(height);
    }

    @Override
    public void chooseHeight(
            CharSequence text,
            int start,
            int end,
            int spanstartv,
            int v,
            Paint.FontMetricsInt fm) {
        // This is more complicated that I wanted it to be. You can find a good explanation of what the
        // FontMetrics mean here: http://stackoverflow.com/questions/27631736.
        // The general solution is that if there's not enough height to show the full line height, we
        // will prioritize in this order: descent, ascent, bottom, top

        if (fm.descent > mHeight) {
            // Show as much descent as possible
            fm.bottom = fm.descent = Math.min(mHeight, fm.descent);
            fm.top = fm.ascent = 0;
        } else if (-fm.ascent + fm.descent > mHeight) {
            // Show all descent, and as much ascent as possible
            fm.bottom = fm.descent;
            fm.top = fm.ascent = -mHeight + fm.descent;
        } else if (-fm.ascent + fm.bottom > mHeight) {
            // Show all ascent, descent, as much bottom as possible
            fm.top = fm.ascent;
            fm.bottom = fm.ascent + mHeight;
        } else if (-fm.top + fm.bottom > mHeight) {
            // Show all ascent, descent, bottom, as much top as possible
            fm.top = fm.bottom - mHeight;
        } else {
            // Show proportionally additional ascent / top & descent / bottom
            final int additional = mHeight - (-fm.top + fm.bottom);

            // Round up for the negative values and down for the positive values  (arbritary choice)
            // So that bottom - top equals additional even if it's an odd number.
            fm.top -= Math.ceil(additional / 2.0f);
            fm.bottom += Math.floor(additional / 2.0f);
            fm.ascent = fm.top;
            fm.descent = fm.bottom;
        }
    }
}
