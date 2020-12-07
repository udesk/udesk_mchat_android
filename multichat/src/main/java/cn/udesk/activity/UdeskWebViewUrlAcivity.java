package cn.udesk.activity;


import android.os.Bundle;
import android.util.Log;
import android.view.View;

import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskConfig;
import cn.udesk.model.InitMode;


/**
 * Created by k on 2016/4/6.
 */
public class UdeskWebViewUrlAcivity extends UdeskBaseWebViewActivity {

    String url = "";
    private String euid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        try {
            if (getIntent() != null) {
                url = getIntent().getStringExtra(UdeskConst.URL);
                euid = getIntent().getStringExtra(UdeskConst.Euid);
            }
            settingTitlebar();
            loadView();
            setH5TitleListener(new UdeskWebChromeClient.GetH5Title() {
                @Override
                public void h5Title(String title) {
                    mTitlebar.setLeftTextSequence(title);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadView() {
        if (UdeskSDKManager.getInstance().getInitMode() != null){
            InitMode initMode = UdeskSDKManager.getInstance().getInitMode();
            StringBuilder builder = new StringBuilder();
            builder.append(url)
                    .append("?tenant_id=").append(initMode.getUuid())
                    .append("&euid=").append(euid)
                    .append("&im_username=").append(initMode.getIm_username())
                    .append("&im_password=").append(initMode.getIm_password())
                    .append("&base_url=").append(initMode.getBase_url())
                    .append("&access_id=").append(initMode.getAccess_id())
                    .append("&prefix=").append(initMode.getPrefix())
                    .append("&policy=").append(initMode.getPolicy_Base64())
                    .append("&signature=").append(initMode.getSignature())
                    .append("&sdk_token=").append("true");
            url=builder.toString();
            mwebView.loadUrl(url);

        }
    }


    private void settingTitlebar() {

        try {
            UdekConfigUtil.setUITextColor(UdeskConfig.udeskTitlebarTextLeftRightResId, mTitlebar.getLeftTextView(), mTitlebar.getRightTextView());
            UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskTitlebarBgResId, mTitlebar.getRootView());
            if (UdeskConfig.DEFAULT != UdeskConfig.udeskbackArrowIconResId) {
                mTitlebar.getUdeskBackImg().setImageResource(UdeskConfig.udeskbackArrowIconResId);
            }
            mTitlebar.setLeftTextSequence(UdeskWebViewUrlAcivity.this.getString(R.string.udesk_titlebar_back));
            mTitlebar.setLeftLinearVis(View.VISIBLE);
            mTitlebar.setLeftViewClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
