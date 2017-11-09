package cn.udesk.multimerchant;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.UdeskSDKManager;
import cn.udesk.muchat.bean.Products;
import cn.udesk.muchat.net.SdkRetrofitClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = MainActivity.class.getSimpleName();


    String uuid = "a04d138d-98fb-4b9d-b2a7-478b7c0c1ce9";
    private EditText customeuid;
    private EditText customeName;
    private Button startBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_view);
        customeuid = (EditText) findViewById(R.id.udesk_customer_uuid);
        customeName = (EditText) findViewById(R.id.custom_name);
        startBtn = (Button) findViewById(R.id.udesk_start);
        startBtn.setOnClickListener(this);
        //设置咨询对象
        createProducts();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.udesk_start) {

            if (!TextUtils.isEmpty(customeuid.getText().toString())) {
                UdeskSDKManager.getInstance().setCustomerInfo(customeuid.getText().toString(), customeName.getText().toString());
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, UdeskUseGuideActivity.class);
                startActivity(intent);

            } else {
                Toast.makeText(this, "按要求输入值", Toast.LENGTH_LONG).show();
            }
        }
    }


    //设置咨询的商品
    private void createProducts() {
        Products products = new Products();
        Products.ProductBean productBean = new Products.ProductBean();
        productBean.setImage("http://img14.360buyimg.com/n1/s450x450_jfs/t3157/63/1645131029/112074/f4f79169/57d0d44dN8cddf5c5.jpg?v=1483595726320");
        productBean.setTitle("Apple iPhone 7");
        productBean.setUrl("http://item.jd.com/3133829.html?cu=true&amp;utm_source…erm=9457752645_0_11333d2bdbd545f1839f020ae9b27f14");
        List<Products.ProductBean.ExtrasBean> extras = new ArrayList<>();

        Products.ProductBean.ExtrasBean extrasBean = new Products.ProductBean.ExtrasBean();
        extrasBean.setTitle("价格");
        extrasBean.setContent("￥6189.00");

        extras.add(extrasBean);
        productBean.setExtras(extras);
        products.setProduct(productBean);

        UdeskSDKManager.getInstance().setProducts(products);

    }

}
