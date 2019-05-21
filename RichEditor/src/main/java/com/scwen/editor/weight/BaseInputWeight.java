package com.scwen.editor.weight;

import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.LayoutRes;

/**
 * Created by scwen on 2019/4/18.
 * QQ ：811733738
 * 作用：
 */
public abstract class BaseInputWeight {

    protected Context mContext;

    protected LayoutInflater mInflater;

    protected View mContentView;


    protected boolean isCheck;


    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public BaseInputWeight(Context context, ViewGroup parent) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        getView(parent);
    }


    public void getView(ViewGroup parent) {
        mContentView = mInflater.inflate(provideResId(), parent, false);
        initView();
    }


    public View getContentView() {
        return mContentView;
    }

    protected abstract void initView();


   public abstract String getHtml();

    abstract @LayoutRes
    int provideResId();

    public void showTodo() {
    }

    public void hideTodo() {

    }

    public  void checkTodo(){

    }

    public  void unCheckTodo(){

    }


   abstract public  String getContent();


    abstract public EditText getEditText();

    public static void clearSpans(EditText editText, int start, int end, Class... spanClass) {
        if (spanClass == null || spanClass.length == 0) {
            return;
        }
        Editable editable = editText.getText();
        for (Class aClass : spanClass) {
            Object[] spans = editable.getSpans(start, end, aClass);
            if (spans != null && spans.length > 0) {
                for (Object span : spans) {
                    editable.removeSpan(span);
                }
            }
        }

    }
}
