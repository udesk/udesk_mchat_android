package cn.udesk.callback;

import android.content.Context;

import cn.udesk.model.NavigationMode;
import cn.udesk.presenter.ChatActivityPresenter;

/**
 *  支持客户在导航处添加自定义按钮的点击回调事件
 */

public interface INavigationItemClickCallBack {

    /**
     * @param context
     * @param mPresenter 支持直接后续操作发送
     */
    void callBack(Context context, ChatActivityPresenter mPresenter, NavigationMode navigationMode);
}
