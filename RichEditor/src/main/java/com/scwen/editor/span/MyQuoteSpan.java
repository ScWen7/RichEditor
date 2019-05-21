package com.scwen.editor.span;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.QuoteSpan;

import com.scwen.editor.R;
import com.scwen.editor.util.BitmapUtil;
import com.scwen.editor.util.DensityUtil;

import androidx.annotation.NonNull;


/**
 * Created by scwen on 2019/4/18.
 * QQ ：811733738
 * 作用：
 */
public class MyQuoteSpan extends QuoteSpan {
    private int drawableRes;

    private Bitmap bitmap;


    private Context mContext;

    public MyQuoteSpan(Context context) {
        this.mContext = context;
        initBitmap();
    }

    private void initBitmap() {
        Bitmap resource = BitmapFactory.decodeResource( mContext.getResources(), R.mipmap.ic_quote_span);
        bitmap = BitmapUtil.zoomBitmap(resource, DensityUtil.dp2px(16), DensityUtil.dp2px(12));
    }

    public MyQuoteSpan(Parcel src) {
        super(src);
        this.drawableRes = src.readInt();
        initBitmap();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(drawableRes);
    }

    @Override
    public int getLeadingMargin(boolean first) {

        return DensityUtil.dp2px(21);
    }


    @Override
    public void drawLeadingMargin(@NonNull Canvas c, @NonNull Paint p, int x, int dir, int top, int baseline, int bottom, @NonNull CharSequence text, int start, int end, boolean first, @NonNull Layout layout) {

        if (((Spanned) text).getSpanStart(this) == start) {
            p.setAntiAlias(true);
            p.setDither(true);
            c.drawBitmap(bitmap, x * 1.0f, top * 1.0f, p);
        }
    }
}
