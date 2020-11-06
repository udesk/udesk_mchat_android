package cn.udesk.model;

import java.io.Serializable;

import cn.udesk.UdeskUtil;

/**
 * Created by user on 2018/3/26.
 */

public class Tag implements Serializable {

    private static final long serialVersionUID = 9045651834480381823L;
    private Object text;
    private boolean isCheck;

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public String getText() {
        return UdeskUtil.objectToString(text);
    }

    public void setText(Object text) {
        this.text = text;
    }
}
