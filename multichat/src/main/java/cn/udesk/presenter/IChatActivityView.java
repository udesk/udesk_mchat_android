package cn.udesk.presenter;

import android.content.Context;
import android.os.Handler;

import java.util.List;

import cn.udesk.model.Merchant;
import cn.udesk.model.SurveyOptionsModel;
import cn.udesk.muchat.bean.NavigatesResult;
import cn.udesk.muchat.bean.ReceiveMessage;

public interface IChatActivityView {
	
	Context getContext();
	
	CharSequence getInputContent();

	void clearInputContent();
	
	void addMessage(List<ReceiveMessage> messages,String fromUUID);

	Handler getHandler();


	String getEuid();

	void setMerchant(Merchant merchant);

	void checkConnect();

	void setIsPermmitSurvy(boolean isPermmitSurvy);

	void setSurvyOption(SurveyOptionsModel model);

	SurveyOptionsModel getSurvyOption();

	void addNavigatesResult(NavigatesResult navigatesResult);
	void showNavigatesMenu(NavigatesResult navigatesResult,boolean isShow);
	List<ReceiveMessage> getNavigatesChatCache();
	void showNavigatesItemMenu(NavigatesResult.DataBean.GroupMenusBean item);
}
