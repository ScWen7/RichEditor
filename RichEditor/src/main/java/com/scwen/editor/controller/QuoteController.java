package com.scwen.editor.controller;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.widget.EditText;

import com.scwen.editor.span.MyBulletSpan;
import com.scwen.editor.span.MyQuoteSpan;
import com.scwen.editor.util.EditConstants;


/**
 * Created by scwen on 2019/4/19.
 * QQ ：811733738
 * 作用：
 */
public class QuoteController extends StyleController {

    @Override
    public boolean excuteDeleteAction(EditText editText, Editable editable, int startPos, int endPos) {

        MyQuoteSpan[] quoteSpans = editable.getSpans(startPos, endPos, MyQuoteSpan.class);
        if (null == quoteSpans || quoteSpans.length == 0) {
            return false;
        }

        MyQuoteSpan quoteSpan = quoteSpans[0];

        int spanStart = editable.getSpanStart(quoteSpan);

        int spanEnd = editable.getSpanEnd(quoteSpan);

        Log.e("TAG", "quote delete spanStart:" + spanStart + "  spanEnd:" + spanEnd);


        if (spanStart >= spanEnd) {
            //当前处于行首
            ForegroundColorSpan[] spans = editable.getSpans(startPos, endPos, ForegroundColorSpan.class);
            for (ForegroundColorSpan span : spans) {
                editable.removeSpan(span);
            }
            editable.removeSpan(quoteSpan);
        } else if (startPos == spanEnd) {
            //之后还有 字符串
            if (editable.length() > startPos) {
                //说明 后面 存在字符串
                if (editable.charAt(startPos) == EditConstants.CHAR_NEW_LINE) {
                    Log.e("TAG", "bullet 前一个 span 末尾是 换行");
                    MyBulletSpan[] spans = editable.getSpans(startPos, startPos, MyBulletSpan.class);
                    if (spans.length > 0) {
                        margeForward(editable, quoteSpan, spanStart, spanEnd);
                    }
                } else {
                    margeForward(editable, quoteSpan, spanStart, spanEnd);
                }

            }
        }

        return true;
    }


    private void margeForward(Editable editable, MyQuoteSpan quoteSpan, int spanStart, int spanEnd) {
        if (editable.length() <= spanEnd + 1) {
            return;
        }

        //检查 之后的 内容是否存在 quote 标签
        MyQuoteSpan[] spans = editable.getSpans(spanEnd, spanEnd + 1, MyQuoteSpan.class);
        if (spans == null || spans.length == 0) {
            //之后紧跟随 不存在 span 返回
            return;
        }
        MyQuoteSpan preSpan = spans[0];
        int targetStart = editable.getSpanStart(preSpan);
        int targetEnd = editable.getSpanEnd(preSpan);
        Log.e("TAG", "quote mergae targetStart: " + targetStart + "   targetEnd: " + targetEnd);
        int targetLength = targetEnd - targetStart;
        //修正最终位置
        spanEnd = spanEnd + targetLength;

        ForegroundColorSpan[] colorSpans = editable.getSpans(spanEnd, spanEnd + 1, ForegroundColorSpan.class);

        for (ForegroundColorSpan colorSpan : colorSpans) {
            editable.removeSpan(colorSpan);
        }

        for (MyQuoteSpan span : spans) {
            editable.removeSpan(span);
        }

        //获取所有需要关闭的 span
        MyQuoteSpan[] closeSpans = editable.getSpans(spanStart, spanEnd, MyQuoteSpan.class);

        for (MyQuoteSpan closeSpan : closeSpans) {
            editable.removeSpan(closeSpan);
        }

        ForegroundColorSpan[] colseColorSpans = editable.getSpans(spanStart, spanEnd, ForegroundColorSpan.class);

        for (ForegroundColorSpan colseColorSpan : colseColorSpans) {
            editable.removeSpan(colseColorSpan);
        }

        editable.setSpan(quoteSpan, spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#666666"));
        editable.setSpan(colorSpan, spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

    }

    @Override
    public boolean excuteInputAction(EditText editText, Editable editable, int start, int end) {
        if (end == 0) {
            return true;
        }
        //只处理 回车符
        char c = editable.charAt(end - 1);
        if (c != EditConstants.CHAR_NEW_LINE) {
            return true;
        }
        //判断当前输入是否存在了 quote 样式
        MyQuoteSpan[] spans = editable.getSpans(0, editable.length(), MyQuoteSpan.class);
        if (spans == null || spans.length == 0) {
            //不存在  引用样式  不处理
            return false;
        }

        //当前区域 存在 样式
        MyQuoteSpan[] quoteSpans = editable.getSpans(start, end, MyQuoteSpan.class);
        if (null == quoteSpans || quoteSpans.length == 0) {
            //表示 当前行 已经 不存在  引用样式

            //可能会有这样的内容
            // "哈酒和卡\n
            // \n

            // 用户执行了回车 操作  需要将之前的 \n全部删除
            int  moveIndex = 1;
            if (end > 1) {
                char lastChar = editable.charAt(end - 2);
                if (lastChar == EditConstants.CHAR_NEW_LINE) {
                    moveIndex = 2;
                }
            }
            //执行回车操作
            //删除之前的 \n符号
            editable.delete(end - moveIndex, end);
            excuteEnter(editText);
        }

        return true;
    }

    public <E> Pair<E, E> findFirstAndLast(Editable editable, E[] targetSpans) {
        E firstTargetSpan = targetSpans[0];
        E lastTargetSpan = targetSpans[0];
        if (targetSpans.length > 0) {
            int firstTargetSpanStart = editable.getSpanStart(firstTargetSpan);
            int lastTargetSpanEnd = editable.getSpanEnd(firstTargetSpan);
            for (E lns : targetSpans) {
                int lnsStart = editable.getSpanStart(lns);
                int lnsEnd = editable.getSpanEnd(lns);
                if (lnsStart < firstTargetSpanStart) {
                    firstTargetSpan = lns;
                    firstTargetSpanStart = lnsStart;
                }
                if (lnsEnd > lastTargetSpanEnd) {
                    lastTargetSpan = lns;
                    lastTargetSpanEnd = lnsEnd;
                }
            }
        }
        return new Pair(firstTargetSpan, lastTargetSpan);
    }
}
