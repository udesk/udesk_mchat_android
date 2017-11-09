package cn.udesk.multimerchant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import cn.jpush.android.api.JPushInterface;
import cn.udesk.UdeskSDKManager;

public class UdeskUseGuideActivity extends Activity {

    public static String merchant_euid = "sdk2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_use_guide_view);


    }

    public void onClick(View v) {

        String rid = JPushInterface.getRegistrationID(getApplicationContext());
        UdeskSDKManager.getInstance().setRegisterId(UdeskUseGuideActivity.this, rid);
        if (v.getId() == R.id.udesk_group_help) {
            Intent intent = new Intent(this, TestFragmentActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.udesk_group_conversation) {
            //咨询会话

            final UdeskCustomDialog dialog = new UdeskCustomDialog(UdeskUseGuideActivity.this);
            dialog.setDialogTitle("输入商户UID进入会话");
            final EditText editText = (EditText) dialog.getEditText();
            editText.setHint("商户的euid");
            dialog.setOkTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    if (TextUtils.isEmpty(editText.getText().toString().trim())) {
                        Toast.makeText(getApplicationContext(), "商户的euid", Toast.LENGTH_LONG).show();
                        return;
                    }
                    UdeskSDKManager.getInstance().entryChat(UdeskUseGuideActivity.this, editText.getText().toString().trim());

                }
            });
            dialog.setCancleTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

}
