package cn.udesk.multimerchant.demo.jpush;

import android.content.Context;
import android.util.Log;

import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;
import cn.udesk.multimerchant.UdeskMultimerchantSDKManager;


/**
 * 自定义接收器
 * <p>
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class JpushReceiver extends JPushMessageReceiver {
    private static final String TAG = "udeskpush";

    @Override
    public void onRegister(Context context, String s) {
        super.onRegister(context, s);
        Log.d(TAG, "onRegister==" + s);
        UdeskMultimerchantSDKManager.getInstance().setRegisterId(context,s);
    }

    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageArrived(context, notificationMessage);
        Log.d(TAG, "onNotifyMessageArrived==" + notificationMessage.toString());

    }

    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageOpened(context, notificationMessage);
        Log.d(TAG, "onNotifyMessageOpened==" + notificationMessage.toString());
    }

    @Override
    public void onConnected(Context context, boolean b) {
        super.onConnected(context, b);
        Log.d(TAG, "onConnected==" +b);
    }
}
