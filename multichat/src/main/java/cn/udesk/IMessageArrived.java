package cn.udesk;

import cn.udesk.muchat.bean.ReceiveMessage;

/**
 * author : ${揭军平}
 * time   : 2017/10/24
 * desc   :
 * version: 1.0
 */

public interface IMessageArrived {
//    收到消息的回调接口
    void onNewMessage(ReceiveMessage receiveMessage);
}
