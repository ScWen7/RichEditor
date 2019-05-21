package com.scwen.editor.styles;

import com.scwen.editor.span.BoldSpan;
import com.scwen.editor.styles.NormalStyle;

/**
 * Created by scwen on 2019/4/20.
 * QQ ：811733738
 * 作用：
 */
public class BoldStyle extends NormalStyle<BoldSpan> {


    @Override
    protected BoldSpan newSpan() {
        return new BoldSpan();
    }
}
