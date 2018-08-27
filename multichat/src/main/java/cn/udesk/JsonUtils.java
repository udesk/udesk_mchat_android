package cn.udesk;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.model.InitMode;
import cn.udesk.model.Merchant;
import cn.udesk.model.OptionsModel;
import cn.udesk.model.SurveyOptionsModel;
import cn.udesk.model.Tag;
import cn.udesk.muchat.bean.AliBean;
import cn.udesk.muchat.bean.ExtrasInfo;
import cn.udesk.muchat.bean.ReceiveMessage;
import cn.udesk.model.SendMsgResult;
import udesk.core.utils.BaseUtils;

public class JsonUtils {

    public static List<ReceiveMessage> parserMessages(String receives) {

        List<ReceiveMessage> receiveMessagess = new ArrayList<>();
        try {
            JSONObject object = new JSONObject(receives);
            if (object.has("messages")) {
                JSONArray jsonArray = object.getJSONArray("messages");
                if (jsonArray != null && jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {

                        ReceiveMessage receiveMessage = parserReceiveMessage(jsonArray.getString(i));
                        receiveMessage.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
                        if (UdeskUtil.objectToString(receiveMessage.getCategory()).equals("event")
                                && UdeskUtil.objectToString(receiveMessage.getContent()).equals("客服发送满意度调查")) {
                            continue;
                        }
                        receiveMessagess.add(receiveMessage);
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return receiveMessagess;
    }

    public static ReceiveMessage parserReceiveMessage(String receive) {
        ReceiveMessage receiveMessage = new ReceiveMessage();
        try {
            JSONObject content = new JSONObject(receive);
            receiveMessage.setId(content.opt("id"));
            receiveMessage.setUuid(content.opt("uuid"));
            receiveMessage.setMerchant_id(content.opt("merchant_id"));
            receiveMessage.setCategory(content.opt("category"));
            receiveMessage.setEvent_name(content.opt("event_name"));
            receiveMessage.setDirection(content.opt("direction"));
            receiveMessage.setContent_type(content.opt("content_type"));
            receiveMessage.setContent(content.opt("content"));
            receiveMessage.setCreated_at(content.opt("created_at"));
            receiveMessage.setSent_at(content.opt("sent_at"));
            receiveMessage.setRead_at(content.opt("read_at"));
            receiveMessage.setFailed_at(content.opt("failed_at"));
            receiveMessage.setMerchant_euid(content.opt("merchant_euid"));
            if (content.has("extras")) {
                JSONObject extrasObject = new JSONObject(content.getString("extras"));
                ExtrasInfo info = new ExtrasInfo();
                info.setDuration(extrasObject.opt("duration"));
                receiveMessage.setExtras(info);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return receiveMessage;
    }

    public static InitMode parserInitMessage(String init) {
        InitMode initMode = new InitMode();
        try {
            JSONObject initObject = new JSONObject(init);
            if (initObject.has("customer")) {
                JSONObject customerJson = new JSONObject(initObject.getString("customer"));
                initMode.setName(customerJson.opt("name"));
                initMode.setEuid(customerJson.opt("euid"));
                initMode.setIm_username(customerJson.opt("im_username"));
                initMode.setIm_password(customerJson.opt("im_password"));
            }
            if (initObject.has("im")) {
                JSONObject imJson = new JSONObject(initObject.getString("im"));
                initMode.setTcp_port(imJson.opt("tcp_port"));
                initMode.setTcp_server(imJson.opt("tcp_server"));
            }
            if (initObject.has("oss")) {
                JSONObject ossJson = new JSONObject(initObject.getString("oss"));
                initMode.setEndpoint(ossJson.opt("endpoint"));
                initMode.setBucket(ossJson.opt("bucket"));
                initMode.setAccess_id(ossJson.opt("access_id"));
                initMode.setPrefix(ossJson.opt("prefix"));
            }
//            if (initObject.has("is_open_survey")) {
//                initMode.setIs_open_survey(initObject.opt("is_open_survey"));
//            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return initMode;
    }

    /**
     * 最近商户列表
     */
    public static List<Merchant> parseRecentMerchants(String merchantStr) {
        List<Merchant> merchantList = new ArrayList<>();
        try {
            JSONObject object = new JSONObject(merchantStr);
            if (object.has("merchants")) {
                JSONArray jsonArray = object.getJSONArray("merchants");
                if (jsonArray != null && jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Merchant merchant = new Merchant();
                        JSONObject merchantObj = (JSONObject) jsonArray.get(i);
                        if (merchantObj.has("euid")) {
                            merchant.setEuid(merchantObj.get("euid"));
                        }
                        if (merchantObj.has("name")) {
                            merchant.setName(merchantObj.get("name"));
                        }
                        if (merchantObj.has("im_username")) {
                            merchant.setIm_username(merchantObj.get("im_username"));
                        }
                        if (merchantObj.has("unread_count")) {
                            merchant.setUnread_count(merchantObj.get("unread_count"));
                        }
                        if (merchantObj.has("logo_url")) {
                            merchant.setLogo_url(merchantObj.get("logo_url"));
                        }
                        if (merchantObj.has("on_duty")) {
                            merchant.setOn_duty(merchantObj.get("on_duty"));
                        }
                        if (merchantObj.has("off_duty_tips")) {
                            merchant.setOff_duty_tips(merchantObj.get("off_duty_tips"));
                        }
                        if (merchantObj.has("last_message")) {
                            JSONObject lastMsgJSONObject = (JSONObject) merchantObj.get("last_message");
                            ReceiveMessage message = new ReceiveMessage();
                            if (lastMsgJSONObject.has("uuid")) {
                                message.setUuid(lastMsgJSONObject.get("uuid"));
                            }
                            if (lastMsgJSONObject.has("direction")) {
                                message.setDirection(lastMsgJSONObject.get("direction"));
                            }
                            if (lastMsgJSONObject.has("content")) {
                                message.setContent(lastMsgJSONObject.get("content"));
                            }
                            if (lastMsgJSONObject.has("content_type")) {
                                message.setContent_type(lastMsgJSONObject.get("content_type"));
                            }
                            if (lastMsgJSONObject.has("category")) {
                                message.setCategory(lastMsgJSONObject.get("category"));
                            }
                            if (lastMsgJSONObject.has("created_at")) {
                                message.setCreated_at(lastMsgJSONObject.get("created_at"));
                            }
                            if (lastMsgJSONObject.has("sent_at")) {
                                message.setSent_at(lastMsgJSONObject.get("sent_at"));
                            }
                            if (lastMsgJSONObject.has("read_at")) {
                                message.setRead_at(lastMsgJSONObject.get("read_at"));
                            }
                            merchant.setLast_message(message);
                        }
                        merchantList.add(merchant);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return merchantList;
    }

    /**
     * 获取商户详情
     */
    public static Merchant parseMerchantDetail(String detailStr) {
        Merchant merchant = new Merchant();
        try {
            JSONObject object = new JSONObject(detailStr);
            if (object.has("merchant")) {
                JSONObject merchantObj = (JSONObject) object.get("merchant");
                if (merchantObj.has("euid")) {
                    merchant.setEuid(merchantObj.get("euid"));
                }
                if (merchantObj.has("name")) {
                    merchant.setName(merchantObj.get("name"));
                }
                if (merchantObj.has("im_username")) {
                    merchant.setIm_username(merchantObj.get("im_username"));
                }
                if (merchantObj.has("unread_count")) {
                    merchant.setUnread_count(merchantObj.get("unread_count"));
                }
                if (merchantObj.has("logo_url")) {
                    merchant.setLogo_url(merchantObj.get("logo_url"));
                }
                if (merchantObj.has("on_duty")) {
                    merchant.setOn_duty(merchantObj.get("on_duty"));
                }
                if (merchantObj.has("off_duty_tips")) {
                    merchant.setOff_duty_tips(merchantObj.get("off_duty_tips"));
                }
                if (merchantObj.has("last_message")) {
                    JSONObject lastMsgJSONObject = (JSONObject) merchantObj.get("last_message");
                    ReceiveMessage message = new ReceiveMessage();
                    if (lastMsgJSONObject.has("uuid")) {
                        message.setUuid(lastMsgJSONObject.get("uuid"));
                    }
                    if (lastMsgJSONObject.has("direction")) {
                        message.setDirection(lastMsgJSONObject.get("direction"));
                    }
                    if (lastMsgJSONObject.has("content")) {
                        message.setContent(lastMsgJSONObject.get("content"));
                    }
                    if (lastMsgJSONObject.has("content_type")) {
                        message.setContent_type(lastMsgJSONObject.get("content_type"));
                    }
                    if (lastMsgJSONObject.has("category")) {
                        message.setCategory(lastMsgJSONObject.get("category"));
                    }
                    if (lastMsgJSONObject.has("created_at")) {
                        message.setCreated_at(lastMsgJSONObject.get("created_at"));
                    }
                    if (lastMsgJSONObject.has("sent_at")) {
                        message.setSent_at(lastMsgJSONObject.get("sent_at"));
                    }
                    if (lastMsgJSONObject.has("read_at")) {
                        message.setRead_at(lastMsgJSONObject.get("read_at"));
                    }
                    merchant.setLast_message(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return merchant;
    }

    public static SendMsgResult getCreateTime(String receive) {
        SendMsgResult result = new SendMsgResult();
        try {
            JSONObject content = new JSONObject(receive);
            if (content.has("message")) {
                JSONObject msgObject = content.getJSONObject("message");
                result.setCreateTime(UdeskUtil.objectToString(msgObject.opt("created_at")));
                result.setUuid(UdeskUtil.objectToString(msgObject.opt("uuid")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static AliBean parseAlInfo(String message) {
        AliBean aliBean = new AliBean();
        try {
            JSONObject content = new JSONObject(message);
            aliBean.setAccess_id(content.opt("access_id"));
            aliBean.setEndpoint(content.opt("endpoint"));
            aliBean.setBucket(content.opt("bucket"));
            aliBean.setPrefix(content.opt("prefix"));
            aliBean.setPolicy_Base64(content.opt("policy_Base64"));
            aliBean.setExpire_at(content.opt("expire_at"));
            aliBean.setSignature(content.opt("signature"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return aliBean;
    }

    //解析设置的满意度调查选项
    public static SurveyOptionsModel parseSurveyOptions(String response) {

        SurveyOptionsModel optionsMode = new SurveyOptionsModel();
        if (TextUtils.isEmpty(response)) {
            return optionsMode;
        }
        try {
            JSONObject json = new JSONObject(response);
            JSONObject result = json.getJSONObject("im_survey");
            if (result.has("enabled")) {
                optionsMode.setEnabled(result.opt("enabled"));
            }
            if (result.has("remark_enabled")) {
                optionsMode.setRemark_enabled(result.opt("remark_enabled"));
            }
            if (result.has("remark")) {
                optionsMode.setRemark(result.opt("remark"));
            }
            if (result.has("name")) {
                optionsMode.setName(result.opt("name"));
            }
            if (result.has("title")) {
                optionsMode.setTitle(result.opt("title"));
            }
            if (result.has("desc")) {
                optionsMode.setDesc(result.opt("desc"));
            }
            if (result.has("show_type")) {
                optionsMode.setType(result.opt("show_type"));
            }

            if (result.has("methods")) {
                JSONArray methodsArray = result.optJSONArray("methods");
                if (methodsArray != null && methodsArray.length() > 0) {
                    for (int i = 0; i < methodsArray.length(); i++) {
                        JSONObject data = methodsArray.optJSONObject(i);
                        if (data.has("flag")) {
                            if (BaseUtils.objectToString(data.opt("flag")).equals("after_session")) {
                                optionsMode.setAfter_session(data.opt("enabled"));
                            }

                            if (BaseUtils.objectToString(data.opt("flag")).equals("customer_invite")) {
                                optionsMode.setCustomer_invite(data.opt("enabled"));
                            }
                        }

                    }
                }
            }

            JSONObject contextObject = null;
            if (optionsMode.getType().equals("text") && result.has("text")) {
                contextObject = result.getJSONObject("text");
            } else if (optionsMode.getType().equals("expression") && result.has("expression")) {
                contextObject = result.getJSONObject("expression");
            } else if (optionsMode.getType().equals("star") && result.has("star")) {
                contextObject = result.getJSONObject("star");
            }

            if (contextObject != null) {

                if (contextObject.has("default_option_id")) {
                    optionsMode.setDefault_option_id(contextObject.opt("default_option_id"));
                }
                if (contextObject.has("options")) {
                    List<OptionsModel> options = new ArrayList<OptionsModel>();
                    JSONArray optionsArray = contextObject.optJSONArray("options");
                    if (optionsArray != null && optionsArray.length() > 0) {
                        for (int i = 0; i < optionsArray.length(); i++) {
                            JSONObject data = optionsArray.optJSONObject(i);
                            OptionsModel optionItem = new OptionsModel();
                            optionItem.setId(data.opt("id"));
                            optionItem.setEnabled(data.opt("enabled"));
                            if (!optionItem.getEnabled() && optionsMode.getType().equals("text")) {
                                continue;
                            }
                            optionItem.setText(data.opt("text"));
                            optionItem.setDesc(data.opt("desc"));
                            optionItem.setRemark_option(data.opt("remark_option"));

                            if (data.has("tags")) {
                                List<Tag> tags = new ArrayList<Tag>();
                                String tagStirng = UdeskUtil.objectToString(data.opt("tags"));
                                if (!TextUtils.isEmpty(tagStirng)) {
                                    String[] tagsArray = tagStirng.split(",");
                                    for (int k = 0; k < tagsArray.length; k++) {
                                        Tag tag = new Tag();
                                        tag.setText(tagsArray[k]);
                                        tags.add(tag);
                                    }
                                }
                                optionItem.setTags(tags);
                            }
                            options.add(optionItem);
                        }
                    }
                    optionsMode.setOptions(options);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return optionsMode;

    }
}
