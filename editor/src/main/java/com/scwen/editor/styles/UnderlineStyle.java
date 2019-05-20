package com.scwen.editor.styles;

import android.text.style.UnderlineSpan;


/**
 * Created by scwen on 2019/4/20.
 * QQ ：811733738
 * 作用：
 */
public class UnderlineStyle extends NormalStyle<UnderlineSpan> {
    @Override
    protected UnderlineSpan newSpan() {
        return new UnderlineSpan();
    }
}
