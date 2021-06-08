package cn.udesk.multimerchant.model;

import java.util.Map;

public class CustomerInfo {
    // 用户的唯一标示（注意：仅限传字母和数字的组合，*必传）
    private String euid;
    // 用户昵称（*必传）
    private String name;
    // 公司名称
    private String org;
    // 用户描述
    private String customerDescription;
    // 用户标签，用逗号分隔 如："帅气,漂亮"
    private String tags;
    // 手机号（唯一值，不同用户不允许重复，重复会导致创建用户失败！！！）
    private String cellphone;
    // 邮箱（唯一值，不同用户不允许重复，重复会导致创建用户失败！！！）
    private String email;
    // 用户自定义字段
    private Map<String,Object> customField;

    public String getEuid() {
        return euid;
    }

    public void setEuid(String euid) {
        this.euid = euid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getCustomerDescription() {
        return customerDescription;
    }

    public void setCustomerDescription(String customerDescription) {
        this.customerDescription = customerDescription;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, Object> getCustomField() {
        return customField;
    }

    public void setCustomField(Map<String, Object> customField) {
        this.customField = customField;
    }
}
