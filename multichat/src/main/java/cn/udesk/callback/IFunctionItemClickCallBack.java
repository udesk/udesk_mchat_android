package cn.udesk.callback;

import android.content.Context;

import cn.udesk.model.FunctionMode;
import cn.udesk.presenter.ChatActivityPresenter;

/**
 * 支持自定义功能按钮后 点击事件回调 和直接发送文本,图片,视频,商品信息
 */

public interface IFunctionItemClickCallBack {

    void callBack(Context context, ChatActivityPresenter mPresenter, FunctionMode functionMode);
}
