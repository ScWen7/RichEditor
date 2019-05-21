package com.scwen.editor.styles;

import android.text.Editable;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.Pair;

/**
 * Created by scwen on 2019/4/21.
 * QQ ：811733738
 * 作用：
 */
public class DynamicStyle {
    //大字体
    public static final int BIG_TEXT_SIZE = 22;
    //正常字体
    public static final int NORMAL_TEXT_SIZE = 16;
    //小字体
    public static final int SMALL_TEXT_SIZE = 12;


    public void applyNewStyle(Editable editable, int start, int end) {

        AbsoluteSizeSpan[] existingSpans = editable.getSpans(start, end, AbsoluteSizeSpan.class);

        if (existingSpans == null || existingSpans.length == 0) {
            //当前选中区域不存在 span
            // 直接创建span
            editable.setSpan(newSpan(BIG_TEXT_SIZE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            //获取 已经存在的 span
            Pair<AbsoluteSizeSpan, AbsoluteSizeSpan> firstAndLast = findFirstAndLast(editable, existingSpans);
            //获取 最左端的 span
            AbsoluteSizeSpan lastSpan = firstAndLast.first;

            int size = lastSpan.getSize();
            int tragetSize = getTragetSize(size);
            removeAllSpans(editable, start, end, AbsoluteSizeSpan.class);
            editable.setSpan(newSpan(tragetSize), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
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

    public int getTragetSize(int currentSize) {
        if (currentSize == NORMAL_TEXT_SIZE) {
            currentSize = BIG_TEXT_SIZE;
        } else if (currentSize == BIG_TEXT_SIZE) {
            currentSize = SMALL_TEXT_SIZE;
        } else {
            currentSize = NORMAL_TEXT_SIZE;
        }
        return currentSize;
    }

    private <E> void removeAllSpans(Editable editable, int start, int end, Class<E> clazzE) {
        E[] allSpans = editable.getSpans(start, end, clazzE);
        for (E span : allSpans) {
            editable.removeSpan(span);
        }
    }

    private AbsoluteSizeSpan newSpan(int currentStyle) {
        return new AbsoluteSizeSpan(currentStyle, true);
    }
}
