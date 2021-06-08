package cn.udesk.multimerchant.activity;


import android.os.Bundle;
import android.view.View;

import cn.udesk.multimerchant.R;
import cn.udesk.multimerchant.UdeskConst;
import cn.udesk.multimerchant.UdeskMultimerchantSDKManager;
import cn.udesk.multimerchant.config.UdekConfigUtil;
import cn.udesk.multimerchant.config.UdeskMultimerchantConfig;
import cn.udesk.multimerchant.model.InitMode;


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
        if (UdeskMultimerchantSDKManager.getInstance().getInitMode() != null){
            InitMode initMode = UdeskMultimerchantSDKManager.getInstance().getInitMode();
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
            UdekConfigUtil.setUITextColor(UdeskMultimerchantConfig.udeskTitlebarTextLeftRightResId, mTitlebar.getLeftTextView(), mTitlebar.getRightTextView());
            UdekConfigUtil.setUIbgDrawable(UdeskMultimerchantConfig.udeskTitlebarBgResId, mTitlebar.getRootView());
            if (UdeskMultimerchantConfig.DEFAULT != UdeskMultimerchantConfig.udeskbackArrowIconResId) {
                mTitlebar.getUdeskBackImg().setImageResource(UdeskMultimerchantConfig.udeskbackArrowIconResId);
            }
            mTitlebar.setLeftTextSequence(UdeskWebViewUrlAcivity.this.getString(R.string.udesk_multimerchant_titlebar_back));
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
