package cn.udesk.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskConfig;
import cn.udesk.emotion.MoonUtils;
import cn.udesk.model.Merchant;
import cn.udesk.model.SendMsgResult;
import cn.udesk.muchat.bean.ExtrasInfo;
import cn.udesk.muchat.bean.Products;
import cn.udesk.muchat.bean.ReceiveMessage;
import cn.udesk.widget.CircleProgressBar;
import cn.udesk.widget.HtmlTagHandler;
import udesk.core.utils.UdeskUtils;

import static android.util.Patterns.PHONE;
import static android.util.Patterns.WEB_URL;

public class
MessageAdatper extends BaseAdapter {
    private static final int[] layoutRes = {
            R.layout.udesk_chat_msg_item_txt_l,//文本消息左边的UI布局文件
            R.layout.udesk_chat_msg_item_txt_r,//文本消息右边的UI布局文件
            R.layout.udesk_chat_msg_item_audiot_l,//语音消息左边的UI布局文件
            R.layout.udesk_chat_msg_item_audiot_r,//语音消息右边的UI布局文件
            R.layout.udesk_chat_msg_item_imgt_l,//图片消息左边的UI布局文件
            R.layout.udesk_chat_msg_item_imgt_r,//图片消息右边的UI布局文件
            R.layout.udesk_chat_rich_item_txt,//富文本消息UI布局文件
            R.layout.udesk_im_commodity_item,  //显示推广链接信息的UI布局文件
            R.layout.udesk_chat_leavemsg_item_txt_l,//显示留言发送消息
            R.layout.udesk_chat_leavemsg_item_txt_r, // 显示收到留言消息的回复
            R.layout.udesk_chat_event_item, // 显示收到留言消息的回复
            R.layout.udesk_chat_msg_item_product_r, // 显示客户定义的商品消息
            R.layout.udesk_chat_msg_item_smallvideo_l,//短视频消息左
            R.layout.udesk_chat_msg_item_smallvideo_r,//短视频消息右
            R.layout.udesk_chat_msg_item_file_l,//文件消息左
            R.layout.udesk_chat_msg_item_file_r,//文件消息右
    };

    /**
     * 非法消息类型
     */
    private static final int ILLEGAL = -1;
    /**
     * 收到的文本消息标识
     */
    private static final int MSG_TXT_L = 0;
    /**
     * 发送的文本消息标识
     */
    private static final int MSG_TXT_R = 1;
    /**
     * 收到的语音消息标识
     */
    private static final int MSG_AUDIO_L = 2;
    /**
     * 发送的语音消息标识
     */
    private static final int MSG_AUDIO_R = 3;
    /**
     * 收到图片消息标识
     */
    private static final int MSG_IMG_L = 4;
    /**
     * 发送图片消息标识
     */
    private static final int MSG_IMG_R = 5;

    /**
     * 收到富文本消息标识
     */
    private static final int RICH_TEXT = 6;
    /**
     * 发送商品链接本消息标识
     */
    private static final int COMMODITY = 7;

    //留言消息的 标识
    private static final int LEAVEMSG_TXT_L = 8;
    private static final int LEAVEMSG_TXT_R = 9;
    private static final int Udesk_Event = 10;
    private static final int MSG_PRODUCT_R = 11;
    private static final int MSG_SMALL_VIDEO_L = 12;
    private static final int MSG_SMALL_VIDEO_R = 13;
    private static final int MSG_FILE_L = 14;
    private static final int MSG_FILE_R = 15;

    private Activity mContext;
    private List<ReceiveMessage> list = new ArrayList<ReceiveMessage>();


    public MessageAdatper(Activity context) {
        mContext = context;

    }

    @Override
    public int getCount() {
        return list.size();
    }

    public List<ReceiveMessage> getList() {
        return list;
    }

    /**
     * @param position
     * @return 返回当前位置消息的类型和方向标识
     */
    @Override
    public int getItemViewType(int position) {
        try {
            ReceiveMessage message = getItem(position);
            if (message == null) {
                return ILLEGAL;
            }
            if (message instanceof Products) {
                return COMMODITY;
            }
            if (UdeskUtil.objectToString(message.getCategory()).equals(UdeskConst.ChatMsgTypeString.TYPE_EVENT)) {
                return Udesk_Event;
            }
            switch (UdeskConst.parseTypeForMessage(UdeskUtil.objectToString(message.getContent_type()))) {
                case UdeskConst.ChatMsgTypeInt.TYPE_IMAGE:
                    if (UdeskUtil.objectToString(message.getDirection()).equals(UdeskConst.ChatMsgDirection.Recv)) {
                        return MSG_IMG_L;
                    } else {
                        return MSG_IMG_R;
                    }
                case UdeskConst.ChatMsgTypeInt.TYPE_TEXT:
                    if (UdeskUtil.objectToString(message.getDirection()).equals(UdeskConst.ChatMsgDirection.Recv)) {
                        return MSG_TXT_L;
                    } else {
                        return MSG_TXT_R;
                    }
                case UdeskConst.ChatMsgTypeInt.TYPE_RICH:
                    return RICH_TEXT;
                case UdeskConst.ChatMsgTypeInt.TYPE_AUDIO:
                    if (UdeskUtil.objectToString(message.getDirection()).equals(UdeskConst.ChatMsgDirection.Recv)) {
                        return MSG_AUDIO_L;
                    } else {
                        return MSG_AUDIO_R;
                    }
                case UdeskConst.ChatMsgTypeInt.TYPE_LEAVEMSG:
                    if (UdeskUtil.objectToString(message.getDirection()).equals(UdeskConst.ChatMsgDirection.Recv)) {
                        return LEAVEMSG_TXT_L;
                    } else {
                        return LEAVEMSG_TXT_R;
                    }
                case UdeskConst.ChatMsgTypeInt.TYPE_PRODUCT:
                    return MSG_PRODUCT_R;

                case UdeskConst.ChatMsgTypeInt.TYPE_VIDEO:
                    if (UdeskUtil.objectToString(message.getDirection()).equals(UdeskConst.ChatMsgDirection.Recv)) {
                        return MSG_SMALL_VIDEO_L;
                    } else {
                        return MSG_SMALL_VIDEO_R;
                    }
                case UdeskConst.ChatMsgTypeInt.TYPE_FILE:
                    if (UdeskUtil.objectToString(message.getDirection()).equals(UdeskConst.ChatMsgDirection.Recv)) {
                        return MSG_FILE_L;
                    } else {
                        return MSG_FILE_R;
                    }

                default:
                    return ILLEGAL;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ILLEGAL;
        }

    }

    /**
     * @return 返回有多少种UI布局样式
     */
    @Override
    public int getViewTypeCount() {
        if (layoutRes.length > 0) {
            return layoutRes.length;
        }
        return super.getViewTypeCount();
    }


    /**
     * 添加一条消息
     */
    public synchronized void addItem(ReceiveMessage message) {
        if (message == null) {
            return;
        }
        if (TextUtils.isEmpty(UdeskUtil.objectToString(message.getContent()))
                && TextUtils.equals(UdeskUtil.objectToString(message.getContent_type()), UdeskConst.ChatMsgTypeString.TYPE_TEXT)){
            return;
        }
        //不是撤回消息则过滤含有相同msgID的消息，如果是撤回消息则替换掉
        try {
            for (ReceiveMessage info : list) {
                if (!TextUtils.isEmpty(UdeskUtil.objectToString(message.getUuid())) &&
                        !TextUtils.isEmpty(UdeskUtil.objectToString(info.getUuid())) &&
                        UdeskUtil.objectToString(message.getUuid()).equals(UdeskUtil.objectToString(info.getUuid()))) {

                    if (message.getSend_status().equals("rollback")) {
                        list.remove(info);
                        message.setCategory(UdeskConst.ChatMsgTypeString.TYPE_EVENT);
                        message.setContent(mContext.getString(R.string.udesk_rollback_tips));
                        break;
                    }
                    return;

                }
            }
            list.add(message);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void listAddItems(List<ReceiveMessage> messages, boolean isMore) {
        try {
            if (messages == null || messages.isEmpty()) {
                return;
            }
            Iterator<ReceiveMessage> iterator = messages.iterator();
            while (iterator.hasNext()){
                if (TextUtils.isEmpty(UdeskUtil.objectToString(iterator.next().getContent()))
                        && TextUtils.equals(UdeskUtil.objectToString(iterator.next().getContent_type()), UdeskConst.ChatMsgTypeString.TYPE_TEXT)){
                    iterator.remove();
                }
            }
            if (isMore) {
                list.addAll(0, messages);
            } else {
                if (list.size() > 0) {
                    ReceiveMessage temp = list.get(list.size() - 1);
                    String createtime = UdeskUtil.objectToString(temp.getCreated_at());
                    ReceiveMessage temp2 = messages.get(messages.size() - 1);
                    String createtime2 = UdeskUtil.objectToString(temp2.getCreated_at());
                    if (createtime.equals(createtime2)) {
                        return;
                    }
                }
                list.clear();
                list.addAll(messages);
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ReceiveMessage getItem(int position) {
        if (position < 0 || position >= list.size()) {
            return null;
        }
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            ReceiveMessage msgInfo = getItem(position);
            if (msgInfo != null) {
                int itemType = getItemViewType(position);
                convertView = initView(convertView, itemType, msgInfo);
                BaseViewHolder holder = (BaseViewHolder) convertView.getTag();
                tryShowTime(holder, msgInfo);
                holder.setMessage(msgInfo);
                holder.initHead(itemType);
                holder.showStatusOrProgressBar();
                holder.bind(mContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    /**
     * 根据传入的 itemType代表消息的类型和方向标识 初始相对应的UI控件
     */
    private View initView(View convertView, int itemType, final ReceiveMessage msgInfo) {
        if (convertView == null) {
            try {
                convertView = LayoutInflater.from(mContext).inflate(
                        layoutRes[itemType], null);
                switch (itemType) {
                    case LEAVEMSG_TXT_L:
                    case LEAVEMSG_TXT_R:
                        LeaveMsgViewHolder leaveMsgViewHolder = new LeaveMsgViewHolder();
                        initItemNormalView(convertView, leaveMsgViewHolder);
                        leaveMsgViewHolder.tvMsg = (TextView) convertView.findViewById(R.id.udesk_tv_msg);
                        if (itemType == MSG_TXT_L) {
                            UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMLeftTextColorResId, leaveMsgViewHolder.tvMsg);
                        } else if (itemType == MSG_TXT_R) {
                            UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMRightTextColorResId, leaveMsgViewHolder.tvMsg);
                        }
                        convertView.setTag(leaveMsgViewHolder);
                        break;
                    case MSG_TXT_L:
                    case MSG_TXT_R: {
                        TxtViewHolder holder = new TxtViewHolder();
                        initItemNormalView(convertView, holder);
                        holder.tvMsg = (HtmlTextView) convertView.findViewById(R.id.udesk_tv_msg);
                        if (itemType == MSG_TXT_L) {
                            UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMLeftTextColorResId, holder.tvMsg);
                        } else if (itemType == MSG_TXT_R) {
                            UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMRightTextColorResId, holder.tvMsg);
                        }
                        convertView.setTag(holder);
                        break;
                    }
                    case RICH_TEXT: {
                        RichTextViewHolder holder = new RichTextViewHolder();
                        initItemNormalView(convertView, holder);
                        holder.rich_tvmsg = (TextView) convertView.findViewById(R.id.udesk_tv_rich_msg);
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMLeftTextColorResId, holder.rich_tvmsg);
                        convertView.setTag(holder);
                        break;
                    }
                    case MSG_AUDIO_L:
                    case MSG_AUDIO_R: {
                        AudioViewHolder holder = new AudioViewHolder();
                        initItemNormalView(convertView, holder);
                        holder.tvDuration = (TextView) convertView
                                .findViewById(R.id.udesk_im_item_record_duration);
                        holder.record_item_content = convertView.findViewById(R.id.udesk_im_record_item_content);
                        holder.record_play = (ImageView) convertView.findViewById(R.id.udesk_im_item_record_play);
                        convertView.setTag(holder);
                        break;
                    }
                    case MSG_IMG_L:
                    case MSG_IMG_R: {
                        ImgViewHolder holder = new ImgViewHolder();
                        initItemNormalView(convertView, holder);
                        holder.imgView = (ImageView) convertView.findViewById(R.id.udesk_im_image);
                        convertView.setTag(holder);
                        break;
                    }
                    case COMMODITY: {
                        CommodityViewHolder holder = new CommodityViewHolder();
                        holder.ivHeader = (ImageView) convertView.findViewById(R.id.udesk_iv_head);
                        holder.tvTime = (TextView) convertView.findViewById(R.id.udesk_tv_time);
                        holder.rootView = convertView.findViewById(R.id.udesk_commit_root);
                        holder.udesk_name_ll = convertView.findViewById(R.id.udesk_name_ll);
                        holder.thumbnail = (ImageView) convertView
                                .findViewById(R.id.udesk_im_commondity_thumbnail);
                        holder.title = (TextView) convertView
                                .findViewById(R.id.udesk_im_commondity_title);
                        holder.subTitle = (TextView) convertView
                                .findViewById(R.id.udesk_im_commondity_subtitle);
                        holder.link = (TextView) convertView
                                .findViewById(R.id.udesk_im_commondity_link);
                        UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskCommityBgResId, holder.rootView);
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskCommityTitleColorResId, holder.title);
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskCommitysubtitleColorResId, holder.subTitle);
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskCommityLinkColorResId, holder.link);
                        convertView.setTag(holder);
                        break;
                    }
                    case Udesk_Event:
                        UdeskEventViewHolder eventViewHolder = new UdeskEventViewHolder();
                        initItemNormalView(convertView, eventViewHolder);
                        eventViewHolder.events = (HtmlTextView) convertView.findViewById(R.id.udesk_event);
                        convertView.setTag(eventViewHolder);
                        break;
                    case MSG_PRODUCT_R:
                        ProductViewHolder productViewHolder = new ProductViewHolder();
                        initItemNormalView(convertView, productViewHolder);
                        productViewHolder.tvMsg = (TextView) convertView.findViewById(R.id.udesk_tv_msg);
                        productViewHolder.product_name = (TextView) convertView.findViewById(R.id.product_name);
                        productViewHolder.productView = convertView.findViewById(R.id.product_view);
                        productViewHolder.imgView = (ImageView) convertView.findViewById(R.id.udesk_product_icon);
                        convertView.setTag(productViewHolder);
                        break;
                    case MSG_SMALL_VIDEO_L:
                    case MSG_SMALL_VIDEO_R:
                        SmallVideoViewHolder smallVideoViewHolder = new SmallVideoViewHolder();
                        initItemNormalView(convertView, smallVideoViewHolder);
                        smallVideoViewHolder.imgView =  convertView.findViewById(R.id.udesk_im_image);
                        smallVideoViewHolder.cancleImg = (ImageView) convertView.findViewById(R.id.udesk_iv_cancle);
                        smallVideoViewHolder.video_tip = (ImageView) convertView.findViewById(R.id.video_tip);
                        smallVideoViewHolder.circleProgressBar = (CircleProgressBar) convertView.findViewById(R.id.video_upload_bar);
                        convertView.setTag(smallVideoViewHolder);
                        break;
                    case MSG_FILE_L:
                    case MSG_FILE_R:
                        FileViewHolder fileViewHolder = new FileViewHolder();
                        initItemNormalView(convertView, fileViewHolder);
                        fileViewHolder.itemFile = convertView.findViewById(R.id.udesk_file_view);
                        fileViewHolder.fileTitle = (TextView) convertView.findViewById(R.id.udesk_file_name);
                        fileViewHolder.fileSize = (TextView) convertView.findViewById(R.id.udesk_file_size);
                        fileViewHolder.operater = (TextView) convertView.findViewById(R.id.udesk_file_operater);
                        fileViewHolder.mProgress = (ProgressBar) convertView.findViewById(R.id.udesk_progress);
                        convertView.setTag(fileViewHolder);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return convertView;
    }


    abstract class BaseViewHolder {
        public ImageView ivHeader;
        public ImageView ivStatus;
        public TextView tvTime;
        public ProgressBar pbWait;
        public TextView agentnickName;
        public ReceiveMessage message;
        public int itemType;
        public boolean isLeft = false;

        public void setMessage(ReceiveMessage message) {
            this.message = message;
        }

        public int getItemType() {
            return itemType;
        }

        public ReceiveMessage getMessage() {
            return message;
        }

        /**
         * 根据收发消息的标识，设置客服客户的头像
         *
         * @param itemType
         */
        void initHead(int itemType) {
            try {
                this.itemType = itemType;
                switch (itemType) {
                    case MSG_TXT_R:
                    case MSG_AUDIO_R:
                    case MSG_IMG_R:
                    case LEAVEMSG_TXT_R:
                    case COMMODITY:
                    case MSG_SMALL_VIDEO_R:
                    case MSG_FILE_R:
                        this.isLeft = false;
                        Glide.with(mContext).load(UdeskConfig.customerUrl).error(R.drawable.udesk_im_default_user_avatar).placeholder(R.drawable.udesk_im_default_user_avatar).into(ivHeader);
                        break;
                    case MSG_TXT_L:
                    case MSG_AUDIO_L:
                    case RICH_TEXT:
                    case MSG_IMG_L:
                    case MSG_SMALL_VIDEO_L:
                    case MSG_FILE_L:
                        this.isLeft = true;
                        Merchant merchant = ((UdeskChatActivity) mContext).getMerchant();
                        if (merchant != null && !TextUtils.isEmpty(UdeskUtil.objectToString(merchant.getLogo_url()))) {
                            Glide.with(mContext).load(Uri.parse(UdeskUtil.objectToString(merchant.getLogo_url()))).error(R.drawable.udesk_im_default_user_avatar).placeholder(R.drawable.udesk_im_default_user_avatar).into(ivHeader);
                        } else {
                            Glide.with(mContext).load(R.drawable.udesk_im_default_agent_avatar).into(ivHeader);
                        }
                        if (merchant != null) {
                            agentnickName.setText(UdeskUtil.objectToString(merchant.getName()));
                        }

                        break;
                    case LEAVEMSG_TXT_L:
                        this.isLeft = true;
//                        if (message.getUser_avatar() != null && !TextUtils.isEmpty(message.getUser_avatar().trim())) {
//                            ivHeader.setImageResource(R.drawable.udesk_im_default_agent_avatar);
//                            UdeskUtil.loadHeadView(mContext, ivHeader, Uri.parse(message.getUser_avatar()));
//                        }
//                        agentnickName.setText(message.getReplyUser());
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         * 设置消息发送状态  发送中，发送成功， 发送失败
         */
        public void showStatusOrProgressBar() {
            try {
                if (itemType == COMMODITY || itemType == Udesk_Event) {
                    return;
                }
                if (itemType == MSG_TXT_L
                        || itemType == MSG_AUDIO_L
                        || itemType == MSG_IMG_L
                        || itemType == MSG_SMALL_VIDEO_L
                        || itemType == MSG_FILE_L
                ) {
                    ivStatus.setVisibility(View.GONE);
                } else {
                    changeUiState(message.getSendFlag());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void changeUiState(int state) {
            try {
                if (state == UdeskConst.SendFlag.RESULT_SUCCESS) {
                    ivStatus.setVisibility(View.GONE);
                    pbWait.setVisibility(View.GONE);
                } else {
                    if (state == UdeskConst.SendFlag.RESULT_RETRY || state == UdeskConst.SendFlag.RESULT_SEND) {
                        ivStatus.setVisibility(View.GONE);
                        pbWait.setVisibility(View.VISIBLE);
                    } else if (state == UdeskConst.SendFlag.RESULT_FAIL) {
                        ivStatus.setVisibility(View.VISIBLE);
                        pbWait.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setTvTime(String create) {
            tvTime.setText(UdeskUtil.formatLongTypeTimeToString(mContext, create).trim());
        }

        abstract void bind(Context context);

    }

    private void dealRichText(TextView textView) {

        try {

            Linkify.addLinks(textView, WEB_URL, null);
            Linkify.addLinks(textView, PHONE, null);
            CharSequence text = textView.getText();
            if (text instanceof Spannable) {
                int end = text.length();
                Spannable sp = (Spannable) textView.getText();
                URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
                SpannableStringBuilder style = new SpannableStringBuilder(text);
                style.clearSpans();
                for (URLSpan url : urls) {
                    MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
                    style.setSpan(myURLSpan, sp.getSpanStart(url),
                            sp.getSpanEnd(url),
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                }
                textView.setText(style);
            } else if (text instanceof SpannedString) {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
                URLSpan[] urls = spannableStringBuilder.getSpans(0, text.length(), URLSpan.class);
                spannableStringBuilder.clearSpans();
                SpannedString sp = (SpannedString) textView.getText();
                for (URLSpan url : urls) {
                    MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
                    int statr = sp.getSpanStart(url);
                    int end = sp.getSpanEnd(url);
                    spannableStringBuilder.setSpan(myURLSpan, statr,
                            end,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                }
                textView.setText(spannableStringBuilder);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * 展示富文本消息
     */
    class RichTextViewHolder extends BaseViewHolder {

        public TextView rich_tvmsg;

        @Override
        void bind(Context context) {
            try {
                CharSequence charSequence = Html.fromHtml(UdeskUtil.objectToString(message.getContent()));
                String msg = charSequence.toString();
                if (msg.endsWith("\n\n")) {
                    charSequence = charSequence.subSequence(0, charSequence.length() - 2);
                    rich_tvmsg.setText(charSequence);
                } else {
                    rich_tvmsg.setText(charSequence);
                }
                Linkify.addLinks(rich_tvmsg, WEB_URL, null);
                Linkify.addLinks(rich_tvmsg, PHONE, null);
                CharSequence text = rich_tvmsg.getText();
                if (text instanceof Spannable) {
                    int end = text.length();
                    Spannable sp = (Spannable) rich_tvmsg.getText();
                    URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
                    SpannableStringBuilder style = new SpannableStringBuilder(text);
                    style.clearSpans();
                    for (URLSpan url : urls) {
                        MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
                        style.setSpan(myURLSpan, sp.getSpanStart(url),
                                sp.getSpanEnd(url),
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                    rich_tvmsg.setText(style);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }

        }

    }

    /**
     * 重写ClickableSpan 实现富文本点击事件跳转到UdeskWebViewUrlAcivity界面
     */
    private class MyURLSpan extends ClickableSpan {

        private String mUrl;

        MyURLSpan(String url) {
            mUrl = url;
        }

        @Override
        public void onClick(View widget) {
            try {
                if (WEB_URL.matcher(mUrl).find()) {
                    Intent intent = new Intent(mContext, UdeskWebViewUrlAcivity.class);
                    intent.putExtra(UdeskConst.WELCOME_URL, mUrl);
                    mContext.startActivity(intent);
                } else if (PHONE.matcher(mUrl).find()) {
                    String phone = mUrl.toLowerCase();
                    if (!phone.startsWith("tel:")) {
                        phone = "tel:" + mUrl;
                    }
                    ((UdeskChatActivity) mContext).callphone(phone);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }
    }

    //文本消息的url事件拦截处理。  客户设置了事件则走客户的事件，没走默认弹出界面
    private class TxtURLSpan extends ClickableSpan {

        private String mUrl;

        TxtURLSpan(String url) {
            mUrl = url;
        }


        @Override
        public void onClick(View widget) {
            try {
                Intent intent = new Intent(mContext, UdeskWebViewUrlAcivity.class);
                intent.putExtra(UdeskConst.WELCOME_URL, mUrl);
                mContext.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 展示文本消息
     */
    class TxtViewHolder extends BaseViewHolder {
        public HtmlTextView tvMsg;

        @Override
        void bind(Context context) {
            try {
                //设置文本消息内容，表情符转换对应的表情,没表情的另外处理
                String content = UdeskUtil.objectToString(message.getContent());
                if (MoonUtils.isHasEmotions(content)){
                    tvMsg.setText(MoonUtils.replaceEmoticons(mContext, content, (int) tvMsg.getTextSize()));
                } else {
                    tvMsg.setHtml(content);
                    dealRichText(tvMsg);
                }


                //设置消息长按事件  复制文本
                tvMsg.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        ((UdeskChatActivity) mContext).handleText(message, v);
                        return false;
                    }
                });

                /**
                 * 设置重发按钮的点击事件
                 */
                ivStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        changeUiState(UdeskConst.SendFlag.RESULT_RETRY);
                        ((UdeskChatActivity) mContext).retrySendMsg(message);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 展示留言消息
     */
    class LeaveMsgViewHolder extends BaseViewHolder {
        public TextView tvMsg;

        @Override
        void bind(Context context) {
            try {
                //设置文本消息内容，表情符转换对应的表情,没表情的另外处理
                String content = UdeskUtil.objectToString(message.getContent());
                if (MoonUtils.isHasEmotions(content)){
                    tvMsg.setText(MoonUtils.replaceEmoticons(mContext, content, (int) tvMsg.getTextSize()));
                } else {
                    tvMsg.setText(content);
                    tvMsg.setMovementMethod(LinkMovementMethod.getInstance());
                    CharSequence text = tvMsg.getText();
                    if (text instanceof Spannable) {
                        int end = text.length();
                        Spannable sp = (Spannable) tvMsg.getText();
                        URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
                        SpannableStringBuilder style = new SpannableStringBuilder(text);
                        style.clearSpans();// should clear old spans
                        for (URLSpan url : urls) {
                            TxtURLSpan txtURLSpan = new TxtURLSpan(url.getURL());
                            style.setSpan(txtURLSpan, sp.getSpanStart(url),
                                    sp.getSpanEnd(url),
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        }
                        tvMsg.setText(style);
                    }
                }

                //设置消息长按事件  复制文本
                tvMsg.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        ((UdeskChatActivity) mContext).handleText(message, v);
                        return false;
                    }
                });

                /**
                 * 设置重发按钮的点击事件
                 */
                ivStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        changeUiState(UdeskConst.SendFlag.RESULT_RETRY);
                        ((UdeskChatActivity) mContext).retrySendMsg(message);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }


    }

    /**
     * 展示语音消息
     */
    class AudioViewHolder extends BaseViewHolder {
        public TextView tvDuration;
        public View record_item_content;
        public ImageView record_play;

        public TextView getDurationView() {
            return tvDuration;
        }

        @Override
        void bind(Context context) {
            try {
                checkPlayBgWhenBind();
                ExtrasInfo info = message.getExtras();
                int duration = 0;
                if (info != null) {
                    duration = UdeskUtil.objectToInt(info.getDuration());
                }
                if (duration > 0) {
                    char symbol = 34;
                    tvDuration.setText(duration + "" + String.valueOf(symbol));
                }
                record_item_content.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((UdeskChatActivity) mContext).clickRecordFile(message);
                    }
                });
                ivStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        changeUiState(UdeskConst.SendFlag.RESULT_RETRY);
                        ((UdeskChatActivity) mContext).retrySendMsg(message);
                    }
                });
                duration = duration == 0 ? 1 : duration;
                int min = UdeskUtil.getDisplayWidthPixels((Activity) mContext) / 6;
                int max = UdeskUtil.getDisplayWidthPixels((Activity) mContext) * 3 / 5;
                int step = (int) ((duration < 10) ? duration : (duration / 10 + 9));
                record_item_content.getLayoutParams().width = (step == 0) ? min
                        : (min + (max - min) / 15 * step);
                ivStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        changeUiState(UdeskConst.SendFlag.RESULT_RETRY);
                        ((UdeskChatActivity) mContext).retrySendMsg(message);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }

        private void checkPlayBgWhenBind() {
            try {
                if (message.isPlaying) {
                    resetAnimationAndStart();
                } else {
                    record_play.setImageDrawable(mContext.getResources().getDrawable(
                            isLeft ? R.drawable.udesk_im_record_left_default : R.drawable.udesk_im_record_right_default));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }

        private void resetAnimationAndStart() {
            try {
                record_play.setImageDrawable(mContext.getResources().getDrawable(
                        isLeft ? R.drawable.udesk_im_record_play_left : R.drawable.udesk_im_record_play_right));
                Drawable playDrawable = record_play.getDrawable();
                if (playDrawable != null
                        && playDrawable instanceof AnimationDrawable) {
                    ((AnimationDrawable) playDrawable).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }

        // 判断开始播放

        public void startAnimationDrawable() {
            try {
                message.isPlaying = true;
                Drawable playDrawable = record_play.getDrawable();
                if (playDrawable instanceof AnimationDrawable) {
                    ((AnimationDrawable) playDrawable).start();
                } else {
                    resetAnimationAndStart();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }

        // 关闭播放
        protected void endAnimationDrawable() {
            try {
                message.isPlaying = false;

                Drawable playDrawable = record_play.getDrawable();
                if (playDrawable != null
                        && playDrawable instanceof AnimationDrawable) {
                    ((AnimationDrawable) playDrawable).stop();
                    ((AnimationDrawable) playDrawable).selectDrawable(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }
    }

    /**
     * 展示图片消息
     */
    public class ImgViewHolder extends BaseViewHolder {
        public ImageView imgView;

        @Override
        void bind(Context context) {
            try {
                if (!TextUtils.isEmpty(message.getLocalPath()) && UdeskUtil.isExitFileByPath(context, message.getLocalPath())) {
                    UdeskUtil.loadIntoFitSize(context, message.getLocalPath(), R.drawable.udesk_defualt_failure, R.drawable.udesk_defalut_image_loading, imgView);
                } else {
                    UdeskUtil.loadIntoFitSize(context, UdeskUtil.uRLEncoder(UdeskUtil.objectToString(message.getContent())), R.drawable.udesk_defualt_failure, R.drawable.udesk_defalut_image_loading, imgView);
                }
                imgView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (message == null) {
                            return;
                        }
                        Uri imgUri = null;
                        if (!TextUtils.isEmpty(message.getLocalPath())) {
                            imgUri = UdeskUtil.getUriFromPath(mContext, message.getLocalPath());

                        } else if (!TextUtils.isEmpty(UdeskUtil.objectToString(message.getContent()))) {
                            imgUri = Uri.parse(UdeskUtil.uRLEncoder(UdeskUtil.objectToString(message.getContent())));
                        }
                        UdeskUtil.previewPhoto(mContext, imgUri);
                    }
                });
                ivStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        changeUiState(UdeskConst.SendFlag.RESULT_RETRY);
                        ((UdeskChatActivity) mContext).retrySendMsg(message);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }
    }


    public class UdeskEventViewHolder extends BaseViewHolder {

        public HtmlTextView events;

        @Override
        void bind(Context context) {
            try {
                tvTime.setVisibility(View.VISIBLE);
                if (!UdeskUtil.objectToString(message.getCreated_at()).isEmpty()) {
                    tvTime.setText("----" + UdeskUtil.parseEventTime(UdeskUtil.objectToString(message.getCreated_at())).trim() + "----");
                }
                events.setHtml(UdeskUtil.objectToString(message.getContent()));
                dealRichText(events);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 展示商品链接消息
     */
    public class CommodityViewHolder extends BaseViewHolder {
        public View rootView;
        public ImageView thumbnail;
        public TextView title;
        public TextView subTitle;
        public TextView link;

        public View udesk_name_ll;

        @Override
        void bind(Context context) {
            try {
                final Products item = (Products) message;
                if (item.isActitve()) {
                    link.setVisibility(View.GONE);
                    ivHeader.setVisibility(View.VISIBLE);
                    udesk_name_ll.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rootView.getLayoutParams();
                    params.leftMargin = 124;
                    rootView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (UdeskSDKManager.getInstance().getCommodityCallBack() != null) {
                                UdeskSDKManager.getInstance().getCommodityCallBack().callBackProduct(item);
                            }
                        }
                    });
                } else {
                    link.setVisibility(View.VISIBLE);
                    ivHeader.setVisibility(View.GONE);
                    udesk_name_ll.setVisibility(View.GONE);
                }

                title.setText(item.getProduct().getTitle());
                List<Products.ProductBean.ExtrasBean> extrasBeens = item.getProduct().getExtras();
                if (extrasBeens != null && extrasBeens.size() > 0) {
                    subTitle.setText(extrasBeens.get(0).getTitle() + ": " + extrasBeens.get(0).getContent());
                }
                UdeskUtil.loadInto(context, UdeskUtil.uRLEncoder(item.getProduct().getImage()), R.drawable.udesk_defualt_failure,
                        R.drawable.udesk_defalut_image_loading, thumbnail);

                link.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((UdeskChatActivity) mContext).sentProduct(item);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }

        }
    }


    /**
     * 展示商品消息
     */
    public class ProductViewHolder extends BaseViewHolder {
        TextView tvMsg;
        TextView product_name;
        View productView;
        ImageView imgView;
        String productUrl;

        @Override
        void bind(Context context) {
            try {
                Object productMessage = message.getContent();
                JSONObject jsonObject = null;
                if (productMessage instanceof String) {
                    jsonObject = new JSONObject(UdeskUtil.objectToString(productMessage));
                } else if (productMessage instanceof JSONObject) {
                    jsonObject = (JSONObject) productMessage;
                } else {
                    Gson gson = new Gson();
                    jsonObject = new JSONObject(gson.toJson(productMessage));
                }
                if (TextUtils.isEmpty(jsonObject.optString("imgUrl"))) {
                    imgView.setVisibility(View.GONE);
                } else {
                    UdeskUtil.loadInto(context, UdeskUtil.uRLEncoder(jsonObject.optString("imgUrl")), R.drawable.udesk_defualt_failure,
                            R.drawable.udesk_defalut_image_loading, imgView);
                }
                productUrl = jsonObject.optString("url");
                product_name.setText(jsonObject.optString("name"));
                if (!TextUtils.isEmpty(productUrl)) {
                    product_name.setTextColor(mContext.getResources().getColor(UdeskConfig.udeskProductNameLinkColorResId));
                    product_name.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (UdeskSDKManager.getInstance().getProductMessageWebonCliclk() != null) {
                                UdeskSDKManager.getInstance().getProductMessageWebonCliclk().txtMsgOnclick(productUrl);
                            } else {
                                Intent intent = new Intent(mContext, UdeskWebViewUrlAcivity.class);
                                intent.putExtra(UdeskConst.WELCOME_URL, productUrl);
                                mContext.startActivity(intent);
                            }
                        }
                    });
                }

                StringBuilder builder = new StringBuilder();
                builder.append("<font></font>");
                JSONArray jsonArray = jsonObject.getJSONArray("params");
                if (jsonArray != null && jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.optJSONObject(i);
                        if (TextUtils.isEmpty(data.optString("text"))) {
                            continue;
                        }
                        String textStr = "<font color=" + data.optString("color") +
                                "  size=" + 2 * data.optInt("size") + ">" + data.optString("text") + "</font>";
                        if (data.optBoolean("fold")) {
                            textStr = "<b>" + textStr + "</b>";
                        }
                        if (data.optBoolean("break")) {
                            textStr = textStr + "<br>";
                        }
                        builder.append(textStr);
                    }
                }
                String htmlString = builder.toString().replaceAll("font", HtmlTagHandler.TAG_FONT);
                Spanned fromHtml = Html.fromHtml(htmlString, null, new HtmlTagHandler());
                tvMsg.setText(fromHtml);

                //重发按钮点击事件
                ivStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        changeUiState(UdeskConst.SendFlag.RESULT_RETRY);
                        ((UdeskChatActivity) mContext).retrySendMsg(message);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }

        }
    }

    /**
     * 展示小视频消息
     */
    public class SmallVideoViewHolder extends BaseViewHolder {
        ImageView imgView;
        ImageView cancleImg;
        ImageView video_tip;
        CircleProgressBar circleProgressBar;

        void showSuccessView() {
            try {
//                cancleImg.setVisibility(View.GONE);
                circleProgressBar.setVisibility(View.GONE);
                ivStatus.setVisibility(View.GONE);
                pbWait.setVisibility(View.GONE);
                video_tip.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void showFailureView() {
            try {
                ivStatus.setVisibility(View.VISIBLE);
                pbWait.setVisibility(View.GONE);
//                cancleImg.setVisibility(View.GONE);
                circleProgressBar.setVisibility(View.GONE);
                video_tip.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void showSendView() {
            try {
                if (circleProgressBar.getPercent() == 100) {
                    pbWait.setVisibility(View.VISIBLE);
                    video_tip.setVisibility(View.VISIBLE);
//                    cancleImg.setVisibility(View.GONE);
                    circleProgressBar.setVisibility(View.GONE);
                } else {
                    pbWait.setVisibility(View.VISIBLE);
//                    cancleImg.setVisibility(View.VISIBLE);
                    circleProgressBar.setVisibility(View.VISIBLE);
                    circleProgressBar.setPercent(circleProgressBar.getPercent());
                    video_tip.setVisibility(View.GONE);
                }
                ivStatus.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        void bind(final Context context) {
            try {
                if (itemType == MSG_SMALL_VIDEO_L) {
                    showSuccessView();
                } else {
                    if (message.getSendFlag() == UdeskConst.SendFlag.RESULT_SUCCESS) {
                        showSuccessView();
                    } else {
                        if (message.getSendFlag() == UdeskConst.SendFlag.RESULT_RETRY || message.getSendFlag() == UdeskConst.SendFlag.RESULT_SEND) {
                            showSendView();
                        } else if (message.getSendFlag() == UdeskConst.SendFlag.RESULT_FAIL) {
                            showFailureView();
                        }
                    }
                }

                if (!TextUtils.isEmpty(message.getLocalPath()) && UdeskUtil.isExitFileByPath(mContext, message.getLocalPath())) {
                    UdeskUtil.loadIntoFitSize(context, message.getLocalPath(), R.drawable.udesk_defualt_failure, R.drawable.udesk_defalut_image_loading, imgView);
                } else if (UdeskUtil.fileIsExitByUrl(mContext, UdeskConst.FileImg, UdeskUtil.objectToString(message.getContent()))) {
                    String loaclpath = UdeskUtil.getPathByUrl(mContext, UdeskConst.FileImg, UdeskUtil.objectToString(message.getContent()));
                    UdeskUtil.loadIntoFitSize(context, loaclpath, R.drawable.udesk_defualt_failure, R.drawable.udesk_defalut_image_loading, imgView);
                } else {
                    if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                        UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                        return;
                    }
                    ((UdeskChatActivity) mContext).showVideoThumbnail(message);
                }


//                imgView.setTag(message.getTime());
                imgView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (message == null) {
                            return;
                        }
                        String path = "";
                        if (!TextUtils.isEmpty(message.getLocalPath()) && UdeskUtil.isExitFileByPath(context,message.getLocalPath())) {
                            path = message.getLocalPath();
                        } else if (!TextUtils.isEmpty(UdeskUtil.objectToString(message.getContent()))) {
                            File file = UdeskUtil.getFileByUrl(mContext, UdeskConst.FileVideo, UdeskUtil.objectToString(message.getContent()));
                            if (file != null && UdeskUtil.getFileSize(file) > 0) {
                                path = file.getPath();
                            } else {
                                if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                                    UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                                    return;
                                }
                                ((UdeskChatActivity) mContext).downLoadVideo(message);
                                path = UdeskUtil.objectToString(message.getContent());
                            }
                        }

                        Intent intent = new Intent();
                        intent.setClass(mContext, PictureVideoPlayActivity.class);
                        Bundle data = new Bundle();
                        data.putString(UdeskConst.PREVIEW_Video_Path, path);
                        intent.putExtras(data);
                        mContext.startActivity(intent);
                    }
                });
                ivStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                            UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                            changeUiState(UdeskConst.SendFlag.RESULT_FAIL);
                            return;
                        }
                        message.setSendFlag(UdeskConst.SendFlag.RESULT_RETRY);
                        changeUiState(UdeskConst.SendFlag.RESULT_RETRY);
                        ((UdeskChatActivity) mContext).retrySendMsg(message);
                    }
                });

                cancleImg.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                            UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                            changeUiState(UdeskConst.SendFlag.RESULT_FAIL);
                            return;
                        }
                        message.setSendFlag(UdeskConst.SendFlag.RESULT_FAIL);
                        showFailureView();
                        ((UdeskChatActivity) mContext).cancleSendVideoMsg(message);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }
    }
    /**
     * 展示文件消息
     */
    public class FileViewHolder extends BaseViewHolder {
        public TextView fileTitle;
        public TextView fileSize;
        public TextView operater;
        public ProgressBar mProgress;
        public LinearLayout itemFile;

        @Override
        void bind(final Context context) {
            try {
                ExtrasInfo info = message.getExtras();
                String title = "";
                String size = "";
                String filetype = "";
                if (info != null) {
                    title = UdeskUtil.objectToString(info.getFilename());
                    size = UdeskUtil.objectToString(info.getFilesize());
                    filetype = UdeskUtil.objectToString(info.getFileext());
                }
                if (message.getDirection() == UdeskConst.ChatMsgDirection.Send) {
                    if (TextUtils.isEmpty(title)) {
                        fileTitle.setText(UdeskUtil.getFileName(mContext, message.getLocalPath()));
                    } else {
                        fileTitle.setText(title);
                    }

                    if (TextUtils.isEmpty(size)) {
                        fileSize.setText(UdeskUtil.getFileSizeByLoaclPath(mContext, message.getLocalPath()));
                    } else {
                        fileSize.setText(size);
                    }
                    if (message.getSendFlag() == UdeskConst.SendFlag.RESULT_SUCCESS) {
                        mProgress.setProgress(100);
                        operater.setText(mContext.getString(R.string.udesk_has_send));
                    } else {
                        mProgress.setProgress(message.getPrecent());
                        operater.setText(String.format("%s%%", String.valueOf(message.getPrecent())));
                    }
                } else {
                    fileTitle.setText(title);
                    fileSize.setText(size);
                    if (UdeskUtil.fileIsExitByUrl(mContext, UdeskConst.File_File, UdeskUtil.objectToString(message.getContent()))
                            && UdeskUtil.getFileSize(UdeskUtil.getFileByUrl(mContext, UdeskConst.File_File, UdeskUtil.objectToString(message.getContent()))) > 0) {
                        mProgress.setProgress(100);
                        operater.setText(mContext.getString(R.string.udesk_has_downed));
                    } else {
                        mProgress.setProgress(0);
                        operater.setText(mContext.getString(R.string.udesk_has_download));
                    }
                    operater.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((UdeskChatActivity) mContext).downLoadMsg(message);
                        }
                    });
                }

                itemFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            File file = null;
                            Uri contentUri;
                            String type;
                            if (message.getDirection() == UdeskConst.ChatMsgDirection.Send) {
                                if (UdeskUtil.isAndroidQ()) {
                                    contentUri = Uri.parse(UdeskUtil.getFilePathQ(mContext, message.getLocalPath()));
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                } else {
                                    file = new File(message.getLocalPath());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        contentUri = UdeskUtil.getOutputMediaFileUri(mContext, file);
                                    } else {
                                        contentUri = Uri.fromFile(file);
                                    }
                                }
                            } else {
                                file = UdeskUtil.getFileByUrl(mContext, UdeskConst.File_File, UdeskUtil.objectToString(message.getContent()));
                                if (file == null || UdeskUtil.getFileSizeQ(mContext.getApplicationContext(), file.getAbsolutePath()) <= 0) {
                                    Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.udesk_has_uncomplete_tip), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    contentUri = UdeskUtil.getOutputMediaFileUri(mContext, file);
                                } else {
                                    contentUri = Uri.fromFile(file);
                                }
                            }
                            if (contentUri == null) {
                                return;
                            }
                            if (message.getContent_type().equals(UdeskConst.ChatMsgTypeString.TYPE_VIDEO)) {
                                intent.setDataAndType(contentUri, "video/mp4");
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    type = UdeskUtil.getMIMEType(mContext, contentUri);
                                }else {
                                    type = UdeskUtil.getMIMEType(file);
                                }
                                intent.setDataAndType(contentUri, type);
                            }
                            mContext.startActivity(intent);
                        } catch (Exception e) {
                            if (!TextUtils.isEmpty(e.getMessage()) && e.getMessage().contains("No Activity found to handle Intent")) {
                                Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.udesk_no_app_handle), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                //设置重发按钮的点击事件
                ivStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                            UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                            changeUiState(UdeskConst.SendFlag.RESULT_FAIL);
                            return;
                        }
                        message.setSendFlag(UdeskConst.SendFlag.RESULT_RETRY);
                        changeUiState(UdeskConst.SendFlag.RESULT_RETRY);
                        ((UdeskChatActivity) mContext).retrySendMsg(message);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }
    }

    private void initItemNormalView(View convertView, BaseViewHolder holder) {
        try {
            holder.ivHeader = (ImageView) convertView.findViewById(R.id.udesk_iv_head);
            holder.tvTime = (TextView) convertView.findViewById(R.id.udesk_tv_time);
            holder.ivStatus = (ImageView) convertView.findViewById(R.id.udesk_iv_status);
            holder.pbWait = (ProgressBar) convertView.findViewById(R.id.udesk_im_wait);
            holder.agentnickName = (TextView) convertView.findViewById(R.id.udesk_nick_name);
            UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMTimeTextColorResId, holder.tvTime);
            UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMAgentNickNameColorResId, holder.agentnickName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算是否要显示当前位置消息的发送或接受时间
     */
    private void tryShowTime(BaseViewHolder holder,
                             ReceiveMessage info) {
        try {
            holder.tvTime.setVisibility(View.VISIBLE);
            if (info instanceof Products) {
                Products products = (Products) info;
                if (products.getSendTime() > 0) {
                    holder.tvTime.setVisibility(View.VISIBLE);
                    holder.tvTime.setText(UdeskUtil.formatLongTypeTimeToString(mContext, products.getSendTime()).trim());
                } else {
                    holder.tvTime.setVisibility(View.GONE);
                }
                return;
            }
            if (info.getCreated_at() != null) {
                holder.tvTime.setText(UdeskUtil.formatLongTypeTimeToString(mContext, UdeskUtil.objectToString(info.getCreated_at())).trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 根据消息ID  修改对应消息的状态
     */
    public boolean changeImState(View convertView, SendMsgResult sendMsgResult) {
        try {
            Object tag = convertView.getTag();
            if (tag != null && tag instanceof BaseViewHolder) {
                BaseViewHolder cache = (BaseViewHolder) tag;
                if (cache.message != null && sendMsgResult.getId().equals(UdeskUtil.objectToString(cache.message.getId()))) {
                    cache.changeUiState(sendMsgResult.getFlag());
                    if (!TextUtils.isEmpty(sendMsgResult.getCreateTime())) {
                        cache.setTvTime(sendMsgResult.getCreateTime());
                    }
                    cache.message.setSendFlag(sendMsgResult.getFlag());
                    cache.message.setUuid(sendMsgResult.getUuid());
                    cache.message.setCreated_at(sendMsgResult.getCreateTime());
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    /**
     * 根据消息ID  修改对应消息的状态
     */
    public void updateStatus(SendMsgResult sendMsgResult) {
        try {
            for (ReceiveMessage msg : list) {
                if (UdeskUtil.objectToString(msg.getId()) != null && UdeskUtil.objectToString(msg.getId()).equals(sendMsgResult.getId())) {
                    msg.setSendFlag(sendMsgResult.getFlag());
                    msg.setUuid(sendMsgResult.getUuid());
                    msg.setCreated_at(sendMsgResult.getCreateTime());
                }
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean changeVideoThumbnail(View convertView, String msgId) {
        try {
            Object tag = convertView.getTag();
            if (tag != null && tag instanceof SmallVideoViewHolder) {
                SmallVideoViewHolder cache = (SmallVideoViewHolder) tag;
                if (cache.message != null && msgId.equals(cache.message.getUuid())) {
                    if (UdeskUtil.fileIsExitByUrl(mContext, UdeskConst.FileImg, UdeskUtil.objectToString(cache.message.getContent()))) {
                        String loaclpath = UdeskUtil.getPathByUrl(mContext, UdeskConst.FileImg, UdeskUtil.objectToString(cache.message.getContent()));
                        UdeskUtil.loadIntoFitSize(mContext, loaclpath, R.drawable.udesk_defualt_failure, R.drawable.udesk_defalut_image_loading, cache.imgView);

                    }
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 根据消息ID  修改对应消息的进度
     */
    boolean changeFileState(View convertView, String msgId, int percent, long fileSize, boolean isSuccess) {
        try {
            Object tag = convertView.getTag();
            if (tag != null){
                BaseViewHolder holder = (BaseViewHolder) tag;
                if (holder.message != null && msgId.contains(UdeskUtil.objectToString(holder.message.getId()))) {
                    switch (UdeskConst.parseTypeForMessage(UdeskUtil.objectToString(holder.message.getContent_type()))) {
                        case UdeskConst.ChatMsgTypeInt.TYPE_FILE:
                            FileViewHolder fileViewHolder = (FileViewHolder) tag;
                            changeFileState(fileViewHolder,percent, fileSize, isSuccess);
                            return true;

                        case UdeskConst.ChatMsgTypeInt.TYPE_VIDEO:
                            SmallVideoViewHolder smallVideo = (SmallVideoViewHolder) tag;
                            smallVideo.circleProgressBar.setPercent(percent);
                            if (percent == 100) {
                                smallVideo.circleProgressBar.setVisibility(View.GONE);
                                smallVideo.video_tip.setVisibility(View.VISIBLE);
                                smallVideo.pbWait.setVisibility(View.GONE);
                            }
                            return true;

                        case UdeskConst.ChatMsgTypeInt.TYPE_IMAGE:
                            return true;
                        default:
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 文件状态改变
     *
     * @param percent
     * @param fileSize
     * @param isSuccess
     */
    public void changeFileState(FileViewHolder fileViewHolder,int percent, long fileSize, boolean isSuccess) {
        try {
            fileViewHolder.mProgress.setProgress(percent);
            if (percent == 100) {
                if (fileViewHolder.message.getDirection() == UdeskConst.ChatMsgDirection.Send) {
                    fileViewHolder.operater.setText(mContext.getString(R.string.udesk_has_send));
                } else {
                    fileViewHolder.operater.setText(mContext.getString(R.string.udesk_has_downed));
                }
            } else {
                if (0 < percent && percent < 100) {
                    fileViewHolder.operater.setText(String.format("%d%%", percent));
                }
            }
            if (fileSize > 0) {
                fileViewHolder.fileSize.setText(UdeskUtil.formetFileSize(fileSize));
            }
            if (!isSuccess) {
                Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.udesk_download_failure), Toast.LENGTH_SHORT).show();
                fileViewHolder.operater.setText(mContext.getString(R.string.udesk_has_download));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据消息ID  修改对应文件上传的进度
     */
    void updateProgress(String msgId, int present) {
        try {
            boolean isNeedRefresh = false;
            for (ReceiveMessage msg : list) {
                if (msg != null && msg.getContent_type() != null
                        && msg.getContent_type().equals(UdeskConst.ChatMsgTypeString.TYPE_VIDEO)
                        && msg.getId() != null
                        && UdeskUtil.objectToString(msg.getId()).equals(msgId)) {
//                    msg.setPrecent(present);
                    isNeedRefresh = true;
                }
            }
            if (isNeedRefresh) {
                notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
