package com.xgjk.common.lib.base;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;

/**
 * Created by shangwf on 2017/4/29.
 */

public abstract class BaseApp extends TinkerApplication {
    public static Application application;
    public static BaseActivityLifeCallback mActivityLifeCallback;

    public BaseApp(String applicationLikeClassPath) {
        super(ShareConstants.TINKER_ENABLE_ALL, applicationLikeClassPath, "com.tencent.tinker.loader.TinkerLoader", false);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        registerActivityLifecycle();
    }

    private void registerActivityLifecycle() {
        mActivityLifeCallback = new BaseActivityLifeCallback();
        registerActivityLifecycleCallbacks(mActivityLifeCallback);
    }

}
