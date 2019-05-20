package com.scwen.editor.controller;

import android.text.Editable;
import android.widget.EditText;

/**
 * Created by scwen on 2019/4/19.
 * QQ ：811733738
 * 作用： 所有样式  针对 用户删除 回车符 的 操作
 * 对于  每个编辑区域最多存在一种特殊样式： 列表  引用  待办
 */
public abstract class StyleController {

    private OnEditActionListener onEditActionListener;

    public void setOnEditActionListener(OnEditActionListener onEditActionListener) {
        this.onEditActionListener = onEditActionListener;
    }


    public abstract boolean excuteDeleteAction(EditText editText, Editable s, int start, int end);


    public abstract boolean excuteInputAction(EditText editText, Editable s, int start, int end);


    /**
     * 没有样式  执行 普通回退 逻辑
     *
     * @param editText
     */
    public void excuteBack(EditText editText) {
        if (onEditActionListener != null) {
            onEditActionListener.onBackspacePress(editText);
        }
    }

    /**
     * 没有特殊样式  执行 回车逻辑
     *
     * @param editText
     */
    public void excuteEnter(EditText editText) {
        if (onEditActionListener != null) {
            onEditActionListener.onEnter(editText);
        }
    }
}
