package com.scwen.editor;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.scwen.editor.controller.AlignmentController;
import com.scwen.editor.controller.BulletContorller;
import com.scwen.editor.controller.NormalController;
import com.scwen.editor.controller.OnEditActionListener;
import com.scwen.editor.controller.QuoteController;
import com.scwen.editor.controller.StyleController;
import com.scwen.editor.model.TodoBean;
import com.scwen.editor.span.MyBulletSpan;
import com.scwen.editor.styles.DynamicStyle;
import com.scwen.editor.styles.ItalicStyle;
import com.scwen.editor.styles.StrikethroughStyle;
import com.scwen.editor.styles.UnderlineStyle;
import com.scwen.editor.util.DensityUtil;
import com.scwen.editor.util.EditConstants;
import com.scwen.editor.util.Util;
import com.scwen.editor.weight.BaseInputWeight;
import com.scwen.editor.weight.ImageActionListener;
import com.scwen.editor.weight.ImageWeight;
import com.scwen.editor.weight.TodoWeight;
import com.sogu.kindlelaw.note.editor.span.MyQuoteSpan;
import com.sogu.kindlelaw.note.editor.styles.BoldStyle;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * Created by scwen on 2019/4/18.
 * QQ ：811733738
 * 作用： 图文混排 编辑器
 */
public class RichEditer extends LinearLayout {


    private List<BaseInputWeight> inputWeights = new ArrayList<>();
    private TextWatcher watcher;

    public RichEditer(Context context) {
        this(context, null);
    }

    public RichEditer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichEditer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private EditText lastFocusEdit;


    private OnKeyListener keyListener;

    private OnFocusChangeListener focusListener; // 所有EditText的焦点监听listener

    private List<StyleController> mControllers;


    private com.sogu.kindlelaw.note.editor.FocusChangeListener mFocusChangeListener;

    public void setFocusChangeListener(com.sogu.kindlelaw.note.editor.FocusChangeListener focusChangeListener) {
        mFocusChangeListener = focusChangeListener;
    }

    private ImageActionListener mImageActionListener;

    public void setImageActionListener(ImageActionListener imageActionListener) {
        mImageActionListener = imageActionListener;
    }

    private void init() {
        setOrientation(VERTICAL);
        // 2. 初始化键盘退格监听
        // 主要用来处理点击回删按钮时，view合并操作
        keyListener = new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                        onBackspacePress((EditText) v);
                        return false;
                    }
                }
                return false;
            }
        };

        focusListener = new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.e("TAG", "focusChange");
                if (hasFocus) {
                    lastFocusEdit = (EditText) v;
                    if (mFocusChangeListener != null) {
                        mFocusChangeListener.onFocusChange(v, hasFocus);
                    }
                }
            }
        };

        initInputControllers();

        watcher = new TextWatcher() {
            int startPos = 0;
            int endPos = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                startPos = start;
                endPos = start + count;
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (endPos <= startPos) {
                    // 用户删除 字符串
                    for (StyleController controller : mControllers) {
                        boolean hasHandle = controller.excuteDeleteAction(lastFocusEdit, s, startPos, endPos);
                        if (hasHandle) break;
                    }
                } else {
                    // 用户输入 字符串
                    for (StyleController controller : mControllers) {
                        boolean hasHandle = controller.excuteInputAction(lastFocusEdit, s, startPos, endPos);
                        if (hasHandle) break;
                    }
                }
            }
        };


        TodoWeight todoWeight = new TodoWeight(getContext(), this, focusListener);
        inputWeights.add(todoWeight);
        todoWeight.getEditText().setHint("请输入正文");
        todoWeight.getEditText().setOnKeyListener(keyListener);
        todoWeight.getEditText().addTextChangedListener(watcher);
        todoWeight.getEditText().setOnFocusChangeListener(focusListener);
        addView(todoWeight.getContentView());
        lastFocusEdit = todoWeight.getEditText();
    }

    private void initInputControllers() {
        OnEditActionListener actionListener = new OnEditActionListener() {
            @Override
            public void onBackspacePress(EditText editText) {

            }

            @Override
            public void onEnter(EditText editText) {
                int lastEditIndex = indexOfChild((View) editText.getParent());
                addTodoWeightAtIndex(lastEditIndex + 1, "");
            }
        };

        mControllers = new ArrayList<>();
        BulletContorller bulletContorller = new BulletContorller();
        bulletContorller.setOnEditActionListener(actionListener);

        QuoteController quoteController = new QuoteController();
        quoteController.setOnEditActionListener(actionListener);

        AlignmentController alignmentController = new AlignmentController();
        alignmentController.setOnEditActionListener(actionListener);

        NormalController controller = new NormalController();
        controller.setOnEditActionListener(actionListener);

        mControllers.add(bulletContorller);
        mControllers.add(quoteController);
        mControllers.add(alignmentController);
        mControllers.add(controller);
    }


    public void markLineAsBullet() {

        int currentLine = Util.getCurrentCursorLine(lastFocusEdit);
        int start = Util.getThisLineStart(lastFocusEdit, currentLine);
        Editable editable = lastFocusEdit.getText();
        editable.insert(start, EditConstants.ZERO_WIDTH_SPACE_STR);

        start = Util.getThisLineStart(lastFocusEdit, currentLine);
        int end = Util.getThisLineEnd(lastFocusEdit, currentLine);

        if (end < 1) {
            return;
        }
//        if (editable.charAt(end - 1) == EditConstants.CHAR_NEW_LINE) {
//            end--;
//        }
        MyBulletSpan bulletSpan = new MyBulletSpan();
        editable.setSpan(bulletSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }


    public void onBackspacePress(EditText editText) {
        int selectionStart = editText.getSelectionStart();
        //只有光标在 edit 区域的 最前方  判断 上一个 控件的类型
        if (selectionStart == 0) {
            int editIndex = indexOfChild((View) editText.getParent());

            BaseInputWeight currentWeight = inputWeights.get(editIndex);

            if (currentWeight.isCheck()) {
                currentWeight.hideTodo();
                return;
            }

            Editable editableText = editText.getText();

            //获取输入当前行
            int currentLine = Util.getCurrentCursorLine(editText);
            //获取 当前行的 起始
            int start = Util.getThisLineStart(editText, currentLine);
            //获取当前行末尾
            int end = Util.getThisLineEnd(editText, currentLine);

            Log.e("TAG", "onBackspacePress Start:" + start + "  current end:" + end);

            MyBulletSpan[] bulletSpans = editableText.getSpans(start, end, MyBulletSpan.class);
            if (bulletSpans != null && bulletSpans.length > 0) {
                for (MyBulletSpan bulletSpan : bulletSpans) {
                    editableText.removeSpan(bulletSpan);
                }
                return;
            }

            MyQuoteSpan[] quoteSpans = editableText.getSpans(start, end, MyQuoteSpan.class);
            if (quoteSpans != null && quoteSpans.length > 0) {
                for (MyQuoteSpan quoteSpan : quoteSpans) {
                    editableText.removeSpan(quoteSpan);
                }

                ForegroundColorSpan[] spans = editableText.getSpans(start, end, ForegroundColorSpan.class);
                for (ForegroundColorSpan span : spans) {
                    editableText.removeSpan(span);
                }
                return;
            }

            AlignmentSpan.Standard[] alignmentSpans = editableText.getSpans(start, end, AlignmentSpan.Standard.class);
            if (alignmentSpans != null && alignmentSpans.length > 0) {
                for (AlignmentSpan.Standard alignmentSpan : alignmentSpans) {
                    editableText.removeSpan(alignmentSpan);
                }
            }

            //第一个控件 直接 返回
            if (editIndex == 0) {
                return;
            }
            //获取前一个 输入控件
            BaseInputWeight baseInputWeight = inputWeights.get(editIndex - 1);
            //执行类型检查
            if (baseInputWeight instanceof ImageWeight) {
                //前一个 控件是  图片 控件
                removeWeight(baseInputWeight);
            } else if (baseInputWeight instanceof TodoWeight) {
                //前一个控件是 edittext  进行 样式的合并
                //获取当前输入的 文本
                Editable currContent = editText.getText();
                //获取 前一个输入控件
                EditText preEdit = baseInputWeight.getEditText();
                //----------------------- 重要代码  这里需要执行前一个控件最后一行的 样式判断
                Editable preEditContent = preEdit.getText();
                int length = preEditContent.length();
                if (length > 0) {
                    //执行 样式检查
                    //判断前一个是否存在了 列表样式 或者 待办事项 样式
                    if (baseInputWeight.isCheck()) {
                        //存在 待办事项
                        //清除 spans
                        clearNormalStyles(editText);
                    }

                    MyBulletSpan[] spans = preEditContent.getSpans(length - 1, length, MyBulletSpan.class);
                    if (spans != null && spans.length > 0) {
                        //存在 列表项
                        //清除 spans
                        clearNormalStyles(editText);
                    }

                }
                //-----------------------
                removeWeight(inputWeights.get(editIndex));

                preEditContent.insert(preEditContent.length(), currContent);
                preEdit.setSelection(preEditContent.length(), preEditContent.length());
                preEdit.requestFocus();
                lastFocusEdit = preEdit;
            }

        }
    }

    public void clearNormalStyles(EditText editText) {
        Class[] spans = {AlignmentSpan.Standard.class, RelativeSizeSpan.class, StyleSpan.class, UnderlineSpan.class, StrikethroughSpan.class};
        BaseInputWeight.clearSpans(editText, 0, editText.getText().length(), spans);

    }

    /**
     * 获取索引位置
     *
     * @return
     */
    public int getLastIndex() {
        int lastEditIndex = getChildCount();
        return lastEditIndex;
    }

    public void removeWeight(BaseInputWeight inputWeight) {
        removeView(inputWeight.getContentView());
        inputWeights.remove(inputWeight);
    }

    public LayoutParams provideLayParam(int topMargin) {
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.topMargin = DensityUtil.dp2px(topMargin);
        return param;
    }


    public ImageWeight insertImage(String path) {
        //lastFocusEdit获取焦点的EditText
        Editable preContent = lastFocusEdit.getText();

        //获取控件位置
        int lastEditIndex = indexOfChild((View) lastFocusEdit.getParent());

        ImageWeight imageWeight = null;

        if (preContent.length() == 0) {
            //如果当前获取焦点的EditText为空，直接在EditText下方插入图片，并且插入空的EditText
            addTodoWeightAtIndex(lastEditIndex + 1, "");
            imageWeight = addImageWeightAtIndex(lastEditIndex + 1, path);
        } else {
            //获取光标所在位置
            int cursorIndex = lastFocusEdit.getSelectionStart();
            //获取光标前面的 内容
            CharSequence start = preContent.subSequence(0, cursorIndex);
            //获取光标后面内容
            CharSequence end = preContent.subSequence(cursorIndex, preContent.length());

            if (start.length() == 0) {
                //如果光标已经顶在了editText的最前面，则直接插入图片，并且EditText下移即可
                imageWeight = addImageWeightAtIndex(lastEditIndex, path);
                //同时插入一个空的EditText，防止插入多张图片无法写文字
                addTodoWeightAtIndex(lastEditIndex + 1, "");
            } else if (end.length() == 0) {
                // 如果光标已经顶在了editText的最末端，则需要添加新的imageView和EditText
                addTodoWeightAtIndex(lastEditIndex + 1, "");
                imageWeight = addImageWeightAtIndex(lastEditIndex + 1, path);
            } else {
                //如果光标已经顶在了editText的最中间，则需要分割字符串，分割成两个EditText，并在两个EditText中间插入图片
                //把光标前面的字符串保留，设置给当前获得焦点的EditText（此为分割出来的第一个EditText）
                lastFocusEdit.setText(start);
                //把光标后面的字符串放在新创建的EditText中（此为分割出来的第二个EditText）
                addTodoWeightAtIndex(lastEditIndex + 1, end);
                //在第二个EditText的位置插入一个空的EditText，以便连续插入多张图片时，有空间写文字，第二个EditText下移
                addTodoWeightAtIndex(lastEditIndex + 1, "");
                //在空的EditText的位置插入图片布局，空的EditText下移
                imageWeight = addImageWeightAtIndex(lastEditIndex + 1, path);
            }
        }

        return imageWeight;

    }


    public void quote() {

        Editable editableText = lastFocusEdit.getText();

        //获取输入当前行
        int currentLine = Util.getCurrentCursorLine(lastFocusEdit);
        //获取 当前行的 起始
        int start = Util.getThisLineStart(lastFocusEdit, currentLine);
        //获取当前行末尾
        int end = Util.getThisLineEnd(lastFocusEdit, currentLine);

        Log.e("TAG", "quote current Start:" + start + "  current end:" + end);

        MyQuoteSpan[] spans = editableText.getSpans(start, end, MyQuoteSpan.class);

        if (spans != null && spans.length > 0) {
            //已经存在了 mam样式   取消引用样式
            ForegroundColorSpan[] colorSpans = editableText.getSpans(start, end, ForegroundColorSpan.class);
            //判断 当前的行号
            if (start == 0) {  //第一行
                //直接取消样式
                editableText.removeSpan(colorSpans[0]);
                editableText.removeSpan(spans[0]);
            } else {
                //将 start 之前的样式设置回去
                MyQuoteSpan quoteSpan = spans[0];
                int spanStart = editableText.getSpanStart(quoteSpan);
                editableText.removeSpan(spans[0]);

                editableText.removeSpan(colorSpans[0]);
                if (start > spanStart) {
                    editableText.setSpan(quoteSpan, spanStart, start - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    editableText.setSpan(colorSpans[0], spanStart, start - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }

            return;
        }
        //当前行不存在 引用样式  判断是否包含其他样式
        boolean hasTodo = checkTodo();

        if (hasTodo) {
            String errorHint = getErrorHint("待办事项", "再加引用样式");
            Toast.makeText(getContext(), errorHint, Toast.LENGTH_SHORT).show();
            return;
        }
        //判断当前行是否存在 列表样式
        boolean hasBullet = checkCurrentStyle(MyBulletSpan.class);
        if (hasBullet) {
            String errorHint = getErrorHint("列表", "再加引用样式");
            Toast.makeText(getContext(), errorHint, Toast.LENGTH_SHORT).show();
            return;
        }
        //当前行不存在列表样式
        // 判断 当前控件是否已经存在了 列表样式

        boolean allHasBullet = checkALlStyle(MyBulletSpan.class);

        if (allHasBullet) {

            int length = editableText.length();
            if (end != length) {
                //当前不是最后一行
                String errorHint = getErrorHint("列表", "再加引用样式");
                Toast.makeText(getContext(), errorHint, Toast.LENGTH_SHORT).show();
                return;
            }
            //获取控件位置
            int lastEditIndex = indexOfChild((View) lastFocusEdit.getParent());

            //存在列表样式  取出最后一行 然后创建新的控件
            CharSequence sequence = editableText.subSequence(start, end);
            //-1 的原因是因为  前一行肯定存在 \n符号
            editableText.delete(start - 1, end);

            addTodoWeightWithStyle(lastEditIndex + 1, sequence, false);

        } else {
            //不存在其他 特殊样式
            MyQuoteSpan quoteSpan = new MyQuoteSpan();
            editableText.setSpan(
                    quoteSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#666666"));
            editableText.setSpan(colorSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

    }


    public void bullet() {

        Editable editableText = lastFocusEdit.getText();

        //获取输入当前行
        int currentLine = Util.getCurrentCursorLine(lastFocusEdit);
        //获取 当前行的 起始
        int start = Util.getThisLineStart(lastFocusEdit, currentLine);
        //获取当前行末尾
        int end = Util.getThisLineEnd(lastFocusEdit, currentLine);

        MyBulletSpan[] spans = editableText.getSpans(start, end, MyBulletSpan.class);

        if (spans != null && spans.length > 0) {

            MyBulletSpan span = spans[0];
            int spanStart = editableText.getSpanStart(span);
            Log.e("TAG", "lastSpanStart:" + spanStart);
            //因为bullet 样式 在最前端有一个 空字符串
            editableText.delete(0, 1);
            editableText.removeSpan(span);

            //            支持层级的 效果
//            先移出已有的 bullet span  然后添加 缩进 span
//            再次添加 bullet span
//            editableText.removeSpan(spans[0]);
//            MyLeadingMarginSpan stopSpan = new MyLeadingMarginSpan(DensityUtil.dp2px(28));
//            editableText.setSpan(
//                    stopSpan, 0, editableText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//            MyBulletSpan span = new MyBulletSpan(0, 0, 0);
//            editableText.setSpan(
//                    span, 0, editableText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            return;
        }

        boolean hasTodo = checkTodo();

        if (hasTodo) {
            String errorHint = getErrorHint("待办事项", "再加列表样式");
            Toast.makeText(getContext(), errorHint, Toast.LENGTH_SHORT).show();
            return;
        }

        //判断 当前行是否存在了 quote
        boolean hasQuote = checkCurrentStyle(MyQuoteSpan.class);
        if (hasQuote) {
            String errorHint = getErrorHint("引用", "再加列表样式");
            Toast.makeText(getContext(), errorHint, Toast.LENGTH_SHORT).show();
            return;
        }
        //当前行 不存在  检查 所有是否存在 quote 样式
        boolean allHasQuote = checkALlStyle(MyQuoteSpan.class);

        if (allHasQuote) {

            if (end != editableText.length()) {
                //当前不是最后一行
                String errorHint = getErrorHint("列表", "再加引用样式");
                Toast.makeText(getContext(), errorHint, Toast.LENGTH_SHORT).show();
                return;
            }

            //获取控件位置
            int lastEditIndex = indexOfChild((View) lastFocusEdit.getParent());
            //存在列表样式  取出最后一行 然后创建新的控件
            CharSequence sequence = editableText.subSequence(start, end);
            //-1 的原因是因为  前一行肯定存在 \n符号
            editableText.delete(start - 1, end);

            addTodoWeightWithStyle(lastEditIndex + 1, sequence, true);
        } else {
            //清除其他样式
            Class[] checkSpans = {AlignmentSpan.Standard.class, RelativeSizeSpan.class, StyleSpan.class, UnderlineSpan.class, StrikethroughSpan.class};
            BaseInputWeight.clearSpans(lastFocusEdit, 0, lastFocusEdit.getText().length(), checkSpans);
            markLineAsBullet();
        }


    }


    public void markLineAsQuote(int start, int end) {
        Editable editable = lastFocusEdit.getText();
        if (end < 1) {
            return;
        }
        //不存在其他 特殊样式
        MyQuoteSpan quoteSpan = new MyQuoteSpan();
        editable.setSpan(
                quoteSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#666666"));
        editable.setSpan(colorSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }


    public void markLineBullet(int start, int end) {
        Editable editable = lastFocusEdit.getText();
        editable.insert(start, EditConstants.ZERO_WIDTH_SPACE_STR);
        end++;
        if (end < 1) {
            return;
        }
        MyBulletSpan bulletSpan = new MyBulletSpan();
        editable.setSpan(bulletSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }


    public ImageWeight addImageWeightAtIndex(int index, String path) {
        ImageWeight imageWeight = new ImageWeight(getContext(), this, path);
        inputWeights.add(index, imageWeight);
        imageWeight.setImageActionListener(new ImageActionListener() {
            @Override
            public void onAction(int action, ImageWeight imageWeight) {
                switch (action) {
                    case ImageActionListener.ACT_DELETE:
                        removeWeight(imageWeight);
                        break;
                    case ImageActionListener.ACT_REPLACE:
                    case ImageActionListener.ACT_PREVIEW:
                        if (mImageActionListener != null) {
                            mImageActionListener.onAction(action, imageWeight);
                        }
                        break;
                }
            }
        });
        addView(imageWeight.getContentView(), index);
        return imageWeight;
    }

    private int index;

    public void addTodoWeight(CharSequence sequence, boolean showTodo, boolean hasCheck) {
        if (index == 0) {
            lastFocusEdit.getText().append(sequence);
            if (showTodo) {
                BaseInputWeight inputWeight = inputWeights.get(0);
                inputWeight.showTodo();
                if (hasCheck) {
                    inputWeight.checkTodo();
                }
            }
        } else {
            TodoWeight todoWeight = new TodoWeight(getContext(), this, focusListener);
            inputWeights.add(todoWeight);

            if (sequence != null && sequence.length() > 0) {
                todoWeight.getEditText().getText().append(sequence);
            }
            todoWeight.getEditText().setOnKeyListener(keyListener);
            todoWeight.getEditText().addTextChangedListener(watcher);
            todoWeight.getEditText().setOnFocusChangeListener(focusListener);
            addView(todoWeight.getContentView());

            lastFocusEdit = todoWeight.getEditText();
            lastFocusEdit.requestFocus();

            if (showTodo) {
                todoWeight.showTodo();
                if (hasCheck) {
                    todoWeight.checkTodo();
                }
            }
            lastFocusEdit.setSelection(sequence.length(), sequence.length());
        }
        index++;
    }

    public TodoWeight addTodoWeightAtIndex(int index, CharSequence sequence) {
        TodoWeight todoWeight = new TodoWeight(getContext(), this, focusListener);
        inputWeights.add(index, todoWeight);

        if (sequence != null && sequence.length() > 0) {
            todoWeight.getEditText().setText(sequence);
        }
        todoWeight.getEditText().setOnKeyListener(keyListener);
        todoWeight.getEditText().addTextChangedListener(watcher);
        todoWeight.getEditText().setOnFocusChangeListener(focusListener);
        addView(todoWeight.getContentView(), index, provideLayParam(0));

        lastFocusEdit = todoWeight.getEditText();
        lastFocusEdit.requestFocus();
        lastFocusEdit.setSelection(sequence.length(), sequence.length());
        return todoWeight;
    }

    public void addTodoWeightWithStyle(int index, CharSequence sequence, boolean needBullet) {
        TodoWeight todoWeight = new TodoWeight(getContext(), this, focusListener);
        inputWeights.add(todoWeight);

        if (sequence != null && sequence.length() > 0) {
            todoWeight.getEditText().getText().append(sequence);
        }
        todoWeight.getEditText().setOnKeyListener(keyListener);
        todoWeight.getEditText().addTextChangedListener(watcher);
        todoWeight.getEditText().setOnFocusChangeListener(focusListener);
        addView(todoWeight.getContentView(), index);

        lastFocusEdit = todoWeight.getEditText();
        lastFocusEdit.requestFocus();
        lastFocusEdit.setSelection(sequence.length(), sequence.length());
        if (needBullet) {
            markLineBullet(0, sequence.length());
        } else {
            markLineAsQuote(0, sequence.length());
        }
    }


    public void todo() {
        //只包含了 普通样式
        //获取控件位置
        TodoWeight todoWeight = getCurrentWeight();
        if (todoWeight == null) return;
        //判断是否已经选中
        if (todoWeight.isCheck()) {
            todoWeight.hideTodo();
            return;
        }

        boolean hasBullet = checkCurrentStyle(MyBulletSpan.class);
        if (hasBullet) {
            String errorHint = getErrorHint("列表", "再加待办事项");
            Toast.makeText(getContext(), errorHint, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasQuote = checkCurrentStyle(MyQuoteSpan.class);
        if (hasQuote) {
            String errorHint = getErrorHint("引用", "再加待办事项");
            Toast.makeText(getContext(), errorHint, Toast.LENGTH_SHORT).show();
            return;
        }

        //执行判断  前一个列表里面 存在了 列表 或者 引用样式  创建新的输入控件
        if (checkALlStyle(MyQuoteSpan.class) || checkALlStyle(MyBulletSpan.class)) {
            //获取当前 输入行
            int currentLine = Util.getCurrentCursorLine(lastFocusEdit);
            int start = Util.getThisLineStart(lastFocusEdit, currentLine);
            int end = Util.getThisLineEnd(lastFocusEdit, currentLine);
            //获取控件位置
            int currIndex = indexOfChild((View) lastFocusEdit.getParent());
            Editable editable = lastFocusEdit.getText();

            CharSequence preContent = editable.subSequence(start, end);

            //这里多减1位  删除掉 换行符
            editable.delete(start - 1, end);

            todoWeight = addTodoWeightAtIndex(currIndex + 1, preContent);
            todoWeight.showTodo();
        } else {
            //只包含了 普通样式
            //获取控件位置
            todoWeight.showTodo();
        }


    }

    private TodoWeight getCurrentWeight() {
        int lastEditIndex = indexOfChild((View) lastFocusEdit.getParent());
        BaseInputWeight inputWeight = inputWeights.get(lastEditIndex);
        if (!(inputWeight instanceof TodoWeight)) {
            return null;
        }
        TodoWeight todoWeight = (TodoWeight) inputWeight;
        return todoWeight;
    }


    public boolean checkALlStyle(Class aClass) {
        Editable editable = lastFocusEdit.getText();
        return checkHasStyle(aClass, 0, editable.length());
    }

    /**
     * 检查尾部是否存在样式
     *
     * @param aClass
     * @return
     */
    public boolean checkCurrentStyle(Class aClass) {
        int currentLine = Util.getCurrentCursorLine(lastFocusEdit);
        int start = Util.getThisLineStart(lastFocusEdit, currentLine);
        int end = Util.getThisLineEnd(lastFocusEdit, currentLine);

        return checkHasStyle(aClass, start, end);
    }


    public boolean checkHasStyle(Class aClass, int start, int end) {
        Editable editable = lastFocusEdit.getText();
        Object[] bulletSpans = editable.getSpans(start, end, aClass);
        if (bulletSpans != null && bulletSpans.length > 0) {
            return true;
        }
        return false;
    }

    public boolean checkTodo() {
        TodoWeight currentWeight = getCurrentWeight();
        if (currentWeight == null) {
            return false;
        }
        return currentWeight.isCheck();
    }

    public String getErrorHint(String typeName, String error) {
        return String.format("在%s中不可以%s", typeName, error);
    }

    public void alignment() {
        boolean hasTodo = checkTodo();
        if (hasTodo) {
            String errorHint = getErrorHint("待办事项", "加其他样式");
            Toast.makeText(getContext(), errorHint, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasBullet = checkCurrentStyle(MyBulletSpan.class);
        if (hasBullet) {
            String errorHint = getErrorHint("列表", "加其他样式");
            Toast.makeText(getContext(), errorHint, Toast.LENGTH_SHORT).show();
            return;
        }

        int currentLine = Util.getCurrentCursorLine(lastFocusEdit);
        int start = Util.getThisLineStart(lastFocusEdit, currentLine);
        int end = Util.getThisLineEnd(lastFocusEdit, currentLine);

        Editable editable = lastFocusEdit.getEditableText();

        Layout.Alignment currentAlign = Layout.Alignment.ALIGN_NORMAL;
        AlignmentSpan.Standard[] alignmentSpans = editable.getSpans(start, end, AlignmentSpan.Standard.class);
        if (null != alignmentSpans) {
            for (AlignmentSpan.Standard span : alignmentSpans) {
                currentAlign = span.getAlignment();
                editable.removeSpan(span);
            }
        }
        if (currentAlign == Layout.Alignment.ALIGN_NORMAL) {
            currentAlign = Layout.Alignment.ALIGN_CENTER;
        } else if (currentAlign == Layout.Alignment.ALIGN_CENTER) {
            currentAlign = Layout.Alignment.ALIGN_OPPOSITE;
        } else {
            currentAlign = Layout.Alignment.ALIGN_NORMAL;
        }

        AlignmentSpan alignCenterSpan = new AlignmentSpan.Standard(currentAlign);

        editable.setSpan(alignCenterSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    }


    public void fontSize() {

        Editable editable = lastFocusEdit.getEditableText();
        int start = lastFocusEdit.getSelectionStart();
        int end = lastFocusEdit.getSelectionEnd();

        Log.e("TAG", " font_size select  Start:" + start + "   end:  " + end);

        if (checkNormalStyle(start, end)) return;

        DynamicStyle dynamicStyle = new DynamicStyle();
        dynamicStyle.applyNewStyle(editable, start, end);
    }

    public void bold() {

        Editable editable = lastFocusEdit.getEditableText();
        int start = lastFocusEdit.getSelectionStart();
        int end = lastFocusEdit.getSelectionEnd();

        Log.e("TAG", "bold select  Start:" + start + "   end:  " + end);

        if (checkNormalStyle(start, end)) return;
        new BoldStyle().applyStyle(editable, start, end);

    }


    /**
     * 修改斜体样式
     */
    public void italic() {
        Editable editable = lastFocusEdit.getEditableText();
        int start = lastFocusEdit.getSelectionStart();
        int end = lastFocusEdit.getSelectionEnd();

        Log.e("TAG", "italic select  Start:" + start + "   end:  " + end);

        if (checkNormalStyle(start, end)) return;
        new ItalicStyle().applyStyle(editable, start, end);
    }

    private boolean checkNormalStyle(int start, int end) {
        boolean hasTodo = checkTodo();
        if (hasTodo) {
            String errorHint = getErrorHint("待办事项", "加其他样式");
            Toast.makeText(getContext(), errorHint, Toast.LENGTH_SHORT).show();
            return true;
        }

        boolean hasBullet = checkCurrentStyle(MyBulletSpan.class);
        if (hasBullet) {
            String errorHint = getErrorHint("列表", "加其他样式");
            Toast.makeText(getContext(), errorHint, Toast.LENGTH_SHORT).show();
            return true;
        }

        if (start > end) {
            return true;
        }
        return false;
    }

    public void underline() {
        Editable editable = lastFocusEdit.getEditableText();
        int start = lastFocusEdit.getSelectionStart();
        int end = lastFocusEdit.getSelectionEnd();

        Log.e("TAG", " underline select  Start:" + start + "   end:  " + end);

        if (checkNormalStyle(start, end)) return;
        new UnderlineStyle().applyStyle(editable, start, end);
    }

    public void strikethrough() {

        Editable editable = lastFocusEdit.getEditableText();
        int start = lastFocusEdit.getSelectionStart();
        int end = lastFocusEdit.getSelectionEnd();

        Log.e("TAG", " strikethrough select  Start:" + start + "   end:  " + end);

        if (checkNormalStyle(start, end)) return;
        new StrikethroughStyle().applyStyle(editable, start, end);

    }

    public String toHtml() {
        StringBuffer html = new StringBuffer();
        for (BaseInputWeight inputWeight : inputWeights) {
            html.append(inputWeight.getHtml());
        }
        return html.toString();
    }

    public EditText getLastFocusEdit() {
        return lastFocusEdit;
    }


    public EditText getLastIndexEdit() {
        return inputWeights.get(inputWeights.size() - 1).getEditText();
    }

    /**
     * 获取当前 需要预览 图片的位置 和所有图片的 path
     *
     * @param imageWeight
     * @return
     */
    public Pair<Integer, List<String>> getIndexAndPaths(ImageWeight imageWeight) {

        List<String> paths = new ArrayList<>();
        int index = 0;

        int calcIndex = -1;

        for (int i = 0; i < inputWeights.size(); i++) {
            BaseInputWeight weight = inputWeights.get(i);
            if (weight instanceof ImageWeight) {
                if (weight == imageWeight) {
                    calcIndex = index;
                } else {
                    index++;
                }
                paths.add(((ImageWeight) weight).getPath());
            }
        }

        Pair<Integer, List<String>> pair = new Pair<>(calcIndex, paths);

        return pair;
    }

    public List<String> getImagePaths() {
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < inputWeights.size(); i++) {
            BaseInputWeight weight = inputWeights.get(i);
            if (weight instanceof ImageWeight) {
                paths.add(((ImageWeight) weight).getShortPath());
            }
        }
        return paths;
    }

    /**
     * 获取所有 需要上传的 imageWeight 组件
     *
     * @return
     */
    public List<ImageWeight> getNeedUploadImages() {
        List<ImageWeight> weights = new ArrayList<>();
        for (BaseInputWeight inputWeight : inputWeights) {
            if (inputWeight instanceof ImageWeight) {
                ImageWeight imageWeight = (ImageWeight) inputWeight;
                if (TextUtils.isEmpty(imageWeight.getShortPath())) {
                    weights.add(imageWeight);
                }
            }
        }

        return weights;
    }


    /**
     * 获取已经添加的图片个数
     *
     * @return
     */
    public int getImageCount() {
        int count = 0;
        for (BaseInputWeight inputWeight : inputWeights) {
            if (inputWeight instanceof ImageWeight) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取正文
     *
     * @return
     */
    public String getContent() {
        StringBuilder stringBuilder = new StringBuilder();
        for (BaseInputWeight inputWeight : inputWeights) {
            stringBuilder.append(inputWeight.getContent());
        }

        return stringBuilder.toString();
    }


    public List<TodoBean> getAllTodos() {
        List<TodoBean> todos = new ArrayList<>();
        for (BaseInputWeight inputWeight : inputWeights) {
            if (inputWeight.isCheck()) {
                TodoWeight todoWeight = (TodoWeight) inputWeight;
                TodoBean todo = new TodoBean();
                todo.setStatus(todoWeight.hasDone() ? 1 : 0);
                todo.setItems(todoWeight.getContent());
                todos.add(todo);
            }
        }
        return todos;
    }
}
