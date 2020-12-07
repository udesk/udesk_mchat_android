package cn.udesk.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.udesk.PreferenceHelper;
import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.activity.MessageAdatper.AudioViewHolder;
import cn.udesk.adapter.UdeskFunctionAdapter;
import cn.udesk.audio.AudioRecordButton;
import cn.udesk.camera.UdeskCameraActivity;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskConfig;
import cn.udesk.emotion.EmotionKeyboard;
import cn.udesk.emotion.EmotionLayout;
import cn.udesk.emotion.IEmotionSelectedListener;
import cn.udesk.emotion.LQREmotionKit;
import cn.udesk.model.FunctionMode;
import cn.udesk.model.Merchant;
import cn.udesk.model.SendMsgResult;
import cn.udesk.model.SurveyOptionsModel;
import cn.udesk.muchat.UdeskLibConst;
import cn.udesk.muchat.bean.FeedbacksResult;
import cn.udesk.muchat.bean.NavigatesResult;
import cn.udesk.muchat.bean.Products;
import cn.udesk.muchat.bean.ReceiveMessage;
import cn.udesk.muchat.bean.SurvyOption;
import cn.udesk.permission.RequestCode;
import cn.udesk.permission.XPermissionUtils;
import cn.udesk.photoselect.PhotoSelectorActivity;
import cn.udesk.photoselect.entity.LocalMedia;
import cn.udesk.presenter.ChatActivityPresenter;
import cn.udesk.presenter.IChatActivityView;
import cn.udesk.voice.RecordFilePlay;
import cn.udesk.voice.RecordPlay;
import cn.udesk.voice.RecordPlayCallback;
import cn.udesk.widget.UDPullGetMoreListView;
import cn.udesk.widget.UdeskConfirmPopWindow;
import cn.udesk.widget.UdeskMultiMenuHorizontalWindow;
import cn.udesk.widget.UdeskMultiMenuHorizontalWindow.OnPopMultiMenuClick;
import cn.udesk.widget.UdeskSurvyPopwindow;
import cn.udesk.widget.UdeskTitleBar;
import udesk.core.utils.BaseUtils;
import udesk.core.utils.UdeskUtils;

public class UdeskChatActivity extends UdeskBaseActivity implements IChatActivityView, IEmotionSelectedListener,
        OnClickListener {

    private TextView sendBtn;
    private EditText mInputEditView;
    private View btnPhoto, btnCamera, btnSurvy;
    private ImageView showVoiceImg;
    private UDPullGetMoreListView mListView;
    private MessageAdatper mChatAdapter;
    private UdeskTitleBar mTitlebar;
    private RecordFilePlay mRecordFilePlay;
    private RecordPlayCallback mPlayCallback;

    private Uri photoUri;
    private File cameraFile;

    //商户的euid
    private String euid = "";
    //当前的商户
    private Merchant merchant;

    private final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 101;
    private final int SELECT_IMAGE_ACTIVITY_REQUEST_CODE = 102;
    private final int CAPTURE_IMAGE_SMALLVIDEO_ACTIVITY_REQUEST_CODE = 103;
    private final int SELECT_UDESK_IMAGE_ACTIVITY_REQUEST_CODE = 104;
    private final int SELECT_FILE_OPTION_REQUEST_CODE = 105;

    private MyHandler mHandler;
    private ChatActivityPresenter mPresenter;
    private BroadcastReceiver mConnectivityChangedReceiver = null;
    private boolean hasAddCommodity = false;
    private boolean hasAutoProduct = false;
    //咨询对象
    public View commodity_rl;
    public ImageView commityThumbnail;
    public TextView commityTitle;
    public TextView commitySubTitle;
    public TextView commityLink;

    private boolean isDestroyed = false;
    private long QUEUE_RETEY_TIME = 5 * 1000;

    private LinearLayout addNavigationFragmentView;

    //满意度配置
    SurveyOptionsModel surveyOptionsModel;
    private UdeskConfirmPopWindow popWindow = null;
    private boolean isblocked;
    private GridView funGridView;
    private UdeskFunctionAdapter udeskFunctionAdapter;
    private List<FunctionMode> functionItems = new ArrayList<FunctionMode>();
    private FunctionMode survyItem;
    private FrameLayout mBottomFramlayout;
    private LinearLayout mMoreLayout;
    private ImageView mMoreImg;
    private boolean isNeedOpenLocalCamera = false;
    private AudioRecordButton mBtnAudio;
    private LinearLayout navigationRootView;
    private LinearLayout mContentLinearLayout;
    private ImageView mAudioImg;
    private ImageView mEmojiImg;
    private EmotionLayout mEmotionlayout;
    private EmotionKeyboard mEmotionKeyboard;

    //导航信息
    private NavigatesResult navigatesResult;
    private boolean isCheckedNavigatesMenu = false;
    private boolean isShowNavigatesMenu = false;

    public static class MessageWhat {

        public static final int BackMerchant = 1;
        public static final int refreshAdapter = 2;
        public static final int changeImState = 3;
        public static final int onNewMessage = 4;
        public static final int RECORD_ERROR = 5;
        public static final int RECORD_Too_Short = 6;
        public static final int UPDATE_VOCIE_STATUS = 7;
        public static final int recordllegal = 8;
        public static final int Xmpp_is_disConent = 9;
        public static final int Survey_error = 10;
        public static final int Has_Survey = 11;
        public static final int surveyNotify = 12;
        public static final int Survey_Success = 13;
        public static final int IM_BOLACKED = 14;
        public static final int ChangeVideoThumbnail = 15;
        public static final int ChangeFileProgress = 16;
        public static final int DownFileError = 17;
        public static final int SEND_AUTO_MESSAGE = 18;
        public static final int GET_FEEDBACK = 19;
        public static final int DEAL_LEAVE_MSG = 20;
    }

    class ConnectivtyChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent
                        .getAction())) {
                    return;
                }
                boolean bNetWorkAvailabl = UdeskUtils.isNetworkConnected(context);
                if (bNetWorkAvailabl) {
                    if (isblocked) {
                        return;
                    }
                    if (merchant == null && mPresenter != null) {
                        mPresenter.getMerchantInfo();
                    }
                } else {
                    UdeskUtils.showToast(
                            context,
                            context.getResources().getString(
                                    R.string.udesk_has_wrong_net));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class MyHandler extends Handler {
        WeakReference<UdeskChatActivity> mWeakActivity;

        public MyHandler(UdeskChatActivity activity) {
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                final UdeskChatActivity activity = mWeakActivity.get();
                if (activity == null) {
                    return;
                }
                switch (msg.what) {
                    case MessageWhat.BackMerchant:
                        activity.setTitlebar(activity.merchant.getName());
                        break;
                    case MessageWhat.refreshAdapter:
                        if (activity.mChatAdapter != null) {
                            List<ReceiveMessage> messages = (List<ReceiveMessage>) msg.obj;
                            int arg1 = msg.arg1;
                            if (messages != null) {
                                int index = messages.size();
                                if (arg1 > 0) {
                                    activity.mChatAdapter.listAddItems(messages, true);
                                    activity.smoothScrollToPosition(activity.mListView, index + 1);

                                } else {
                                    activity.mChatAdapter.listAddItems(messages, false);
                                    activity.smoothScrollToPosition(activity.mListView, activity.mChatAdapter.getCount());

                                }
                                if (activity.getNavigatesChatCache() !=null){
                                    activity.mChatAdapter.listAddEventItems(activity.getNavigatesChatCache());
                                }
                            }
                        }
                        break;
                    case MessageWhat.changeImState:
                        SendMsgResult sendMsgResult = (SendMsgResult) msg.obj;
                        activity.changeImState(sendMsgResult);
                        break;
                    case MessageWhat.onNewMessage:
                        ReceiveMessage msgInfo = (ReceiveMessage) msg.obj;
                        if (msgInfo != null && UdeskUtil.objectToString(msgInfo.getMerchant_euid()).equals(activity.euid) && activity.mChatAdapter != null) {
                            activity.mChatAdapter.addItem(msgInfo);
                            activity.smoothScrollToPosition(activity.mListView, activity.mChatAdapter.getCount());

                        }
                        break;
                    case MessageWhat.RECORD_ERROR:
                        UdeskUtils.showToast(activity, activity.getResources()
                                .getString(R.string.udesk_im_record_error));
                        break;
                    case MessageWhat.Xmpp_is_disConent:
                        this.postDelayed(activity.myRunnable, activity.QUEUE_RETEY_TIME);
                        break;
                    case MessageWhat.Survey_error:
                        UdeskUtils.showToast(activity, activity.getResources()
                                .getString(R.string.udesk_survey_error));
                        break;
                    case MessageWhat.Has_Survey:
                        UdeskUtils.showToast(activity, activity.getResources()
                                .getString(R.string.udesk_has_survey));
                        break;
                    case MessageWhat.surveyNotify:
                        if (activity.surveyOptionsModel != null) {
                            activity.toLuanchSurveyView(activity.surveyOptionsModel);
                        } else if (activity.mPresenter != null) {
                            activity.mPresenter.getIMSurveyOptions(true);
                        }
                        break;
                    case MessageWhat.Survey_Success:
                        UdeskUtils.showToast(activity, activity.getResources()
                                .getString(R.string.udesk_thanks_survy));
                        break;
                    case MessageWhat.IM_BOLACKED:
                        activity.setTitlebar(activity.getResources().getString(R.string.add_bolcked_tips));
                        this.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                activity.toBlockedView();
                            }
                        }, 1500);
                    case MessageWhat.ChangeVideoThumbnail:
                        String videoMsgId = (String) msg.obj;
                        activity.changeVideoThumbnail(videoMsgId);
                        break;
                    case MessageWhat.ChangeFileProgress:
                        String fileMsgId = UdeskUtil.objectToString(msg.obj);
                        int percent = msg.arg1;
                        activity.changeFileProgress(fileMsgId, percent, 0, true);
                        break;
                    case MessageWhat.DownFileError:
                        String fileId = UdeskUtil.objectToString(msg.obj);
                        activity.changeFileProgress(fileId, 0, 0, false);
                        break;
                    case MessageWhat.SEND_AUTO_MESSAGE:
                        if (UdeskSDKManager.getInstance().getProducts() != null && !activity.hasAddCommodity) {
                            activity.showCommityThunbnail(UdeskSDKManager.getInstance().getProducts());
                            activity.sendCommodity();
                        }
                        activity.sendProductMessage();

                        break;
                    case MessageWhat.GET_FEEDBACK:
                        if (activity.mPresenter != null) {
                            activity.getPresenter().getFeedbacks();
                        }

                        break;
                    case MessageWhat.DEAL_LEAVE_MSG:
                        FeedbacksResult feedbacksResult = (FeedbacksResult) msg.obj;
                        if (feedbacksResult != null && feedbacksResult.getData() != null) {
                            String type = feedbacksResult.getData().getLeave_message_type();
                            if (TextUtils.equals(UdeskConst.UdeskLeaveMessageType.Form,type)){
                                Intent intent = new Intent(activity, UdeskWebViewUrlAcivity.class);
                                intent.putExtra(UdeskConst.URL, feedbacksResult.getData().getFeedback_url());
                                intent.putExtra(UdeskConst.Euid, UdeskUtil.objectToString(activity.getMerchant().getEuid()));
                                activity.startActivity(intent);
                            }
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendCommodity() {
        try {
            if (UdeskSDKManager.getInstance().getProducts() != null && !hasAddCommodity && !isblocked && isCheckedNavigatesMenu()) {
                hasAddCommodity = true;
                mPresenter.sendCommodity(UdeskSDKManager.getInstance().getProducts());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendProductMessage() {
        try {
            if (UdeskSDKManager.getInstance().getProductMessage() != null && !hasAutoProduct && !isblocked && isCheckedNavigatesMenu()) {
                hasAutoProduct = true;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPresenter.sendProductMessage(UdeskSDKManager.getInstance().getProductMessage());
                    }
                }, 1500);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void smoothScrollToPosition(final UDPullGetMoreListView listView, final int positon) {
        try {
            listView.smoothScrollToPosition(positon);
            listView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    listView.setSelection(positon);
                }
            }, 400);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据消息的ID 修改发送状态
     */
    private void changeFileProgress(String msgId, int precent, long fileSize, boolean isSuccess) {
        try {
            if (!TextUtils.isEmpty(msgId) && mListView != null
                    && mChatAdapter != null) {
                //getChildAt(i) 只能获取可见区域View， 会有bug
                for (int i = mListView.getChildCount() - 1; i >= 0; i--) {
                    View child = mListView.getChildAt(i);
                    if (child != null) {
                        if (mChatAdapter.changeFileState(child, msgId, precent, fileSize, isSuccess)) {
                            return;
                        }
                    }
                }
                //当不在可见区域则调用整个刷新
                mChatAdapter.updateProgress(msgId, precent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    //显示黑名单提示框
    private void toBlockedView() {
        try {
            String positiveLabel = this.getString(R.string.udesk_sure);
            String negativeLabel = this.getString(R.string.udesk_cancel);
            String title = this.getString(R.string.add_bolcked_tips);
            if (UdeskChatActivity.this.isFinishing()) {
                return;
            }
            if (!popWindow.isShowing() && this.getWindow() != null
                    && this.getWindow().getDecorView() != null && this.getWindow().getDecorView().getWindowToken() != null) {
                popWindow.show(this, this.getWindow().getDecorView(),
                        positiveLabel, negativeLabel, title,
                        new UdeskConfirmPopWindow.OnPopConfirmClick() {
                            @Override
                            public void onPositiveClick() {
                                finish();
                            }

                            @Override
                            public void onNegativeClick() {
                                popWindow.dismiss();
                            }

                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        try {
            setContentView(R.layout.udesk_activity_im);
            mHandler = new MyHandler(UdeskChatActivity.this);
            mPresenter = new ChatActivityPresenter(this);
            initIntent();
            settingTitlebar();
            setNavigatesChatCacheClickable();
            initView();
            mPresenter.getMessages("");
            mPresenter.getMerchantInfo();
            mPresenter.setMessageRead();
            mPresenter.getIMSurveyOptions(false);
            mPresenter.getCustomerStatus();
            UdeskSDKManager.getInstance().setCustomerOffline(true);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }

    private void setNavigatesChatCacheClickable() {
        try {
            List<ReceiveMessage> navigatesChatCache = getNavigatesChatCache();
            if (navigatesChatCache != null && navigatesChatCache.size() > 0) {
                for (ReceiveMessage msg : navigatesChatCache) {
                    msg.setNavigatesClickable(false);
                }
            }
            UdeskUtil.savePreferenceCache(getContext(), UdeskSDKManager.getInstance().getCustomerEuid() + UdeskLibConst.SharePreParams.NavigatesChatCache, navigatesChatCache);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public synchronized void checkConnect() {
        if (UdeskSDKManager.getInstance().getInitMode() != null) {
            if (!UdeskSDKManager.getInstance().isConnection()) {
                sendXmppIsDisConnect();
                mPresenter.getMessages("");
                UdeskSDKManager.getInstance().connectXmpp(UdeskSDKManager.getInstance().getInitMode());
            }
        }
    }

    //在指定客服组ID  或者指定客服ID  会传入值  其它的方式进入不会传值
    private void initIntent() {
        try {
            Intent intent = getIntent();
            if (intent != null) {
                euid = intent.getStringExtra(UdeskConst.Euid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * titlebar 的设置
     */
    private void settingTitlebar() {
        try {
            mTitlebar = (UdeskTitleBar) findViewById(R.id.udesktitlebar);
            if (mTitlebar != null) {
                UdekConfigUtil.setUITextColor(UdeskConfig.udeskTitlebarTextLeftRightResId, mTitlebar.getLeftTextView(), mTitlebar.getRightTextView());
                UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskTitlebarBgResId, mTitlebar.getRootView());
                if (UdeskConfig.DEFAULT != UdeskConfig.udeskbackArrowIconResId) {
                    mTitlebar.getUdeskBackImg().setImageResource(UdeskConfig.udeskbackArrowIconResId);
                }
                mTitlebar
                        .setLeftTextSequence(getString(R.string.udesk_agent_connecting));
                mTitlebar.setLeftLinearVis(View.VISIBLE);
                mTitlebar.setLeftViewClick(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finishAcitivty();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ChatActivityPresenter getPresenter() {
        return mPresenter;
    }

    private void showCommityThunbnail(final Products products) {

        commodity_rl.setVisibility(View.VISIBLE);
        commodity_rl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UdeskSDKManager.getInstance().getCommodityCallBack() != null) {
                    UdeskSDKManager.getInstance().getCommodityCallBack().callBackProduct(products);
                }
            }
        });
        commityTitle.setText(products.getProduct().getTitle());
        List<Products.ProductBean.ExtrasBean> extrasBeens = products.getProduct().getExtras();
        if (extrasBeens != null && extrasBeens.size() > 0) {
            commitySubTitle.setText(extrasBeens.get(0).getTitle() + ": " + extrasBeens.get(0).getContent());
        }
        UdeskUtil.loadInto(getApplicationContext(), products.getProduct().getImage(), R.drawable.udesk_defualt_failure, R.drawable.udesk_defalut_image_loading, commityThumbnail);
        commityLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isblocked) {
                    toBlockedView();
                    return;
                }
                if (isCheckedNavigatesMenu()){
                    sentProduct(products);
                }else {
                    UdeskUtils.showToast(getApplicationContext(),getResources().getString(R.string.udesk_navigatesMenu_unchecked));
                }
            }
        });
    }

    private void initView() {
        try {
            popWindow = new UdeskConfirmPopWindow(this);

            commodity_rl = findViewById(R.id.commodity_rl);
            commityThumbnail = (ImageView) findViewById(R.id.udesk_im_commondity_thumbnail);
            commityTitle = (TextView) findViewById(R.id.udesk_im_commondity_title);
            commitySubTitle = (TextView) findViewById(R.id.udesk_im_commondity_subtitle);
            commityLink = (TextView) findViewById(R.id.udesk_im_commondity_link);
            commodity_rl.setVisibility(View.GONE);

            sendBtn = findViewById(R.id.udesk_bottom_send);
            sendBtn.setOnClickListener(this);

            mInputEditView = (EditText) findViewById(R.id.udesk_bottom_input);

            mListView = (UDPullGetMoreListView) findViewById(R.id.udesk_conversation);

            mContentLinearLayout = (LinearLayout) findViewById(R.id.udesk_content_ll);
            mAudioImg = (ImageView) findViewById(R.id.udesk_img_audio);
            if (UdeskConfig.isUseVoice) {
                mAudioImg.setVisibility(View.VISIBLE);
            } else {
                mAudioImg.setVisibility(View.GONE);
            }
            mBtnAudio = (AudioRecordButton) findViewById(R.id.udesk_audio_btn);
            mBtnAudio.init(UdeskUtil.getDirectoryPath(getApplicationContext(), UdeskConst.FileAudio));
            mBtnAudio.setRecordingListener(new AudioRecordButton.OnRecordingListener() {
                @Override
                public void recordStart() {
                    if (mRecordFilePlay != null) {
                        showStartOrStopAnaimaition(
                                mRecordFilePlay.getPlayAduioMessage(), false);
                        recycleVoiceRes();
                    }

                }

                @Override
                public void recordFinish(String audioFilePath, long recordTime) {
                    if (mPresenter != null) {
                        mPresenter.sendRecordAudioMsg(audioFilePath, recordTime);
                    }
                }

                @Override
                public void recordError(String message) {
                    Message msg = Message.obtain();
                    msg.what = MessageWhat.RECORD_ERROR;
                    mHandler.sendMessage(msg);

                }
            });

            mEmojiImg = (ImageView) findViewById(R.id.udesk_emoji_img);
            showEmoji();
            mMoreImg = (ImageView) findViewById(R.id.udesk_more_img);
            mBottomFramlayout = (FrameLayout) findViewById(R.id.udesk_bottom_frame);
            mEmotionlayout = (EmotionLayout) findViewById(R.id.udesk_emotion_view);
            mMoreLayout = (LinearLayout) findViewById(R.id.udesk_more_layout);
            mEmotionlayout.attachEditText(mInputEditView);
            initEmotionKeyboard();

            navigationRootView = findViewById(R.id.navigation_root_view);
            addNavigationFragmentView = findViewById(R.id.fragment_view);
            if (UdeskConfig.isUseNavigationView) {
                navigationRootView.setVisibility(View.VISIBLE);
            } else {
                navigationRootView.setVisibility(View.GONE);
            }

            if (UdeskSDKManager.getInstance().getNavigationModes() != null
                    && UdeskSDKManager.getInstance().getNavigationModes().size() > 0) {
                addNavigationFragment();
            } else {
                navigationRootView.setVisibility(View.GONE);
            }

            initFunctionAdapter();
            setListView();
            UdeskSDKManager.getInstance().setCustomerOffline(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showEmoji() {
        try {
            if (UdeskConfig.isUseEmotion && LQREmotionKit.getEmotionPath() != null) {
                mEmojiImg.setVisibility(View.VISIBLE);
            } else {
                mEmojiImg.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initFunctionAdapter() {
        funGridView = findViewById(R.id.function_gridview);
        udeskFunctionAdapter = new UdeskFunctionAdapter(this);
        funGridView.setAdapter(udeskFunctionAdapter);
        initfunctionItems();
        funGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    FunctionMode functionItem = (FunctionMode) adapterView.getItemAtPosition(i);
                    switch (functionItem.getId()) {
                        case UdeskConst.UdeskFunctionFlag.Udesk_Camera:
                            clickCamera();
                            mBottomFramlayout.setVisibility(View.GONE);
                            break;
                        case UdeskConst.UdeskFunctionFlag.Udesk_Photo:
                            clickPhoto();
                            mBottomFramlayout.setVisibility(View.GONE);
                            break;
                        case UdeskConst.UdeskFunctionFlag.Udesk_Survy:
                            clickSurvy();
                            mBottomFramlayout.setVisibility(View.GONE);
                            break;
                        case UdeskConst.UdeskFunctionFlag.Udesk_Video:
                            clickVideo();
                            mBottomFramlayout.setVisibility(View.GONE);
                            break;
                        case UdeskConst.UdeskFunctionFlag.Udesk_File:
                            clickFile();
                            mBottomFramlayout.setVisibility(View.GONE);
                            break;
                        default:
                            if (UdeskSDKManager.getInstance().getFunctionItemClickCallBack() != null) {
                                UdeskSDKManager.getInstance().getFunctionItemClickCallBack()
                                        .callBack(getApplicationContext(), mPresenter, functionItem);
                            }
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initEmotionKeyboard() {
        try {
            mEmotionKeyboard = EmotionKeyboard.with(this);
            mEmotionKeyboard.bindToEditText(mInputEditView);
            mEmotionKeyboard.bindToContent(mContentLinearLayout);
            mEmotionKeyboard.setEmotionLayout(mBottomFramlayout);
            mEmotionKeyboard.bindToEmotionButton(mEmojiImg, mMoreImg);
            mEmotionKeyboard.setOnEmotionButtonOnClickListener(new EmotionKeyboard.OnEmotionButtonOnClickListener() {
                @Override
                public boolean onEmotionButtonOnClickListener(View view) {
                    try {
                        if (!isCheckedNavigatesMenu()){
                            UdeskUtils.showToast(getApplicationContext(),getResources().getString(R.string.udesk_navigatesMenu_unchecked));
                            return true;
                        }
                        if (isblocked) {
                            toBlockedView();
                            return true;
                        }

                        if (!isShowNotSendMsg()) {
                            mEmotionKeyboard.hideSoftInput();
                            return true;
                        }
                        int i = view.getId();
                        if (i == R.id.udesk_emoji_img) {
                            if (!mEmotionlayout.isShown()) {
                                if (mMoreLayout.isShown()) {
                                    showEmotionLayout();
                                    hideMoreLayout();
                                    hideAudioButton();
                                    return true;
                                }
                            } else if (mEmotionlayout.isShown() && !mMoreLayout.isShown()) {
                                mEmojiImg.setImageResource(R.drawable.udesk_ic_cheat_emo);
                                return false;
                            }
                            showEmotionLayout();
                            hideMoreLayout();
                            hideAudioButton();

                        } else if (i == R.id.udesk_more_img) {
                            if (!mMoreLayout.isShown()) {
                                if (mEmotionlayout.isShown()) {
                                    showMoreLayout();
                                    hideEmotionLayout();
                                    hideAudioButton();
                                    return true;
                                }
                            }
                            showMoreLayout();
                            hideEmotionLayout();
                            hideAudioButton();

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });

            initListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initListener() {
        try {
            mAudioImg.setOnClickListener(this);

            mEmotionlayout.setEmotionSelectedListener(this);
            mEmotionlayout.setEmotionAddVisiable(true);
            mEmotionlayout.setEmotionSettingVisiable(true);


            mInputEditView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        if (mInputEditView.getText().toString().trim().length() > 0) {
                            sendBtn.setVisibility(View.VISIBLE);
                            mMoreImg.setVisibility(View.GONE);
                        } else {
                            sendBtn.setVisibility(View.GONE);
                            setMoreVis(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showEmotionLayout() {
        try {
            mEmotionlayout.setVisibility(View.VISIBLE);
            mEmojiImg.setImageResource(R.drawable.udesk_ic_cheat_keyboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideEmotionLayout() {
        try {
            mEmotionlayout.setVisibility(View.GONE);
            mEmojiImg.setImageResource(R.drawable.udesk_ic_cheat_emo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideMoreLayout() {
        try {
            mMoreLayout.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMoreLayout() {
        try {
            mMoreLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAudioButton() {
        try {
            mBtnAudio.setVisibility(View.VISIBLE);
            mInputEditView.setVisibility(View.GONE);
            mAudioImg.setImageResource(R.drawable.udesk_ic_cheat_keyboard);

            if (mBottomFramlayout.isShown()) {
                if (mEmotionKeyboard != null) {
                    mEmotionKeyboard.interceptBackPress();
                }
            } else {
                if (mEmotionKeyboard != null) {
                    mEmotionKeyboard.hideSoftInput();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideAudioButton() {
        try {
            mBtnAudio.setVisibility(View.GONE);
            mInputEditView.setVisibility(View.VISIBLE);
            mAudioImg.setImageResource(R.drawable.udesk_ic_cheat_voice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //点击文件入口
    private void clickFile() {
        try {
            if (Build.VERSION.SDK_INT < 23) {
                selectFile();
            } else {
                XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.EXTERNAL,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        new XPermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                selectFile();
                            }

                            @Override
                            public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.file_denied));
                            }
                        });
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    //启动手机默认的选择mp4文件
    private void selectFile() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            Intent wrapperIntent = Intent.createChooser(intent, null);
            startActivityForResult(wrapperIntent, SELECT_FILE_OPTION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    //拍视频
    private void clickVideo() {
        try {
            if (Build.VERSION.SDK_INT < 23) {
                takeVideo();
                bottomoPannelBegginStatus();
            } else {
                XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.CAMERA,
                        new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        new XPermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                takeVideo();
                                bottomoPannelBegginStatus();
                            }

                            @Override
                            public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.camera_denied));
                            }
                        });

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void takeVideo() {
        try {
            if (UdeskConfig.isUseSmallVideo) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), UdeskCameraActivity.class);
                startActivityForResult(intent, CAPTURE_IMAGE_SMALLVIDEO_ACTIVITY_REQUEST_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //相册
    private void clickPhoto() {
        try {
            if (Build.VERSION.SDK_INT < 23) {
                selectPhoto();
                bottomoPannelBegginStatus();
            } else {
                XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.EXTERNAL,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        new XPermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                selectPhoto();
                                bottomoPannelBegginStatus();
                            }

                            @Override
                            public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                Toast.makeText(UdeskChatActivity.this,
                                        getResources().getString(R.string.photo_denied),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //拍照
    private void clickCamera() {
        try {
            if (Build.VERSION.SDK_INT < 23) {
                takePhoto();
                bottomoPannelBegginStatus();
            } else {
                XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.CAMERA,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        new XPermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                takePhoto();
                                bottomoPannelBegginStatus();
                            }

                            @Override
                            public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                Toast.makeText(UdeskChatActivity.this,
                                        getResources().getString(R.string.camera_denied),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initfunctionItems() {
        try {
            functionItems.clear();
            if (UdeskConfig.isUsephoto) {
                FunctionMode photoItem = new FunctionMode(getString(R.string.photo), UdeskConst.UdeskFunctionFlag.Udesk_Photo, R.drawable.udesk_image_normal1);
                functionItems.add(photoItem);
            }
            if (UdeskConfig.isUsecamera) {
                FunctionMode cameraItem = new FunctionMode(getString(R.string.funtion_camera), UdeskConst.UdeskFunctionFlag.Udesk_Camera, R.drawable.udesk_camer_normal1);
                functionItems.add(cameraItem);
            }
            if (UdeskConfig.isUseSmallVideo) {
                FunctionMode videoItem = new FunctionMode(getString(R.string.video), UdeskConst.UdeskFunctionFlag.Udesk_Video, R.drawable.udesk_video);
                functionItems.add(videoItem);
            }
            if (UdeskConfig.isUsefile) {
                FunctionMode videoItem = new FunctionMode(getString(R.string.file), UdeskConst.UdeskFunctionFlag.Udesk_File, R.drawable.udesk_file);
                functionItems.add(videoItem);
            }
            if (UdeskSDKManager.getInstance().getExtraFunctions() != null
                    && UdeskSDKManager.getInstance().getExtraFunctions().size() > 0) {
                functionItems.addAll(UdeskSDKManager.getInstance().getExtraFunctions());
            }

            udeskFunctionAdapter.setFunctionItems(functionItems);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addNavigationFragment() {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_view, new NavigationFragment());
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setListView() {
        try {
            mChatAdapter = new MessageAdatper(this);
            mListView.setAdapter(mChatAdapter);
            mListView
                    .setOnRefreshListener(new UDPullGetMoreListView.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            loadHistoryRecords();
                        }
                    });

            mListView.setRecyclerListener(new AbsListView.RecyclerListener() {
                @Override
                public void onMovedToScrapHeap(View view) {
                    if (mRecordFilePlay != null) {
                        checkRecoredView(view);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onEmojiSelected(String key) {

    }

    @Override
    public void onStickerSelected(String categoryName, String stickerName, String stickerBitmapPath) {
        try {
            mPresenter.sendBitmapMessage(stickerBitmapPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Glide.with(this).resumeRequests();
            if (!UdeskSDKManager.getInstance().isConnection()) {
                sendXmppIsDisConnect();
                UdeskSDKManager.getInstance().connectXmpp(UdeskSDKManager.getInstance().getInitMode());
            }
            if (TextUtils.isEmpty(LQREmotionKit.getEmotionPath())) {
                LQREmotionKit.init(getApplicationContext());
            }
            registerNetWorkReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendXmppIsDisConnect() {
        if (mHandler != null) {
            mHandler.removeCallbacks(myRunnable);
            Message msgWaitAgent = mHandler
                    .obtainMessage(MessageWhat.Xmpp_is_disConent);
            mHandler.sendMessage(msgWaitAgent);
        }
    }

    private Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            if (!UdeskSDKManager.getInstance().isConnection() && UdeskUtils.isNetworkConnected(UdeskChatActivity.this.getApplicationContext())) {
                if (mPresenter != null) {
                    mPresenter.getMessages("");
                }
                sendXmppIsDisConnect();
                UdeskSDKManager.getInstance().connectXmpp(UdeskSDKManager.getInstance().getInitMode());
            }
        }
    };


    @Override
    public void onClick(View v) {
        try {
            //检查是否处在可发消息的状态
            if (isblocked) {
                toBlockedView();
                return;
            }
            //检查是否处在可发消息的状态
            if (!isShowNotSendMsg()) {
                UdeskUtils.hideSoftKeyboard(this, mInputEditView);
                return;
            }

            if (v.getId() == R.id.udesk_img_audio) {
                if (!isCheckedNavigatesMenu()){
                    UdeskUtils.showToast(getApplicationContext(),getResources().getString(R.string.udesk_navigatesMenu_unchecked));
                    return;
                }
                if (mBtnAudio.isShown()) {
                    hideAudioButton();
                    mInputEditView.requestFocus();
                    if (mEmotionKeyboard != null) {
                        mEmotionKeyboard.showSoftInput();
                    }
                } else {
                    if (Build.VERSION.SDK_INT < 23) {
                        showAudioButton();
                        hideEmotionLayout();
                        hideMoreLayout();
                    } else {
                        XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.AUDIO,
                                new String[]{Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                new XPermissionUtils.OnPermissionListener() {
                                    @Override
                                    public void onPermissionGranted() {
                                        showAudioButton();
                                        hideEmotionLayout();
                                        hideMoreLayout();
                                    }

                                    @Override
                                    public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                        Toast.makeText(UdeskChatActivity.this,
                                                getResources().getString(R.string.aduido_denied),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                }
            } else if (R.id.udesk_bottom_send == v.getId()) { //发送文本消息
                if (!isCheckedNavigatesMenu()){
                    UdeskUtils.showToast(getApplicationContext(),getResources().getString(R.string.udesk_navigatesMenu_unchecked));
                    return;
                }
                if (TextUtils.isEmpty(mInputEditView.getText().toString())) {
                    UdeskUtils.showToast(getApplicationContext(),
                            getString(R.string.udesk_send_message_empty));
                    return;
                }
                mPresenter.sendTxtMessage();

            }

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }

    //电话呼叫
    @SuppressLint("MissingPermission")
    public void callphone(final String mobile) {
        try {
            if (Build.VERSION.SDK_INT < 23) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(mobile));
                UdeskChatActivity.this.startActivity(intent);
            } else {
                XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.CallPhone,
                        new String[]{Manifest.permission.CALL_PHONE},
                        new XPermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(mobile));
                                if (ActivityCompat.checkSelfPermission(UdeskChatActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                UdeskChatActivity.this.startActivity(intent);
                            }

                            @Override
                            public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                Toast.makeText(UdeskChatActivity.this,
                                        getResources().getString(R.string.call_denied),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE == requestCode) { //拍照后发生图片
                if (Activity.RESULT_OK == resultCode) {
                    if (mPresenter != null && photoUri != null && photoUri.getPath() != null) {
                        if (UdeskConfig.isScaleImg) {
                            mPresenter.scaleBitmap(UdeskUtil.parseOwnUri(photoUri, UdeskChatActivity.this, cameraFile));
                        } else {
                            mPresenter.sendBitmapMessage(UdeskUtil.parseOwnUri(photoUri, UdeskChatActivity.this, cameraFile));
                        }
                    }

                }
            } else if (SELECT_IMAGE_ACTIVITY_REQUEST_CODE == requestCode) { //选择图片后发送
                if (resultCode != Activity.RESULT_OK || data == null) {
                    return;
                }
                Uri mImageCaptureUri = data.getData();
                if (mImageCaptureUri != null) {
                    try {
                        if (mImageCaptureUri != null) {
                            String path = UdeskUtil.getFilePath(this, mImageCaptureUri);
                            if (UdeskConfig.isScaleImg) {
                                mPresenter.scaleBitmap(path);
                            } else {
                                mPresenter.sendBitmapMessage(path);
                            }

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError error) {
                        error.printStackTrace();
                    }
                }

            } else if (SELECT_FILE_OPTION_REQUEST_CODE == requestCode) {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    return;
                }
                Uri mImageCaptureUri = data.getData();
                if (mImageCaptureUri != null) {
                    try {
                        String path = UdeskUtil.getFilePath(this, mImageCaptureUri);
                        if (this.getWindow() != null && this.getWindow().getDecorView().getWindowToken() != null && UdeskUtil.isGpsNet(getApplicationContext())) {
                            toGpsNetView(true, null, path);
                            return;
                        }
                        sendFile(path);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError error) {
                        error.printStackTrace();
                    }
                }
            } else if (CAPTURE_IMAGE_SMALLVIDEO_ACTIVITY_REQUEST_CODE == requestCode) {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    return;
                }
                Bundle bundle = data.getBundleExtra(UdeskConst.SEND_BUNDLE);
                if (bundle != null) {
                    String type = bundle.getString(UdeskConst.SEND_SMALL_VIDEO);
                    if (type.equals(UdeskConst.SMALL_VIDEO)) {
                        String path = bundle.getString(UdeskConst.PREVIEW_Video_Path);
                        mPresenter.sendVideoMessage(path);
                    } else if (type.equals(UdeskConst.PICTURE)) {
                        String path = bundle.getString(UdeskConst.BitMapData);
                        if (UdeskConfig.isScaleImg) {
                            mPresenter.scaleBitmap(path);
                        } else {
                            mPresenter.sendBitmapMessage(path);
                        }
                    }
                }
            } else if (SELECT_UDESK_IMAGE_ACTIVITY_REQUEST_CODE == requestCode) {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    return;
                }
                Bundle bundle = data.getBundleExtra(UdeskConst.SEND_BUNDLE);
                if (bundle != null) {
                    ArrayList<LocalMedia> localMedias = bundle.getParcelableArrayList(UdeskConst.SEND_PHOTOS);
                    boolean isOrigin = bundle.getBoolean(UdeskConst.SEND_PHOTOS_IS_ORIGIN, false);

                    for (LocalMedia media : localMedias) {
                        final String pictureType = media.getPictureType();
                        final int mediaMimeType = UdeskUtil.isPictureType(pictureType);
                        if (mediaMimeType == UdeskUtil.TYPE_SHORT_VIDEO) {
                            long size = UdeskUtil.getFileSizeQ(this.getApplicationContext(), media.getPath());
                            if (size >= 30 * 1000 * 1000) {
                                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_file_to_large));
                                break;
                            }
                            mPresenter.sendVideoMessage(media.getPath());
                        } else if (mediaMimeType == UdeskUtil.TYPE_IMAGE) {
                            if (UdeskConfig.isScaleImg) {
                                mPresenter.scaleBitmap(media.getPath());
                            } else {
                                mPresenter.sendBitmapMessage(media.getPath());
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }


    }

    /**
     * 非wifi网络提示框
     *
     * @param isupload
     * @param info
     * @param path
     */
    private void toGpsNetView(final boolean isupload, final ReceiveMessage info, final String path) {
        try {
            String positiveLabel = this.getString(R.string.udesk_sure);
            String negativeLabel = this.getString(R.string.udesk_cancel);
            String content;
            if (isupload) {
                content = getApplicationContext().getString(R.string.udesk_gps_tips);
            } else {
                content = getApplicationContext().getString(R.string.udesk_gps_downfile_tips);
            }

            if (UdeskChatActivity.this.isFinishing()) {
                return;
            }
            if (!popWindow.isShowing()) {
                popWindow.show(this, this.getWindow().getDecorView(),
                        positiveLabel, negativeLabel, content,
                        new UdeskConfirmPopWindow.OnPopConfirmClick() {
                            @Override
                            public void onPositiveClick() {
                                try {
                                    if (isupload && !TextUtils.isEmpty(path)) {
                                        sendFile(path);
                                    }
                                    if (!isupload && info != null) {
                                        mPresenter.downFile(info, getApplicationContext(), UdeskConst.File_File);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onNegativeClick() {

                            }

                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFile(String path) {
        try {
            long size = UdeskUtil.getFileSizeQ(getApplicationContext(), path);
            if (size >= 30 * 1000 * 1000) {
                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_file_to_large));
                return;
            } else if (size == 0) {
                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_file_not_exist));
                return;
            }
            if (path.contains(".mp4")) {
                mPresenter.sendVideoMessage(path);
            } else {
                mPresenter.sendFileMessage(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    //下载文件
    public void downLoadMsg(ReceiveMessage message) {
        try {
            if (!UdeskUtils.isNetworkConnected(getApplicationContext())) {
                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_has_wrong_net));
                return;
            }
            if (message != null) {
                if (UdeskUtil.isGpsNet(getApplicationContext())) {
                    toGpsNetView(false, message, null);
                    return;
                }
                mPresenter.downFile(message, getApplicationContext(), UdeskConst.File_File);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadHistoryRecords() {
        try {
            mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
            if (mChatAdapter.getItem(0) != null) {
                mPresenter.getMessages(UdeskUtil.objectToString(mChatAdapter.getItem(0).getUuid()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }


    //启动手机默认的选择照片
    private void selectPhoto() {
        try {
            if (Build.VERSION.SDK_INT < 21) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_IMAGE_ACTIVITY_REQUEST_CODE);
            } else {
                Intent intent = new Intent();
                intent.setClass(UdeskChatActivity.this, PhotoSelectorActivity.class);
                startActivityForResult(intent, SELECT_UDESK_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    // 启动手机相机拍照
    private void takePhoto() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraFile = UdeskUtil.cameaFile(UdeskChatActivity.this);
            photoUri = UdeskUtil.getOutputMediaFileUri(UdeskChatActivity.this, cameraFile);
            if (Build.VERSION.SDK_INT >= 24) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            if (photoUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            }
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }


    //语音的回收
    private void checkRecoredView(View view) {
        try {
            Object tag = view.getTag();
            if (tag == null || !(tag instanceof AudioViewHolder)) {
                return;
            }

            AudioViewHolder holder = (AudioViewHolder) tag;
            final RecordFilePlay recordFilePlay = mRecordFilePlay;
            if (recordFilePlay != null) {
                String path = recordFilePlay.getMediaPath();
                if (path != null
                        && (path.equalsIgnoreCase(holder.message.getLocalPath()) || path
                        .equalsIgnoreCase(UdeskUtil.objectToString(holder.message.getContent())))) {
                    recordFilePlay.recycleCallback();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * 根据消息的ID 修改发送状态
     */
    private void changeImState(SendMsgResult sendMsgResult) {
        try {
            if (sendMsgResult != null && !TextUtils.isEmpty(sendMsgResult.getId()) && mListView != null
                    && mChatAdapter != null) {
                //getChildAt(i) 只能获取可见区域View， 会有bug
                for (int i = mListView.getChildCount() - 1; i >= 0; i--) {
                    View child = mListView.getChildAt(i);
                    if (child != null) {
                        if (mChatAdapter.changeImState(child, sendMsgResult)) {
                            return;
                        }
                    }
                }
                //当不在可见区域则调用整个刷新
                mChatAdapter.updateStatus(sendMsgResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }


    // 判断可发送消息
    private boolean isShowNotSendMsg() {
        try {

            if (!UdeskUtils.isNetworkConnected(this)) {
                UdeskUtils.showToast(this,
                        getResources().getString(R.string.udesk_has_wrong_net));
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public CharSequence getInputContent() {
        if (mInputEditView != null) {
            return mInputEditView.getText();
        }
        return "";
    }

    @Override
    public void clearInputContent() {
        if (mInputEditView != null) {
            mInputEditView.setText("");
        }
    }


    @Override
    public void addMessage(List<ReceiveMessage> message, String fromUUID) {
        try {
            mListView.onRefreshComplete();
            Message chatMessage = mHandler
                    .obtainMessage(MessageWhat.refreshAdapter);
            chatMessage.obj = message;
            if (TextUtils.isEmpty(fromUUID)) {
                chatMessage.arg1 = 0;
            } else {
                chatMessage.arg1 = 1;
            }

            mHandler.sendMessage(chatMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addNavigatesResult(NavigatesResult navigatesResult) {
        this.navigatesResult = navigatesResult;
    }
    @Override
    public void showNavigatesMenu(final NavigatesResult navigatesResult, boolean isShow) {
        try {
            isShowNavigatesMenu = isShow;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (navigatesResult != null && navigatesResult.getData() != null && navigatesResult.getData().isEnable()) {
                        ReceiveMessage receiveMessage = mPresenter.buildNavigatesMessage(navigatesResult, UdeskConst.NAVIGATES_ITEM);
                        Message messge = mHandler.obtainMessage(
                                MessageWhat.onNewMessage);
                        messge.obj = receiveMessage;
                        mHandler.sendMessage(messge);
                    }
                }
            },800);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void sendNavigatesMenu(NavigatesResult.DataBean.GroupMenusBean item) {
        try {
            mPresenter.sendNavigatesMenuMessage(item);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendPrevious(final NavigatesResult result) {
        try {
            if (result != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPresenter.buildNavigatesPreviousMessage(UdeskConst.ChatMsgTypeString.TYPE_NAVIGATES, getResources().getString(R.string.udesk_navigatesMenu_previous));
                        String navigatesParentId = result.getNavigatesParentId();
                        if (navigatesResult != null && navigatesResult.getData() != null) {
                            List<NavigatesResult.DataBean.GroupMenusBean> group_menus = navigatesResult.getData().getGroup_menus();
                            if (group_menus != null) {
                                if (TextUtils.equals(navigatesParentId, UdeskConst.NAVIGATES_ITEM)) {
                                    showNavigatesMenu(navigatesResult, true);
                                } else {
                                    for (NavigatesResult.DataBean.GroupMenusBean item : group_menus) {
                                        if (TextUtils.equals(navigatesParentId, item.getId())) {
                                            showNavigatesItemMenu(item);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }, 800);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showNavigatesItemMenu(final NavigatesResult.DataBean.GroupMenusBean item) {
        try {
            if (navigatesResult != null && navigatesResult.getData() != null && navigatesResult.getData().isEnable()) {
                setNavigatesChatCacheClickable();
                mChatAdapter.listAddEventItems(getNavigatesChatCache());
                if (item.isHas_next()) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ReceiveMessage receiveMessage = mPresenter.buildNavigatesMessage(navigatesResult, item.getId());
                            Message messge = mHandler.obtainMessage(
                                    MessageWhat.onNewMessage);
                            messge.obj = receiveMessage;
                            mHandler.sendMessage(messge);
                        }
                    }, 800);
                }else {
                    isCheckedNavigatesMenu = true;
                    PreferenceHelper.write(getContext(), UdeskLibConst.SharePreParams.Udesk_Sharepre_Name, UdeskSDKManager.getInstance().getCustomerEuid() + UdeskLibConst.SharePreParams.MENU_ID, item.getId());
                }
                sendCommodity();
                sendProductMessage();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public List<ReceiveMessage> getNavigatesChatCache(){
        try {
            return  UdeskUtil.getPreferenceCache(getContext(), UdeskSDKManager.getInstance().getCustomerEuid()+ UdeskLibConst.SharePreParams.NavigatesChatCache);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private boolean isCheckedNavigatesMenu(){
        if (navigatesResult != null && navigatesResult.getData()!=null && navigatesResult.getData().isEnable() && isShowNavigatesMenu){
            return isCheckedNavigatesMenu;
        }
        return true;
    }
    @Override
    public Handler getHandler() {
        return mHandler;
    }

    @Override
    public String getEuid() {
        return euid;
    }

    @Override
    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
        try {
            isblocked = BaseUtils.objectToBoolean(merchant.getIs_blocked());
            if (isblocked) {
                isblocked = true;
                Message blockMessage = mHandler
                        .obtainMessage(MessageWhat.IM_BOLACKED);
                mHandler.sendMessage(blockMessage);
            } else {
                Message backMerchant = mHandler
                        .obtainMessage(MessageWhat.BackMerchant);
                mHandler.sendMessage(backMerchant);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Merchant getMerchant() {
        return merchant;
    }

    private void registerNetWorkReceiver() {
        try {
            if (mConnectivityChangedReceiver == null) {
                mConnectivityChangedReceiver = new ConnectivtyChangedReceiver();
                UdeskChatActivity.this.registerReceiver(
                        mConnectivityChangedReceiver, new IntentFilter(
                                ConnectivityManager.CONNECTIVITY_ACTION));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unRegister() {
        try {
            if (mConnectivityChangedReceiver != null) {
                UdeskChatActivity.this
                        .unregisterReceiver(mConnectivityChangedReceiver);
                mConnectivityChangedReceiver = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void showStartOrStopAnaimaition(final ReceiveMessage info,
                                           final boolean isstart) {
        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (info == null) {
                        return;
                    }
                    for (int i = 0, count = mListView.getChildCount(); i < count; i++) {
                        View child = mListView.getChildAt(i);
                        if (child == null || child.getTag() == null
                                || !(child.getTag() instanceof AudioViewHolder)) {
                            continue;
                        }
                        AudioViewHolder holder = (AudioViewHolder) child.getTag();
                        ReceiveMessage msgTemp = holder.message;
                        holder.endAnimationDrawable();
                        if (msgTemp != info) {
                            msgTemp = info;
                            continue;
                        } else {
                            if (isstart) {
                                holder.startAnimationDrawable();
                            } else {
                                holder.endAnimationDrawable();
                            }

                        }

                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 点击播放录音及动画
    public void clickRecordFile(ReceiveMessage message) {

        try {
            if (mRecordFilePlay == null) {
                mRecordFilePlay = new RecordPlay(UdeskChatActivity.this);

            }
            if (mPlayCallback == null) {
                mPlayCallback = new RecordPlayCallback() {
                    @Override
                    public void onPlayComplete(ReceiveMessage message) {
                        showStartOrStopAnaimaition(message, false);
                        recycleVoiceRes();
                    }

                    @Override
                    public void onPlayStart(ReceiveMessage message) {
                        showStartOrStopAnaimaition(message, true);
                    }

                    @Override
                    public void onPlayPause(ReceiveMessage message) {
                        showStartOrStopAnaimaition(message, false);
                        recycleVoiceRes();
                    }

                    @Override
                    public void onPlayEnd(ReceiveMessage message) {
                        showStartOrStopAnaimaition(message, false);
                        recycleVoiceRes();// 新添加
                    }

                    @Override
                    public void endAnimation() {
                        if (mChatAdapter != null) {
                            List<ReceiveMessage> list = mChatAdapter.getList();
                            int size = list.size();
                            for (int i = 0; i < size; i++) {
                                ReceiveMessage message = list.get(i);
                                if (message.isPlaying) {
                                    showStartOrStopAnaimaition(message, false);
                                }
                            }
                        }

                    }

                };

            }
            mRecordFilePlay.click(message, mPlayCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //回收录音资源
    private void recycleVoiceRes() {
        try {
            if (mRecordFilePlay != null) {
                mRecordFilePlay.recycleRes();
                mRecordFilePlay.recycleCallback();
                mRecordFilePlay = null;
            }

            mPlayCallback = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //复制文本消息
    public void handleText(final ReceiveMessage message, View targetView) {
        try {
            String[] menuLabel = new String[]{getResources().getString(
                    R.string.udesk_copy)};
            UdeskMultiMenuHorizontalWindow menuWindow = new UdeskMultiMenuHorizontalWindow(
                    UdeskChatActivity.this);
            menuWindow.show(UdeskChatActivity.this, targetView, menuLabel,
                    new OnPopMultiMenuClick() {
                        @Override
                        public void onMultiClick(int MenuIndex) {
                            if (MenuIndex == 0) {
                                doCopy(UdeskUtil.objectToString(message.getContent()));
                            }
                        }
                    });
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private void doCopy(String content) {
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                android.content.ClipboardManager c = (android.content.ClipboardManager) UdeskChatActivity.this
                        .getSystemService(Activity.CLIPBOARD_SERVICE);
                c.setPrimaryClip(ClipData.newPlainText(null, content));
            } else {
                android.text.ClipboardManager c = (android.text.ClipboardManager) UdeskChatActivity.this
                        .getSystemService(Activity.CLIPBOARD_SERVICE);
                c.setText(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //重试发送消息
    public void retrySendMsg(ReceiveMessage message) {
        try {
            if (!UdeskUtils.isNetworkConnected(this)) {
                UdeskUtils.showToast(this,
                        getResources().getString(R.string.udesk_has_wrong_net));
                return;
            }
            if (mPresenter != null && message != null) {
                mPresenter.startRetryMsg(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //取消上传视频消息
    public void cancleSendVideoMsg(ReceiveMessage message) {
        try {
            if (mPresenter != null && message != null) {
                mPresenter.cancleUploadFile(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送广告的连接地址消息
    public void sentProduct(Products products) {
        if (mPresenter != null) {
            Products product = new Products();
            product.setActitve(true);
            product.setSendTime(System.currentTimeMillis());
            product.setProduct(products.getProduct());
            product.setUuid(UUID.randomUUID());
            product.setCreated_at(UdeskUtil.setCreateTime());
            mChatAdapter.getList().add(product);
            mPresenter.sendCommodity(product);
            mChatAdapter.notifyDataSetChanged();
            smoothScrollToPosition(mListView, mChatAdapter.getCount());
        }

    }

    public synchronized void downLoadVideo(ReceiveMessage message) {
        try {
            if (!UdeskUtils.isNetworkConnected(this)) {
                UdeskUtils.showToast(this,
                        getResources().getString(R.string.udesk_has_wrong_net));
                return;
            }
            if (mPresenter != null && message != null) {
                mPresenter.downFile(message, getApplicationContext(), UdeskConst.FileVideo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bottomoPannelBegginStatus() {
        try {
            mEmotionKeyboard.hideSoftInput();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTitlebar(Object title) {
        try {
            if (mTitlebar != null) {
                mTitlebar.getudeskStateImg().setVisibility(View.GONE);
                if (merchant != null) {
                    mTitlebar.setLeftTextSequence(UdeskUtil.objectToString(title));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更多控件的显示隐藏
     *
     * @param vis
     */
    private void setMoreVis(int vis) {
        if (View.VISIBLE == vis && UdeskConfig.isUseMore) {
            mMoreImg.setVisibility(vis);
        } else {
            mMoreImg.setVisibility(View.GONE);
        }
    }


    public void showVideoThumbnail(final ReceiveMessage info) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = UdeskUtil.getVideoThumbnail(UdeskUtil.objectToString(info.getContent()));
                    if (bitmap != null) {
                        UdeskUtil.saveBitmap(getApplicationContext(), UdeskUtil.objectToString(info.getContent()), bitmap);
                        Message message = getHandler().obtainMessage(
                                MessageWhat.ChangeVideoThumbnail);
                        message.obj = info.getUuid();
                        getHandler().sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void changeVideoThumbnail(String msgId) {
        try {
            if (!TextUtils.isEmpty(msgId) && mListView != null
                    && mChatAdapter != null) {
                for (int i = mListView.getChildCount() - 1; i >= 0; i--) {
                    View child = mListView.getChildAt(i);
                    if (child != null) {
                        if (mChatAdapter.changeVideoThumbnail(child, msgId)) {
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        try {
            if (mEmotionlayout.isShown() || mMoreLayout.isShown()) {
                mEmotionKeyboard.interceptBackPress();
            } else {
                finishAcitivty();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        try {
            XPermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMessage(ReceiveMessage msgInfo) {
        try {
            if (mChatAdapter != null && UdeskUtil.objectToString(msgInfo.getMerchant_euid()).equals(euid)) {
                mChatAdapter.addItem(msgInfo);
                smoothScrollToPosition(mListView, mChatAdapter.getCount());
            }
            if (msgInfo.getEvent_name().equals(UdeskConst.UdeskXmppEventName.INVITE_SURVEY)) {
                Message messge = getHandler().obtainMessage(
                        MessageWhat.surveyNotify);
                getHandler().sendMessage(messge);
            }else if (msgInfo.getEvent_name().equals(UdeskConst.UdeskXmppEventName.FEEDBACK_FORM)){
                Message messge = getHandler().obtainMessage(
                        MessageWhat.GET_FEEDBACK);
                getHandler().sendMessage(messge);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPresenter != null) {
            Glide.with(this).pauseRequests();
            mPresenter.setMessageRead();
        }
        if (isFinishing()) {
            cleanSource();
        }

    }

    @Override
    protected void onStop() {

        recycleVoiceRes();
        super.onStop();
    }

    private boolean isPermmitSurvy = true;

    //点击评价入口
    private void clickSurvy() {

        if (mPresenter != null && !TextUtils.isEmpty(euid) && isPermmitSurvy && merchant != null) {
            setIsPermmitSurvy(false);
            mPresenter.getHasSurvey(null);
        } else {
            Toast.makeText(UdeskChatActivity.this,
                    getResources().getString(R.string.udesk_survey_error),
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void setIsPermmitSurvy(boolean isPermmit) {
        try {
            isPermmitSurvy = isPermmit;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSurvyOption(SurveyOptionsModel model) {
        this.surveyOptionsModel = model;
        if (survyItem == null) {
            survyItem = new FunctionMode(getString(R.string.survy), UdeskConst.UdeskFunctionFlag.Udesk_Survy, R.drawable.udesk_survy_normal);
        }
        if (surveyOptionsModel.getEnabled() && surveyOptionsModel.getCustomer_invite()) {
//            btnSurvy.setVisibility(View.VISIBLE);
            functionItems.remove(survyItem);
            functionItems.add(survyItem);
        } else {
//            btnSurvy.setVisibility(View.GONE);
            functionItems.remove(survyItem);
        }
        udeskFunctionAdapter.setFunctionItems(functionItems);
        udeskFunctionAdapter.notifyDataSetChanged();
    }

    @Override
    public SurveyOptionsModel getSurvyOption() {
        return this.surveyOptionsModel;
    }

    //启动满意度调查
    private void toLuanchSurveyView(SurveyOptionsModel surveyOptions) {
        try {
            setIsPermmitSurvy(true);
            if (surveyOptions.getOptions() == null || surveyOptions.getOptions().isEmpty()
                    || surveyOptions.getType().isEmpty()) {
                UdeskUtils.showToast(this,
                        getString(R.string.udesk_no_set_survey));
                return;
            }
            showSurveyPopWindow(surveyOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    WindowManager.LayoutParams params;
    UdeskSurvyPopwindow udeskSurvyPopwindow;

    private void showSurveyPopWindow(final SurveyOptionsModel surveyOptions) {

        if (udeskSurvyPopwindow != null && udeskSurvyPopwindow.isShowing()) {
            return;
        }

        udeskSurvyPopwindow = new UdeskSurvyPopwindow(this, surveyOptions, new UdeskSurvyPopwindow.SumbitSurvyCallBack() {

            @Override
            public void sumbitSurvyCallBack(String optionId, String show_type, String survey_remark, String tags) {
                SurvyOption.VoteSurvy voteSurvy = new SurvyOption.VoteSurvy();
                voteSurvy.setOption_id(optionId);
                voteSurvy.setType(show_type);
                if (!TextUtils.isEmpty(survey_remark)) {
                    voteSurvy.setSurvey_remark(survey_remark);
                }
                if (!TextUtils.isEmpty(tags)) {
                    voteSurvy.setTags(tags);
                }
                SurvyOption survyOption = new SurvyOption(voteSurvy);
                mPresenter.putIMSurveyResult(survyOption);
            }
        });
        udeskSurvyPopwindow.showAtLocation(findViewById(R.id.udesk_im_content), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        params = getWindow().getAttributes();
        params.alpha = 0.7f;
        getWindow().setAttributes(params);
        udeskSurvyPopwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                params = getWindow().getAttributes();
                params.alpha = 1f;
                getWindow().setAttributes(params);
            }
        });
    }


    private void finishAcitivty() {
        //后台勾选开启后，对于同一个对话，用户多次进入，点击返回离开，若没有进行过满意度调查，
        // 则返回点击后均弹出满意度调查窗口，若已经有满意度调查结果，则返回不再发起调查都关闭.

        if (surveyOptionsModel == null || !surveyOptionsModel.isAfter_session()) {
            finish();
            return;
        }

        surveyOptionsModel.setAfter_session(false);
        try {

            if (mPresenter != null && !TextUtils.isEmpty(euid) && isPermmitSurvy && merchant != null) {
                mPresenter.getHasSurvey(new ChatActivityPresenter.IUdeskHasSurvyCallBack() {
                    @Override
                    public void hasSurvy(boolean hasSurvy) {

                        if (hasSurvy) {
                            finish();
                        } else {
                            //未评价，可以发起评价
                            if (getSurvyOption() != null) {
                                Message messge = getHandler().obtainMessage(
                                        MessageWhat.surveyNotify);
                                getHandler().sendMessage(messge);
                            } else {
                                finish();
                            }
                        }
                    }
                });
            } else {
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }


    @Override
    protected void onDestroy() {
        cleanSource();
        super.onDestroy();

    }

    private void cleanSource() {
        EventBus.getDefault().unregister(this);
        if (isDestroyed) {
            return;
        }
        // 回收资源
        isDestroyed = true;
        if (mHandler != null) {
            mHandler.removeCallbacks(myRunnable);
        }
        try {
            if (popWindow != null && popWindow.isShowing()) {
                popWindow.dismiss();
            }
            XPermissionUtils.destory();
            functionItems.clear();
            recycleVoiceRes();
            unRegister();
            if (mPresenter != null) {
                mPresenter.queue();
                mPresenter.unBind();
                mPresenter = null;
            }
            if (mEmotionKeyboard != null) {
                mEmotionKeyboard.destory();
                mEmotionKeyboard = null;
            }
            if (mBtnAudio != null) {
                mBtnAudio.setRecordingListener(null);
                mBtnAudio.destoryRelease();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
