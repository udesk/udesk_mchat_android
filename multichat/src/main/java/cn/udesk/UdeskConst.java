package cn.udesk;


public class UdeskConst {

    /**
     * 上传图片的后缀
     */
    public final static String ORIGINAL_SUFFIX = "_upload.jpg";

    /**
     * 录音声音大小的分段
     */
    public final static int recordStateNum = 8;

    public static final String Euid = "euid";
    public static final String WELCOME_URL = "welcome_url";


    public static final String EXTERNAL_CACHE_FOLDER = "udeskcache";

    public static final String FileImg = "image";
    public static final String FileVideo = "video";
    public static final String PICTURE = "picture";
    public static final String FileAudio = "aduio";

    public final static String AUDIO_SUF_WAV= ".wav";
    //    public final static String AUDIO_SUF = ".aac";
    public final static String IMG_SUF = ".jpg";
    public final static String VIDEO_SUF = ".mp4";

    public static final String Camera_Error = "camera_error";
    public static final String SEND_BUNDLE = "udesk_send_bundle";
    public static final String SEND_SMALL_VIDEO = "send_small_aideo";
    public static final String SMALL_VIDEO = "small_video";
    public static final String PREVIEW_Video_Path = "udeskkeyVideoPath";
    public static final String BitMapData = "bitmap_data";
    public static final String PREVIEW_PHOTO_IS_ALL = "udesk_preview_is_all";
    public static final String SEND_PHOTOS = "udesk_send_photo";
    public static final String SEND_PHOTOS_IS_ORIGIN = "udesk_send_is_origin";
    public static final String PREVIEW_PHOTO_INDEX = "udeskkeyOfPreviewPhotoIndex";
    public static final String IS_SEND = "udesk_is_send";

    public final static String EXTERNAL_FOLDER = "udeskmchat";
    public static final String FileEmotion = "emotion";
    public static int count = 9;
    public static class UdeskSurvyShowType {
        public static final int TEXT = 1;
        public static final int EXPRESSION = 2;
        public static final int STAR = 3;
    }

    public static final String REMARK_OPTION_HIDE = "hide";
    public static final String REMARK_OPTION_REQUIRED = "required";
    public static final String REMARK_OPTION_OPTIONAL = "optional";

    public static class SendFlag {
        public final static int RESULT_SEND = 0;// 这个是默认值 发送中
        public final static int RESULT_SUCCESS = 1;// 发送成功
        public final static int RESULT_RETRY = 2;
        public final static int RESULT_FAIL = 3;// 发送失败
    }

    public static class ChatMsgDirection {
        public static final String Send = "in";
        public static final String Recv = "out";
    }

    public static class ChatMsgReadFlag {
        public static final int read = 0;
        public static final int unread = 1;
    }

    public static class ChatMsgTypeInt {
        public static final int TYPE_IMAGE = 0;
        public static final int TYPE_AUDIO = 1;
        public static final int TYPE_TEXT = 2;
        public static final int TYPE_REDIRECT = 3;
        public static final int TYPE_RICH = 4;
        public static final int TYPE_LEAVEMSG = 6;
        public static final int TYPE_EVENT = 7;
        public static final int TYPE_PRODUCT = 8;
        public static final int TYPE_VIDEO = 9;
        public static final int TYPE_STRUCT = 10;

    }

    public static int parseTypeForMessage(String type) {
        if ("text".equalsIgnoreCase(type)) {
            return ChatMsgTypeInt.TYPE_TEXT;
        } else if ("image".equalsIgnoreCase(type)) {
            return ChatMsgTypeInt.TYPE_IMAGE;
        } else if ("audio".equalsIgnoreCase(type)) {
            return ChatMsgTypeInt.TYPE_AUDIO;
        } else if ("redirect".equalsIgnoreCase(type)) {
            return ChatMsgTypeInt.TYPE_REDIRECT;
        } else if ("rich".equalsIgnoreCase(type)) {
            return ChatMsgTypeInt.TYPE_RICH;
        }else if ("product".equals(type)){
            return ChatMsgTypeInt.TYPE_PRODUCT;
        }else if ("video".equals(type)){
            return ChatMsgTypeInt.TYPE_VIDEO;
        } else if ("struct".equalsIgnoreCase(type)) {
            return ChatMsgTypeInt.TYPE_STRUCT;
        }
        return ChatMsgTypeInt.TYPE_TEXT;
    }

    public static class ChatMsgTypeString {

        public static final String TYPE_IMAGE = "image";
        public static final String TYPE_AUDIO = "audio";
        public static final String TYPE_TEXT = "text";
        public static final String TYPE_EVENT = "event";
        public static final String TYPE_PRODUCT = "product";
        public static final String TYPE_VIDEO = "video";
        public static final String TYPE_STRUCT = "struct";
    }

    public static class UdeskFunctionFlag {
        public static final int Udesk_Camera = 1;
        public static final int Udesk_Photo = 2;
        public static final int Udesk_Survy = 3;
        public static final int Udesk_Message_Center = 4;
        public static final int Udesk_Video = 5;

    }

}
