package cn.udesk.multimerchant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Date;
import java.util.Locale;

import cn.udesk.UdeskSDKManager;
import cn.udesk.widget.UdeskLodingDialog;
import udesk.core.utils.LocalManageUtil;

public class InitActivity extends Activity implements View.OnClickListener {

    private final static String TAG = InitActivity.class.getSimpleName();


//    String uuid = "561c8173-e3c8-4c19-94bf-6e45aeca2e39";
//    String key = "111b0a6c368d49b8b1bb8887b5112c34";
     String uuid = "b1ce357b-8ce8-4ea1-9a87-7d15519dd7e6";
    String key = "27aa6696cba45cc091ee66fbc25aedab";
//     String uuid = "fdb9d75a-ccae-4cd8-b761-3ac436454810";
//    String key = "f6103178f7ca8e5b2c988e688850d106";

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
        findViewById(R.id.set_language).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.udesk_next) {
            testSign();
            Intent intent = new Intent();
            intent.setClass(InitActivity.this, MainActivity.class);
            startActivity(intent);
        }else if(view.getId() == R.id.set_language){
            //请查找对应android系统的对应简写填入，不确定找应用系统的简写在接口中查找填入
            final UdeskCustomDialog dialog = new UdeskCustomDialog(this);
            dialog.setDialogTitle("输入语言android系统对应的简写");
            final EditText editText = (EditText) dialog.getEditText();
            editText.setHint("语言简写");
            dialog.setOkTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    if (TextUtils.isEmpty(editText.getText().toString().trim())) {
                        Toast.makeText(getApplicationContext(), "设置语言", Toast.LENGTH_LONG).show();
                        return;
                    }
                    LocalManageUtil.saveSelectLanguage(getApplicationContext(),new Locale(editText.getText().toString().trim()));
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
