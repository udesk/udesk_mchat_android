package cn.udesk.multimerchant.camera.callback;

import android.graphics.Bitmap;


public interface UdeskCameraListener {

    void captureSuccess(Bitmap bitmap);

    void recordSuccess(String url, Bitmap firstFrame);

}
