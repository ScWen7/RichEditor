package com.scwen.editor.weight;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.scwen.editor.R;
import com.scwen.editor.util.EditerTranform;

import androidx.annotation.Nullable;


/**
 * Created by scwen on 2019/4/18.
 * QQ ：811733738
 * 作用： 图片区域
 */
public class ImageWeight extends BaseInputWeight implements View.OnClickListener {


    private ImageView iv_input_image;

    private LinearLayout ll_bottom_tools;
    private RelativeLayout rl_delete;
    private RelativeLayout rl_replace;
    private RelativeLayout rl_full;

    private String path;


    private String shortPath;

    public String getShortPath() {
        return shortPath == null ? "" : shortPath;
    }

    public void setShortPath(String shortPath) {
        this.shortPath = shortPath;
    }

    public String getPath() {
        return path == null ? "" : path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void replacePath(String path) {
        this.path = path;
        loadImage(path);
    }

    public ImageWeight(Context context, ViewGroup parent, String path) {
        super(context, parent);
        this.path = path;
        Log.e("TAG", "image 设置的 path:" + path);
        loadImage(path);
    }

    public void loadImage(String path) {

        RequestOptions options = new RequestOptions();
        options.placeholder(R.drawable.big_image_placeholder)
//                .error(R.drawable.big_image_placeholder)
                .sizeMultiplier(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new EditerTranform(mContext, 45));

        Glide.with(mContext)
                .load(path)
                .apply(options)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
                        int minimumHeight = resource.getMinimumHeight();
                        layoutParams.height = minimumHeight;
                        return false;
                    }
                })
                .into(iv_input_image);
    }


    private ImageActionListener mImageActionListener;

    public void setImageActionListener(ImageActionListener imageActionListener) {
        mImageActionListener = imageActionListener;
    }

    @Override
    public String getHtml() {
        return provideHtml(shortPath);
    }

    public String provideHtml(String path) {
        return String.format("<div class=\"image\"><img src=\"%s\"></img></div>", path);
    }

    @Override
    int provideResId() {
        return R.layout.note_input_image;
    }

    @Override
    public String getContent() {
        return "";
    }


    @Override
    public EditText getEditText() {
        return null;
    }

    private void initListener() {
        iv_input_image.setOnClickListener(this);
        rl_delete.setOnClickListener(this);
        rl_replace.setOnClickListener(this);
        rl_full.setOnClickListener(this);
    }

    @Override
    public void initView() {
        iv_input_image = mContentView.findViewById(R.id.iv_input_image);
        ll_bottom_tools = mContentView.findViewById(R.id.ll_bottom_tools);
        rl_delete = mContentView.findViewById(R.id.rl_delete);
        rl_replace = mContentView.findViewById(R.id.rl_replace);
        rl_full = mContentView.findViewById(R.id.rl_full);
        initListener();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.iv_input_image) {
            ll_bottom_tools.setVisibility(ll_bottom_tools.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        }else if(v.getId()==R.id.rl_delete) {
            if (mImageActionListener != null) {
                mImageActionListener.onAction(ImageActionListener.ACT_DELETE, this);
            }
        }else  if(v.getId()==R.id.rl_replace) {
            if (mImageActionListener != null) {
                mImageActionListener.onAction(ImageActionListener.ACT_REPLACE, this);
            }
        }else  if(v.getId()==R.id.rl_full) {
            if (mImageActionListener != null) {
                mImageActionListener.onAction(ImageActionListener.ACT_PREVIEW, this);
            }
        }

    }
}
