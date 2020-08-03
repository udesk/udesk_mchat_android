package cn.udesk.presenter;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import cn.udesk.JsonUtils;
import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.activity.UdeskChatActivity.MessageWhat;
import cn.udesk.config.UdeskConfig;
import cn.udesk.model.InitMode;
import cn.udesk.model.Merchant;
import cn.udesk.model.ProductMessage;
import cn.udesk.model.SendMsgResult;
import cn.udesk.model.SurveyOptionsModel;
import cn.udesk.muchat.HttpCallBack;
import cn.udesk.muchat.HttpDownloadCallBack;
import cn.udesk.muchat.HttpFacade;
import cn.udesk.muchat.UdeskLibConst;
import cn.udesk.muchat.bean.AliBean;
import cn.udesk.muchat.bean.ExtrasInfo;
import cn.udesk.muchat.bean.Products;
import cn.udesk.muchat.bean.ReceiveMessage;
import cn.udesk.muchat.bean.SendMessage;
import cn.udesk.muchat.bean.SurvyOption;
import cn.udesk.voice.VoiceRecord;
import cn.udesk.xmpp.Concurrents;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import retrofit2.Response;
import udesk.core.utils.UdeskIdBuild;
import udesk.core.utils.UdeskUtils;

public class ChatActivityPresenter {

    private IChatActivityView mChatView;
    private VoiceRecord mVoiceRecord = null;
    private String mRecordTmpFile = "";
    int failureCount;
    private OkHttpClient okHttpClient;
    private Map<Object, Call> concurrentHashMap = new ConcurrentHashMap();

    public ChatActivityPresenter(IChatActivityView chatview) {
        this.mChatView = chatview;

    }

    public void unBind() {
        mChatView = null;
    }


    /**
     * 收到新消息
     */
    public void onNewMessage(ReceiveMessage msgInfo) {

        try {
            if (mChatView != null && mChatView.getHandler() != null) {
                Message messge = mChatView.getHandler().obtainMessage(
                        MessageWhat.onNewMessage);
                messge.obj = msgInfo;
                mChatView.getHandler().sendMessage(messge);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //发送商品链接广告
    public void sendCommodity(final Products products) {
        InitMode initMode = UdeskSDKManager.getInstance().getInitMode();
        if (initMode != null) {
            HttpFacade.getInstance().sendProducts(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                    UdeskUtil.objectToString(initMode.getIm_password())), mChatView.getEuid(), products, new HttpCallBack() {
                @Override
                public void onSuccess(String message) {
                    if (UdeskLibConst.isDebug) {
                        Log.i("udesk", "sendCommodity result =" + message);
                    }
                }

                @Override
                public void onFail(Throwable message) {
                    failureCount++;
                    if (failureCount < 3) {
                        sendCommodity(products);
                    }
                    if (message instanceof ConnectTimeoutException) {
                        if (UdeskLibConst.isDebug) {
                            Log.i("udesk", "sendCommodity result =" + mChatView.getContext().getString(R.string.time_out));
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

    }


    //发送文本消息
    public void sendTxtMessage() {
        try {
            if (!TextUtils.isEmpty(mChatView.getInputContent().toString().trim())) {
                sendTxtMessage(mChatView.getInputContent().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //封装发送文本消息
    public void sendTxtMessage(String msgString) {
        try {
            ReceiveMessage receiveMessage = buildSendMessage(UdeskConst.ChatMsgTypeString.TYPE_TEXT, msgString);
            mChatView.clearInputContent();
            onNewMessage(receiveMessage);
            createMessage(UdeskUtil.objectToString(receiveMessage.getId()), msgString, UdeskConst.ChatMsgTypeString.TYPE_TEXT, receiveMessage.getExtras());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //封装商品消息
    public void sendProductMessage(ProductMessage mProduct) {
        if (mProduct == null) {
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(mProduct.getName())) {
                jsonObject.put("name", mProduct.getName());
            }
            if (!TextUtils.isEmpty(mProduct.getUrl())) {
                jsonObject.put("url", mProduct.getUrl());
            }
            if (!TextUtils.isEmpty(mProduct.getImgUrl())) {
                jsonObject.put("imgUrl", mProduct.getImgUrl());
            }

            List<ProductMessage.ParamsBean> params = mProduct.getParams();
            if (params != null && params.size() > 0) {
                JSONArray jsonsArray = new JSONArray();
                for (ProductMessage.ParamsBean paramsBean : params) {
                    JSONObject param = new JSONObject();
                    param.put("text", paramsBean.getText());
                    param.put("color", paramsBean.getColor());
                    param.put("fold", paramsBean.isFold());
                    param.put("break", paramsBean.isBreakX());
                    param.put("size", paramsBean.getSize());
                    jsonsArray.put(param);
                }

                jsonObject.put("params", jsonsArray);
            }

            ReceiveMessage receiveMessage = buildSendMessage(UdeskConst.ChatMsgTypeString.TYPE_PRODUCT, jsonObject.toString());
            onNewMessage(receiveMessage);
            createMessage(UdeskUtil.objectToString(receiveMessage.getId()), mProduct,
                    UdeskConst.ChatMsgTypeString.TYPE_PRODUCT, receiveMessage.getExtras());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 发送录音信息
    public void sendRecordAudioMsg(String audiopath, long duration) {
        try {
            ReceiveMessage receiveMessage = buildSendMessage(UdeskConst.ChatMsgTypeString.TYPE_AUDIO, "", audiopath);
            duration = duration / 1000 + 1;
            ExtrasInfo info = new ExtrasInfo();
            info.setDuration(duration);
            receiveMessage.setExtras(info);
            onNewMessage(receiveMessage);
            upLoadFileQ(audiopath, receiveMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //发送图片消息
    public void sendBitmapMessage(String photoPath) {
        try {
            if (TextUtils.isEmpty(photoPath)) {
                UdeskUtils.showToast(mChatView.getContext(), mChatView.getContext().getString(R.string.udesk_upload_img_error));
                return;
            }
            ReceiveMessage receiveMessage = buildSendMessage(UdeskConst.ChatMsgTypeString.TYPE_IMAGE, "", photoPath);
            onNewMessage(receiveMessage);
            upLoadFileQ(photoPath, receiveMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //发送小视频
    public void sendVideoMessage(String videoPath) {
        try {
            if (TextUtils.isEmpty(videoPath)) {
                UdeskUtils.showToast(mChatView.getContext(), mChatView.getContext().getString(R.string.udesk_upload_img_error));
                return;
            }
            if (UdeskUtil.isAndroidQ()) {
                videoPath = UdeskUtil.getFilePathQ(mChatView.getContext(), videoPath);
            }
            ReceiveMessage receiveMessage = buildSendMessage(UdeskConst.ChatMsgTypeString.TYPE_VIDEO, "", videoPath);
            if (!UdeskUtils.isNetworkConnected(mChatView.getContext())){
                receiveMessage.setSendFlag(UdeskConst.SendFlag.RESULT_RETRY);
            }
            onNewMessage(receiveMessage);
            upLoadFileQ(videoPath, receiveMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ReceiveMessage buildSendMessage(String msgtype, String content) {
        return buildSendMessage(msgtype, content, "");
    }

    //构建消息模型
    public ReceiveMessage buildSendMessage(String msgtype, String content, String locationPath) {
        ReceiveMessage msg = new ReceiveMessage();
        try {
            msg.setContent_type(msgtype);
            msg.setId(UdeskIdBuild.buildMsgId());
            msg.setMerchant_euid(mChatView.getEuid());
            msg.setDirection(UdeskConst.ChatMsgDirection.Send);
            msg.setSendFlag(UdeskConst.SendFlag.RESULT_SEND);
            msg.setReadFlag(UdeskConst.ChatMsgReadFlag.read);
            msg.setContent(content);
            msg.setLocalPath(locationPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    private void upLoadFileQ(final String filePath, final ReceiveMessage receiveMessage) {
        try {

            InitMode initMode = UdeskSDKManager.getInstance().getInitMode();
            if (initMode != null) {

                HttpFacade.getInstance().getAliInfo(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                        UdeskUtil.objectToString(initMode.getIm_password())), new HttpCallBack() {
                    @Override
                    public void onSuccess(final String message) {

                        if (UdeskLibConst.isDebug) {
                            Log.i("udesk", "getAliInfo result =" + message);
                        }
                        String fileName = "";
                        String path = filePath;
                        if (UdeskUtil.isAndroidQ()) {
                            path = UdeskUtil.getFilePathQ(mChatView.getContext().getApplicationContext(), filePath);
                            fileName = UdeskUtil.getFileName(mChatView.getContext(), path);

                        } else {
                            File file = new File(path);
                            fileName = file.getName();
                        }
                        final AliBean aliBean = JsonUtils.parseAlInfo(message);
                        final String alikey = UdeskUtil.objectToString(aliBean.getPrefix()) + "/" + fileName;
                        String endpoint = UdeskUtil.objectToString(aliBean.getEndpoint());
                        if (!endpoint.contains("http")) {
                            endpoint = "https://" + UdeskUtil.objectToString(aliBean.getBucket()) + "." + endpoint;
                        }
                        final String uploadurl = endpoint + "/" + alikey;

                        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                        builder.addFormDataPart("OSSAccessKeyId", UdeskUtil.objectToString(aliBean.getAccess_id()));
                        builder.addFormDataPart("bucket", UdeskUtil.objectToString(aliBean.getBucket()));
                        builder.addFormDataPart("policy", UdeskUtil.objectToString(aliBean.getPolicy_Base64()));
                        builder.addFormDataPart("Signature", UdeskUtil.objectToString(aliBean.getSignature()));
                        builder.addFormDataPart("key", alikey);
                        builder.addFormDataPart("expire", UdeskUtil.objectToString(aliBean.getExpire_at()));
                        addCustomRequestBody(mChatView.getContext().getApplicationContext(), receiveMessage,builder, path, fileName);
                        Call call = getCall(endpoint, receiveMessage,builder);
                        call.enqueue(new okhttp3.Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d("OkHttp", "onFailure=====");
                                concurrentHashMap.remove(receiveMessage.getId());
                                sendMessageResult(UdeskUtil.objectToString(receiveMessage.getId()), UdeskConst.SendFlag.RESULT_FAIL, new SendMsgResult());
                            }

                            @Override
                            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                                Log.d("OkHttp", "onResponse=====" + response.code());
                                concurrentHashMap.remove(receiveMessage.getId());
                                createMessage(UdeskUtil.objectToString(receiveMessage.getId()), uploadurl, UdeskUtil.objectToString(receiveMessage.getContent_type()), receiveMessage.getExtras());
                            }
                        });
//
                    }

                    @Override
                    public void onFail(Throwable message) {

                    }

                    @Override
                    public void onSuccessFail(String message) {
                        if (UdeskLibConst.isDebug) {
                            Log.i("udesk", "getAliInfo result =" + message);
                        }
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }

    private Call getCall(String url,ReceiveMessage messageInfo, MultipartBody.Builder builder) {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder().build();
        }
        RequestBody requestBody = builder.build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        concurrentHashMap.put(messageInfo.getId(), call);
        return call;
    }
    public void cancleUploadFile(ReceiveMessage message) {
        try {
            Call call = concurrentHashMap.get(message.getId());
            if (call != null ){
                call.cancel();
            }
            concurrentHashMap.remove(message.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void addCustomRequestBody(Context context, final ReceiveMessage receiveMessage, MultipartBody.Builder builder, String filePath, String fileName) {
        try {
            builder.addFormDataPart("file", URLEncoder.encode(fileName, "UTF-8"), createCustomRequestBody(context, filePath, new ProgressListener() {
                int lastProgress = 0;
                @Override
                public void onProgress(long totalBytes, long remainingBytes, boolean done) {
                    try {
                        float percent = (totalBytes - remainingBytes) * 100 / totalBytes;
                        int progress = Float.valueOf(percent).intValue();
                        if (done) {
                            progress = 100;
                        }
                        if (progress != lastProgress) {
                            lastProgress = progress;
                            if (mChatView != null && mChatView.getHandler() != null) {
                                Message message = mChatView.getHandler().obtainMessage(
                                        MessageWhat.ChangeFielProgress);
                                message.obj = receiveMessage.getId();
                                message.arg1 = progress;
                                mChatView.getHandler().sendMessage(message);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public RequestBody createCustomRequestBody(Context context, String filePath, final ProgressListener listener) {

        try {
            FileInputStream fileInputStream = null;
            long contentLength = 0;
            if (UdeskUtil.isAndroidQ()) {
                AssetFileDescriptor assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(Uri.parse(UdeskUtil.getFilePathQ(context, filePath)), "r");
                if (assetFileDescriptor != null) {
                    contentLength = assetFileDescriptor.getLength();
                    fileInputStream = assetFileDescriptor.createInputStream();
                }
            } else {
                File file = new File(filePath);
                contentLength = file.length();
                fileInputStream = new FileInputStream(file);
            }
            final FileInputStream fis = fileInputStream;
            final long length = contentLength;
            return new RequestBody() {
                @Override
                public MediaType contentType() {
                    return MediaType.parse("application/octet-stream");
                }

                @Override
                public long contentLength() {
                    return length;
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    Source source;
                    try {
                        source = Okio.source(fis);
                        Buffer buf = new Buffer();
                        Long remaining = contentLength();
                        for (long readCount; (readCount = source.read(buf, 2048)) != -1; ) {
                            sink.write(buf, readCount);
                            listener.onProgress(contentLength(), remaining -= readCount, remaining == 0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //下载视频
    public void downVideo(final ReceiveMessage info) {
        try {
            final File file = new File(UdeskUtil.getDirectoryPath(mChatView.getContext(), UdeskConst.FileVideo),
                    UdeskUtil.getFileName(UdeskUtil.objectToString(info.getContent()), UdeskConst.FileVideo));

            HttpFacade.getInstance().downloadFile(UdeskUtil.uRLEncoder(UdeskUtil.objectToString(info.getContent())),info, new HttpDownloadCallBack() {

                @Override
                public void onStart(Response<ResponseBody> response) {
                    OutputStream os = null;
                    InputStream is = response.body().byteStream();
                    long totalLength = response.body().contentLength();
                    long currentLength = 0;
                    try {
                        int bufferSize = 2048;
                        os = new BufferedOutputStream(new FileOutputStream(file));
                        byte data[] = new byte[bufferSize];
                        int len;
                        while ((len = is.read(data, 0, bufferSize)) != -1) {
                            os.write(data, 0, len);
                            currentLength += len;
                            //计算当前下载进度
                            int progress = (int) (100 * currentLength / totalLength);

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Log.d("udeskdownload","downloadFile    loadingFail   "+e.getMessage());
                    }finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            if (os != null) {
                                os.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFail(Throwable message) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    interface ProgressListener {
        void onProgress(long totalBytes, long remainingBytes, boolean done);
    }

    //点击失败按钮 重试发送消息
    public void startRetryMsg(ReceiveMessage message) {
        try {
            if (UdeskUtil.objectToString(message.getContent_type()).equals(UdeskConst.ChatMsgTypeString.TYPE_TEXT)) {
                createMessage(UdeskUtil.objectToString(message.getId()), UdeskUtil.objectToString(message.getContent()), UdeskUtil.objectToString(message.getContent_type()), message.getExtras());
            } else if (UdeskUtil.objectToString(message.getContent_type()).equals(UdeskConst.ChatMsgTypeString.TYPE_AUDIO)
                    || UdeskUtil.objectToString(message.getContent_type()).equals(UdeskConst.ChatMsgTypeString.TYPE_IMAGE)
                    || UdeskUtil.objectToString(message.getContent_type()).equals(UdeskConst.ChatMsgTypeString.TYPE_VIDEO)
            ) {

                if (!UdeskUtil.objectToString(message.getLocalPath()).isEmpty()) {
                    upLoadFileQ(UdeskUtil.objectToString(message.getLocalPath()), message);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }


    //获取商户详情
    public void getMerchantInfo() {
        try {
            InitMode initMode = UdeskSDKManager.getInstance().getInitMode();
            if (initMode != null) {
                HttpFacade.getInstance().getMerchantsDetails(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                        UdeskUtil.objectToString(initMode.getIm_password())), mChatView.getEuid(), new HttpCallBack() {
                    @Override
                    public void onSuccess(String message) {
                        Merchant merchant = JsonUtils.parseMerchantDetail(mChatView.getContext().getApplicationContext(), message);
                        mChatView.setMerchant(merchant);
                    }

                    @Override
                    public void onFail(Throwable message) {
                        if (message instanceof ConnectTimeoutException) {
                            if (UdeskLibConst.isDebug) {
                                Log.i("udesk", "getMerchantInfo result =" + mChatView.getContext().getString(R.string.time_out));
                            }
                        }
                    }

                    @Override
                    public void onSuccessFail(String message) {
                        if (UdeskLibConst.isDebug) {
                            Log.i("udesk", "getMerchantInfo result =" + message);
                        }

                        try {
                            JSONObject object = new JSONObject(message);
                            String error = object.optString("message");
                            if (!TextUtils.isEmpty(error)) {
                                Toast.makeText(mChatView.getContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //创建消息
    public void createMessage(final String id, final Object messa, String type, ExtrasInfo info) {
        try {
            InitMode initMode = UdeskSDKManager.getInstance().getInitMode();
            if (initMode != null) {
                final SendMessage sendMessage = new SendMessage();
                SendMessage.MessageBean  messageBean= new SendMessage.MessageBean();
                messageBean.setContent(messa);
                messageBean.setContent_type(type);
                if (info != null) {
                    messageBean.setExtras(info);
                }
                sendMessage.setMessage(messageBean);
                HttpFacade.getInstance().createMessage(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                        UdeskUtil.objectToString(initMode.getIm_password())), mChatView.getEuid(), sendMessage, new HttpCallBack() {
                    @Override
                    public void onSuccess(String backstirng) {
                        mChatView.checkConnect();
                        sendMessageResult(id, UdeskConst.SendFlag.RESULT_SUCCESS, JsonUtils.getCreateTime(backstirng));
                    }

                    @Override
                    public void onFail(Throwable message) {
                        sendMessageResult(id, UdeskConst.SendFlag.RESULT_FAIL, new SendMsgResult());
                        if (message instanceof ConnectTimeoutException) {
                            if (UdeskLibConst.isDebug) {
                                Log.i("udesk", "createMessage result =" + mChatView.getContext().getString(R.string.time_out));
                            }
                        }
                    }

                    @Override
                    public void onSuccessFail(String message) {
                        sendMessageResult(id, UdeskConst.SendFlag.RESULT_FAIL, new SendMsgResult());
                        if (UdeskLibConst.isDebug) {
                            Log.i("udesk", "createMessage result =" + message);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取消息列表
     */
    public void getMessages(final String fromUUID) {
        try {
            InitMode initMode = UdeskSDKManager.getInstance().getInitMode();
            if (initMode != null) {
                HttpFacade.getInstance().getMessages(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                        UdeskUtil.objectToString(initMode.getIm_password())), mChatView.getEuid(), fromUUID, new HttpCallBack() {
                    @Override
                    public void onSuccess(String message) {
                        List<ReceiveMessage> messagess = JsonUtils.parserMessages(mChatView.getContext().getApplicationContext(), message);
                        Collections.reverse(messagess);
                        mChatView.addMessage(messagess, fromUUID);

                        for (int i = messagess.size() - 1; i > 0; i--) {

                            ReceiveMessage receiveMessage = messagess.get(i);
                            if (UdeskUtil.objectToString(receiveMessage.getCategory()).equals("event") &&
                                    UdeskUtil.objectToString(receiveMessage.getContent()).contains("发送满意度调查")) {

                                long createtime = UdeskUtil.stringToLong(UdeskUtil.objectToString(receiveMessage.getCreated_at()));
                                long currentTimeMillis = System.currentTimeMillis();
                                Log.i("xxxxxx", "createtime=" + createtime
                                        + ";  currentTimeMillis=" + currentTimeMillis + " ;  receiveMessage= " + receiveMessage.toString());
                                if (currentTimeMillis - createtime < 10 * 1000) {
                                    //
                                    getHasSurvey(new IUdeskHasSurvyCallBack() {
                                        @Override
                                        public void hasSurvy(boolean hasSurvy) {

                                            if (!hasSurvy) {
                                                //未评价，可以发起评价
                                                if (mChatView.getSurvyOption() != null) {
                                                    Message messge = mChatView.getHandler().obtainMessage(
                                                            MessageWhat.surveyNotify);
                                                    mChatView.getHandler().sendMessage(messge);
                                                } else {
                                                    getIMSurveyOptions(true);
                                                }
                                            }
                                        }
                                    });
                                }

                                break;

                            }
                        }

                    }

                    @Override
                    public void onFail(Throwable message) {
                        List<ReceiveMessage> messagess = new ArrayList<ReceiveMessage>();
                        mChatView.addMessage(messagess, fromUUID);
                        if (message instanceof ConnectTimeoutException) {
                            if (UdeskLibConst.isDebug) {
                                Log.i("udesk", "getMessages result =" + mChatView.getContext().getString(R.string.time_out));
                            }
                        }
                    }

                    @Override
                    public void onSuccessFail(String message) {
                        if (UdeskLibConst.isDebug) {
                            Log.i("udesk", "getMessages result =" + message);
                        }
                        List<ReceiveMessage> messagess = new ArrayList<ReceiveMessage>();
                        mChatView.addMessage(messagess, fromUUID);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setMessageRead() {
        try {
            InitMode initMode = UdeskSDKManager.getInstance().getInitMode();
            if (initMode != null) {
                HttpFacade.getInstance().setMessageRead(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                        UdeskUtil.objectToString(initMode.getIm_password())), mChatView.getEuid(), new HttpCallBack() {
                    @Override
                    public void onSuccess(String message) {

                    }

                    @Override
                    public void onFail(Throwable message) {

                    }

                    @Override
                    public void onSuccessFail(String message) {

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 消息发送结果
     *
     * @param msgId
     */
    public void sendMessageResult(String msgId, int status, SendMsgResult sendMsgResult) {
        try {
            sendMsgResult.setId(msgId);
            sendMsgResult.setFlag(status);
            if (mChatView != null && mChatView.getHandler() != null) {
                Message message = mChatView.getHandler().obtainMessage(
                        MessageWhat.changeImState);
                message.obj = sendMsgResult;
                mChatView.getHandler().sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private ExecutorService scaleExecutor;

    private void ensureMessageExecutor() {
        if (scaleExecutor == null) {
            scaleExecutor = Concurrents
                    .newSingleThreadExecutor("scaleExecutor");
        }
    }

    public void scaleBitmap(final String path) {
        if (!TextUtils.isEmpty(path)) {
            ensureMessageExecutor();
            scaleExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bitmap scaleImage = null;
                        byte[] data = null;
                        int max = 0;
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        /**
                         * 在不分配空间状态下计算出图片的大小
                         */
                        options.inJustDecodeBounds = true;
                        UdeskUtil.decodeFileAndContent(mChatView.getContext(), path, options);
                        int width = options.outWidth;
                        int height = options.outHeight;
                        max = Math.max(width, height);
                        options.inTempStorage = new byte[100 * 1024];
                        options.inJustDecodeBounds = false;
                        options.inPurgeable = true;
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        InputStream inStream;
                        if (UdeskUtil.isAndroidQ()) {
                            inStream = mChatView.getContext().getContentResolver().openInputStream(Uri.parse(UdeskUtil.getFilePathQ(mChatView.getContext(), path)));
                        } else {
                            inStream = new FileInputStream(path);
                        }
                        if (inStream == null) {
                            return;
                        }
                        data = readStream(inStream);
                        if (data == null || data.length <= 0) {
                            sendBitmapMessage(path);
                            return;
                        }
                        String imageName = UdeskUtil.MD5(data);
                        File scaleImageFile = UdeskUtil.getOutputMediaFile(mChatView.getContext(), imageName
                                + UdeskConst.ORIGINAL_SUFFIX);
                        if (scaleImageFile == null) {
                            return;
                        }
                        if (!scaleImageFile.exists()) {
                            // 缩略图不存在，生成上传图
                            if (max > UdeskConfig.scaleMax) {
                                options.inSampleSize = max / UdeskConfig.scaleMax;
                            } else {
                                options.inSampleSize = 1;
                            }
                            FileOutputStream fos = new FileOutputStream(scaleImageFile);
                            scaleImage = BitmapFactory.decodeByteArray(data, 0,
                                    data.length, options);
                            scaleImage.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                            fos.close();
                            fos = null;
                        }

                        if (scaleImage != null) {
                            scaleImage.recycle();
                            scaleImage = null;
                        }
                        data = null;
                        if (TextUtils.isEmpty(scaleImageFile.getPath())) {
                            sendBitmapMessage(path);
                        } else {
                            if (UdeskUtil.isAndroidQ()) {
                                sendBitmapMessage(UdeskUtil.getOutputMediaFileUri(mChatView.getContext().getApplicationContext(), scaleImageFile).toString());
                            } else {
                                sendBitmapMessage(scaleImageFile.getPath());
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError error) {
                        error.printStackTrace();
                    }
                }
            });
        }
    }


    /**
     * @param inStream
     * @return byte[]
     * @throws Exception
     */
    public byte[] readStream(InputStream inStream) throws Exception {
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;

    }

    public interface IUdeskHasSurvyCallBack {

        void hasSurvy(boolean hasSurvy);
    }

    //客户主动发起满意度调查，先获取是否评价
    public void getHasSurvey(final IUdeskHasSurvyCallBack hasSurvyCallBack) {
        try {
            InitMode initMode = UdeskSDKManager.getInstance().getInitMode();
            if (initMode != null) {
                HttpFacade.getInstance().hasSurvey(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                        UdeskUtil.objectToString(initMode.getIm_password())), mChatView.getEuid(), new HttpCallBack() {
                    @Override
                    public void onSuccess(String message) {

                        try {
                            JSONObject result = new JSONObject(message);
//                            has_survey: true(已经评价过)|false(还没有评价过)
//                                    true 的时候提示不能评价
                            if (result.has("has_survey")) {
//                                {"has_survey":false}
                                if (!result.optBoolean("has_survey")) {
                                    if (hasSurvyCallBack != null) {
                                        hasSurvyCallBack.hasSurvy(false);
                                    } else {
                                        //未评价，可以发起评价
                                        if (mChatView.getSurvyOption() != null) {
                                            Message messge = mChatView.getHandler().obtainMessage(
                                                    MessageWhat.surveyNotify);
                                            mChatView.getHandler().sendMessage(messge);
                                        } else {
                                            getIMSurveyOptions(true);
                                        }
                                    }
                                } else {
                                    //已评价，给出提示
                                    mChatView.setIsPermmitSurvy(true);
                                    if (hasSurvyCallBack != null) {
                                        hasSurvyCallBack.hasSurvy(true);
                                    } else {
                                        if (mChatView.getHandler() != null) {
                                            Message messge = mChatView.getHandler().obtainMessage(
                                                    MessageWhat.Has_Survey);
                                            mChatView.getHandler().sendMessage(messge);
                                        }
                                    }

                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            if (hasSurvyCallBack != null) {
                                hasSurvyCallBack.hasSurvy(true);
                            } else {
                                //出错给提示
                                sendSurveyerror();
                            }
                        }

                    }

                    @Override
                    public void onFail(Throwable message) {
                        if (hasSurvyCallBack != null) {
                            hasSurvyCallBack.hasSurvy(true);
                        } else {
                            sendSurveyerror();
                        }
                    }

                    @Override
                    public void onSuccessFail(String message) {
                        if (hasSurvyCallBack != null) {
                            hasSurvyCallBack.hasSurvy(true);
                        } else {
                            sendSurveyerror();
                        }
                    }
                });
            } else {
                mChatView.setIsPermmitSurvy(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (hasSurvyCallBack != null) {
                hasSurvyCallBack.hasSurvy(true);
            } else {
                //出错给提示
                sendSurveyerror();
            }
        }
    }

    //请求满意度调查选项的内容
    public void getIMSurveyOptions(final boolean isSurveyNotify) {
        InitMode initMode = UdeskSDKManager.getInstance().getInitMode();
        try {
            if (initMode != null) {
                HttpFacade.getInstance().surveyConfig(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                        UdeskUtil.objectToString(initMode.getIm_password())), mChatView.getEuid(), new HttpCallBack() {
                    @Override
                    public void onSuccess(String message) {
                        SurveyOptionsModel model = JsonUtils.parseSurveyOptions(message);
                        mChatView.setSurvyOption(model);
                        if (isSurveyNotify) {
                            Message messge = mChatView.getHandler().obtainMessage(
                                    MessageWhat.surveyNotify);
                            mChatView.getHandler().sendMessage(messge);
                        }

                    }

                    @Override
                    public void onFail(Throwable message) {
                        if (isSurveyNotify) {
                            sendSurveyerror();
                        }
                    }

                    @Override
                    public void onSuccessFail(String message) {
                        if (isSurveyNotify) {
                            sendSurveyerror();
                        }
                    }
                });
            } else {
                if (isSurveyNotify) {
                    sendSurveyerror();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (isSurveyNotify) {
                sendSurveyerror();
            }
        }


    }

    public void putIMSurveyResult(SurvyOption survyOption) {
        try {
            InitMode initMode = UdeskSDKManager.getInstance().getInitMode();
            if (initMode != null) {
                HttpFacade.getInstance().voteSurvey(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                        UdeskUtil.objectToString(initMode.getIm_password())), mChatView.getEuid(), survyOption, new HttpCallBack() {
                            @Override
                            public void onSuccess(String string) {

                                Message message = mChatView.getHandler().obtainMessage(
                                        MessageWhat.Survey_Success);
                                mChatView.getHandler().sendMessage(message);
                            }

                            @Override
                            public void onFail(Throwable message) {
                                sendSurveyerror();
                            }

                            @Override
                            public void onSuccessFail(String message) {
                                sendSurveyerror();
                            }
                        }
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //提交满意度调查出错
    private void sendSurveyerror() {
        try {
            if (mChatView.getHandler() != null) {
                Message message = mChatView.getHandler().obtainMessage(
                        MessageWhat.Survey_error);
                mChatView.getHandler().sendMessage(message);
                mChatView.setIsPermmitSurvy(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void queue() {
        try {
            InitMode initMode = UdeskSDKManager.getInstance().getInitMode();
            if (initMode != null) {
                HttpFacade.getInstance().queue(UdeskUtil.getAuthToken(UdeskUtil.objectToString(initMode.getIm_username()),
                        UdeskUtil.objectToString(initMode.getIm_password())), mChatView.getEuid(), new HttpCallBack() {
                            @Override
                            public void onSuccess(String string) {
                                Log.i("udesk", "queue result =" + string);
                            }

                            @Override
                            public void onFail(Throwable message) {
                                Log.i("udesk", "queue onFail =" + message);

                            }

                            @Override
                            public void onSuccessFail(String message) {
                                Log.i("udesk", "queue onSuccessFail =" + message);

                            }
                        }
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
