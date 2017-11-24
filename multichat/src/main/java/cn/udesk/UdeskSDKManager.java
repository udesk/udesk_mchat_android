package cn.udesk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.ExecutorService;

import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.callback.ICommodityCallBack;
import cn.udesk.callback.IMerchantUnreadMsgCnt;
import cn.udesk.callback.IMessageArrived;
import cn.udesk.callback.ItotalUnreadMsgCnt;
import cn.udesk.config.UdeskConfig;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.xmpp.Concurrents;
import cn.udesk.xmpp.ConnectManager;
import cn.udesk.model.InitMode;
import cn.udesk.muchat.HttpCallBack;
import cn.udesk.muchat.HttpFacade;
import cn.udesk.muchat.UdeskLibConst;
import cn.udesk.muchat.bean.Products;
import udesk.core.utils.Cockroach;


public class UdeskSDKManager {

    private static UdeskSDKManager instance = new UdeskSDKManager();

    private ExecutorService messageExecutor;

    private InitMode initMode;

    private Context context;

    private IMessageArrived messageArrived;

    private ItotalUnreadMsgCnt itotalCount;
    private ICommodityCallBack commodityCallBack;

    /**
     * 设置的咨询对象
     */
    private Products products;

    private static String customerEuid;
    private static String customerName;


    private UdeskSDKManager() {
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

    private void ensureMessageExecutor() {
        if (messageExecutor == null) {
            messageExecutor = Concurrents
                    .newSingleThreadExecutor("managerExecutor");
        }
    }

    public InitMode getInitMode() {
        if (initMode == null) {
            initMode(customerEuid, customerName);
        }
        return initMode;
    }

    public void setInitMode(InitMode initMode) {
        this.initMode = initMode;
    }

    public Products getProducts() {
        return products;
    }

    public void setProducts(Products products) {
        this.products = products;
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
        initCrashReport(context);
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
        initMode(customer_euid, customer_name);
    }

    private void initMode(final String customer_euid, final String customer_name) {
        ensureMessageExecutor();
        messageExecutor.submit(new Runnable() {
            @Override
            public void run() {
                initMode = UdeskDBManager.getInstance().getInitMode(customer_euid);
                if (initMode == null) {
                    HttpFacade.getInstance().init(customer_euid, customer_name, new HttpCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            initMode = JsonUtils.parserInitMessage(message);
                            if (initMode != null) {
                                UdeskDBManager.getInstance().addInitInfo(initMode);
                                connectXmpp(initMode);
                                getUnReadMessages();

                            }
                        }

                        @Override
                        public void onFail(Throwable message) {

                        }

                        @Override
                        public void onSuccessFail(String message) {

                        }
                    });
                } else {
                    connectXmpp(initMode);
                    getUnReadMessages();
                }
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


    public void connectXmpp(InitMode initMode) {

        ConnectManager.getInstance().connection(UdeskUtil.objectToString(initMode.getIm_username()),
                UdeskUtil.objectToString(initMode.getIm_password()),
                UdeskUtil.objectToString(initMode.getTcp_server()),
                UdeskUtil.objectToInt(initMode.getTcp_port())
        );
    }

    /**
     * 进入会话入口,支持配置,根据配置进入会话
     *
     * @param context
     * @param euid    商户的euid
     */
    public void entryChat(Context context, String euid) {
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
     * 控制控制台日志的开关
     *
     * @param isShow true 开启 false 关闭
     */
    public void isShowLog(boolean isShow) {
        UdeskLibConst.xmppDebug = isShow;
        UdeskLibConst.isDebug = isShow;
    }


    private void initCrashReport(Context context) {
        try {
            CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
            strategy.setAppVersion(UdeskLibConst.sdkversion + UdeskUtil.getAppName(context));
            CrashReport.initCrashReport(context, UdeskLibConst.buglyAppid, false, strategy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(final Context context) {
        try {
            final int MAX_HEAP_SIZE = (int) Runtime.getRuntime().maxMemory();
            final int MAX_DISK_CACHE_SIZE = 300 * ByteConstants.MB;
            final int MAX_MEMORY_CACHE_SIZE = MAX_HEAP_SIZE / 3;
            final MemoryCacheParams bitmapCacheParams = new MemoryCacheParams(
                    MAX_MEMORY_CACHE_SIZE,
                    Integer.MAX_VALUE,
                    MAX_MEMORY_CACHE_SIZE,
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE);

            DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context)
                    .setMaxCacheSize(MAX_DISK_CACHE_SIZE)//最大缓存
                    .setBaseDirectoryName("udesk")//子目录
                    .setBaseDirectoryPathSupplier(new Supplier<File>() {
                        @Override
                        public File get() {
                            return UdeskUtil.getExternalCacheDir(context);
                        }
                    })
                    .build();
            ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context)
                    .setBitmapMemoryCacheParamsSupplier(
                            new Supplier<MemoryCacheParams>() {
                                public MemoryCacheParams get() {
                                    return bitmapCacheParams;
                                }
                            })
                    .setMainDiskCacheConfig(diskCacheConfig)
                    .setDownsampleEnabled(true)
                    .setBitmapsConfig(Bitmap.Config.RGB_565)
                    .build();

            Fresco.initialize(context, config);
        } catch (Exception e) {
            e.printStackTrace();
            Fresco.initialize(context);
        }
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
    public void getgetMerchants(HttpCallBack httpCallBack) {
        if (initMode != null) {
            HttpFacade.getInstance().getMerchants(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                    UdeskUtil.objectToString(initMode.getIm_password())), httpCallBack);
        }
    }
}
