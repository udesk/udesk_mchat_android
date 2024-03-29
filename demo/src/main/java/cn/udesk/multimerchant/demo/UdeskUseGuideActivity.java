package cn.udesk.multimerchant.demo;

import android.app.Activity;
import android.content.Context;
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
import cn.udesk.multimerchant.callback.ICommodityCallBack;
import cn.udesk.multimerchant.callback.IUdeskMultimerchantFunctionItemClickCallBack;
import cn.udesk.multimerchant.callback.IMessageArrived;
import cn.udesk.multimerchant.callback.IUdeskMultimerchantNavigationItemClickCallBack;
import cn.udesk.multimerchant.callback.ItotalUnreadMsgCnt;
import cn.udesk.multimerchant.UdeskMultimerchantSDKManager;
import cn.udesk.multimerchant.callback.IMerchantUnreadMsgCnt;
import cn.udesk.multimerchant.UdeskUtil;
import cn.udesk.multimerchant.config.UdeskMultimerchantConfig;
import cn.udesk.multimerchant.model.UdeskMultimerchantFunctionMode;
import cn.udesk.multimerchant.model.UdeskMultimerchantNavigationMode;
import cn.udesk.multimerchant.model.ProductMessage;
import cn.udesk.multimerchant.core.bean.Products;
import cn.udesk.multimerchant.core.bean.ReceiveMessage;
import cn.udesk.multimerchant.presenter.ChatActivityPresenter;

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
        findViewById(R.id.all_unread_msg).setOnClickListener(this);
        findViewById(R.id.add_navigation).setOnClickListener(this);
        findViewById(R.id.add_extraFunction).setOnClickListener(this);
        findViewById(R.id.add_product_message).setOnClickListener(this);
        merchant_unread_count = (TextView) findViewById(R.id.merchant_unread_count);
        all_unread_count = (TextView) findViewById(R.id.all_unread_count);
        receive_msg = (TextView) findViewById(R.id.receive_msg);


    }

    @Override
    public void onClick(View v) {

        String rid = JPushInterface.getRegistrationID(getApplicationContext());
        UdeskMultimerchantSDKManager.getInstance().setRegisterId(UdeskUseGuideActivity.this, rid);
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
                    UdeskMultimerchantSDKManager.getInstance().entryChat(UdeskUseGuideActivity.this, editText.getText().toString().trim());

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
                    UdeskMultimerchantSDKManager.getInstance().getMerchantUnReadMsg(editText.getText().toString().trim(), new IMerchantUnreadMsgCnt() {
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
            UdeskMultimerchantSDKManager.getInstance().setItotalCount(new ItotalUnreadMsgCnt() {
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
            UdeskMultimerchantSDKManager.getInstance().getUnReadMessages();
        } else if (v.getId() == R.id.msg_callback) {

            //设置在线状态下收到消息的监听事件
            UdeskMultimerchantSDKManager.getInstance().setMessageArrived(new IMessageArrived() {
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
            UdeskMultimerchantSDKManager.getInstance().setCommodityCallBack(new ICommodityCallBack() {
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
        } else if (v.getId() == R.id.add_navigation) {

            UdeskMultimerchantConfig.isUseNavigationView = true;
            UdeskMultimerchantSDKManager.getInstance().setNavigationModes(getNavigations());
            UdeskMultimerchantSDKManager.getInstance().setNavigationItemClickCallBack(new IUdeskMultimerchantNavigationItemClickCallBack() {
                @Override
                public void callBack(Context context, ChatActivityPresenter mPresenter, UdeskMultimerchantNavigationMode navigationMode) {
                    if (navigationMode.getId() == 1) {
                        mPresenter.sendProductMessage(createProduct());
                    }
                }
            });
            Toast.makeText(UdeskUseGuideActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.add_extraFunction) {

            UdeskMultimerchantSDKManager.getInstance().setExtraFunctions(getExtraFunctions(), new IUdeskMultimerchantFunctionItemClickCallBack() {
                @Override
                public void callBack(Context context, ChatActivityPresenter mPresenter, UdeskMultimerchantFunctionMode functionMode) {
                    switch (functionMode.getId()){
                        case 11:
                            mPresenter.sendTxtMessage("发送文本消息");
                            break;
                        case 12:
                            mPresenter.sendProductMessage(createProduct());
                            break;
                        case 13:
                            UdeskMultimerchantSDKManager.getInstance().cancleXmpp();
                            break;
                        default:
                            break;
                    }
                }
            });
            Toast.makeText(UdeskUseGuideActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.add_product_message) {
            UdeskMultimerchantSDKManager.getInstance().setProductMessage(createProduct());
            Toast.makeText(UdeskUseGuideActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
        }
    }


    private List<UdeskMultimerchantFunctionMode> getExtraFunctions() {
        // id  1-5 已被占用
        List<UdeskMultimerchantFunctionMode> modes = new ArrayList<>();
        UdeskMultimerchantFunctionMode functionMode1 = new UdeskMultimerchantFunctionMode("发送文本消息", 11, R.mipmap.udesk_form_table);
        UdeskMultimerchantFunctionMode functionMode2 = new UdeskMultimerchantFunctionMode("发送商品消息", 12, R.mipmap.udesk_form_table);
        UdeskMultimerchantFunctionMode functionMode3 = new UdeskMultimerchantFunctionMode("断开xmpp连接", 13, R.mipmap.udesk_form_table);
        modes.add(functionMode1);
        modes.add(functionMode2);
        modes.add(functionMode3);
        return modes;
    }

    private List<UdeskMultimerchantNavigationMode> getNavigations() {
        List<UdeskMultimerchantNavigationMode> modes = new ArrayList<>();
        UdeskMultimerchantNavigationMode navigationMode1 = new UdeskMultimerchantNavigationMode("发送商品消息发送商", 1);
        modes.add(navigationMode1);
        return modes;
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
        paramsBean3.setText("满1999元另加30元，或满2999元另加50元，即可在购物车换购热销商品 " +
                "满1999元另加30元，或满2999元另加50元，即可在购物车换购热销商品 满1999元另加30元，" +
                "或满2999元另加50元，即可在购物车换购热销商品满1999元另加30元，" +
                "或满2999元另加50元，即可在购物车换购热销商品满1999元另加30元，或满2999元另加50元，" +
                "即可在购物车换购热销商品满1999元另加30元，或满2999元另加50元，即可在购物车换购热销商品满1999元另加30元，" +
                "或满2999元另加50元，即可在购物车换购热销商品满1999元另加30元，" +
                "或满2999元另加50元，即可在购物车换购热销商品满1999元另加30元，" +
                "或满2999元另加50元，即可在购物车换购热销商品满1999元另加30元，" +
                "或满2999元另加50元，即可在购物车换购热销商品满1999元另加30元，或满2999元另加50元，即可在购物车换购热销商品满1999元另加30元，" +
                "或满2999元另加50元，即可在购物车换购热销商品满1999元另加30元，" +
                "或满2999元另加50元，即可在购物车换购热销商品满1999元另加30元，" +
                "或满2999元另加50元，即可在购物车换购热销商品满1999元另加30元，或满2999元另加50元，即可在购物车换购热销商品满1999元另加30元，" +
                "或满2999元另加50元，即可在购物车换购热销商品满1999元另加30元，" +
                "或满2999元另加50元，即可在购物车换购热销商品满1999元另加30元，或满2999元另加50元，" +
                "即可在购物车换购热销商品满1999元另加30元，或满2999元另加50元，即可在购物车换购热销商品");
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
