package com.xgjk.common.lib.base;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.alibaba.android.arouter.launcher.ARouter;
import com.gyf.barlibrary.ImmersionBar;
import com.orhanobut.logger.Logger;
import com.xgjk.common.lib.R;
import com.xgjk.common.lib.databinding.ActivityBaseBinding;
import com.xgjk.common.lib.listener.OnClickEvent;
import com.xgjk.common.lib.manager.listenerHook.HookListenerContent;
import com.xgjk.common.lib.manager.listenerHook.HookListenerManager;
import com.xgjk.common.lib.manager.listenerHook.IHookListener;
import com.xgjk.common.lib.utils.DoubleClickExitDetector;
import com.xgjk.common.lib.utils.NetWorkUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * Created by shangwf on 2017/4/30.
 */

public abstract class DataBindingActivity<B extends ViewDataBinding> extends AppCompatActivity {
    public Context mContext;
    public ActivityBaseBinding mPageBinding;
    public B mViewBinding;
    public ViewDataBinding mExcepitonView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ImmersionBar.with(this)
                .statusBarColor(R.color.common_white)
                .fitsSystemWindows(true)
                .statusBarDarkFont(true,1) //原理：如果当前设备支持状态栏字体变色，会设置状态栏字体为黑色，如果当前设备不支持状态栏字体变色，会使当前状态栏加上透明度，否则不执行透明度
                .init();
        ARouter.getInstance().inject(this);
        EventBus.getDefault().register(this);
        mPageBinding = DataBindingUtil.setContentView(this, R.layout.activity_base);
        initBaseView();
        mContext = this;
        initPresenter();
        initView();
    }

    private void initBaseView() {
        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewBinding = DataBindingUtil.inflate(inflater, getLayoutId(), null, false);

        if (isShowExceptionView()) {
            mExcepitonView = DataBindingUtil.inflate(inflater, getExceptionLayoutId(), null, false);
            mPageBinding.baseContainer.addView(mExcepitonView.getRoot());
            showExceptionPage(!NetWorkUtils.isConnectedByState(this));
            mExcepitonView.getRoot().setOnClickListener(new OnClickEvent() {
                @Override
                public void singleClick(View v) {
                    refreshCurPage();
                }
            });
        }
        mPageBinding.baseContainer.addView(mViewBinding.getRoot());
    }

    protected void initPresenter() {
    }

    public abstract int getLayoutId();

    public abstract int getExceptionLayoutId();

    public abstract void initView();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private boolean doubleBackExit = false;
    private DoubleClickExitDetector doubleClickExitDetector;

    public void setDoubleBackExit(boolean doubleBackExit) {
        this.doubleBackExit = doubleBackExit;
        this.doubleClickExitDetector = new DoubleClickExitDetector(this);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK && doubleBackExit) {
            if (doubleClickExitDetector.click()) {
                //安全退出
                BaseApp.mActivityLifeCallback.finishAll();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {/* Do something */}


    public static class MessageEvent { /* Additional fields if needed */
    }

    public void showExceptionPage(boolean isShow) {
        mExcepitonView.getRoot().setVisibility(isShow ? View.VISIBLE : View.GONE);
        mViewBinding.getRoot().setVisibility(isShow ? View.GONE : View.VISIBLE);
    }

    public abstract boolean isShowExceptionView();

    public  void refreshCurPage(){
        showExceptionPage(!NetWorkUtils.isConnectedByState(this));
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        HookListenerContent.Builer builer = new HookListenerContent.Builer();
        builer.buildOnClickListener(new IHookListener() {
            @Override
            public void doListener(View v) {
            }
        });
        HookListenerManager.getInstance().startHook(this, HookListenerContent.create(builer));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        ARouter.getInstance().inject(this);
    }

    public void showPage(){
        mPageBinding.baseContainer.setVisibility(View.VISIBLE);
    }

    public void hidePage(){
        mPageBinding.baseContainer.setVisibility(View.INVISIBLE);
    }
}
