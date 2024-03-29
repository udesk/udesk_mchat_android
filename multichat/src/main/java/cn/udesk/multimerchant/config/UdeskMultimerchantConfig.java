package cn.udesk.multimerchant.config;


import cn.udesk.multimerchant.R;

/**
 * Created by user on 2016/8/12.
 */
public class UdeskMultimerchantConfig {

    public static final int DEFAULT = -1;

    //相关推送平台注册生成的ID
    public static String registerId = "";

    /**
     * 保存客户的头像地址，由用户app传递
     */
    public static String customerUrl = null;


    // 标题栏TitleBar的背景色  通过颜色设置
    public static int udeskTitlebarBgResId = DEFAULT;

    // 标题栏TitleBar，左右两侧文字的颜色
    public static int udeskTitlebarTextLeftRightResId = DEFAULT;

    //IM界面，左侧文字的字体颜色
    public static int udeskIMLeftTextColorResId = DEFAULT;

    //IM界面，右侧文字的字体颜色
    public static int udeskIMRightTextColorResId = DEFAULT;

    //IM界面，左侧客服昵称文字的字体颜色
    public static int udeskIMAgentNickNameColorResId = DEFAULT;

    //IM界面，时间文字的字体颜色
    public static int udeskIMTimeTextColorResId = DEFAULT;

    // IM界面，提示语文字的字体颜色，比如客服转移
    public static int udeskIMTipTextColorResId = DEFAULT;

    // 返回箭头图标资源id
    public static int udeskbackArrowIconResId = DEFAULT;

    // 咨询商品item的背景颜色
    public static int udeskCommityBgResId = DEFAULT;

    //    商品介绍Title的字样颜色
    public static int udeskCommityTitleColorResId = DEFAULT;

    //  商品咨询页面中，商品介绍子Title的字样颜色
    public static int udeskCommitysubtitleColorResId = DEFAULT;

    //    商品咨询页面中，发送链接的字样颜色
    public static int udeskCommityLinkColorResId = DEFAULT;

    // 商品消息 含有链接时的标题的颜色
    public static int udeskProductNameLinkColorResId = R.color.udesk_multimerchant_color_1850cc;

    //设置是否使用导航UI rue表示使用 false表示不使用
    public static boolean isUseNavigationView = false;

    //配置是否把domain 和 appid 和 appkey 和 sdktoken 存在sharePrefence中， true保存，false 不存
    public static boolean isUseShare = true;

    //是否使用录音功能  true表示使用 false表示不使用
    public static boolean isUseVoice = true;

    //是否使用发送图片的功能  true表示使用 false表示不使用
    public static boolean isUsephoto = true;

    //是否使用拍照的功能  true表示使用 false表示不使用
    public static boolean isUsecamera = true;

    //上传图片是否使用原图 还是缩率图
    public static  boolean isScaleImg = true;

    //缩放图 设置宽高最大值，如果超出则压缩，否则不压缩
    public static  int scaleMax = 1024;
    //是否使用更多展示出的列表选项 true表示使用 false表示不使用
    public static boolean isUseMore = true;
    //设置是否需要小视频的功能 rue表示使用 false表示不使用
    public static boolean isUseSmallVideo = true;

    //是否使用表情 true表示使用 false表示不使用
    public static boolean isUseEmotion = true;

    //是否使用上传文件功能  true表示使用 false表示不使用
    public static boolean isUsefile = true;
}
