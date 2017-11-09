package cn.udesk.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.model.InitMode;


public class UdeskDBManager {

    private static UdeskDBHelper helper;
    private SQLiteDatabase mDatabase;
    private String mSdktoken;

    private static UdeskDBManager instance = new UdeskDBManager();

    private UdeskDBManager() {

    }

    public static UdeskDBManager getInstance() {
        return instance;
    }

    /**
     * 初始化，需要在使用数据库之前调用此方法
     *
     * @param context
     */
    public synchronized void init(Context context, String sdktoken) {
        try {
            if (context == null) {
                return;
            }
            if (helper == null) {
                helper = new UdeskDBHelper(context, mSdktoken);
            }
            mDatabase = helper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出时，释放
     */
    public synchronized void release() {
        if (helper != null) {
            helper.close();
            helper = null;
        }

        if (mSdktoken != null) {
            mSdktoken = null;
        }
    }

    public SQLiteDatabase getSQLiteDatabase() {
        return mDatabase;
    }

    /**
     * 增加客户信息
     */
    public boolean addInitInfo(InitMode mode) {
        if (getSQLiteDatabase() == null || mode == null) {
            return false;
        }
        String sql = "replace into " + UdeskDBHelper.MultInitMsg
                + "(euid,name,tcp_server,tcp_port,im_username," +
                "im_password,endpoint,bucket,access_id,prefix) values(?,?,?,?,?,?,?,?,?,?)";
        try {
            getSQLiteDatabase().execSQL(sql,
                    new Object[]{UdeskUtil.objectToString(mode.getEuid()),
                            UdeskUtil.objectToString(mode.getName()),
                            UdeskUtil.objectToString(mode.getTcp_server()),
                            UdeskUtil.objectToString(mode.getTcp_port()),
                            UdeskUtil.objectToString(mode.getIm_username()),
                            UdeskUtil.objectToString(mode.getIm_password()),
                            UdeskUtil.objectToString(mode.getEndpoint()),
                            UdeskUtil.objectToString(mode.getBucket()),
                            UdeskUtil.objectToString(mode.getAccess_id()),
                            UdeskUtil.objectToString(mode.getPrefix())

                    });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @return
     */
    public InitMode getInitMode(String euid) {

        String sql = "select * from " + UdeskDBHelper.MultInitMsg + " where euid = ? ";
        Cursor cursor = null;
        if (getSQLiteDatabase() == null) {
            return null;
        }
        try {
            cursor = getSQLiteDatabase().rawQuery(sql, new String[]{euid});
            int count = cursor.getCount();
            if (count < 1) {
                return null;
            }
            InitMode initMode = new InitMode();
            while (cursor.moveToNext()) {
                initMode.setEuid(cursor.getString(0));
                initMode.setName(cursor.getString(1));
                initMode.setTcp_server(cursor.getString(2));
                initMode.setTcp_port(cursor.getString(3));
                initMode.setIm_username(cursor.getString(4));
                initMode.setIm_password(cursor.getString(5));
                initMode.setEndpoint(cursor.getString(6));
                initMode.setBucket(cursor.getString(7));
                initMode.setAccess_id(cursor.getString(8));
                initMode.setPrefix(cursor.getString(9));
                return initMode;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


}
