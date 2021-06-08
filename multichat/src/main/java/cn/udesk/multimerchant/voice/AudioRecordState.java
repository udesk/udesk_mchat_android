
package cn.udesk.multimerchant.voice;

public interface AudioRecordState {
     void onRecordSuccess(final String resultFilePath , long time);

     void onRecordingError();

     void onRecordSaveError();

     void onRecordTooShort();
    
     void onRecordCancel();
    
     void updateRecordState(int micAmplitude);
    
     void onRecordllegal();
}
