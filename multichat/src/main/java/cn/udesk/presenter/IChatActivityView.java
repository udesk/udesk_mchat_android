package cn.udesk.presenter;

import android.content.Context;
import android.os.Handler;

import java.util.List;

import cn.udesk.model.Merchant;
import cn.udesk.muchat.bean.ReceiveMessage;

public interface IChatActivityView {
	
	Context getContext();
	
	CharSequence getInputContent();

	void clearInputContent();
	
	void addMessage(List<ReceiveMessage> messages,String fromUUID);

	Handler getHandler();
	
	void refreshInputEmjio(String s) ;
	
	List<String> getEmotionStringList();
	
	void onRecordSuccess(String filePath , long duration);

	String getEuid();

	void setMerchant(Merchant merchant);

}
