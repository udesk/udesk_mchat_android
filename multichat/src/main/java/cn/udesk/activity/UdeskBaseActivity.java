package cn.udesk.activity;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import udesk.core.utils.LocalManageUtil;

public class UdeskBaseActivity extends AppCompatActivity {


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocalManageUtil.setLocal(newBase));
    }
}
