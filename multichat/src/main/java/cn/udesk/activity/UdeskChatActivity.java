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
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.activity.MessageAdatper.AudioViewHolder;
import cn.udesk.adapter.UDEmojiAdapter;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskConfig;
import cn.udesk.model.Merchant;
import cn.udesk.model.SendMsgResult;
import cn.udesk.muchat.bean.Products;
import cn.udesk.muchat.bean.ReceiveMessage;
import cn.udesk.permission.RequestCode;
import cn.udesk.permission.XPermissionUtils;
import cn.udesk.presenter.ChatActivityPresenter;
import cn.udesk.presenter.IChatActivityView;
import cn.udesk.voice.RecordFilePlay;
import cn.udesk.voice.RecordPlay;
import cn.udesk.voice.RecordPlayCallback;
import cn.udesk.voice.RecordStateCallback;
import cn.udesk.voice.RecordTouchListener;
import cn.udesk.widget.HorVoiceView;
import cn.udesk.widget.UDPullGetMoreListView;
import cn.udesk.widget.UdeskMultiMenuHorizontalWindow;
import cn.udesk.widget.UdeskMultiMenuHorizontalWindow.OnPopMultiMenuClick;
import cn.udesk.widget.UdeskTitleBar;
import cn.udesk.xmpp.ConnectManager;
import udesk.core.utils.UdeskUtils;

public class UdeskChatActivity extends Activity implements IChatActivityView,
        OnClickListener, OnTouchListener, OnLongClickListener,
        OnItemClickListener, RecordStateCallback, HorVoiceView.UdeskTimeCallback {

    private Button sendBtn;
    private EditText mInputEditView;
    private View showEmjoImg;//表情选择图片，用户可根据自己的需求自行设置。
    private GridView emjoGridView;
    private ImageView audioPop;
    private HorVoiceView mHorVoiceView;
    private TextView udesk_audio_tips;
    private View emojisPannel;
    private View btnPhoto, btnCamera;
    private View showVoiceImg;
    private View audioPanel;
    private View audioCancle;
    private UDEmojiAdapter mEmojiAdapter;
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

    private MyHandler mHandler;
    private ChatActivityPresenter mPresenter;
    private BroadcastReceiver mConnectivityChangedReceiver = null;
    private boolean hasAddCommodity = false;

    //咨询对象
    public View commodity_rl;
    public ImageView commityThumbnail;
    public TextView commityTitle;
    public TextView commitySubTitle;
    public TextView commityLink;

    private boolean isDestroyed = false;

    public static class MessageWhat {

        public static final int BackMerchant = 1;
        public static final int refreshAdapter = 2;
        public static final int changeImState = 3;
        public static final int onNewMessage = 4;
        public static final int RECORD_ERROR = 5;
        public static final int RECORD_Too_Short = 6;
        public static final int UPDATE_VOCIE_STATUS = 7;
        public static final int recordllegal = 8;


    }

    class ConnectivtyChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent
                        .getAction()))
                    return;
                boolean bNetWorkAvailabl = UdeskUtils.isNetworkConnected(context);
                if (bNetWorkAvailabl) {
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
                        activity.setTitlebar();
                        break;
                    case MessageWhat.refreshAdapter:
                        if (activity.mChatAdapter != null) {
                            List<ReceiveMessage> messages = (List<ReceiveMessage>) msg.obj;
                            int arg1 = msg.arg1;
                            if (UdeskSDKManager.getInstance().getProducts() != null && !activity.hasAddCommodity) {
                                activity.hasAddCommodity = true;
                                activity.showCommityThunbnail(UdeskSDKManager.getInstance().getProducts());
                                activity.mPresenter.sendCommodity(UdeskSDKManager.getInstance().getProducts());
                            }
                            if (messages != null) {
                                int index = messages.size();
                                if (arg1 > 0) {
                                    activity.mChatAdapter.listAddItems(messages, true);
                                } else {
                                    activity.mChatAdapter.listAddItems(messages, false);
                                }
                                activity.mListView.smoothScrollToPosition(index + 1);
                            }
                        }
                        break;
                    case MessageWhat.changeImState:
                        SendMsgResult sendMsgResult = (SendMsgResult) msg.obj;
                        activity.changeImState(sendMsgResult);
                        break;
                    case MessageWhat.onNewMessage:
                        ReceiveMessage msgInfo = (ReceiveMessage) msg.obj;
                        if (activity.mChatAdapter != null) {
                            activity.mChatAdapter.addItem(msgInfo);
                            activity.mListView.smoothScrollToPosition(activity.mChatAdapter.getCount());
                        }
                        break;
                    case MessageWhat.RECORD_ERROR:
                        UdeskUtils.showToast(activity, activity.getResources()
                                .getString(R.string.udesk_im_record_error));
                        break;
                    case MessageWhat.RECORD_Too_Short:
                        UdeskUtils.showToast(activity, activity.getResources()
                                .getString(R.string.udesk_label_hint_too_short));
                        break;
                    case MessageWhat.UPDATE_VOCIE_STATUS:
                        activity.updateRecordStatus(msg.arg1);
                        break;
                    case MessageWhat.recordllegal:
                        UdeskUtils.showToast(activity, activity.getResources()
                                .getString(R.string.udesk_im_record_error));
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            initView();
            mPresenter.getMessages("");
            mPresenter.getMerchantInfo();
            mPresenter.setMessageRead();
            checkConnect();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }

    @Override
    public synchronized void checkConnect() {
        if (UdeskSDKManager.getInstance().getInitMode() != null) {
            if (!ConnectManager.getInstance().isConnection()) {
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
                sentProduct(products);
            }
        });
    }

    private void initView() {
        try {
            commodity_rl = findViewById(R.id.commodity_rl);
            commityThumbnail = (ImageView) findViewById(R.id.udesk_im_commondity_thumbnail);
            commityTitle = (TextView) findViewById(R.id.udesk_im_commondity_title);
            commitySubTitle = (TextView) findViewById(R.id.udesk_im_commondity_subtitle);
            commityLink = (TextView) findViewById(R.id.udesk_im_commondity_link);
            commodity_rl.setVisibility(View.GONE);
            commityLink.setVisibility(View.GONE);

            sendBtn = (Button) findViewById(R.id.udesk_bottom_send);
            sendBtn.setOnClickListener(this);
            mInputEditView = (EditText) findViewById(R.id.udesk_bottom_input);
            mInputEditView.setOnTouchListener(this);
            emojisPannel = findViewById(R.id.udesk_bottom_emojis);
            showEmjoImg = findViewById(R.id.udesk_bottom_show_emoji);
            showEmjoImg.setOnClickListener(this);
            mEmojiAdapter = new UDEmojiAdapter(this);
            emjoGridView = (GridView) findViewById(R.id.udesk_bottom_emoji_pannel);
            emjoGridView.setAdapter(mEmojiAdapter);
            emjoGridView.setOnItemClickListener(this);
            btnCamera = findViewById(R.id.udesk_bottom_option_camera);
            btnCamera.setOnClickListener(this);
            if (UdeskConfig.isUsecamera) {
                btnCamera.setVisibility(View.VISIBLE);
            } else {
                btnCamera.setVisibility(View.GONE);
            }
            btnPhoto = findViewById(R.id.udesk_bottom_option_photo);
            btnPhoto.setOnClickListener(this);
            if (UdeskConfig.isUsephoto) {
                btnPhoto.setVisibility(View.VISIBLE);
            } else {
                btnPhoto.setVisibility(View.GONE);
            }
            mListView = (UDPullGetMoreListView) findViewById(R.id.udesk_conversation);
            showVoiceImg = findViewById(R.id.udesk_bottom_voice_rl);
            showVoiceImg.setOnClickListener(this);
            if (UdeskConfig.isUseVoice) {
                showVoiceImg.setVisibility(View.VISIBLE);
            } else {
                showVoiceImg.setVisibility(View.GONE);
            }
            audioPanel = findViewById(R.id.udesk_bottom_audios);
            mHorVoiceView = (HorVoiceView) findViewById(R.id.udesk_horvoiceview);
            udesk_audio_tips = (TextView) findViewById(R.id.udesk_audio_tips);
            audioCancle = findViewById(R.id.udesk_audio_cancle_image);
            audioPop = (ImageView) findViewById(R.id.udesk_audio_pop);
            setListView();
            UdeskSDKManager.getInstance().setCustomerOffline(true);

            mInputEditView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    setUdeskAudioPanelVis(View.GONE);
                    setUdeskEmojisPannel(View.GONE);
                    emjoGridView.setVisibility(View.GONE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setListView() {
        try {
            mChatAdapter = new MessageAdatper(this);
            mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            mListView.setAdapter(mChatAdapter);
            mListView
                    .setOnRefreshListener(new UDPullGetMoreListView.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            loadHistoryRecords();
                        }
                    });

            mListView.setRecyclerListener(new AbsListView.RecyclerListener() {
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
    protected void onResume() {
        super.onResume();
        try {
            Glide.with(this).resumeRequests();
            registerNetWorkReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        try {
            if (v == mInputEditView && event.getAction() == MotionEvent.ACTION_DOWN) {
                setUdeskAudioPanelVis(View.GONE);
                setUdeskEmojisPannel(View.GONE);
                emjoGridView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public void onClick(View v) {
        try {
            //检查是否处在可发消息的状态
            if (!isShowNotSendMsg()) {
                UdeskUtils.hideSoftKeyboard(this, mInputEditView);
                return;
            }
            if (R.id.udesk_bottom_send == v.getId()) { //发送文本消息
                if (TextUtils.isEmpty(mInputEditView.getText().toString())) {
                    UdeskUtils.showToast(UdeskChatActivity.this,
                            getString(R.string.udesk_send_message_empty));
                    return;
                }
                mPresenter.sendTxtMessage();
            } else if (R.id.udesk_bottom_show_emoji == v.getId()) { // 显示表情面板

                if (emojisPannel.getVisibility() == View.VISIBLE) {
                    bottomoPannelBegginStatus();
                } else {
                    bottomoPannelBegginStatus();
                    setUdeskEmojisPannel(View.VISIBLE);
                }
                setUdeskEditClickabled(mInputEditView);
                UdeskUtils.hideSoftKeyboard(this, mInputEditView);
            } else if (R.id.udesk_bottom_option_photo == v.getId()) {  //选择本地的图片
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

            } else if (R.id.udesk_bottom_option_camera == v.getId()) { // 拍照发送图片
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
            } else if (R.id.udesk_bottom_voice_rl == v.getId()) {  //录音 发送语音
                if (Build.VERSION.SDK_INT < 23) {
                    if (audioPanel.getVisibility() == View.VISIBLE) {
                        bottomoPannelBegginStatus();
                    } else {
                        bottomoPannelBegginStatus();
                        initAduioPannel();
                    }
                } else {
                    XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.AUDIO,
                            new String[]{Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            new XPermissionUtils.OnPermissionListener() {
                                @Override
                                public void onPermissionGranted() {
                                    if (audioPanel.getVisibility() == View.VISIBLE) {
                                        bottomoPannelBegginStatus();
                                    } else {
                                        bottomoPannelBegginStatus();
                                        initAduioPannel();
                                    }
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
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }

    //电话呼叫
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

    // 长按录音
    @Override
    public boolean onLongClick(View v) {
        try {
            if (v.getId() == R.id.udesk_audio_pop) {
                if (!UdeskUtils.checkSDcard()) {
                    Toast.makeText(this,
                            getResources().getString(R.string.udesk_label_no_sd),
                            Toast.LENGTH_LONG).show();
                    return false;
                }

                if (Build.VERSION.SDK_INT < 23) {
                    recordVoiceStart();
                } else {
                    XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.AUDIO,
                            new String[]{Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            new XPermissionUtils.OnPermissionListener() {
                                @Override
                                public void onPermissionGranted() {
                                    recordVoiceStart();
                                }

                                @Override
                                public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                    Toast.makeText(UdeskChatActivity.this,
                                            getResources().getString(R.string.aduido_denied),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        try {
            if (parent == emjoGridView) {
                if (mPresenter != null) {
                    mPresenter.clickEmoji(id, mEmojiAdapter.getCount(),
                            mEmojiAdapter.getItem((int) id));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
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

            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
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
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, SELECT_IMAGE_ACTIVITY_REQUEST_CODE);
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


    //开始录音
    private void recordVoiceStart() {
        try {
            if (mRecordFilePlay != null) {
                showStartOrStopAnaimaition(
                        mRecordFilePlay.getPlayAduioMessage(), false);
                recycleVoiceRes();
            }
            setAudiotipsVis(View.GONE);
            setHorVoiceViewVis(View.VISIBLE);
            setAudioCancleViewVis(View.VISIBLE);

            if (audioCancle != null) {
                audioPop.setOnTouchListener(new RecordTouchListener(this,
                        UdeskChatActivity.this, audioCancle));
                if (mPresenter != null) {
                    // 开始录音
                    mPresenter.recordStart();
                    if (mHorVoiceView != null) {
                        mHorVoiceView.startRecording(this);
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
    public Handler getHandler() {
        return mHandler;
    }


    @Override
    public void refreshInputEmjio(String s) {
        try {
            if (UDEmojiAdapter.replaceEmoji(this, s,
                    (int) mInputEditView.getTextSize()) != null) {
                mInputEditView.setText(UDEmojiAdapter.replaceEmoji(this, s,
                        (int) mInputEditView.getTextSize()));
            } else {
                mInputEditView.setText(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<String> getEmotionStringList() {
        List<String> emotionList = new ArrayList<String>();
        try {
            int emojiSum = mEmojiAdapter.getCount();
            for (int i = 0; i < emojiSum; i++) {
                if (mEmojiAdapter.getItem(i) != null) {
                    emotionList.add(mEmojiAdapter.getItem(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return emotionList;
    }

    @Override
    public void onTimeOver() {
        if (mPresenter != null) {
            mPresenter.doRecordStop(false);
        }
        initAduioPannel();
    }

    @Override
    public void readyToCancelRecord() {

        try {
            setHorVoiceViewVis(View.GONE);
            setAudiotipsVis(View.VISIBLE);
            setAudiotiptext(getString(R.string.udesk_voice_cancle));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doCancelRecord() {
        try {
            if (mPresenter != null) {
                mPresenter.doRecordStop(true);

            }
            if (mHorVoiceView != null) {
                mHorVoiceView.stopRecording();
            }
            initAduioPannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readyToContinue() {
        try {
            setHorVoiceViewVis(View.VISIBLE);
            setAudiotipsVis(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void endRecord() {
        try {
            if (mPresenter != null) {
                mPresenter.doRecordStop(false);
            }
            if (mHorVoiceView != null) {
                mHorVoiceView.stopRecording();
            }
            initAduioPannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRecordSuccess(String filePath, long duration) {
        if (mPresenter != null) {
            mPresenter.sendRecordAudioMsg(filePath, duration);
        }
    }

    @Override
    public String getEuid() {
        return euid;
    }

    @Override
    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
        try {
            Message backMerchant = mHandler
                    .obtainMessage(MessageWhat.BackMerchant);
            mHandler.sendMessage(backMerchant);
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
                    public void onPlayComplete(ReceiveMessage message) {
                        showStartOrStopAnaimaition(message, false);
                        recycleVoiceRes();
                    }

                    public void onPlayStart(ReceiveMessage message) {
                        showStartOrStopAnaimaition(message, true);
                    }

                    public void onPlayPause(ReceiveMessage message) {
                        showStartOrStopAnaimaition(message, false);
                        recycleVoiceRes();
                    }

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

    private void updateRecordStatus(int status) {
        try {
            if (mHorVoiceView != null) {
                mHorVoiceView.addElement(status * 10);
            }
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
            if (mPresenter != null && message != null) {
                mPresenter.startRetryMsg(message);
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
            mChatAdapter.getList().add(product);
            mPresenter.sendCommodity(product);
            mChatAdapter.notifyDataSetChanged();
        }

    }


    private void bottomoPannelBegginStatus() {
        try {
            UdeskUtils.hideSoftKeyboard(this, mInputEditView);
            setUdeskAudioPanelVis(View.GONE);
            setUdeskEmojisPannel(View.GONE);
            emjoGridView.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initAduioPannel() {
        try {
            setUdeskAudioPanelVis(View.VISIBLE);
            setHorVoiceViewVis(View.GONE);
            setAudioCancleViewVis(View.GONE);
            setAudiotipsVis(View.VISIBLE);
            setAudiotiptext(getString(R.string.udesk_voice_init));
            audioPop.setOnLongClickListener(this);
            audioPop.setOnTouchListener(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void setTitlebar() {
        try {
            if (mTitlebar != null) {
                mTitlebar.getudeskStateImg().setVisibility(View.GONE);
                if (merchant != null) {
                    mTitlebar.setLeftTextSequence(UdeskUtil.objectToString(merchant.getName()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUdeskAudioPanelVis(int vis) {
        try {
            audioPanel.setVisibility(vis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setHorVoiceViewVis(int vis) {
        try {
            mHorVoiceView.setVisibility(vis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAudiotipsVis(int vis) {
        try {
            udesk_audio_tips.setVisibility(vis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAudioCancleViewVis(int vis) {
        try {
            audioCancle.setVisibility(vis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAudiotiptext(String s) {
        try {
            udesk_audio_tips.setText(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUdeskEmojisPannel(int vis) {
        try {
            emojisPannel.setVisibility(vis);
            emjoGridView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUdeskEditClickabled(EditText editText) {
        try {
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            editText.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        finishAcitivty();
    }


    private void finishAcitivty() {
        finish();
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
            if (mChatAdapter != null) {
                mChatAdapter.addItem(msgInfo);
                mListView.smoothScrollToPosition(mChatAdapter.getCount());
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
        try {
            recycleVoiceRes();
            unRegister();
            if (mPresenter != null) {
                mPresenter.unBind();
                mPresenter = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
