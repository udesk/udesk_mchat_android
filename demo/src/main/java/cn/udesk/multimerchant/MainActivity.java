package cn.udesk.multimerchant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.udesk.UdeskSDKManager;
import cn.udesk.model.CustomerInfo;
import cn.udesk.model.ProductMessage;
import cn.udesk.muchat.bean.Products;

public class MainActivity extends Activity implements View.OnClickListener {

    private final static String TAG = MainActivity.class.getSimpleName();
    private EditText customEuid,name,org,cellphone,email,description,tags,textfiledkey,textfiledvalue,listfiledkey,listfiledvalue;

    private Button startBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_view);
        customEuid = findViewById(R.id.customer_euid);
        name = findViewById(R.id.nick_name);
        org = findViewById(R.id.org);
        cellphone = findViewById(R.id.cellphone);
        email = findViewById(R.id.email);
        description = findViewById(R.id.description);
        tags = findViewById(R.id.tags);
        textfiledkey = findViewById(R.id.textfiledkey);
        textfiledvalue = findViewById(R.id.textfiledvalue);
        listfiledkey = findViewById(R.id.listfiledkey);
        listfiledvalue= findViewById(R.id.listfiledvalue);
        startBtn = (Button) findViewById(R.id.udesk_start);
        startBtn.setOnClickListener(this);
        //设置咨询对象
        createProducts();
//        UdeskSDKManager.getInstance().setProductMessage(createProduct());

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.udesk_start) {
            CustomerInfo customerInfo = buildCustomerInfo();
            if (!TextUtils.isEmpty(customerInfo.getEuid())) {
                UdeskSDKManager.getInstance().setCustomerInfo(customerInfo);
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, UdeskUseGuideActivity.class);
                startActivity(intent);

            } else {
                Toast.makeText(this, "按要求输入值", Toast.LENGTH_LONG).show();
            }
        }
    }

    private CustomerInfo buildCustomerInfo(){
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setCellphone(cellphone.getText().toString());
        customerInfo.setCustomerDescription(description.getText().toString());
        customerInfo.setEmail(email.getText().toString());
        customerInfo.setEuid(customEuid.getText().toString());
        customerInfo.setName(name.getText().toString());
        customerInfo.setOrg(org.getText().toString());
        customerInfo.setTags(tags.getText().toString());
        Map<String, Object> definedUserTextField = getDefinedUserTextField();
        definedUserTextField.putAll(getDefinedUserRoplist());
        customerInfo.setCustomField(definedUserTextField);
        return customerInfo;
    }
    private Map<String, Object> getDefinedUserTextField() {
        Map<String, Object> definedInfos=new HashMap<>();
        if (!TextUtils.isEmpty(textfiledkey.getText().toString())
                && !TextUtils.isEmpty(textfiledvalue.getText().toString())) {
            definedInfos.put(textfiledkey.getText().toString(), textfiledvalue.getText().toString());
        }
        return definedInfos;
    }

    private Map<String, Object> getDefinedUserRoplist() {
        Map<String, Object> definedDropListInfo = new HashMap<>();
        try {
            // key 是后台自定义字段id  value 是列表角标值
            if (!TextUtils.isEmpty(listfiledkey.getText().toString())
                    && !TextUtils.isEmpty(listfiledvalue.getText().toString())) {
                String[] split = listfiledvalue.getText().toString().split(",");
                int[] ints = new int[split.length];
                for (int i = 0; i < split.length; i++) {
                    ints[i] = Integer.parseInt(split[i]);
                }
                definedDropListInfo.put(listfiledkey.getText().toString(), ints);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return definedDropListInfo;
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


    private ProductMessage createProduct() {
        ProductMessage product = new ProductMessage();
        product.setImgUrl("http://img12.360buyimg.com/n1/s450x450_jfs/t10675/253/1344769770/66891/92d54ca4/59df2e7fN86c99a27.jpg");
        product.setName(" Apple iPhone X (A1903) 64GB 深空灰色 移动联通4G手机");
        product.setUrl("https://item.jd.com/6748052.html");

        List<ProductMessage.ParamsBean> paramsBeans = new ArrayList<>();

        ProductMessage.ParamsBean paramsBean0 = new ProductMessage.ParamsBean();
        paramsBean0.setText("京 东 价  ");
        paramsBean0.setColor("#C1B6B6");
        paramsBean0.setFold(false);
        paramsBean0.setBreakX(false);
        paramsBean0.setSize(12);

        ProductMessage.ParamsBean paramsBean1 = new ProductMessage.ParamsBean();
        paramsBean1.setText("￥6999.00");
        paramsBean1.setColor("#E6321A");
        paramsBean1.setFold(true);
        paramsBean1.setBreakX(true);
        paramsBean1.setSize(16);

        ProductMessage.ParamsBean paramsBean2 = new ProductMessage.ParamsBean();
        paramsBean2.setText("促　销  ");
        paramsBean2.setColor("#C1B6B6");
        paramsBean2.setFold(false);
        paramsBean2.setBreakX(false);
        paramsBean2.setSize(12);

        ProductMessage.ParamsBean paramsBean3 = new ProductMessage.ParamsBean();
        paramsBean3.setText("满1999元另加30元，或满2999元另加50元，即可在购物车换购热销商品 ");
        paramsBean3.setColor("#E6321A");
        paramsBean3.setFold(true);
        paramsBean3.setBreakX(false);
        paramsBean3.setSize(16);
        paramsBeans.add(paramsBean0);
        paramsBeans.add(paramsBean1);
        paramsBeans.add(paramsBean2);
        paramsBeans.add(paramsBean3);

        product.setParams(paramsBeans);

        return product;
    }


}
