package com.scwen.editor.styles;


import com.scwen.editor.span.ItalicSpan;
import com.scwen.editor.styles.NormalStyle;

/**
 * Created by scwen on 2019/4/20.
 * QQ ：811733738
 * 作用：
 */
public class ItalicStyle extends NormalStyle<ItalicSpan> {
    @Override
    protected ItalicSpan newSpan() {
        return new ItalicSpan();
    }
}
