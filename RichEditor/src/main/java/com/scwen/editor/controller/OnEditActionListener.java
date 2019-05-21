package com.scwen.editor.controller;

import android.widget.EditText;

/**
 * Created by scwen on 2019/4/19.
 * QQ ：811733738
 * 作用：
 */
public interface OnEditActionListener {
    void onBackspacePress(EditText editText);

    void  onEnter(EditText editText);
}
