package cn.udesk.xmpp;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import cn.udesk.JsonUtils;
import cn.udesk.UdeskSDKManager;
import cn.udesk.muchat.UdeskLibConst;
import cn.udesk.muchat.bean.ReceiveMessage;


public class UdeskXmppManager implements ConnectionListener, StanzaListener {

    private AbstractXMPPConnection xmppConnection = null;
    private StanzaFilter msgfilter = new StanzaTypeFilter(Message.class);
    private StanzaFilter presenceFilter = new StanzaTypeFilter(Presence.class);

    XMPPTCPConnectionConfiguration.Builder mConfiguration;
    private Handler handler = new Handler();


    volatile boolean isConnecting = false;


    String loginName;
    String loginPassword;
    String loginServer;
    int loginPort;

    private int time;

    public UdeskXmppManager() {

    }


    /**
     * @param loginName
     * @param loginPassword
     * @param loginServer
     * @param loginPort
     */
    public synchronized boolean startLoginXmpp(String loginName,
                                               String loginPassword, String loginServer, int loginPort) {

        if (TextUtils.isEmpty(loginName) || TextUtils.isEmpty(loginPassword) || TextUtils.isEmpty(loginServer)) {
            return false;
        }
        if (loginName.contains("@" + loginServer)) {
            int index = loginName.indexOf("@");
            loginName = loginName.substring(0, index);
        }
        this.loginName = loginName;
        this.loginPassword = loginPassword;
        this.loginServer = loginServer;
        this.loginPort = loginPort;
        if (!isConnecting) {
            isConnecting = true;
            try {
                if (TextUtils.isEmpty(loginServer) || TextUtils.isEmpty(loginName)
                        || TextUtils.isEmpty(loginPassword)) {
                    return false;
                }
                if (mConfiguration == null) {
                    mConfiguration = XMPPTCPConnectionConfiguration.builder();
                    mConfiguration.setUsernameAndPassword(loginName, loginPassword);
                    mConfiguration.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled).setCompressionEnabled(false);
                    mConfiguration.setResource(UUID.randomUUID().toString());
                    mConfiguration.setDebuggerEnabled(UdeskLibConst.xmppDebug);
                    mConfiguration.setXmppDomain(loginServer);
//                    mConfiguration.setServiceName(loginServer);
                    mConfiguration.setHost(loginServer);
                    mConfiguration.setPort(loginPort);
                }
                if (xmppConnection == null) {
                    xmppConnection = new XMPPTCPConnection(mConfiguration.build());
                }
                if (xmppConnection != null && !xmppConnection.isConnected()) {
                    xmppConnection.removeAsyncStanzaListener(this);
                    xmppConnection.addAsyncStanzaListener(this, new OrFilter(msgfilter,
                            presenceFilter));
                    xmppConnection.removeConnectionListener(this);
                    xmppConnection.addConnectionListener(this);
                    return connectXMPPServer(loginName, loginPassword);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            } finally {
                isConnecting = false;
            }
        }
        return false;
    }


    /**
     * 连接xmpp服务器
     */
    private synchronized boolean connectXMPPServer(String name, String password) {
        try {
            if (xmppConnection != null) {
                xmppConnection.connect();
                xmppConnection.login(name, password);
                xmppConnection.sendStanza(new Presence(Presence.Type.available));
                if (handler != null) {
                    handler.post(runnable);
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            sendSelfStatus();
            if (handler != null) {
                handler.postDelayed(this, 10000);
            }

        }
    };

    private void sendSelfStatus() {
        try {
            Presence statusPacket = new Presence(Presence.Type.available);
            if (xmppConnection != null) {
                xmppConnection.sendStanza(statusPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
            reConnected();
        }
    }

    private void processPresence(Presence pre) {

        if (pre.getType().equals(Presence.Type.subscribe)) {
            Presence presencePacket = new Presence(Presence.Type.subscribed);
            presencePacket.setTo(pre.getFrom());
            try {
                if (xmppConnection != null) {
                    xmppConnection.sendStanza(presencePacket);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processMessage(Message message) {
        if (!message.getBodies().isEmpty()) {
            try {
                Set<Message.Body> bodies = message.getBodies();
                Iterator<Message.Body> iterable = bodies.iterator();
                if (iterable.hasNext()) {
                    Message.Body body = iterable.next();
                    JSONObject json = new JSONObject(body.getMessage());
                    if (json.has("type")) {
                        String type = json.optString("type");
                        if (type.equals("message")) {
                            if (json.has("content")) {
                                ReceiveMessage receiveMessage = JsonUtils.parserReceiveMessage(json.getString("content"));
                                EventBus.getDefault().post(receiveMessage);
                                if (UdeskSDKManager.getInstance().getMessageArrived() != null) {
                                    UdeskSDKManager.getInstance().getMessageArrived().onNewMessage(receiveMessage);
                                }
                            }

                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private synchronized void reConnected() {
        if (time > 5) {
            return;
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    time++;
                    cancel();
                    startLoginXmpp(loginName,
                            loginPassword, loginServer, loginPort);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }


    /**
     * 断开连接
     */
    public boolean cancel() {
        try {
            Log.i("xxx", "cancel xmpp");
            if (xmppConnection != null) {
                xmppConnection.removeAsyncStanzaListener(UdeskXmppManager.this);
                xmppConnection.removeConnectionListener(UdeskXmppManager.this);
                handler.removeCallbacks(runnable);
                xmppConnection.disconnect();
                xmppConnection = null;
            }
            if (mConfiguration != null) {
                mConfiguration = null;
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public void connected(XMPPConnection arg0) {


    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {

    }

    @Override
    public void connectionClosed() {

    }

    @Override
    public void connectionClosedOnError(Exception arg0) {
        reConnected();
    }

    @Override
    public void reconnectingIn(int arg0) {

    }

    @Override
    public void reconnectionFailed(Exception arg0) {

    }

    @Override
    public void reconnectionSuccessful() {
        time = 0;
    }


    /**
     * 是否与服务器连接上
     *
     * @return
     */
    public boolean isConnection() {
        if (xmppConnection != null) {
            return (xmppConnection.isConnected() && xmppConnection.isAuthenticated());
        }
        return false;
    }


    @Override
    public void processStanza(Stanza stanza) throws SmackException.NotConnectedException, InterruptedException {
        if (stanza instanceof Message) {
            Message message = (Message) stanza;
            processMessage(message);

        } else if (stanza instanceof Presence) {
            Presence pre = (Presence) stanza;
            processPresence(pre);
        }
    }



//    @Override
//    public void processPacket(Stanza stanza) throws SmackException.NotConnectedException {
//        if (stanza instanceof Message) {
//            Message message = (Message) stanza;
//            processMessage(message);
//
//        } else if (stanza instanceof Presence) {
//            Presence pre = (Presence) stanza;
//            processPresence(pre);
//        }
//    }
}
