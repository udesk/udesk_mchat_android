package cn.udesk.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.http.conn.ConnectTimeoutException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.udesk.IMessageArrived;
import cn.udesk.JsonUtils;
import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.adapter.UDEmojiAdapter;
import cn.udesk.adapter.AbsCommonAdapter;
import cn.udesk.adapter.AbsViewHolder;
import cn.udesk.model.InitMode;
import cn.udesk.model.Merchant;
import cn.udesk.muchat.HttpCallBack;
import cn.udesk.muchat.HttpFacade;
import cn.udesk.muchat.UdeskLibConst;
import cn.udesk.muchat.bean.ReceiveMessage;
import cn.udesk.widget.BadgeView;
import cn.udesk.widget.swipelistview.PullToRefreshSwipeMenuListView;
import cn.udesk.widget.swipelistview.SwipeMenu;
import cn.udesk.widget.swipelistview.SwipeMenuCreator;
import cn.udesk.widget.swipelistview.SwipeMenuItem;
import udesk.core.utils.BaseUtils;

/**
 * Created by Administrator on 2017/10/18.
 */
public class ConversationFragment extends BaseFragment implements PullToRefreshSwipeMenuListView.IXListViewListener, AdapterView.OnItemClickListener, IMessageArrived {

    private static final String TAG = ConversationFragment.class.getSimpleName();

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
    public static final int ReceviveMessageWhat = 1;

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


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_conversation;
    }

    @Override
    protected void initView() {
        if (!Fresco.hasBeenInitialized()) {
            UdeskSDKManager.getInstance().init(ConversationFragment.this.getActivity());
        }
        UdeskSDKManager.getInstance().setCustomerOffline(true);
        mHandler = new MyHandler(ConversationFragment.this);
        UdeskSDKManager.getInstance().setCustomerOffline(true);
        UdeskSDKManager.getInstance().setMessageArrived(this);
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
        mAdapter = new AbsCommonAdapter<Merchant>(activity, R.layout.conversation_adapter_view) {
            @Override
            public void convert(AbsViewHolder helper, Merchant item, int pos) {
                SimpleDraweeView head = helper.getView(R.id.iv_head);
                BadgeView badgeView = helper.getView(R.id.id_unread_tips);
                TextView nickTv = helper.getView(R.id.id_nickName);
                TextView timeTv = helper.getView(R.id.tv_time);
                TextView contentTv = helper.getView(R.id.tv_content);

                nickTv.setText(BaseUtils.objectToString(item.getName()));
                ReceiveMessage message = item.getLast_message();
                if (message != null) {
                    timeTv.setText(UdeskUtil.formatLongTypeTimeToString(ConversationFragment.this.getContext(), BaseUtils.objectToString(message.getCreated_at())));
                    //timeTv.setText(TimeUtil.formatFilterTime(BaseUtils.objectToString(message.getCreated_at())));
                    String contentType = BaseUtils.objectToString(message.getContent_type());
                    if (UdeskConst.ChatMsgTypeString.TYPE_TEXT.equals(contentType)) {
                        if (UDEmojiAdapter.replaceEmoji(mContext, BaseUtils.objectToString(message.getContent()),
                                (int) contentTv.getTextSize()) != null) {
                            contentTv.setText(UDEmojiAdapter.replaceEmoji(mContext, BaseUtils.objectToString(message.getContent()),
                                    (int) contentTv.getTextSize()));
                        } else {
                            contentTv.setText(BaseUtils.objectToString(message.getContent()));
                        }
                    } else if (UdeskConst.ChatMsgTypeString.TYPE_AUDIO.equals(contentType)) {
                        contentTv.setText(getString(R.string.msg_type_audio));
                    } else if (UdeskConst.ChatMsgTypeString.TYPE_IMAGE.equals(contentType)) {
                        contentTv.setText(getString(R.string.msg_type_image));
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
                UdeskUtil.loadHeadView(activity, head, Uri.parse(BaseUtils.objectToString(item.getLogo_url())));
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
                item1.setWidth((int) activity.getResources().getDimension(R.dimen.udesk_80));
                item1.setTitle(getResources().getString(
                        R.string.close_conversation));
                item1.setId(UdeskLibConst.SwipeMenuItemDeleteId);
                item1.setTitleColor(activity.getResources().getColor(R.color.list_item_nomal));
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
    }

    @Override
    protected void initVariable() {

    }

    @Override
    public void onStart() {
        super.onStart();
        refreshData();
    }

    @Override
    protected void initData() {

    }

    private void refreshData() {
        InitMode initMode = UdeskSDKManager.getInstance().getInitMode();
        if (initMode != null) {
            HttpFacade.getInstance().getMerchants(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                    UdeskUtil.objectToString(initMode.getIm_password())), new HttpCallBack() {
                @Override
                public void onSuccess(String message) {
                    if (mListView != null && mListView.getVisibility() == View.VISIBLE) {
                        mListView.stopRefresh(true);
                    }
                    List<Merchant> merchants = JsonUtils.parseRecentMerchants(message);
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
                            InitMode initMode = UdeskSDKManager.getInstance().getInitMode();
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
                                                Log.i("udesk", "sendCommodity result =" + getString(R.string.time_out));
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

        UdeskSDKManager.getInstance().entryChat(ConversationFragment.this.getActivity(), UdeskUtil.objectToString(merchant.getEuid()));
    }

    @Override
    public void onNewMessage(ReceiveMessage receiveMessage) {

        if (receiveMessage != null) {
            addMessage(receiveMessage);
        }
    }

    private void addMessage(ReceiveMessage receiveMessage) {
        if (receiveMessage != null && mAdapter != null) {
            List<Merchant> merchants = mAdapter.getDatas();
            Merchant tempMerchant = null;
            for (Merchant merchant : merchants) {
                if (UdeskUtil.objectToString(merchant.getEuid()).equals(receiveMessage.getMerchant_euid())) {

                    merchant.setLast_message(receiveMessage);
                    merchant.setUnread_count(UdeskUtil.objectToInt(merchant.getUnread_count()) + 1);
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
            if (UdeskSDKManager.getInstance().getItotalCount() != null) {
                UdeskSDKManager.getInstance().getItotalCount().totalcount(count);
            }
        }

    }

}
