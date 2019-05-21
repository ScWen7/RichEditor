package com.scwen.editor.controller;

import android.text.Editable;
import android.text.Spanned;
import android.util.Log;
import android.util.Pair;
import android.widget.EditText;

import com.scwen.editor.span.MyBulletSpan;
import com.scwen.editor.util.EditConstants;
import com.scwen.editor.util.Util;


/**
 * Created by scwen on 2019/4/19.
 * QQ ：811733738
 * 作用：
 */
public class BulletContorller extends StyleController {


    @Override
    public boolean excuteDeleteAction(EditText editText, Editable editable, int start, int end) {
        MyBulletSpan[] listSpans = editable.getSpans(start, end,
                MyBulletSpan.class);
        if (null == listSpans || listSpans.length == 0) {
            return false;
        }

        MyBulletSpan theFirstSpan;

        Pair<MyBulletSpan, MyBulletSpan> firstAndLast = findFirstAndLast(editable, listSpans);

        theFirstSpan = firstAndLast.first;

        int spanStart = editable.getSpanStart(theFirstSpan);

        int spanEnd = editable.getSpanEnd(theFirstSpan);

        Log.e("TAG", "spanStart:" + spanStart + "  spanEnd:  " + spanEnd);


        if (spanStart == 0 && spanEnd == 1) {
            //由于在编辑行 需要直接显示 列表样式 所以在首行直接 添加了空字符串
            //删除字符串 删除 span
            //
            for (MyBulletSpan listSpan : listSpans) {
                editable.removeSpan(listSpan);
            }
            editable.delete(0, 1);
        } else if (spanStart >= spanEnd) {
            //
            for (MyBulletSpan listSpan : listSpans) {
                editable.removeSpan(listSpan);
            }
            //
            // 删除末尾 \n 换行
            // So the focus will go to the end of previous span
            if (spanStart > 0) {
                editable.delete(spanStart - 1, spanEnd);
            }
        } else if (start == spanEnd) {
            //删除 \n 后 start= spanEnd 成立
            //检查 cursor 后 是否存在字符串
            //存在字符串 进行 span 的merge
            //注意：此时这里的 start 为 前一行的末尾  位置
            if (editable.length() > start) {
                //说明 后面 存在字符串
                if (editable.charAt(start) == EditConstants.CHAR_NEW_LINE) {
                    Log.e("TAG", "bullet 前一个 span 末尾是 换行");
                    MyBulletSpan[] spans = editable.getSpans(start, start, MyBulletSpan.class);

                    if (spans.length > 0) {
                        mergeForward(editable, theFirstSpan, spanStart, spanEnd);
                    }
                } else {
                    mergeForward(editable, theFirstSpan, spanStart, spanEnd);
                }
            }

        }
        return true;
    }

    private void mergeForward(Editable editable, MyBulletSpan bulletSpan, int spanStart, int spanEnd) {
        //表示 已经在 第一行了 不需要 进行 合并
        if (editable.length() <= spanEnd + 1) {
            return;
        }

        MyBulletSpan[] spans = editable.getSpans(spanEnd, spanEnd + 1, MyBulletSpan.class);
        //检查之后是否存在 bulletSpan
        if (spans == null || spans.length == 0) {
            //之后不存在 span 不合并
            return;
        }

        Pair<MyBulletSpan, MyBulletSpan> firstAndLast = findFirstAndLast(editable, spans);

        MyBulletSpan firstTargetSpan = firstAndLast.first;
        MyBulletSpan lastTargetSpan = firstAndLast.second;

        int targetStart = editable.getSpanStart(firstTargetSpan);
        int targetEnd = editable.getSpanEnd(lastTargetSpan);

        Log.e("TAG", "targetStart:" + targetStart + "targetEnd:" + targetEnd);

        int targetLength = targetEnd - targetStart;
        spanEnd = spanEnd + targetLength;
        for (MyBulletSpan targetSpan : spans) {
            editable.removeSpan(targetSpan);
        }

        MyBulletSpan[] compositeSpans = editable.getSpans(spanStart, spanEnd, MyBulletSpan.class);

        for (MyBulletSpan span : compositeSpans) {
            editable.removeSpan(span);
        }

        editable.setSpan(bulletSpan, spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

    }


    @Override
    public boolean excuteInputAction(EditText editText, Editable editable, int start, int end) {
        if (end == 0) {
            return true;
        }
        //只处理  回车符
        char c = editable.charAt(end - 1);
        if (c != EditConstants.CHAR_NEW_LINE) {
            return true;
        }

        //判断当前输入是否存在了 quote 样式
        MyBulletSpan[] spans = editable.getSpans(0, editable.length(), MyBulletSpan.class);
        if (spans == null || spans.length == 0) {
            //不存在  引用样式  不处理
            return false;
        }

        //判断当前输入是否存在了 bullet 样式
        MyBulletSpan[] listSpans = editable.getSpans(start, end,
                MyBulletSpan.class);
        if (null == listSpans || listSpans.length == 0) {
            //表示 当前行 已经 不存在   列表样式
            //可能会有这样的内容
            // ·哈酒和卡\n
            // \n
            // 然后执行了回车 操作
            int moveIndex = 0;
            if (end > 1) {
                for (int i = end - 1; i >= 0; i--) {
                    char lastChar = editable.charAt(i);
                    if (lastChar == EditConstants.CHAR_NEW_LINE || c == EditConstants.ZERO_WIDTH_SPACE_INT) {
                        moveIndex++;
                    } else {
                        break;
                    }
                }
                if (moveIndex > 0) {
                    editable.delete(end - moveIndex, end);
                }
            }
            excuteEnter(editText);
            return true;
        } else {
            //获取当前的 样式数量
            int listSpanSize = listSpans.length;
            int previousListSpanIndex = listSpanSize - 1;
            //判断之前知否存在了 bulletspan
            if (previousListSpanIndex > -1) {  //存在了 span
                //获取前一个 span
                MyBulletSpan previousListSpan = listSpans[previousListSpanIndex];
                int lastListItemSpanStartPos = editable
                        .getSpanStart(previousListSpan);
                int lastListItemSpanEndPos = editable
                        .getSpanEnd(previousListSpan);
                //获取前一个 span 的内容
                CharSequence listItemSpanContent = editable.subSequence(
                        lastListItemSpanStartPos, lastListItemSpanEndPos);
                if (listItemSpanContent.length() == 0) {
                    //前一个 span 是空的 删除
                    editable.removeSpan(previousListSpan);
                    editable.delete(lastListItemSpanStartPos, lastListItemSpanEndPos);
                } else {
                    //当前执行了回车  对 上一行的span 执行分割 末尾定位到 当前的换行符
                    if (end > lastListItemSpanStartPos) {
                        editable.removeSpan(previousListSpan);
                        editable.setSpan(previousListSpan, lastListItemSpanStartPos, end - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    }

                }
                markLineAsBullet(editText);
            }
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

    private void markLineAsBullet(EditText editText) {
        int currentLine = Util.getCurrentCursorLine(editText);
        int start = Util.getThisLineStart(editText, currentLine);
        Editable editable = editText.getText();
        editable.insert(start, EditConstants.ZERO_WIDTH_SPACE_STR);

        start = Util.getThisLineStart(editText, currentLine);
        int end = Util.getThisLineEnd(editText, currentLine);

        if (end < 1) {
            return;
        }
//        if (editable.charAt(end - 1) == EditConstants.CHAR_NEW_LINE) {
//            end--;
//        }
        MyBulletSpan bulletSpan = new MyBulletSpan();
        editable.setSpan(bulletSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

}
