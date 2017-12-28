package cn.udesk.multimerchant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import cn.udesk.callback.ICommodityCallBack;
import cn.udesk.callback.IMessageArrived;
import cn.udesk.callback.ItotalUnreadMsgCnt;
import cn.udesk.UdeskSDKManager;
import cn.udesk.callback.IMerchantUnreadMsgCnt;
import cn.udesk.UdeskUtil;
import cn.udesk.muchat.bean.Products;
import cn.udesk.muchat.bean.ReceiveMessage;

public class UdeskUseGuideActivity extends Activity implements View.OnClickListener {

    private TextView merchant_unread_count, all_unread_count, receive_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_use_guide_view);
        findViewById(R.id.conversation).setOnClickListener(this);
        findViewById(R.id.history_conversation).setOnClickListener(this);
        findViewById(R.id.merchant_unread_msg).setOnClickListener(this);
        findViewById(R.id.msg_callback).setOnClickListener(this);
        findViewById(R.id.commity_callback).setOnClickListener(this);
        findViewById(R.id.commity_set_null).setOnClickListener(this);
        findViewById(R.id.commity_show).setOnClickListener(this);
        findViewById(R.id.all_unread_msg).setOnClickListener(this);
        merchant_unread_count = (TextView) findViewById(R.id.merchant_unread_count);
        all_unread_count = (TextView) findViewById(R.id.all_unread_count);
        receive_msg = (TextView) findViewById(R.id.receive_msg);


    }

    @Override
    public void onClick(View v) {

        String rid = JPushInterface.getRegistrationID(getApplicationContext());
        UdeskSDKManager.getInstance().setRegisterId(UdeskUseGuideActivity.this, rid);
        if (v.getId() == R.id.conversation) {
            //输入特定的商户id，咨询会话
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
        } else if (v.getId() == R.id.history_conversation) {
            //获取对话过的商户列表
            Intent intent = new Intent(this, TestFragmentActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.merchant_unread_msg) {
            //获取指定的商户的未读消息
            final UdeskCustomDialog dialog = new UdeskCustomDialog(UdeskUseGuideActivity.this);
            dialog.setDialogTitle("输入商户UID查未读消息数");
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
                    UdeskSDKManager.getInstance().getMerchantUnReadMsg(editText.getText().toString().trim(), new IMerchantUnreadMsgCnt() {
                        @Override
                        public void totalCount(final int count) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    merchant_unread_count.setVisibility(View.VISIBLE);
                                    merchant_unread_count.setText(editText.getText().toString().trim() + "未读消息数 = " + count);
                                }
                            });
                        }
                    });

                }
            });
            dialog.setCancleTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();

        } else if (v.getId() == R.id.all_unread_msg) {
            //查询所有商户未读消息
            UdeskSDKManager.getInstance().setItotalCount(new ItotalUnreadMsgCnt() {
                @Override
                public void totalcount(final int count) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            all_unread_count.setVisibility(View.VISIBLE);
                            all_unread_count.setText("所有商户未读消息数：" + count);
                        }
                    });

                }
            });
            UdeskSDKManager.getInstance().getUnReadMessages();
        } else if (v.getId() == R.id.msg_callback) {

            //设置在线状态下收到消息的监听事件
            UdeskSDKManager.getInstance().setMessageArrived(new IMessageArrived() {
                @Override
                public void onNewMessage(final ReceiveMessage receiveMessage) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            receive_msg.setVisibility(View.VISIBLE);
                            receive_msg.setText("收到消息---" + UdeskUtil.objectToString(receiveMessage.getContent()));
                        }
                    });

                }
            });
            Toast.makeText(UdeskUseGuideActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.commity_callback) {
            //设置咨询对象的回调
            UdeskSDKManager.getInstance().setCommodityCallBack(new ICommodityCallBack() {
                @Override
                public void callBackProduct(Products products) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UdeskUseGuideActivity.this, "你点击了咨询对象", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            Toast.makeText(UdeskUseGuideActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.commity_set_null) {
            UdeskSDKManager.getInstance().setProducts(null);
            Toast.makeText(UdeskUseGuideActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
        }else if (v.getId() == R.id.commity_show) {
            createProducts();
            Toast.makeText(UdeskUseGuideActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
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
