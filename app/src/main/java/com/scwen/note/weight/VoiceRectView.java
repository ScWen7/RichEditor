package com.scwen.note.weight;

import android.animation.FloatEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.scwen.editor.util.DensityUtil;

import java.util.LinkedList;

/**
 * Created by scwen on 2019/4/23.
 * QQ ：811733738
 * 作用：
 */
public class VoiceRectView extends View {


    private Paint mPaint;

    private int rectCount = 30;

    private float rectMinHeight, rectMaxHeight;

    private int mWidth, mHeight;

    private float spaceWidth;

    private LinkedList<Float> voiceLines;

    private FloatEvaluator evaluator;

    private float lineWidth = DensityUtil.dp2px(2);


    public VoiceRectView(Context context) {
        this(context, null);
    }

    public VoiceRectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoiceRectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.parseColor("#DDDDDD"));
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(lineWidth);
        rectMinHeight = DensityUtil.dp2px(6);
        rectMaxHeight = DensityUtil.dp2px(30);
        voiceLines = new LinkedList<>();
        evaluator = new FloatEvaluator();
        for (int i = 0; i < rectCount ; i++) {
            voiceLines.add(rectMinHeight);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;

        mHeight = h;
        spaceWidth = (mWidth * 1.0f - rectCount * lineWidth) / (rectCount - 1);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerY = mHeight / 2f;
        for(int i = 0; i < voiceLines.size(); i++) {
            Float voiceLine = voiceLines.get(i);
            canvas.drawLine(spaceWidth*i+lineWidth*i+(lineWidth/2f), centerY-voiceLine/2, spaceWidth*i+lineWidth*i+(lineWidth/2f), centerY+voiceLine/2, mPaint);
        }
    }

    public void setVloume(float volume) {
        float percent = volume / 100;
        Float evaluate = evaluator.evaluate(percent, rectMinHeight, rectMaxHeight);
        voiceLines.addFirst(evaluate);
        if (voiceLines.size() == rectCount) {
            voiceLines.removeLast();
        }
        postInvalidate();

    }
}
