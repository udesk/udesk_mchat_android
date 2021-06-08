package cn.udesk.multimerchant.camera.callback;

import android.graphics.Bitmap;


public interface TakePictureCallback {
    void captureResult(Bitmap bitmap, boolean isVertical);
}
