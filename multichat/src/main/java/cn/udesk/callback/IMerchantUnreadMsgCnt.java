package cn.udesk.callback;

/**
 * author : ${揭军平}
 * time   : 2017/11/17
 * desc   :指定商户的未读消息数量回调
 * version: 1.0
 */

public interface IMerchantUnreadMsgCnt {

    void totalCount(int count);
}
