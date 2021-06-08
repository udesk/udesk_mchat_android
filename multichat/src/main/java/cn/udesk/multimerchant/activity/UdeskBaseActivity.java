package cn.udesk.multimerchant.activity;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import cn.udesk.multimerchant.core.utils.UdeskMultimerchantLocalManageUtil;

public class UdeskBaseActivity extends AppCompatActivity {


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(UdeskMultimerchantLocalManageUtil.setLocal(newBase));
    }
}
