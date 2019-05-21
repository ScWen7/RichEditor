package com.scwen.editor.styles;

import android.text.style.StrikethroughSpan;


/**
 * Created by scwen on 2019/4/20.
 * QQ ：811733738
 * 作用：
 */
public class StrikethroughStyle extends NormalStyle<StrikethroughSpan> {
    @Override
    protected StrikethroughSpan newSpan() {
        return new StrikethroughSpan();
    }
}
