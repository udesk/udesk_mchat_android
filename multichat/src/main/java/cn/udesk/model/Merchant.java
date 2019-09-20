package cn.udesk.model;

import cn.udesk.muchat.bean.ReceiveMessage;

/**
 * Created by Administrator on 2017/10/20.
 */
public class Merchant {

    /**
     * euid : merchant01
     * name : 测试商户
     * im_username : demo_merchant_1@udeskchannel.com
     * unread_count : 0
     * last_message : {"uuid":"0bbcd6ba-b8d6-4ad4-b90f-a634454e0d47","direction":"in","content":"你好","content_type":"text","category":"chat","created_at":"","sent_at":"","read_at":""}
     * logo_url : https://test.oss.com/merchants/10/logo.png
     * on_duty : true
     * off_duty_tips : 不在工作时间, 请留言
     */
//
    private Object code;
    private Object message;

    private Object euid;
    private Object name;
    private Object im_username;
    private Object unread_count;
    private ReceiveMessage last_message;
    private Object logo_url;
    private Object on_duty;
    private Object off_duty_tips;
    private Object id;
    private Object contacted_at;
    private Object is_blocked;

    public Object getEuid() {
        return euid;
    }

    public void setEuid(Object euid) {
        this.euid = euid;
    }

    public Object getName() {
        return name;
    }

    public void setName(Object name) {
        this.name = name;
    }

    public Object getIm_username() {
        return im_username;
    }

    public void setIm_username(Object im_username) {
        this.im_username = im_username;
    }

    public Object getUnread_count() {
        return unread_count;
    }

    public void setUnread_count(Object unread_count) {
        this.unread_count = unread_count;
    }

    public ReceiveMessage getLast_message() {
        return last_message;
    }

    public void setLast_message(ReceiveMessage last_message) {
        this.last_message = last_message;
    }

    public Object getLogo_url() {
        return logo_url;
    }

    public void setLogo_url(Object logo_url) {
        this.logo_url = logo_url;
    }

    public Object isOn_duty() {
        return on_duty;
    }

    public void setOn_duty(Object on_duty) {
        this.on_duty = on_duty;
    }

    public Object getOff_duty_tips() {
        return off_duty_tips;
    }

    public void setOff_duty_tips(Object off_duty_tips) {
        this.off_duty_tips = off_duty_tips;
    }

    public Object getCode() {
        return code;
    }

    public void setCode(Object code) {
        this.code = code;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getContacted_at() {
        return contacted_at;
    }

    public void setContacted_at(Object contacted_at) {
        this.contacted_at = contacted_at;
    }

    public Object getIs_blocked() {
        return is_blocked;
    }

    public void setIs_blocked(Object is_blocked) {
        this.is_blocked = is_blocked;
    }
}
