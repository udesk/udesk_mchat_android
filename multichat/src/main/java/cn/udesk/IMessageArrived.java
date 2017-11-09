package cn.udesk;

import cn.udesk.muchat.bean.ReceiveMessage;

/**
 * author : ${揭军平}
 * time   : 2017/10/24
 * desc   :
 * version: 1.0
 */

public interface IMessageArrived {

    void onNewMessage(ReceiveMessage receiveMessage);
}
