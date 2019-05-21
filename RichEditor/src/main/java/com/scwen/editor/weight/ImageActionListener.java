package com.scwen.editor.weight;


/**
 * Created by scwen on 2019/4/23.
 * QQ ：811733738
 * 作用：
 */
public interface ImageActionListener {
      int  ACT_DELETE=0;
      int  ACT_REPLACE=1;
      int  ACT_PREVIEW=2;

      void onAction(int action, ImageWeight imageWeight);

}
