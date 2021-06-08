package cn.udesk.multimerchant.camera.callback;

import android.graphics.Bitmap;


public interface StopRecordCallback {

    void recordResult(String url, Bitmap firstFrame);
}
