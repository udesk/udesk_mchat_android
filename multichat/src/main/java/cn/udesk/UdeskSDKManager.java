package cn.udesk;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.callback.ICommodityCallBack;
import cn.udesk.callback.IInitCallBack;
import cn.udesk.callback.IMerchantUnreadMsgCnt;
import cn.udesk.callback.IMessageArrived;
import cn.udesk.callback.INavigationItemClickCallBack;
import cn.udesk.callback.IProductMessageWebonCliclk;
import cn.udesk.callback.ItotalUnreadMsgCnt;
import cn.udesk.config.UdeskConfig;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.emotion.LQREmotionKit;
import cn.udesk.model.InitMode;
import cn.udesk.model.NavigationMode;
import cn.udesk.model.ProductMessage;
import cn.udesk.muchat.HttpCallBack;
import cn.udesk.muchat.HttpFacade;
import cn.udesk.muchat.UdeskLibConst;
import cn.udesk.muchat.bean.Products;
import cn.udesk.xmpp.Concurrents;
import cn.udesk.xmpp.UdeskXmppManager;
import udesk.core.utils.Cockroach;


public class UdeskSDKManager {

    private static UdeskSDKManager instance = new UdeskSDKManager();

    private UdeskXmppManager mUdeskXmppManager = new UdeskXmppManager();


    private InitMode initMode;

    private Context context;

    private IMessageArrived messageArrived;

    private ItotalUnreadMsgCnt itotalCount;
    private ICommodityCallBack commodityCallBack;

    /**
     * 设置的咨询对象
     */
    private Products products;

    /**
     * 设置商品消息
     */
    private ProductMessage productMessage;

    private static String customerEuid;
    private static String customerName;

    private ExecutorService scaleExecutor;

    private IProductMessageWebonCliclk productMessageWebonCliclk;

    //约定传递的自定义按钮集合
    public List<NavigationMode> navigationModes;

    private INavigationItemClickCallBack navigationItemClickCallBack;
    private ExecutorService singleExecutor;

    private void ensureMessageExecutor() {
        if (scaleExecutor == null) {
            scaleExecutor = Concurrents
                    .newSingleThreadExecutor("xmppconnectExecutor");
        }
    }


    private UdeskSDKManager() {
        singleExecutor = Executors.newSingleThreadExecutor();
    }
    public ExecutorService getSingleExecutor() {
        return singleExecutor;
    }
    public List<NavigationMode> getNavigationModes() {
        return navigationModes;
    }

    public void setNavigationModes(List<NavigationMode> navigationModes) {
        this.navigationModes = navigationModes;
    }

    public INavigationItemClickCallBack getNavigationItemClickCallBack() {
        return navigationItemClickCallBack;
    }

    public void setNavigationItemClickCallBack(INavigationItemClickCallBack navigationItemClickCallBack) {
        this.navigationItemClickCallBack = navigationItemClickCallBack;
    }

    public static UdeskSDKManager getInstance() {
        return instance;
    }


    public IMessageArrived getMessageArrived() {
        return messageArrived;
    }

    //设置消息到达的回调
    public void setMessageArrived(IMessageArrived messageArrived) {
        this.messageArrived = messageArrived;
    }

    public ICommodityCallBack getCommodityCallBack() {
        return commodityCallBack;
    }

    //设置咨询对象的回调
    public void setCommodityCallBack(ICommodityCallBack commodityCallBack) {
        this.commodityCallBack = commodityCallBack;
    }

    //设置总未读消息的回调
    public void setItotalCount(ItotalUnreadMsgCnt itotalCount) {
        this.itotalCount = itotalCount;
    }

    public ItotalUnreadMsgCnt getItotalCount() {
        return itotalCount;
    }

    public InitMode getInitMode() {
        if (initMode == null) {
            initMode(customerEuid, customerName,null);
        }
        return initMode;
    }

    public Products getProducts() {
        return products;
    }

    public void setProducts(Products products) {
        this.products = products;
    }


    public ProductMessage getProductMessage() {
        return productMessage;
    }

    public void setProductMessage(ProductMessage productMessage) {
        this.productMessage = productMessage;
    }

    /**
     * @param context
     * @param uuid      租户的唯一标识
     * @param sign      加密的key
     * @param timestamp 加密时传的时间
     */
    public void init(Context context, String uuid, String sign, String timestamp) {
        this.context = context;
        initDB(context, uuid);
        UdeskLibConst.uuid = uuid;
        UdeskLibConst.sign = sign;
        UdeskLibConst.timestamp = timestamp;
        if (UdeskConfig.isUseShare) {
            PreferenceHelper.write(context, UdeskLibConst.SharePreParams.Udesk_Sharepre_Name,
                    UdeskLibConst.SharePreParams.UUID, uuid);
            PreferenceHelper.write(context, UdeskLibConst.SharePreParams.Udesk_Sharepre_Name,
                    UdeskLibConst.SharePreParams.Sign, sign);
            PreferenceHelper.write(context, UdeskLibConst.SharePreParams.Udesk_Sharepre_Name,
                    UdeskLibConst.SharePreParams.TimeStamp, timestamp);
        }
        LQREmotionKit.init(context.getApplicationContext());
        Cockroach.install(new Cockroach.ExceptionHandler() {
            @Override
            public void handlerException(final Thread thread, final Throwable throwable) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.e("AndroidRuntime", "--->CockroachException:" + thread + "<---", throwable);
                        } catch (Throwable e) {

                        }
                    }
                });
            }
        });

    }

    /**
     * 初始化用户信息
     *
     * @param customer_euid 必填   用户唯一标识
     * @param customer_name 选填， 没有填写则设置euid的值
     */
    public void setCustomerInfo(final String customer_euid, String customer_name) {

        if (TextUtils.isEmpty(customer_euid)) {
            Toast.makeText(context, "必须设置客户的唯一标识", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(customer_name)) {
            customer_name = customer_euid;
        }
        customerEuid = customer_euid;
        customerName = customer_name;
        initMode(customer_euid, customer_name,null);
    }

    private void initMode(final String customer_euid, final String customer_name, final IInitCallBack iInitCallBack) {
        HttpFacade.getInstance().init(customer_euid, customer_name, new HttpCallBack() {
            @Override
            public void onSuccess(String message) {
                initMode = JsonUtils.parserInitMessage(message);
                if (initMode != null) {
                    cancleXmpp();
                    connectXmpp(initMode);
                    UdeskDBManager.getInstance().addInitInfo(initMode);
                    getUnReadMessages();
                    if (iInitCallBack != null){
                        iInitCallBack.initSuccess(initMode);
                    }
                }
            }

            @Override
            public void onFail(Throwable message) {

            }

            @Override
            public void onSuccessFail(String message) {

            }
        });
    }



    public void setCustomerOffline(boolean offline) {
        if (initMode != null) {
            HttpFacade.getInstance().switchPush(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                    UdeskUtil.objectToString(initMode.getIm_password())), getRegisterId(context), offline, new HttpCallBack() {
                @Override
                public void onSuccess(String message) {

                }

                @Override
                public void onFail(Throwable message) {

                }

                @Override
                public void onSuccessFail(String message) {

                }
            });
        }
    }


    public synchronized void connectXmpp(InitMode initMode) {

        if (initMode == null){
            return;
        }
        connection(UdeskUtil.objectToString(initMode.getIm_username()),
                UdeskUtil.objectToString(initMode.getIm_password()),
                UdeskUtil.objectToString(initMode.getTcp_server()),
                UdeskUtil.objectToInt(initMode.getTcp_port())
        );
    }

    public void connection(final String loginName,
                           final String loginPassword, final String loginServer, final int loginPort) {
        try {

            if (isConnection()) {
                return;
            }
            ensureMessageExecutor();
            scaleExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    if (mUdeskXmppManager != null) {
                        mUdeskXmppManager.startLoginXmpp(loginName, loginPassword, loginServer, loginPort);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancleXmpp() {
        try {
            LoaderTask.getThreadPoolExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    if (mUdeskXmppManager != null) {
                        mUdeskXmppManager.cancel();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnection() {
        if (mUdeskXmppManager != null) {
            return mUdeskXmppManager.isConnection();
        }
        return false;
    }

    /**
     * 进入会话入口,支持配置,根据配置进入会话
     *
     * @param context
     * @param euid    商户的euid
     */
    public void entryChat(final Context context, final String euid) {
        if (initMode != null) {
            entryUdeskChat(context,euid);
        } else {
            initMode(customerEuid, customerName, new IInitCallBack() {
                @Override
                public void initSuccess(InitMode initMode) {
                    entryUdeskChat(context,euid);
                }
            });
        }
    }

    private void entryUdeskChat(Context context, String euid) {
        Intent intent = new Intent(context, UdeskChatActivity.class);
        intent.putExtra(UdeskConst.Euid, euid);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    //获取注册推送的唯一ID
    public String getRegisterId(Context context) {
        if (TextUtils.isEmpty(UdeskConfig.registerId)) {
            return PreferenceHelper.readString(context, UdeskLibConst.SharePreParams.RegisterIdName, UdeskLibConst.SharePreParams.Udesk_Push_RegisterId);
        }
        return UdeskConfig.registerId;
    }

    //保存注册推送的的唯一ID
    public void setRegisterId(Context context, String registerId) {
        UdeskConfig.registerId = registerId;
        PreferenceHelper.write(context, UdeskLibConst.SharePreParams.RegisterIdName,
                UdeskLibConst.SharePreParams.Udesk_Push_RegisterId, registerId);
    }


    /**
     * 初始话DB
     *
     * @param context
     * @param sdkToken
     */
    public void initDB(Context context, String sdkToken) {
        releaseDB();
        UdeskDBManager.getInstance().init(context, sdkToken);
    }

    /**
     * 销毁DB
     */
    public void releaseDB() {
        UdeskDBManager.getInstance().release();
    }

    /**
     * 断开xmpp链接
     */
    public void logout() {
        cancleXmpp();
    }


    /**
     * 控制控制台日志的开关
     *
     * @param isShow true 开启 false 关闭
     */
    public void isShowLog(boolean isShow) {
        UdeskLibConst.xmppDebug = isShow;
        UdeskLibConst.isDebug = isShow;
    }




    /**
     * @param merchant_euid 指定商户euid
     */
    public void getMerchantUnReadMsg(String merchant_euid, final IMerchantUnreadMsgCnt merchantUnreadMsgCnt) {

        if (initMode != null) {
            if (initMode != null) {
                HttpFacade.getInstance().unreadCount(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                        UdeskUtil.objectToString(initMode.getIm_password())), merchant_euid, new HttpCallBack() {
                    @Override
                    public void onSuccess(String message) {
                        try {
                            JSONObject jsonObject = new JSONObject(message);
                            if (merchantUnreadMsgCnt != null) {
                                merchantUnreadMsgCnt.totalCount(UdeskUtil.objectToInt(jsonObject.opt("count")));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFail(Throwable message) {
                    }

                    @Override
                    public void onSuccessFail(String message) {
                        if (UdeskLibConst.isDebug) {
                            Log.i("udesk", "getMerchantUnReadMsg result =" + message);
                        }

                    }
                });
            }
        }

    }


    /**
     * 获取未读消息总数量
     */
    public void getUnReadMessages() {
        if (initMode != null) {
            HttpFacade.getInstance().unreadCount(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                    UdeskUtil.objectToString(initMode.getIm_password())), null, new HttpCallBack() {
                @Override
                public void onSuccess(String message) {

                    try {
                        JSONObject jsonObject = new JSONObject(message);
                        if (itotalCount != null) {
                            itotalCount.totalcount(UdeskUtil.objectToInt(jsonObject.opt("count")));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFail(Throwable message) {
                }

                @Override
                public void onSuccessFail(String message) {
                    if (UdeskLibConst.isDebug) {
                        Log.i("udesk", "getUnReadMessages result =" + message);
                    }

                }
            });
        }

    }


    /**
     * 获取历史会话商户的列表
     */
    public void getMerchants(HttpCallBack httpCallBack) {
        if (initMode != null) {
            HttpFacade.getInstance().getMerchants(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                    UdeskUtil.objectToString(initMode.getIm_password())), httpCallBack);
        }
    }

    public IProductMessageWebonCliclk getProductMessageWebonCliclk() {
        return productMessageWebonCliclk;
    }

    public void setProductMessageWebonCliclk(IProductMessageWebonCliclk productMessageWebonCliclk) {
        this.productMessageWebonCliclk = productMessageWebonCliclk;
    }
}
