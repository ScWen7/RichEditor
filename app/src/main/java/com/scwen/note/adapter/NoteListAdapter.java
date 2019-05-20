package com.scwen.note.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.luck.picture.lib.tools.ScreenUtils;
import com.scwen.editor.util.DensityUtil;
import com.scwen.note.NoteBean;
import com.scwen.note.R;
import com.scwen.note.util.DateUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * Created by scwen on 2019/5/7.
 * QQ ：811733738
 * 作用：
 */
public class NoteListAdapter extends BaseQuickAdapter<NoteBean, BaseViewHolder> {

    private float itemWidth, itemHeight;

    public NoteListAdapter(@Nullable List<NoteBean> data) {
        super(R.layout.item_note_list, data);
    }

    private void calcItemWidth() {
        //计算
        int space = ScreenUtils.getScreenWidth(mContext) - DensityUtil.dp2px(115);
        itemWidth = space / 3.0f;
        itemHeight = (60f / 88f) * itemWidth;
    }

    @Override
    protected void convert(BaseViewHolder helper, NoteBean item) {
        if (itemWidth == 0) {
            calcItemWidth();
        }
        LinearLayout llImages = helper.getView(R.id.ll_note_list_images);

        List<String> note_img = Arrays.asList(item.getNoteImg().split(","));

        llImages.removeAllViews();
        if (note_img != null && note_img.size() > 0) {
//            llImages.setVisibility(View.VISIBLE);
            if (note_img.size() > 3) {
                note_img = note_img.subList(0, 3);
            }
            for (int i = 0; i < note_img.size(); i++) {
                String imagePath = note_img.get(i);
                if (!TextUtils.isEmpty(imagePath)) {
                    ImageView imageView = new ImageView(mContext);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) itemWidth, (int) itemHeight);
                    if (i != 0) {
                        params.leftMargin = DensityUtil.dp2px(10);
                    }

                    llImages.addView(imageView, params);

                    Glide.with(mContext)
                            .load(imagePath)
                            .into(imageView);
                }

            }
        } else {
//            llImages.setVisibility(View.GONE);
        }
        TextView tvContent = helper.getView(R.id.tv_note_list_content);

        String content = item.getContent();
        if (TextUtils.isEmpty(content)) {
            tvContent.setVisibility(View.GONE);
        } else {
            tvContent.setVisibility(View.VISIBLE);
            tvContent.setText(item.getContent());
        }

        String date = DateUtil.format(new Date(item.getCreatetime()),DateUtil.PATTERN_3);
        helper.setText(R.id.tv_note_list_time,date);

        helper.setText(R.id.tv_note_list_title, item.getTitle());
    }
}
