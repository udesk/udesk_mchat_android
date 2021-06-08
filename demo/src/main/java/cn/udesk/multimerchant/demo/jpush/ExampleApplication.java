package cn.udesk.multimerchant.demo.jpush;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import androidx.multidex.MultiDex;

import cn.jpush.android.api.JPushInterface;
import cn.udesk.multimerchant.core.utils.UdeskMultimerchantLocalManageUtil;

/**
 * For developer startup JPush SDK
 * <p>
 * 一般建议在自定义 Application 类里初始化。也可以在主 Activity 里。
 */
public class ExampleApplication extends Application {
    private static final String TAG = "JPush";


    @Override
    public void onCreate() {
        Log.d(TAG, "[ExampleApplication] onCreate");
        super.onCreate();
        UdeskMultimerchantLocalManageUtil.setApplicationLanguage(this);
        //管理activity的生命周期，用来判断处在前后台情况
        registerActivityLifecycleCallbacks(new MyLifecycleHandler());

        JPushInterface.setDebugMode(true);    // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);            // 初始化 JPush

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        UdeskMultimerchantLocalManageUtil.saveSystemCurrentLanguage(base);
        MultiDex.install(base);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //保存系统选择语言
        UdeskMultimerchantLocalManageUtil.onConfigurationChanged(getApplicationContext());
    }

}
