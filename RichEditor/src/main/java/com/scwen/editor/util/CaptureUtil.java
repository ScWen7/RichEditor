package com.scwen.editor.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;


import com.scwen.editor.RichEditer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by scwen on 2019/4/24.
 * QQ ：811733738
 * 作用：
 */
public class CaptureUtil {

    /**
     * 保存图片到文件
     *
     * @param fileName 文件名称
     * @return
     * @throws Exception
     */
    public static File saveBitmap(RichEditer view, String dir, String fileName) {

        int imageCount = view.getImageCount();
        if (imageCount >= 9) {

            return null;
        }

        EditText lastFocusEdit = view.getLastIndexEdit();
        //判断最后一行 是否是空的
        int height = view.getHeight();
        if (lastFocusEdit != null && TextUtils.isEmpty(lastFocusEdit.getText())) {
            height = height - DensityUtil.dp2px(30);
        }

        Bitmap bmp = Bitmap.createBitmap(view.getWidth(), 1, Bitmap.Config.RGB_565);
        int rowBytes = bmp.getRowBytes();
        bmp.recycle();
        bmp = null;

        long size = rowBytes * view.getHeight();
        Log.e("TAG", "size:" + size);
        if (size >= getAvailMemory()) {

            return null;
        }
        Bitmap bitmap = null;
        bitmap = Bitmap.createBitmap(view.getWidth(), height, Bitmap.Config.ARGB_4444);
        bitmap.setHasAlpha(false);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.parseColor("#ffffff"));
        view.draw(canvas);

        if (bitmap == null)
            return null;
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        final File realFile = new File(file, fileName);
        FileOutputStream fos = null;
        try {
            if (!realFile.exists()) {
                realFile.createNewFile();
            }
            fos = new FileOutputStream(realFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {

        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!bitmap.isRecycled()) {
            bitmap.recycle();
            System.gc(); // 通知系统回收
        }
        return realFile;
    }

    public static long getAvailMemory() {// 获取android当前可用内存大小
        return Runtime.getRuntime().maxMemory();
    }
}
