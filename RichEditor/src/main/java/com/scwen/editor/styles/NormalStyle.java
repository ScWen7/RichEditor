package com.scwen.editor.styles;

import android.text.Editable;
import android.text.Spanned;
import android.util.Pair;

import java.lang.reflect.ParameterizedType;

/**
 * Created by scwen on 2019/4/20.
 * QQ ：811733738
 * 作用：
 */
public abstract class NormalStyle<E> {

    protected Class<E> clazzE;

    public NormalStyle() {
        clazzE = (Class<E>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public void applyStyle(Editable editable, int start, int end) {

        E[] spans = editable.getSpans(start, end, clazzE);

        E existingSpan = null;

        if (spans.length > 0) {
            existingSpan = spans[0];
        }
        if (existingSpan == null) {  //当前选中 内部无此样式
            checkAndMergeSpan(editable, start, end, clazzE);
        } else {
            int existingESpanStart = editable.getSpanStart(existingSpan);
            int existingESpanEnd = editable.getSpanEnd(existingSpan);
            if (existingESpanStart <= start && existingESpanEnd >= end) {
                //在一个 完整的 span 中
                //删除 样式
                removeStyle(editable, start, end, clazzE, true);
            } else {
                checkAndMergeSpan(editable, start, end, clazzE);
            }
        }

    }

    /**
     * @param editable
     * @param start
     * @param end
     * @param clazzE
     * @param isSame   是否在 同一个 span 内部
     */
    private void removeStyle(Editable editable, int start, int end, Class<E> clazzE, boolean isSame) {

        E[] spans = editable.getSpans(start, end, clazzE);
        if (spans.length > 0) {
            if (isSame) {
                //在 同一个 span 中
                E span = spans[0];
                if (null != span) {
                    //
                    // User stops the style, and wants to show
                    // un-UNDERLINE characters
                    int ess = editable.getSpanStart(span); // ess == existing span start
                    int ese = editable.getSpanEnd(span); // ese = existing span end
                    if (start >= ese) {
                        // User inputs to the end of the existing e span
                        // End existing e span
                        editable.removeSpan(span);
                        editable.setSpan(span, ess, start - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else if (start == ess && end == ese) {
                        // Case 1 desc:
                        // *BBBBBB*
                        // All selected, and un-showTodo e
                        editable.removeSpan(span);
                    } else if (start > ess && end < ese) {
                        // Case 2 desc:
                        // BB*BB*BB
                        // *BB* is selected, and un-showTodo e
                        editable.removeSpan(span);
                        E spanLeft = newSpan();
                        editable.setSpan(spanLeft, ess, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        E spanRight = newSpan();
                        editable.setSpan(spanRight, end, ese, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else if (start == ess && end < ese) {
                        // Case 3 desc:
                        // *BBBB*BB
                        // *BBBB* is selected, and un-showTodo e
                        editable.removeSpan(span);
                        E newSpan = newSpan();
                        editable.setSpan(newSpan, end, ese, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else if (start > ess && end == ese) {
                        // Case 4 desc:
                        // BB*BBBB*
                        // *BBBB* is selected, and un-showTodo e
                        editable.removeSpan(span);
                        E newSpan = newSpan();
                        editable.setSpan(newSpan, ess, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            } else {
                Pair<E, E> firstAndLast = findFirstAndLast(editable, spans);

                E firstSpan = firstAndLast.first;
                E lastSpan = firstAndLast.second;

                int leftStart = editable.getSpanStart(firstSpan);

                int rightEnd = editable.getSpanEnd(lastSpan);

                editable.removeSpan(firstSpan);
                editable.setSpan(firstSpan, leftStart, start, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

                editable.removeSpan(lastSpan);
                editable.setSpan(lastSpan,end,rightEnd,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            }
        }


    }

    public Pair<E, E> findFirstAndLast(Editable editable, E[] targetSpans) {
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


    private void checkAndMergeSpan(Editable editable, int start, int end, Class<E> clazzE) {
        E leftSpan = null;
        E[] leftSpans = editable.getSpans(start, start, clazzE);
        if (leftSpans.length > 0) {
            leftSpan = leftSpans[0];
        }

        E rightSpan = null;
        E[] rightSpans = editable.getSpans(end, end, clazzE);
        if (rightSpans.length > 0) {
            rightSpan = rightSpans[0];
        }


        int leftSpanStart = editable.getSpanStart(leftSpan);
        int leftSpanEnd = editable.getSpanEnd(leftSpan);
        int rightStart = editable.getSpanStart(rightSpan);
        int rightSpanEnd = editable.getSpanEnd(rightSpan);

        removeAllSpans(editable, start, end, clazzE);
        if (leftSpan != null && rightSpan != null) {
            if (leftSpanEnd == rightStart) {
                //选中的两端是  连续的 样式
                removeStyle(editable, start, end, clazzE, false);
            } else {
                E eSpan = newSpan();
                editable.setSpan(eSpan, leftSpanStart, rightSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else if (leftSpan != null && rightSpan == null) {
            E eSpan = newSpan();
            editable.setSpan(eSpan, leftSpanStart, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (leftSpan == null && rightSpan != null) {
            E eSpan = newSpan();
            editable.setSpan(eSpan, start, rightSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            E eSpan = newSpan();
            editable.setSpan(eSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    protected abstract E newSpan();


    private <E> void removeAllSpans(Editable editable, int start, int end, Class<E> clazzE) {
        E[] allSpans = editable.getSpans(start, end, clazzE);
        for (E span : allSpans) {
            editable.removeSpan(span);
        }
    }
}
