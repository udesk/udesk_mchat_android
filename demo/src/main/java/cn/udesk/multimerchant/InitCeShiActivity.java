package cn.udesk.multimerchant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import cn.udesk.UdeskSDKManager;
import cn.udesk.muchat.net.SdkRetrofitClient;
import cn.udesk.widget.UdeskLodingDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InitCeShiActivity extends Activity implements View.OnClickListener {
    private final static String TAG = InitActivity.class.getSimpleName();
    String uuid = "1ef5ac37-478e-4dee-b057-1d1394ae1d8f"; //b1
//    String uuid = "0ad5f61a-3769-4d32-87fc-4ce562d2677a";  // t1
    private EditText uuidEdit, udesk_sign;
    private Button startBtn, udesk_next;
    private UdeskLodingDialog lodingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_init_demo_key_view);
        uuidEdit = (EditText) findViewById(R.id.udesk_uuid);
        udesk_sign = (EditText) findViewById(R.id.udesk_sign);
        uuidEdit.setText(uuid);
        startBtn = (Button) findViewById(R.id.udesk_start);
        udesk_next = (Button) findViewById(R.id.udesk_next);
        startBtn.setOnClickListener(this);
        udesk_next.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.udesk_start) {
            if (!TextUtils.isEmpty(uuidEdit.getText().toString())) {
                showloadingDialog("请求中，请稍后");
                testSign();
            } else {
                Toast.makeText(this, "请输入租户的UUID", Toast.LENGTH_LONG).show();
            }
        } else if (view.getId() == R.id.udesk_next) {
            if (!TextUtils.isEmpty(udesk_sign.getText().toString())) {
                Intent intent = new Intent();
                intent.setClass(InitCeShiActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                showloadingDialog("请先获取签名");
            }
        }
    }

    private void testSign() {
        SdkRetrofitClient retrofitClient = SdkRetrofitClient.getInstance();
        Call<ResponseBody> call = retrofitClient.getSign(uuidEdit.getText().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                hideLodingDialog();
                try {
                    long timestamp = 0;
                    String sign = "";
                    String responseStr = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseStr);
                    if (jsonObject.has("timestamp")) {
                        timestamp = jsonObject.getLong("timestamp");
                    }
                    if (jsonObject.has("sign")) {
                        sign = jsonObject.getString("sign");
                        udesk_sign.setText(sign);
                    }
                    UdeskSDKManager.getInstance().init(InitCeShiActivity.this, uuidEdit.getText().toString(), sign, String.valueOf(timestamp));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                try {
                    hideLodingDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void showloadingDialog(String dialogTitle) {
        try {
            hideLodingDialog();
            lodingDialog = new UdeskLodingDialog(this, R.style.dialogstyle, R.layout.udesk_request_view, dialogTitle);
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
}