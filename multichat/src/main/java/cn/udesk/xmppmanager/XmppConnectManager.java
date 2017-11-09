package cn.udesk.xmppmanager;

import java.util.concurrent.ExecutorService;

import cn.udesk.UdeskSDKManager;


public class XmppConnectManager {

    private UdeskXmppManager mUdeskXmppManager;
    private ExecutorService messageExecutor;
    private static XmppConnectManager instance = new XmppConnectManager();


    public static XmppConnectManager getInstance() {
        return instance;
    }

    private XmppConnectManager() {
        mUdeskXmppManager = new UdeskXmppManager();
    }

    private void ensureMessageExecutor() {
        if (messageExecutor == null) {
            messageExecutor = Concurrents
                    .newSingleThreadExecutor("messageExecutor");
        }
    }

    public UdeskXmppManager getmUdeskXmppManager() {
        return mUdeskXmppManager;
    }

    public boolean isConnection() {
        if (mUdeskXmppManager != null) {
            return mUdeskXmppManager.isConnection();
        }
        return false;
    }

    public void connection(final String loginName,
                           final String loginPassword, final String loginServer, final int loginPort) {
        try {
            if (isConnection()) {
                return;
            }
            ensureMessageExecutor();
            messageExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    if (mUdeskXmppManager != null) {
                        mUdeskXmppManager.cancel();
                        mUdeskXmppManager.startLoginXmpp(loginName, loginPassword, loginServer, loginPort);
                        UdeskSDKManager.getInstance().setCustomerOffline(true);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void cancleXmpp() {
        try {
            ensureMessageExecutor();
            messageExecutor.submit(new Runnable() {
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

}
