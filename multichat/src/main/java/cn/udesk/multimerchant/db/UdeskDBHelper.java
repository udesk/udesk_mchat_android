package cn.udesk.multimerchant.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UdeskDBHelper extends SQLiteOpenHelper {

	public static String DATABASE_NAME = "udesk_mult";
	public final static int DATABASE_VERSION = 1;

	
	public static String MultInitMsg = "init_msg";

	public UdeskDBHelper(Context context, String sdktoken) {
		super(context, DATABASE_NAME + sdktoken, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ MultInitMsg
				+ "(euid TEXT primary key,name TEXT,tcp_server TEXT,"
				+ "tcp_port TEXT, im_username TEXT,im_password TEXT,"
				+ "endpoint TEXT,bucket TEXT, access_id TEXT,prefix TEXT)");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}



}
