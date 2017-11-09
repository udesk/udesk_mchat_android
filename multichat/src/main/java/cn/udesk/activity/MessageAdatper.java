package cn.udesk.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskUtil;
import cn.udesk.adapter.UDEmojiAdapter;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskConfig;
import cn.udesk.model.Merchant;
import cn.udesk.muchat.bean.ExtrasInfo;
import cn.udesk.muchat.bean.Products;
import cn.udesk.muchat.bean.ReceiveMessage;
import cn.udesk.model.SendMsgResult;

import static android.util.Patterns.PHONE;
import static android.util.Patterns.WEB_URL;

public class MessageAdatper extends BaseAdapter {
    private static final int[] layoutRes = {
            R.layout.udesk_chat_msg_item_txt_l,//文本消息左边的UI布局文件
            R.layout.udesk_chat_msg_item_txt_r,//文本消息右边的UI布局文件
            R.layout.udesk_chat_msg_item_audiot_l,//语音消息左边的UI布局文件
            R.layout.udesk_chat_msg_item_audiot_r,//语音消息右边的UI布局文件
            R.layout.udesk_chat_msg_item_imgt_l,//图片消息左边的UI布局文件
            R.layout.udesk_chat_msg_item_imgt_r,//图片消息右边的UI布局文件
            R.layout.udesk_chat_rich_item_txt,//富文本消息UI布局文件
            R.layout.udesk_im_commodity_item,  //显示商品信息的UI布局文件
            R.layout.udesk_chat_leavemsg_item_txt_l,//显示留言发送消息
            R.layout.udesk_chat_leavemsg_item_txt_r, // 显示收到留言消息的回复
            R.layout.udesk_chat_event_item, // 显示收到留言消息的回复
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
    public void addItem(ReceiveMessage message) {
        if (message == null) {
            return;
        }
        //不是撤回消息则过滤含有相同msgID的消息，如果是撤回消息则替换掉
        try {
//            for (ReceiveMessage info : list) {
//                if (!TextUtils.isEmpty(UdeskUtil.objectToString(message.getUuid())) &&
//                        !TextUtils.isEmpty(UdeskUtil.objectToString(info.getUuid())) &&
//                        UdeskUtil.objectToString(message.getUuid()).equals(UdeskUtil.objectToString(info.getUuid()))) {
//
//                    if (message.getSend_status().equals("rollback")) {
//                        list.remove(info);
//                        break;
//                    }
//                    return;
//
//                }
//            }
            list.add(message);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void listAddItems(List<ReceiveMessage> messages, boolean isMore) {
        try {
            if (messages == null) {
                return;
            }
            if (isMore) {
                list.addAll(0, messages);
            } else {
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
                        holder.tvMsg = (TextView) convertView.findViewById(R.id.udesk_tv_msg);
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
                        holder.imgView = (SimpleDraweeView) convertView.findViewById(R.id.udesk_im_image);
                        convertView.setTag(holder);
                        break;
                    }
                    case COMMODITY: {
                        CommodityViewHolder holder = new CommodityViewHolder();
                        holder.rootView = convertView.findViewById(R.id.udesk_commit_root);
                        holder.tvTime = (TextView) convertView.findViewById(R.id.udesk_tv_time);
                        holder.thumbnail = (SimpleDraweeView) convertView
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
                        eventViewHolder.events = (TextView) convertView.findViewById(R.id.udesk_event);
                        convertView.setTag(eventViewHolder);
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return convertView;
    }


    abstract class BaseViewHolder {
        public SimpleDraweeView ivHeader;
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
                        this.isLeft = false;
                        if (!TextUtils.isEmpty(UdeskConfig.customerUrl)) {
                            UdeskUtil.loadHeadView(mContext, ivHeader, Uri.parse(UdeskConfig.customerUrl));
                        }
                        break;

                    case MSG_TXT_L:
                    case MSG_AUDIO_L:
                    case RICH_TEXT:
                    case MSG_IMG_L:
                        this.isLeft = true;
                        Merchant merchant = ((UdeskChatActivity) mContext).getMerchant();
                        if (merchant != null && !TextUtils.isEmpty(UdeskUtil.objectToString(merchant.getLogo_url()))) {
                            UdeskUtil.loadHeadView(mContext, ivHeader, Uri.parse(UdeskUtil.objectToString(merchant.getLogo_url())));
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
            tvTime.setText(UdeskUtil.formatLongTypeTimeToString(mContext, create));
        }

        abstract void bind(Context context);

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
        public TextView tvMsg;

        @Override
        void bind(Context context) {
            try {
                //设置文本消息内容，表情符转换对应的表情,没表情的另外处理
                if (UDEmojiAdapter.replaceEmoji(context, UdeskUtil.objectToString(message.getContent()),
                        (int) tvMsg.getTextSize()) != null) {
                    tvMsg.setText(UDEmojiAdapter.replaceEmoji(context, UdeskUtil.objectToString(message.getContent()),
                            (int) tvMsg.getTextSize()));
                } else {
                    tvMsg.setText(UdeskUtil.objectToString(message.getContent()));
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
                if (UDEmojiAdapter.replaceEmoji(context, UdeskUtil.objectToString(message.getContent()),
                        (int) tvMsg.getTextSize()) != null) {
                    tvMsg.setText(UDEmojiAdapter.replaceEmoji(context, UdeskUtil.objectToString(message.getContent()),
                            (int) tvMsg.getTextSize()));
                } else {
                    tvMsg.setText(UdeskUtil.objectToString(message.getContent()));
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
        public SimpleDraweeView imgView;

        @Override
        void bind(Context context) {
            try {
                if (!TextUtils.isEmpty(message.getLocalPath()) && UdeskUtil.isExitFileByPath(message.getLocalPath())) {
                    int[] wh = UdeskUtil.getImageWH(message.getLocalPath());
                    UdeskUtil.loadFileFromSdcard(context, imgView, Uri.fromFile(new File(message.getLocalPath())), wh[0], wh[1]);
                } else {
                    UdeskUtil.loadImageView(context, imgView, Uri.parse(UdeskUtil.objectToString(message.getContent())));
                }

                imgView.setTag(message.getCreated_at());
                imgView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (message == null) {
                            return;
                        }
                        Uri imgUri = null;
                        if (!TextUtils.isEmpty(message.getLocalPath())) {
                            imgUri = Uri.fromFile(new File(message.getLocalPath()));
                        } else if (!TextUtils.isEmpty(UdeskUtil.objectToString(message.getContent()))) {
                            imgUri = Uri.parse(UdeskUtil.objectToString(message.getContent()));
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

        public TextView events;

        @Override
        void bind(Context context) {
            try {
                tvTime.setVisibility(View.VISIBLE);
                if (!UdeskUtil.objectToString(message.getCreated_at()).isEmpty()) {
                    tvTime.setText("----" + UdeskUtil.parseEventTime(UdeskUtil.objectToString(message.getCreated_at())) + "----");
                }
                events.setText(UdeskUtil.objectToString(message.getContent()));
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
        public SimpleDraweeView thumbnail;
        public TextView title;
        public TextView subTitle;
        public TextView link;

        @Override
        void bind(Context context) {
            try {
                final Products item = (Products) message;
                title.setText(item.getProduct().getTitle());
                List<Products.ProductBean.ExtrasBean> extrasBeens = item.getProduct().getExtras();
                if (extrasBeens != null && extrasBeens.size() > 0) {
                    subTitle.setText(extrasBeens.get(0).getTitle() + ": " + extrasBeens.get(0).getContent());
                }

                UdeskUtil.loadNoChangeView(thumbnail, Uri.parse(item.getProduct().getImage()));
                link.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((UdeskChatActivity) mContext).sentLink(item.getProduct().getUrl());
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
            holder.ivHeader = (SimpleDraweeView) convertView.findViewById(R.id.udesk_iv_head);
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
                holder.tvTime.setVisibility(View.GONE);
                holder.tvTime.setText(UdeskUtil.formatLongTypeTimeToString(mContext, System.currentTimeMillis()));

                return;
            }
            if (info.getCreated_at() != null) {
                holder.tvTime.setText(UdeskUtil.formatLongTypeTimeToString(mContext, UdeskUtil.objectToString(info.getCreated_at())));
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
                }
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
