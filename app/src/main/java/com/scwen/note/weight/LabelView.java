package com.scwen.note.weight;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.scwen.note.R;

import java.lang.ref.WeakReference;

/**
 * Created by scwen on 2019/4/22.
 * QQ ：811733738
 * 作用：
 */
public class LabelView extends RelativeLayout {

    private RelativeLayout rl_content;
    private EditText et_label;
    private ImageView iv_lcose;

    private String labelContent;

    private boolean needFocus;

    public LabelView(Context context, String labelContent, boolean needFocus) {
        super(context);
        this.labelContent = labelContent;
        this.needFocus = needFocus;
        init(context);
    }
//
//    public LabelView(Context context, AttributeSet attrs) {
//        this(context, attrs, -1);
//    }
//
//    public LabelView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//
//    }


    public void setActionListener(TextView.OnEditorActionListener actionListener) {
        et_label.setOnEditorActionListener(actionListener);
    }

    public void setFocusListener(OnFocusChangeListener focusListener) {
        et_label.setOnFocusChangeListener(focusListener);
    }

    public interface OnCloseListener {
        void onClose(View view);
    }

    private OnCloseListener mOnCloseListener;

    public void setOnCloseListener(OnCloseListener onCloseListener) {
        mOnCloseListener = onCloseListener;
    }

    private MyHandler mHandler;

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.note_editor_label, this);
        rl_content = findViewById(R.id.rl_content);
        et_label = findViewById(R.id.et_label);
        iv_lcose = findViewById(R.id.iv_lcose);
        mHandler = new MyHandler(this);
        if (TextUtils.isEmpty(labelContent) && needFocus) {
            et_label.requestFocus();
        } else if (!TextUtils.isEmpty(labelContent)) {
            et_label.setText(labelContent);
            showClose();
        }
    }

    public EditText getEditText() {
        return et_label;
    }


    public String getLableText() {
        return et_label.getText().toString().trim();
    }

    public void showClose() {
        et_label.clearFocus();
        et_label.setFocusable(false);
        et_label.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iv_lcose.getVisibility() == View.GONE) {
                    iv_lcose.setVisibility(View.VISIBLE);
                    rl_content.setBackgroundResource(R.drawable.shape_gray_stroke);
                    et_label.setTextColor(getResources().getColor(R.color.text_999));
                    mHandler.sendEmptyMessageDelayed(0, 3000);
                } else {
                    mHandler.removeCallbacksAndMessages(null);
                    if (mOnCloseListener != null) {
                        mOnCloseListener.onClose(LabelView.this);
                    }
                }
            }
        });

    }

    public String getContent() {
        return et_label.getText().toString();
    }


    @Override
    protected void onDetachedFromWindow() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        super.onDetachedFromWindow();
    }

    private void hideClose() {
        iv_lcose.setVisibility(View.GONE);
        rl_content.setBackground(null);
        et_label.setTextColor(getResources().getColor(R.color.text_333));
    }

    public static class MyHandler extends Handler {
        private WeakReference<LabelView> mReference;

        public MyHandler(LabelView reference) {
            mReference = new WeakReference(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            mReference.get().hideClose();
        }
    }


}
