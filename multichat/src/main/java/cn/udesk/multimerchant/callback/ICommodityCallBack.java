package cn.udesk.multimerchant.callback;

import cn.udesk.multimerchant.core.bean.Products;

/**
 * author : ${揭军平}
 * time   : 2017/10/25
 * desc   : 咨询对象的回调
 * version: 1.0
 */

public interface ICommodityCallBack {

    void callBackProduct(Products products);
}
