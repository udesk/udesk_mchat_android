package cn.udesk.multimerchant.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.conn.ConnectTimeoutException;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.udesk.multimerchant.JsonUtils;
import cn.udesk.multimerchant.R;
import cn.udesk.multimerchant.UdeskConst;
import cn.udesk.multimerchant.UdeskMultimerchantSDKManager;
import cn.udesk.multimerchant.UdeskUtil;
import cn.udesk.multimerchant.activity.UdeskChatActivity;
import cn.udesk.multimerchant.adapter.AbsCommonAdapter;
import cn.udesk.multimerchant.adapter.AbsViewHolder;
import cn.udesk.multimerchant.emotion.MoonUtils;
import cn.udesk.multimerchant.model.InitMode;
import cn.udesk.multimerchant.model.Merchant;
import cn.udesk.multimerchant.core.HttpCallBack;
import cn.udesk.multimerchant.core.HttpFacade;
import cn.udesk.multimerchant.core.UdeskLibConst;
import cn.udesk.multimerchant.core.bean.ReceiveMessage;
import cn.udesk.multimerchant.widget.BadgeView;
import cn.udesk.multimerchant.widget.swipelistview.PullToRefreshSwipeMenuListView;
import cn.udesk.multimerchant.widget.swipelistview.SwipeMenu;
import cn.udesk.multimerchant.widget.swipelistview.SwipeMenuCreator;
import cn.udesk.multimerchant.widget.swipelistview.SwipeMenuItem;
import cn.udesk.multimerchant.core.utils.BaseUtils;

/**
 * Created by Administrator on 2017/10/18.
 */
public class ConversationFragment extends BaseFragment implements PullToRefreshSwipeMenuListView.IXListViewListener, AdapterView.OnItemClickListener {

    private EditText mSearchEt;
    private ImageView mClearIv;
    private PullToRefreshSwipeMenuListView mListView;
    private LinearLayout mNoDataTipLl;
    private AbsCommonAdapter<Merchant> mAdapter;
    // 搜索结果List
    private List<Merchant> mSearchResult = new ArrayList<>();
    // 正常显示List
    private List<Merchant> mNormalResult = new ArrayList<>();
    private MyHandler mHandler;
    private final int REQUEST_CODE = 1;
    public static final int ReceviveMessageWhat = 1;
    public static final int Xmpp_is_disConent = 2;
    private long QUEUE_RETEY_TIME = 5 * 1000;

    private static class MyHandler extends Handler {
        WeakReference<ConversationFragment> mWeaks;

        public MyHandler(ConversationFragment fragment) {
            mWeaks = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                final ConversationFragment fragment = mWeaks.get();
                if (fragment == null) {
                    return;
                }
                switch (msg.what) {

                    case ReceviveMessageWhat:
                        fragment.mAdapter.notifyDataSetChanged();
                        fragment.getTotalCount();
                        break;
                    case Xmpp_is_disConent:
                        this.postDelayed(fragment.myRunnable, fragment.QUEUE_RETEY_TIME);
                        break;


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            if (!UdeskMultimerchantSDKManager.getInstance().isConnection()) {
                refreshData();
                sendXmppIsDisConnect();
                UdeskMultimerchantSDKManager.getInstance().connectXmpp(UdeskMultimerchantSDKManager.getInstance().getInitMode());
            }
        }
    };

    private void sendXmppIsDisConnect() {
        if (mHandler != null) {
            mHandler.removeCallbacks(myRunnable);
            Message msgWaitAgent = mHandler
                    .obtainMessage(Xmpp_is_disConent);
            mHandler.sendMessage(msgWaitAgent);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.udesk_multimerchant_fragment_conversation;
    }

    @Override
    protected void initView() {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        UdeskMultimerchantSDKManager.getInstance().setCustomerOffline(true);
        mHandler = new MyHandler(ConversationFragment.this);
        mClearIv = (ImageView) rootView.findViewById(R.id.iv_clear);
        mSearchEt = (EditText) rootView.findViewById(R.id.et_search_msg);
        mSearchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {
                rootView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        searchMerchant(charSequence.toString());
                    }
                }, 500);
            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (TextUtils.isEmpty(mSearchEt.getText().toString())) {
                    mClearIv.setVisibility(View.GONE);
                } else {
                    mClearIv.setVisibility(View.VISIBLE);
                }
            }


            private void searchMerchant(String keyword) {
                // 当输入框里面的值为空，更新为原来的列表，否则为搜索列表
                if (TextUtils.isEmpty(keyword)) {
                    //显示为原来的列表
                    if (mNormalResult.isEmpty()) {
                        mListView.setVisibility(View.GONE);
                        mNoDataTipLl.setVisibility(View.VISIBLE);
                    } else {
                        mListView.setVisibility(View.VISIBLE);
                        mNoDataTipLl.setVisibility(View.GONE);
                        mAdapter.addData(mNormalResult, false);
                    }
                } else {
                    List<Merchant> searchData = new ArrayList<>();
                    for (Merchant merchant : mNormalResult) {
                        if (BaseUtils.objectToString(merchant.getName()).contains(keyword.toLowerCase())) {
                            searchData.add(merchant);
                        }
                    }
                    mAdapter.clearData(true);
                    if (searchData.isEmpty()) {
                        mListView.setVisibility(View.GONE);
                        mNoDataTipLl.setVisibility(View.VISIBLE);
                    } else {
                        mListView.setVisibility(View.VISIBLE);
                        mNoDataTipLl.setVisibility(View.GONE);
                        mAdapter.addData(searchData, false);
                    }

                }
            }
        });
        mNoDataTipLl = (LinearLayout) rootView.findViewById(R.id.no_data_tips);
        mListView = (PullToRefreshSwipeMenuListView) rootView.findViewById(R.id.lv_conversation);
        mAdapter = new AbsCommonAdapter<Merchant>(activity, R.layout.udesk_multimerchant_conversation_adapter_view) {
            @Override
            public void convert(AbsViewHolder helper, Merchant item, int pos) {
                ImageView head = helper.getView(R.id.iv_head);
                BadgeView badgeView = helper.getView(R.id.id_unread_tips);
                TextView nickTv = helper.getView(R.id.id_nickName);
                TextView timeTv = helper.getView(R.id.tv_time);
                TextView contentTv = helper.getView(R.id.tv_content);

                nickTv.setText(BaseUtils.objectToString(item.getName()));
                ReceiveMessage message = item.getLast_message();
                if (message != null) {
                    timeTv.setText(UdeskUtil.formatLongTypeTimeToString(ConversationFragment.this.getContext(), BaseUtils.objectToString(message.getCreated_at())));
                    String contentType = BaseUtils.objectToString(message.getContent_type());
                    if (UdeskConst.ChatMsgTypeString.TYPE_TEXT.equals(contentType) || UdeskConst.ChatMsgTypeString.TYPE_NAVIGATES.equals(contentType)) {
                        String content = BaseUtils.objectToString(message.getContent());
                        if (MoonUtils.isHasEmotions(content)){
                            contentTv.setText(MoonUtils.replaceEmoticons(mContext, content, (int) contentTv.getTextSize()));
                        }else {
                            contentTv.setText(content);
                        }
                    } else if (UdeskConst.ChatMsgTypeString.TYPE_AUDIO.equals(contentType)) {
                        contentTv.setText(getString(R.string.udesk_multimerchant_msg_type_audio));
                    } else if (UdeskConst.ChatMsgTypeString.TYPE_IMAGE.equals(contentType)) {
                        contentTv.setText(getString(R.string.udesk_multimerchant_msg_type_image));
                    } else if (UdeskConst.ChatMsgTypeString.TYPE_VIDEO.equals(contentType)) {
                        contentTv.setText(getString(R.string.udesk_multimerchant_msg_type_video));
                    } else if (UdeskConst.ChatMsgTypeString.TYPE_FILE.equals(contentType)) {
                        contentTv.setText(getString(R.string.udesk_multimerchant_msg_type_file));
                    } else if (UdeskConst.ChatMsgTypeString.TYPE_PRODUCT.equals(contentType)) {
                        contentTv.setText(getString(R.string.udesk_multimerchant_msg_type_product));
                    }

                }
                if (BaseUtils.objectToInt(item.getUnread_count()) > 0) {
                    badgeView.setVisibility(View.VISIBLE);
                    badgeView.setTextColor(Color.WHITE);
                    if (BaseUtils.objectToInt(item.getUnread_count()) > 99) {
                        badgeView.setText("99+");
                    } else {
                        badgeView.setText(BaseUtils.objectToInt(item.getUnread_count()) + "");
                    }
                } else {
                    badgeView.setVisibility(View.GONE);
                }
                UdeskUtil.loadInto(ConversationFragment.this.getContext().getApplicationContext(), BaseUtils.objectToString(item.getLogo_url()),
                        R.drawable.udesk_multimerchant_im_default_agent_avatar, R.drawable.udesk_multimerchant_im_default_agent_avatar, head);
            }
        };
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                switch (menu.getViewType()) {
                    case 0:
                        createMenu1(menu);
                        break;
                    default:
                        createMenu1(menu);
                        break;

                }
            }

            private void createMenu1(SwipeMenu menu) {
                SwipeMenuItem item1 = new SwipeMenuItem(
                        ConversationFragment.this.getActivity());
                item1.setBackground(new ColorDrawable(0xffEE615E));
                item1.setWidth((int) activity.getResources().getDimension(R.dimen.udesk_multimerchant_80));
                item1.setTitle(getResources().getString(
                        R.string.udesk_multimerchant_close_conversation));
                item1.setId(UdeskLibConst.SwipeMenuItemDeleteId);
                item1.setTitleColor(activity.getResources().getColor(R.color.udesk_multimerchant_list_item_nomal));
                item1.setTitleSize((int) activity.getResources().getDimension(R.dimen.px28tosp));
                menu.addMenuItem(item1);
            }
        };
        mListView.setMenuCreator(creator);
        mListView.setAdapter(mAdapter);
        setMsgListClickListener();
        mClearIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchEt.setText("");
            }
        });
        refreshData();

    }

    @Override
    protected void initVariable() {

    }

    @Override
    protected void initData() {

    }

    @Override
    public void onResume() {
        super.onResume();
        checkConnect();
    }

    private void checkConnect() {
        if (UdeskMultimerchantSDKManager.getInstance().getInitMode() != null) {
            if (!UdeskMultimerchantSDKManager.getInstance().isConnection()) {
                sendXmppIsDisConnect();
                UdeskMultimerchantSDKManager.getInstance().connectXmpp(UdeskMultimerchantSDKManager.getInstance().getInitMode());
            }
        }
    }

    private void refreshData() {
        UdeskMultimerchantSDKManager.getInstance().getMerchants(new HttpCallBack() {
            @Override
            public void onSuccess(String message) {
                if (mListView != null && mListView.getVisibility() == View.VISIBLE) {
                    mListView.stopRefresh(true);
                }
                List<Merchant> merchants = JsonUtils.parseRecentMerchants(activity.getApplicationContext(),message);
                Collections.reverse(merchants);
                mAdapter.clearData(true);
                mAdapter.addData(merchants, false);
                mNormalResult = merchants;
                mNoDataTipLl.setVisibility(merchants.isEmpty() ? View.VISIBLE : View.GONE);
                getTotalCount();
            }

            @Override
            public void onFail(Throwable message) {
                if (UdeskLibConst.isDebug) {
                    Log.i("udesk", "getMerchants result =" + message);
                }
                if (mListView != null && mListView.getVisibility() == View.VISIBLE) {
                    mListView.stopRefresh(true);
                }
            }

            @Override
            public void onSuccessFail(String message) {
                if (UdeskLibConst.isDebug) {
                    Log.i("udesk", "getMerchants result =" + message);
                }
                if (mListView != null && mListView.getVisibility() == View.VISIBLE) {
                    mListView.stopRefresh(true);
                }
            }
        });
    }

    private void setMsgListClickListener() {
        try {
            mListView.setPullRefreshEnable(true);
            mListView.setXListViewListener(this);
            mListView.setOnItemClickListener(this);
            mListView.setOnMenuItemClickListener(new PullToRefreshSwipeMenuListView.OnMenuItemClickListener() {

                @Override
                public void onMenuItemClick(int position,
                                            SwipeMenu menu, int index) {
                    Merchant deleteMerchant = mAdapter.getItem(position);
                    if (deleteMerchant == null) {
                        return;
                    }
                    switch (index) {
                        case UdeskLibConst.SwipeMenuItemDeleteId:
                            List<Merchant> merchantList = mAdapter.getDatas();
                            //本地删除回话
                            //先从当前adapter的数据中删除(当前adapter可能是正常的也可能是搜索的，所以下面要再执行删除)
                            deleteMerchant(merchantList, deleteMerchant);
                            //不管是搜索界面还是正常显示的界面，都执行者两个list中的删除（方法里面会判断是否有这个元素）
                            deleteMerchant(mNormalResult, deleteMerchant);
                            deleteMerchant(mSearchResult, deleteMerchant);
                            mNoDataTipLl.setVisibility(merchantList.isEmpty() ? View.VISIBLE : View.GONE);
                            mAdapter.notifyDataSetChanged();
                            //提交服务端删除会话，不管会话是否删除成功
                            InitMode initMode = UdeskMultimerchantSDKManager.getInstance().getInitMode();
                            if (initMode != null) {
                                HttpFacade.getInstance().deleteMerchant(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                                        UdeskUtil.objectToString(initMode.getIm_password())), BaseUtils.objectToString(deleteMerchant.getEuid()), new HttpCallBack() {
                                    @Override
                                    public void onSuccess(String message) {
                                    }

                                    @Override
                                    public void onFail(Throwable message) {
                                        if (message instanceof ConnectTimeoutException) {
                                            if (UdeskLibConst.isDebug) {
                                                Log.i("udesk", "sendCommodity result =" + getString(R.string.udesk_multimerchant_time_out));
                                            }
                                        }

                                    }

                                    @Override
                                    public void onSuccessFail(String message) {
                                        if (UdeskLibConst.isDebug) {
                                            Log.i("udesk", "sendCommodity result =" + message);
                                        }
                                    }
                                });
                            }

                            break;
                        default:
                            break;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteMerchant(List<Merchant> merchants, Merchant deleteMerchant) {
        if (merchants.contains(deleteMerchant)) {
            merchants.remove(deleteMerchant);
        }
    }

    @Override
    protected void lazyLoad() {

    }


    @Override
    public void onRefresh() {
        refreshData();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
        Merchant merchant = mAdapter.getItem((int) id);
        if (merchant != null) {
            Intent intent = new Intent(ConversationFragment.this.getContext(), UdeskChatActivity.class);
            intent.putExtra(UdeskConst.Euid, UdeskUtil.objectToString(merchant.getEuid()));
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refreshData();
    }

    private void addMessage(ReceiveMessage receiveMessage) {
        if (receiveMessage != null && mAdapter != null) {
            List<Merchant> merchants = mAdapter.getDatas();
            Merchant tempMerchant = null;
            for (Merchant merchant : merchants) {
                if (merchant != null && UdeskUtil.objectToString(merchant.getEuid()).equals(receiveMessage.getMerchant_euid())) {
                    if (TextUtils.equals(receiveMessage.getSend_status(),"rollback")){
                        receiveMessage.setCategory(UdeskConst.ChatMsgTypeString.TYPE_EVENT);
                        receiveMessage.setContent(activity.getApplicationContext().getString(R.string.udesk_multimerchant_rollback_tips));
                    }else {
                        merchant.setUnread_count(UdeskUtil.objectToInt(merchant.getUnread_count()) + 1);
                    }
                    merchant.setLast_message(receiveMessage);
                    tempMerchant = merchant;
                    merchants.remove(merchant);
                    break;
                }
            }
            if (tempMerchant == null) {
                //刷新方法
                refreshData();
            } else {
                sortByChange(tempMerchant, merchants);
                Message messge = mHandler.obtainMessage(ReceviveMessageWhat);
                mHandler.sendMessage(messge);

            }
        }
    }

    private void sortByChange(Merchant tempMerchant, List<Merchant> merchants) {

        int i;
        for (i = 0; i < merchants.size(); ++i) {
            if (UdeskUtil.compare(UdeskUtil.objectToString(tempMerchant.getLast_message().getCreated_at()),
                    UdeskUtil.objectToString(merchants.get(i).getLast_message().getCreated_at()))) {
                merchants.add(i, tempMerchant);
                break;
            }
        }
        if (i >= merchants.size()) {
            merchants.add(tempMerchant);
        }
    }

    private void getTotalCount() {
        if (mAdapter != null) {
            int count = 0;
            List<Merchant> merchants = mAdapter.getDatas();
            for (Merchant merchant : merchants) {
                count = count + BaseUtils.objectToInt(merchant.getUnread_count());
            }
            if (UdeskMultimerchantSDKManager.getInstance().getItotalCount() != null) {
                UdeskMultimerchantSDKManager.getInstance().getItotalCount().totalcount(count);
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMessage(ReceiveMessage receiveMessage) {
        try {
            if (receiveMessage != null) {
                addMessage(receiveMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        if (mHandler != null) {
            mHandler.removeCallbacks(myRunnable);
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);

        super.onDestroyView();
    }
}
