## 特别声明

**为和其他sdk兼容，从1.1.0 版本开始，修改了包名和部分类名，从低版本升级会报错，建议重新import，类名修改信息如下：**

|  原类名                        |     修改后类名                           |
|-------------------------------|-----------------------------------------|
| UdeskSDKManager               | UdeskMultimerchantSDKManager|
| UdeskConfig                   | UdeskMultimerchantConfig|
| LocalManageUtil               | UdeskMultimerchantLocalManageUtil|
| IFunctionItemClickCallBack    | IUdeskMultimerchantFunctionItemClickCallBack|
| INavigationItemClickCallBack  | IUdeskMultimerchantNavigationItemClickCallBack|
| FunctionMode                  | UdeskMultimerchantFunctionMode|
| NavigationMode                | UdeskMultimerchantNavigationMode|


## 1.导入集成 ##


**1 远程依赖集成**

 1.Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
 2.Add the dependency
	
	dependencies {
	        implementation 'com.github.udesk:udesk_mchat_android:版本号（比如 1.0.16）'
	}


**2 本地集成**

1.Add this in your root build.gradle file (not your module build.gradle file):

	allprojects {
		repositories {
			maven { url "https://jitpack.io" }
		}
	}

2.将multichat作为独立模块导入你的项目，并在你APP build.gradle文件中加入：
	
	implementation project(':multichat')

3.集成其他依赖

	implementation 'androidx.recyclerview:recyclerview:1.1.0'
    implementation 'com.github.bumptech.glide:glide:4.11.0'
    implementation 'com.github.chrisbanes:PhotoView:2.0.0'
    implementation 'org.sufficientlysecure:html-textview:3.6'
    implementation 'com.squareup.retrofit2:converter-gson:2.3.0'
    implementation 'org.greenrobot:eventbus:3.0.0'
    implementation 'org.igniterealtime.smack:smack-android-extensions:4.2.0'
    implementation 'org.igniterealtime.smack:smack-tcp:4.2.0'
    implementation 'com.squareup.retrofit2:retrofit:2.3.0'
    implementation'com.squareup.okhttp3:logging-interceptor:4.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.9.0'
    implementation 'com.squareup.okio:okio:2.8.0'


**注意**

**项目默认使用glide v4，如果要使用glide v3,将imageloader 文件夹下的UdeskGlideImageLoaderV3.jave 替换 multichat 库下面的cn.udesk.imageloader.UdeskGlideImageLoaderV4.java 即可**

## 2.快速使用 ##
  
### 1 初始化
  
	UdeskMultimerchantSDKManager.getInstance().init(context, uuid, sign, time);

	----------.getInstance().setCustomerInfo(CustomerInfo customerInfo，IInitCallBack iInitCallBack);
  **注意： 后续的操作放在初始化成功之后 示例**
	
	UdeskMultimerchantSDKManager.getInstance().setCustomerInfo(customerInfo, new IInitCallBack() {
                    @Override
                    public void initSuccess(boolean isSuccess) {
                        if (isSuccess){
                            //初始化成功 进行后续操作 
                        }
                    }
                });

  出于安全的考虑，建议租户将 key 保存在自己的服务器端，App 端通过租户提供的接口获取经过 SHA1
  计算后的加密字符串(sign)和时间戳(timestamp),时间戳精确到秒，然后传给 SDK。密码的有效期为时间戳 +/- 5分钟

|  数据名称  |     说明                                 |
|-----------|-----------------------------------------|
| uuid      | 租户ID，Udesk后台系统获取                            |
| secret    | Udesk后台提供                            |
| timestamp | 获取精确到秒的时间戳,由你们后端返回                       |
| sign      | SHA1("uuid+secret+timestamp")                       |
   
**备注：签名生成规则：建议由客户的服务端提供接口计算签名并返回对应的参数**

**客户自定义信息（CustomerInfo）参数**

|  参数                            |     说明                                 |
|---------------------------------|-----------------------------------------|
| euid                      | 用户ID是用户的唯一标示，请不要重复，并且只允许使用数字、字母、数字+字母（必传）|
| name                      | 用户昵称（*必传）|
| org                      | 公司名称|
| customerDescription                      | 用户描述|
| tags                      | 用户标签，用逗号分隔 如："帅气,漂亮"|
| cellphone                      | 手机号（唯一值，不同用户不允许重复，重复会导致创建用户失败！！！）|
| email                      | 邮箱（唯一值，不同用户不允许重复，重复会导致创建用户失败！！！）|
| customField                      | 用户自定义字段 |

	private CustomerInfo buildCustomerInfo(){
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setCellphone(cellphone.getText().toString());
        customerInfo.setCustomerDescription(description.getText().toString());
        customerInfo.setEmail(email.getText().toString());
        customerInfo.setEuid(customEuid.getText().toString());
        customerInfo.setName(name.getText().toString());
        customerInfo.setOrg(org.getText().toString());
        customerInfo.setTags(tags.getText().toString());
        Map<String, Object> definedUserTextField = getDefinedUserTextField();
        definedUserTextField.putAll(getDefinedUserRoplist());
        customerInfo.setCustomField(definedUserTextField);
        return customerInfo;
    }
   
**配置信息（UdeskMultimerchantConfig）明细**

|  属性                            |     说明                                 |
|---------------------------------|-----------------------------------------|
| registerId                      | 相关推送平台注册生成的ID                            |
| customerUrl                     | 保存客户的头像地址，由用户app传递                            |
| udeskTitlebarBgResId            | 标题栏TitleBar的背景色  通过颜色设置                       |
| udeskTitlebarTextLeftRightResId | 标题栏TitleBar，左右两侧文字的颜色                       |
| udeskIMLeftTextColorResId       | IM界面，左侧文字的字体颜色                       |
| udeskIMRightTextColorResId      | IM界面，右侧文字的字体颜色                      |
| udeskIMAgentNickNameColorResId  | IM界面，左侧客服昵称文字的字体颜色                     |
| udeskIMTimeTextColorResId       | IM界面，时间文字的字体颜色                       |
| udeskIMTipTextColorResId        | IM界面，提示语文字的字体颜色，比如客服转移                       |
| udeskbackArrowIconResId         | 返回箭头图标资源id                       |
| udeskCommityBgResId             | 咨询商品item的背景颜色                       |
| udeskCommityTitleColorResId     | 商品介绍Title的字样颜色                       |
| udeskCommitysubtitleColorResId  | 商品咨询页面中，商品介绍子Title的字样颜色        |
| udeskCommityLinkColorResId      | 商品咨询页面中，发送链接的字样颜色                       |
| udeskProductNameLinkColorResId  | 商品消息 含有链接时的标题的颜色                       |
| isUseNavigationView             | 设置是否使用导航UI                       |
| isUseShare                      | 配置是否把uuid、sign、timestamp存在sharePrefence中            |
| isUseVoice                      | 是否使用录音功能                       |
| isUsephoto                      | 是否使用相册的功能                       |
| isUsecamera                     | 是否使用拍照的功能                       |
| isScaleImg                      | 上传图片是否使用缩率图                       |
| scaleMax                        | 缩放图 设置宽高最大值，如果超出则压缩，否则不压缩                       |
| isUseMore                       | 是否使用更多展示出的列表选项                       |
| isUseSmallVideo                 | 设置是否需要小视频的功能                       |
| isUseEmotion                    | 是否使用表情                      |
| isUsefile                    | 是否使用上传文件功能                      |


### 2.客户通过某个商品详情页点击咨询按钮直接和客服进行会话
  
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

        UdeskMultimerchantSDKManager.getInstance().setProducts(products);

    }
	
	//如果不需要咨询对象显示
	 UdeskMultimerchantSDKManager.getInstance().setProducts(null);
	
	//通过商户ID，咨询商户
     // merchantId  商户ID
    UdeskMultimerchantSDKManager.getInstance().entryChat(context, merchantId);



### 3.获取历史对话商户列表

	//提供了历史对话商户列表，提供ConversationFragment，可根据你们app加入，参见demo。


### 4.获取指定的商户的未读消息

	UdeskMultimerchantSDKManager.getInstance().getMerchantUnReadMsg(merchant_euid,merchantUnreadMsgCnt)


### 5.查询所有商户未读消息

    UdeskMultimerchantSDKManager.getInstance().setItotalCount(new ItotalUnreadMsgCnt() {
                @Override
                public void totalcount(final int count) {
        
                }
            });
    UdeskMultimerchantSDKManager.getInstance().getUnReadMessages();



### 6.设置在线状态下收到消息的监听事件


	UdeskMultimerchantSDKManager.getInstance().setMessageArrived(new IMessageArrived() {
                @Override
                public void onNewMessage(final ReceiveMessage receiveMessage) {

                }
            });

### 7.设置咨询对象的回调


	UdeskMultimerchantSDKManager.getInstance().setCommodityCallBack(new ICommodityCallBack() {
                @Override
                public void callBackProduct(Products products) {

                
                }
            });


### 8.设置商品信息

商品信息属性 存放在ProductMessage类中;


	public class ProductMessage implements Serializable {


    /**
     * name :  Apple iPhone X (A1903) 64GB 深空灰色 移动联通4G手机
     * url : https://item.jd.com/6748052.html
     * imgUrl : http://img12.360buyimg.com/n1/s450x450_jfs/t10675/253/1344769770/66891/92d54ca4/59df2e7fN86c99a27.jpg
     * params : [{"text":"￥6999.00","color":"#FF0000","fold":false,"break":false,"size":12},{"text":"满1999元另加30元"}]
     */

    /**
     * 商品名称
     */
    private String name;
    /**
     * 商品跳转链接(新页显示)，如果值为空，则不能点击
     */
    private String url;
    /**
     * 商品显示图片的url
     */
    private String imgUrl;

    /**
     * 参数列表
     */
    private List<ParamsBean> params;
	
	
	public static class ParamsBean {
        /**
         * text : ￥6999.00
         * color : #FF0000
         * fold : false
         * break : false
         * size : 12
         */

        /**
         * 参数文本
         */
        private String text;
        /**
         * 参数颜色值，规定为十六进制值的颜色
         */
        private String color;

        /**
         * 是否粗体
         */
        private boolean fold;
        /**
         * 是否换行
         */
        @SerializedName("break")
        private boolean breakX;
        /**
         * 字体大小
         */
        private int size;
		
		
 
		使用方式 可以参考demo :
		1 直接进入会话界面后 设置传入商品信息 :
		 // 直接进入会话界面后 设置传入商品信息 
         UdeskMultimerchantSDKManager.getInstance().setProducts(products);
		 
		
		2 通过导航栏的方式 发送商品消息:
		  	UdeskMultimerchantConfig.isUseNavigationView = true;
            UdeskMultimerchantSDKManager.getInstance().setNavigationModes(getNavigations());
            UdeskMultimerchantSDKManager.getInstance().setNavigationItemClickCallBack(new IUdeskMultimerchantNavigationItemClickCallBack() {
                @Override
                public void callBack(Context context, ChatActivityPresenter mPresenter, UdeskMultimerchantNavigationMode navigationMode) {
                    if (navigationMode.getId() == 1) {
                        mPresenter.sendProductMessage(createProduct());
                    }
                }
            });
		
		private List<UdeskMultimerchantNavigationMode> getNavigations() {
	        List<UdeskMultimerchantNavigationMode> modes = new ArrayList<>();
	        UdeskMultimerchantNavigationMode navigationMode1 = new UdeskMultimerchantNavigationMode("发送商品消息发送商", 1);
	        modes.add(navigationMode1);
	        return modes;
    }

### 9.离线推送

	当前推送方案为Udesk务端发送消息至开发者的服务端，开发者再推送消息到 App。
	
	1、设置接收推送的服务器地址

	2、保存注册推送的的唯一ID
	UdeskMultimerchantSDKManager.getInstance().setRegisterId(context, registerId);

    public void setRegisterId(Context context, String registerId) {
        UdeskMultimerchantConfig.registerId = registerId;
        PreferenceHelper.write(context, UdeskLibConst.SharePreParams.RegisterIdName,
                UdeskLibConst.SharePreParams.Udesk_Push_RegisterId, registerId);
    }
	
	3、开启关闭推送开关  开启传false  关闭传true.
	UdeskMultimerchantSDKManager.getInstance().setCustomerOffline(false);
	
	demo 现在默认是进入会话界面关闭推送，退到后台开启推送
	建议 在application中registerActivityLifecycleCallbacks（ActivityLifecycleCallbacks activityLifecycleCallbacks）来控所有前后台逻辑

### 10.退出断开xmpp链接


	//退出登录时 断开xmpp链接 
	
	UdeskMultimerchantSDKManager.getInstance().logout();

### 11.图片上传压缩配置

     在UdeskMultimerchantConfig类中
    //上传图片是否使用原图 还是缩率图
    public static  boolean isScaleImg = true;

    //缩放图 设置宽高最大值，如果超出则压缩，否则不压缩
    public static  int ScaleMax = 1024;

### 12 多语言设置

application 中加入 

    @Override
    public void onCreate() {
        super.onCreate();
        UdeskMultimerchantLocalManageUtil.setApplicationLanguage(this);
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        UdeskMultimerchantLocalManageUtil.saveSystemCurrentLanguage(base);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //保存系统选择语言
        UdeskMultimerchantLocalManageUtil.onConfigurationChanged(getApplicationContext());
    }

在初始化的时候设置

	UdeskMultimerchantLocalManageUtil.saveSelectLanguage(getApplicationContext(),new Locale("en-us");

传入语言对应的编码，比如

	"en-us" => "英语",
    "es" => "西班牙语",
    "fr" => "法语",
    "ja" => "日语",
    "th" => "泰语",
    "id" => "印度尼西亚语",
    "pt" => "葡萄牙语",
    "ru" => "俄语"，
	"zh-CN" => “中文”


### 13.自定义按钮

	demo中示例

	 UdeskMultimerchantSDKManager.getInstance().setExtraFunctions(getExtraFunctions(), new IUdeskMultimerchantFunctionItemClickCallBack() {
                @Override
                public void callBack(Context context, ChatActivityPresenter mPresenter, UdeskMultimerchantFunctionMode functionMode) {
                    switch (functionMode.getId()){
                        case 21:
                            mPresenter.sendTxtMessage("发送文本消息");
                            break;
                        case 22:
                            mPresenter.sendProductMessage(createProduct());
                            break;
                        case 23:
                            UdeskMultimerchantSDKManager.getInstance().cancleXmpp();
                            break;
                        default:
                            break;
                    }
                }
            });

	1、创建自定义按钮

	说明：一个功能按钮设置成一个UdeskMultimerchantFunctionMode， 包含属性
        //显示内容
         private String name;
        //用来映射选择后对应的操作 id值 前20 是udesk 预留的,  客户自定义添加的，用于返回后根据id值建立映射关系
        private int id;
        //如 R.drawable.udesk_001
        //显示的图标
        private int mIconSrc ;
	
	    private List<UdeskMultimerchantFunctionMode> getExtraFunctions() {
        List<UdeskMultimerchantFunctionMode> modes = new ArrayList<>();
        UdeskMultimerchantFunctionMode functionMode1 = new UdeskMultimerchantFunctionMode("发送文本消息", 21, R.mipmap.udesk_form_table);
        UdeskMultimerchantFunctionMode functionMode2 = new UdeskMultimerchantFunctionMode("发送商品消息", 22, R.mipmap.udesk_form_table);
        UdeskMultimerchantFunctionMode functionMode3 = new UdeskMultimerchantFunctionMode("断开xmpp连接", 23, R.mipmap.udesk_form_table);
        modes.add(functionMode1);
        modes.add(functionMode2);
        modes.add(functionMode3);
        return modes;
    }

	2、设置自定义按钮及回调
	
	 /**
     * @param extraFunctions            设置额外的功能按钮
     * @param functionItemClickCallBack 支持自定义功能按钮后 点击事件回调 直接发送文本,图片,视频,商品信息等
     */
    public void setExtraFunctions(List<UdeskMultimerchantFunctionMode> extraFunctions, IFunctionItemClickCallBack functionItemClickCallBack) {
        this.extraFunctions = extraFunctions;
        this.functionItemClickCallBack = functionItemClickCallBack;
    }

	3、根据接口回调返回的 参数进行调用方法操作 ChatActivityPresenter mPresenter
	
		1 发送文本 
		  public void sendTxtMessage(String msgString)
		  mPresenter.sendTxtMessage(msgString);
		2 发送商品消息
		  public void sendProductMessage(ProductMessage mProduct)
		  mPresenter.sendProductMessage(mProduct)
		3 发送图片
		  public void sendBitmapMessage(String photoPath)
		  mPresenter.sendBitmapMessage(photoPath)
		4 发送小视频
		  public void sendVideoMessage(String videoPath)
		  mPresenter.sendVideoMessage(videoPath)
		5 发送录音消息
		  public void sendRecordAudioMsg(String audiopath, long duration)
		  mPresenter.sendRecordAudioMsg(audioFilePath, duration)

### 14.混淆配置

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
	
	//JSONobject
	-keep class org.json.** {*; }
	
	//okhttp
	-keep class okhttp3.** {*;} 
	-keep class okio.** {*;} 
	
	//retrofit2
	# Platform calls Class.forName on types which do not exist on Android to determine platform.
	-dontnote retrofit2.Platform
	# Platform used when running on Java 8 VMs. Will not be used at runtime.
	-dontwarn retrofit2.Platform$Java8
	# Retain generic type information for use by reflection by converters and adapters.
	-keepattributes Signature
	# Retain declared checked exceptions for use by a Proxy instance.
	-keepattributes Exceptions
	
	//glide
	-keep public class * implements com.bumptech.glide.module.GlideModule
	-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
	  **[] $VALUES;
	  public *;
	}
	
	# for DexGuard only
	-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
	
	-keep class com.github.chrisbanes.** {*;} 
	
	-dontwarn okio.**
	-dontwarn com.squareup.okhttp.**
	-dontwarn okhttp3.**
	-dontwarn javax.annotation.**
	-dontwarn com.android.volley.toolbox.**
	-dontwarn com.facebook.infer.**
	-dontwarn com.bumptech.glide.**
	
	 //其它
	-keep class org.sufficientlysecure.htmltextview.** {*; } 


## 3.更新日志 ##

### 1.1.0 更新内容 ###

1. 更改sdk包名和类名

### 1.0.17 更新内容 ###
1. 支持表单留言


### 1.0.16 更新内容（从本版本开始支持远程依赖） ###
1. 修改多商户sdk初始化逻辑
2. 修改xmpp问题


### 1.0.15 更新内容 ###
1. 适配Android11
2. Glide升级适配

### 1.0.14 更新内容 ###
1. 多商户支持导航菜单


### 1.0.13 更新内容 ###
1. 多商户支持信息丰富
2. 多商户支持文件
3. 多商户支持客户传参

### 1.0.12 更新内容 ###
1. 支持离线推送
2. 支持自定义按钮
3. 添加排队放弃和离线客户检测机制
4. 修复xmpp连接失败app无响应问题

### 1.0.11 更新内容 ###
1. 多商户支持部分表情

### 1.0.10 更新内容 ###
1. 多商户多语言适配 
2. 多商户sdk功能 
3. 多商户消息形式  

### 1.0.9 更新内容 ###
1. 适配androidQ

### 1.0.9 更新内容 ###
1. 添加黑名单功能
