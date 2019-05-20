package com.scwen.editor.weight;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AlignmentSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.scwen.editor.R;
import com.scwen.editor.parser.Html;
import com.scwen.editor.span.HeightSpan;
import com.scwen.editor.util.DensityUtil;


/**
 * Created by scwen on 2019/4/18.
 * QQ ：811733738
 * 作用： 包含 待办事项的 输入区域
 */
public class TodoWeight extends BaseInputWeight {


    private CheckBox cb_todo_state;
    private EditText et_input;

    View.OnFocusChangeListener changeListener;

    public TodoWeight(Context context, ViewGroup parent, View.OnFocusChangeListener focusListener) {
        super(context, parent);
        this.changeListener = focusListener;
    }

    @Override
    protected void initView() {
        cb_todo_state = mContentView.findViewById(R.id.cb_todo_state);
        et_input = mContentView.findViewById(R.id.et_input);
        Editable editable = et_input.getText();
        editable.setSpan(new HeightSpan(DensityUtil.dp2px(26)), 0, editable.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        cb_todo_state.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //选中  表示已完成
                    Editable text = et_input.getText();
                    StrikethroughSpan span = new StrikethroughSpan();
                    text.setSpan(span, 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    et_input.setTextColor(Color.parseColor("#cccccc"));
                } else {
                    uncheckStyle();
                }
            }
        });

    }

    @Override
    public void checkTodo() {
        cb_todo_state.setChecked(true);
    }

    @Override
    public void unCheckTodo() {
        uncheckStyle();
    }

    private void uncheckStyle() {
        //反选  表示未完成
        Editable text = et_input.getText();
        et_input.setTextColor(Color.parseColor("#333333"));
        StrikethroughSpan[] spans = text.getSpans(0, text.length(), StrikethroughSpan.class);
        if (spans != null && spans.length > 0) {
            for (StrikethroughSpan strikethroughSpan : spans) {
                text.removeSpan(strikethroughSpan);
            }
        }
    }

    @Override
    public String getHtml() {
        if (TextUtils.isEmpty((et_input.getText()))) {
            return "";
        }
        if (isCheck) {
            //选中待办样式
            return provideCheckBox();
        } else {
            String html = Html.toHtml(et_input.getEditableText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);
            return html.replaceAll("<q><p>", "<q>").replaceAll("</p></q>", "</q>");
        }
    }

    @Override
    public String getContent() {
        String content = et_input.getText().toString().trim().replaceAll("\n", "");
        return content;
    }

    public String provideCheckBox() {
        String checked = "";
        if (cb_todo_state.isChecked()) {
            checked = "checked";
        }
        String regix = "<p><form><input type=\"checkbox\" disabled %s>%s</form></p>";
        return String.format(regix, checked, et_input.getText().toString());
    }


    @Override
    int provideResId() {
        return R.layout.note_input_todo;
    }


    @Override
    public EditText getEditText() {
        return et_input;
    }


    public  boolean hasDone(){
        return cb_todo_state.isChecked();
    }

    @Override
    public void showTodo() {
        Class[] spans = {AlignmentSpan.Standard.class, RelativeSizeSpan.class, StyleSpan.class, UnderlineSpan.class, StrikethroughSpan.class};

        clearSpans(et_input, 0, et_input.getText().length(), spans);

        et_input.setHint("待办事项");
        cb_todo_state.setVisibility(View.VISIBLE);
        //执行样式清除
        setCheck(true);
    }

    @Override
    public void hideTodo() {
        cb_todo_state.setVisibility(View.GONE);
        et_input.setHint("");
        uncheckStyle();
        setCheck(false);
    }
}
