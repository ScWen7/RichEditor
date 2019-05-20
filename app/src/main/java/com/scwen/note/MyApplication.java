package com.scwen.note;

import android.content.Context;

import org.litepal.LitePalApplication;

import androidx.multidex.MultiDex;

/**
 * Created by scwen on 2019/4/27.
 * QQ ：811733738
 * 作用：
 */
public class MyApplication extends LitePalApplication {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}
