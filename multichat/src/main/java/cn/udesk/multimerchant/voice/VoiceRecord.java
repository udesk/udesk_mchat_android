package cn.udesk.multimerchant.voice;public interface VoiceRecord {     void initResource(String filePath, AudioRecordState state);     void startRecord();    void stopRecord();     void cancelRecord();     void receycleResource();     long getMaxAmplitude();}