package cn.udesk.callback;

import cn.udesk.muchat.bean.ReceiveMessage;

/**
 * author : ${揭军平}
 * time   : 2017/10/24
 * desc   : 历史会话处理消息到达的回调
 * version: 1.0
 */

public interface IConversionMsgArrived {

    void onNewMessage(ReceiveMessage receiveMessage);
}
