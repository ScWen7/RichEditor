package com.scwen.editor.span;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.BulletSpan;

import com.scwen.editor.util.DensityUtil;


public class MyBulletSpan extends BulletSpan {
    private static final int DEFAULT_COLOR = Color.parseColor("#999999");
    private static final int DEFAULT_RADIUS = DensityUtil.dp2px(2);
    private static final int DEFAULT_GAP_WIDTH = DensityUtil.dp2px(5);
    private static Path bulletPath = null;

    private int bulletColor = DEFAULT_COLOR;
    private int bulletRadius = DEFAULT_RADIUS;
    private int bulletGapWidth = DEFAULT_GAP_WIDTH;


    private int mLevel = 0; //ul 的层级

    public int getmLevel() {
        return mLevel;
    }

    public void setmLevel(int mLevel) {
        this.mLevel = mLevel;
    }

    public MyBulletSpan() {
//        this.bulletColor = bulletColor != 0 ? bulletColor : DEFAULT_COLOR;
//        this.bulletRadius = bulletRadius != 0 ? bulletRadius : DEFAULT_RADIUS;
//        this.bulletGapWidth = bulletGapWidth != 0 ? bulletGapWidth : DEFAULT_GAP_WIDTH;
    }

    public MyBulletSpan(Parcel src) {
        super(src);
        this.bulletColor = src.readInt();
        this.bulletRadius = src.readInt();
        this.bulletGapWidth = src.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(bulletColor);
        dest.writeInt(bulletRadius);
        dest.writeInt(bulletGapWidth);
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return DensityUtil.dp2px(21);
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout l) {
        if (((Spanned) text).getSpanStart(this) == start) {
            Paint.Style style = p.getStyle();

            int oldColor = p.getColor();
            p.setColor(bulletColor);
            p.setStyle(Paint.Style.FILL);

//            if (c.isHardwareAccelerated()) {
//                if (bulletPath == null) {
//                    bulletPath = new Path();
//                    // Bullet is slightly better to avoid aliasing artifacts on mdpi devices.
//                    bulletPath.addCircle(0.0f, 0.0f, bulletRadius, Path.Direction.CW);
//                }
//
//                c.save();
//                c.translate(x + dir * bulletRadius, (top + bottom) / 2.0f);
//                c.drawPath(bulletPath, p);
//                c.restore();
//            } else {
//                c.drawCircle(x + dir * bulletRadius, (top + bottom) / 2.0f, bulletRadius, p);
//            }

            c.drawText("\u2022", x + dir+DensityUtil.dp2px(5), baseline, p);
            p.setColor(oldColor);
            p.setStyle(style);
        }
    }
}