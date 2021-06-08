package cn.udesk.multimerchant.callback;

import android.content.Context;

import cn.udesk.multimerchant.model.UdeskMultimerchantFunctionMode;
import cn.udesk.multimerchant.presenter.ChatActivityPresenter;

/**
 * 支持自定义功能按钮后 点击事件回调 和直接发送文本,图片,视频,商品信息
 */

public interface IUdeskMultimerchantFunctionItemClickCallBack {

    void callBack(Context context, ChatActivityPresenter mPresenter, UdeskMultimerchantFunctionMode functionMode);
}
