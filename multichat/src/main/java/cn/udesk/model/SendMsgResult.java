package cn.udesk.model;

import java.io.Serializable;

/**
 * author : ${揭军平}
 * time   : 2017/10/21
 * desc   :
 * version: 1.0
 */

public class SendMsgResult implements Serializable {

    private  String id;
    private  String uuid;
    private  int flag;
    private String createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
