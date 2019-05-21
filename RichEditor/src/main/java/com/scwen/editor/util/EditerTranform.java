package com.scwen.editor.util;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;

import java.security.MessageDigest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Scale the image so that either the width of the image matches the given width and the height of the image is
 * greater than the given height or vice versa, and then crop the larger dimension to match the given dimension.
 * <p>
 * Does not maintain the image's aspect ratio
 */
public class EditerTranform extends BitmapTransformation {

    private int imageContentWidth;
    private float mScale;


    private static final String ID = "com.sogu.kindlelaw.note.editor.util.EditerTranform";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);


    public EditerTranform(Context context, int padding) {
        mScale = context.getResources().getDisplayMetrics().density;
        int contentWidth = ScreenUtils.getScreenWidth(context) - DensityUtil.dp2px(padding);
        imageContentWidth = contentWidth;
    }


    // Bitmap doesn't implement equals, so == and .equals are equivalent here.
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {

        int width = toTransform.getWidth(); // 图片的宽
        int height = toTransform.getHeight(); // 图片的高


//        Log.e("TAG", "width:" + width);
//        Log.e("TAG", "height:" + height);
//
//        Log.e("TAG", "imageContentWidth:" + imageContentWidth);

        width = (int) (width * mScale);
        height = (int) (height * mScale);

//        Log.e("TAG", "scale width:" + width);
//        Log.e("TAG", "scale height:" + height);


        if (width > imageContentWidth) {
            outWidth = imageContentWidth;
            float scale = outWidth / (float) width;
            outHeight = (int) (scale * height);
        } else {
            outWidth = width;
            outHeight = height;
        }

        Bitmap transformed = TransformationUtils.centerCrop(pool, toTransform, outWidth, outHeight);
        return transformed;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof EditerTranform;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}
