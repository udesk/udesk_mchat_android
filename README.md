### 1.导入multichat

### 2.快速使用

```java
  
  
  1初始化
  
  // String uuid 租户ID，Udesk后台系统获取
 
  //String timestamp 时间戳，由你们后端返回

  //String sign  签名，由你们后端返回
  
  //customer_euid  用户ID是用户的唯一标示，请不要重复，并且只允许使用数字、字母、数字+字母
  
  UdeskSDKManager.getInstance().init(content, uuid, sign, time);
  UdeskSDKManager.getInstance().setCustomerInfo(customer_euid, customer_name);     
  
  备注：签名生成规则：建议由客户的服务端提供接口计算签名并返回对应的参数
|  数据名称  |     说明                                 |
|-----------|-----------------------------------------|
| uuid      | Udesk后台提供                            |
| secret    | Udesk后台提供                            |
| timestamp | 获取精确到秒的时间戳                       |
 
 sign = SHA1("uuid+secret+timestamp")

  2 客户通过某个商品详情页点击咨询按钮直接和客服进行会话
  
      //进入会话可以设置咨询对象 (可选)
	   //设置咨询的商品 如下：
    private void createProducts() {
        Products products = new Products();
        Products.ProductBean productBean = new Products.ProductBean();
        productBean.setImage("http://img14.360buyimg.com/n1/s450x450_jfs/t3157/63/1645131029/112074/f4f79169/57d0d44dN8cddf5c5.jpg?v=1483595726320");
        productBean.setTitle("Apple iPhone 7");
        productBean.setUrl("http://item.jd.com/3133829.html?cu=true&amp;utm_source…erm=9457752645_0_11333d2bdbd545f1839f020ae9b27f14");
        List<Products.ProductBean.ExtrasBean> extras = new ArrayList<>();

        Products.ProductBean.ExtrasBean extrasBean = new Products.ProductBean.ExtrasBean();
        extrasBean.setTitle("价格");
        extrasBean.setContent("￥6189.00");

        extras.add(extrasBean);
        productBean.setExtras(extras);
        products.setProduct(productBean);

        UdeskSDKManager.getInstance().setProducts(products);

    }
	
	//如果不需要咨询对象显示
	 UdeskSDKManager.getInstance().setProducts(null);
	
	//通过商户ID，咨询商户
     // merchantId  商户ID
    UdeskSDKManager.getInstance().entryChat(content, merchantId);
  
  //提供了历史对话商户列表，提供ConversationFragment，可根据你们app加入，参见demo。
}
```

### 3.获取历史对话商户列表

```java

 //提供了历史对话商户列表，提供ConversationFragment，可根据你们app加入，参见demo。
```

### 4.获取指定的商户的未读消息

```java

  UdeskSDKManager.getInstance().getMerchantUnReadMsg(merchant_euid,merchantUnreadMsgCnt)
```

### 5.查询所有商户未读消息

```java

    UdeskSDKManager.getInstance().setItotalCount(new ItotalUnreadMsgCnt() {
                @Override
                public void totalcount(final int count) {
        
                }
            });
    UdeskSDKManager.getInstance().getUnReadMessages();
```


### 6.设置在线状态下收到消息的监听事件

```java

       UdeskSDKManager.getInstance().setMessageArrived(new IMessageArrived() {
                @Override
                public void onNewMessage(final ReceiveMessage receiveMessage) {

                }
            });
```

### 7.设置咨询对象的回调

```java

        UdeskSDKManager.getInstance().setCommodityCallBack(new ICommodityCallBack() {
                @Override
                public void callBackProduct(Products products) {

                
                }
            });
```

### 8.离线推送
```java

//App 进入后台时，开启Udesk推送

调用 UdeskSDKManager.getInstance().setCustomerOffline(false);

建议 在application中registerActivityLifecycleCallbacks（ActivityLifecycleCallbacks activityLifecycleCallbacks）来控所有前后台逻辑

```

### 9.退出断开xmpp链接

```java

//退出登录时 断开xmpp链接 

UdeskSDKManager.getInstance().logout();

```

### 10.混淆配置

``` java
//udesk
-keep class udesk.** {*;} 
-keep class cn.udesk.**{*; } 

//oss
-keep com.alibaba.sdk.**{*; } 
-keep com.google.gson.**{*; } 
-keep org.jxmpp.**{*; } 
-keep  de.measite.minidns.**{*; } 

//eventbus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
 
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

//smack
-keep class org.jxmpp.** {*;} 
-keep class de.measite.** {*;} 
-keep class org.jivesoftware.** {*;} 
-keep class org.xmlpull.** {*;} 
-dontwarn org.xbill.**
-keep class org.xbill.** {*;} 

//retrofit2

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

//freso
-keep class com.facebook.** {*; }  
-keep class com.facebook.imagepipeline.** {*; } 
-keep class com.facebook.animated.gif.** {*; }  
-keep class com.facebook.drawee.** {*; }  
-keep class com.facebook.drawee.backends.pipeline.** {*; }  
-keep class com.facebook.imagepipeline.** {*; }  
-keep class bolts.** {*; }  
-keep class me.relex.photodraweeview.** {*; }  

-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}
# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

//eventbus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
 
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
-dontwarn javax.annotation.**
-dontwarn com.android.volley.toolbox.**
-dontwarn com.facebook.infer.**


 //其它
-keep class com.tencent.bugly.** {*; } 

```

