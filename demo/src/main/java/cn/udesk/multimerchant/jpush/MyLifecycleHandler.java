package cn.udesk.multimerchant.jpush;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import cn.udesk.UdeskSDKManager;

/**
 * Created by user on 2017/3/17.
 */

public class MyLifecycleHandler implements Application.ActivityLifecycleCallbacks {

    private static int resumed;
    private static int paused;
    public int count = 0;


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

        count++;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumed;
        UdeskSDKManager.getInstance().setCustomerOffline(true);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        count--;
        try {
            //应用退到后台 设置客户离线即开启推送
            if (count == 0) {
                UdeskSDKManager.getInstance().setCustomerOffline(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public static boolean isApplicationInForeground() {
        // 当所有 Activity 的状态中处于 resumed 的大于 paused 状态的，即可认为有Activity处于前台状态中
        return resumed > paused;
    }


}
