package cn.udesk.multimerchant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Date;

import cn.udesk.UdeskSDKManager;
import cn.udesk.widget.UdeskLodingDialog;

public class InitActivity extends Activity implements View.OnClickListener {

    private final static String TAG = InitActivity.class.getSimpleName();


//    String uuid = "c6042aa7-a1b2-4594-aed8-bf15b547627f";
//    String key = "240858ffb00b1c814259a6569393bf4e";
     String uuid = "b1ce357b-8ce8-4ea1-9a87-7d15519dd7e6";
    String key = "27aa6696cba45cc091ee66fbc25aedab";

    private EditText uuidEdit, udesk_sign;
    private Button udesk_next;
    private UdeskLodingDialog lodingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_init_key_view);
        uuidEdit = (EditText) findViewById(R.id.udesk_uuid);
        udesk_sign = (EditText) findViewById(R.id.udesk_sign);
        uuidEdit.setText(uuid);
        udesk_sign.setText(key);
        udesk_next = (Button) findViewById(R.id.udesk_next);
        udesk_next.setOnClickListener(this);
        testSign();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.udesk_next) {
            Intent intent = new Intent();
            intent.setClass(InitActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void testSign() {
        uuid = uuidEdit.getText().toString();
        key = udesk_sign.getText().toString();
        String time = getSecondTimestamp(new Date());
        String password = uuid + key + time;
        String sign = DigestUtils.shaHex(password);
        UdeskSDKManager.getInstance().init(InitActivity.this, uuid, sign, time);
    }


    public void showloadingDialog(String dialogTitle) {
        try {
            hideLodingDialog();
            lodingDialog = new UdeskLodingDialog(this, R.style.dialogstyle,
                    R.layout.udesk_request_view, dialogTitle);
            if (!lodingDialog.isShowing()) {
                lodingDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideLodingDialog() {
        try {
            if (lodingDialog != null) {
                lodingDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取精确到秒的时间戳
     *
     * @return
     */
    public static String getSecondTimestamp(Date date) {
        if (null == date) {
            return "";
        }
        String timestamp = String.valueOf(date.getTime());
        int length = timestamp.length();
        if (length > 3) {
            return timestamp.substring(0, length - 3);
        } else {
            return "";
        }
    }

}
