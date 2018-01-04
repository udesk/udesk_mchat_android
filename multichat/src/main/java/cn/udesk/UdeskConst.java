package cn.udesk;


public class UdeskConst {

    /**
     * 上传图片的后缀
     */
    public final static String ORIGINAL_SUFFIX = "_upload.jpg";
    /**
     * 语音的后缀
     */
    public final static String AUDIO_SUF = ".aac";

    /**
     * 录音声音大小的分段
     */
    public final static int recordStateNum = 8;

    public static final String Euid = "euid";
    public static final String WELCOME_URL = "welcome_url";





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
        }
        return ChatMsgTypeInt.TYPE_TEXT;
    }

    public static class ChatMsgTypeString {

        public static final String TYPE_IMAGE = "image";
        public static final String TYPE_AUDIO = "audio";
        public static final String TYPE_TEXT = "text";
        public static final String TYPE_EVENT = "event";


    }



}
