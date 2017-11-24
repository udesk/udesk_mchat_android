package cn.udesk.callback;

import cn.udesk.muchat.bean.ReceiveMessage;

/**
 * author : ${揭军平}
 * time   : 2017/10/24
 * desc   : 设置全局的消息到达回调
 * version: 1.0
 */

public interface IMessageArrived {

    void onNewMessage(ReceiveMessage receiveMessage);
}
