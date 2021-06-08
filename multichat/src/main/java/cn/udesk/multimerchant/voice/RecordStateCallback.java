package cn.udesk.multimerchant.voice;

public interface RecordStateCallback {
	
	 void readyToCancelRecord();

	 void doCancelRecord();

	 void readyToContinue();

	 void endRecord();

}
