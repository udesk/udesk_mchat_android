package cn.udesk.multimerchant.callback;

import android.content.Context;

import cn.udesk.multimerchant.model.UdeskMultimerchantNavigationMode;
import cn.udesk.multimerchant.presenter.ChatActivityPresenter;

/**
 *  支持客户在导航处添加自定义按钮的点击回调事件
 */

public interface IUdeskMultimerchantNavigationItemClickCallBack {

    /**
     * @param context
     * @param mPresenter 支持直接后续操作发送
     */
    void callBack(Context context, ChatActivityPresenter mPresenter, UdeskMultimerchantNavigationMode navigationMode);
}
