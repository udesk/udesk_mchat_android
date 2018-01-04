package cn.udesk.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.udesk.R;
import cn.udesk.UdeskUtil;


public class UdeskZoomImageActivty extends Activity implements
        OnClickListener {

    private PhotoView zoomImageView;
    private View saveIdBtn;
    private Uri uri;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        try {
            setContentView(R.layout.udesk_zoom_imageview);
            zoomImageView = (PhotoView) findViewById(R.id.udesk_zoom_imageview);
            Bundle bundle = getIntent().getExtras();
            uri = bundle.getParcelable("image_path");
            loadInto(getApplicationContext(),uri.toString(),R.drawable.udesk_defualt_failure,R.drawable.udesk_defalut_image_loading,zoomImageView);
//            Glide.with(getApplicationContext())
//                    .load(uri)
//                    .diskCacheStrategy(DiskCacheStrategy.SOURCE ).error(R.drawable.udesk_defualt_failure)
//                    .into(zoomImageView);
            saveIdBtn = findViewById(R.id.udesk_zoom_save);
            saveIdBtn.setOnClickListener(this);
            zoomImageView.setOnPhotoTapListener(new OnPhotoTapListener() {
                @Override
                public void onPhotoTap(ImageView view, float x, float y) {
                    finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }


    public static void loadInto(final Context context, final String imageUrl, int errorImageId, int placeHolder, final ImageView imageView) {
        final int screenWidth = UdeskUtil.getScreenWidth(context.getApplicationContext());
        final int imgWidth = screenWidth ;
        final int imgHight = UdeskUtil.getScreenHeight(context);
        SimpleTarget<GlideDrawable> target = new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                if (resource == null || TextUtils.equals(imageUrl, (String) imageView.getTag())) {
                    return;
                }
                imageView.setTag(imageUrl);
                int imageWidth = resource.getIntrinsicWidth();
                int imageHeight = resource.getIntrinsicHeight();

                double widthRatio = (double) imageWidth / imgWidth;
                double heightRatio = (double) imageHeight / imgHight;

                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                Log.i("xxxxxxx", "screenWidth = " + screenWidth + "screenHeight = " + UdeskUtil.getScreenHeight(context) +"; imageWidth=" + imageWidth + "   ;imageHeight=" + imageHeight + ";imgWidth=" + imgWidth + ";=imgHight" + imgHight + "; bitScalew= ");

                if (heightRatio>1){
                    layoutParams.height = (int) (imageHeight / heightRatio);
                }else {
                    layoutParams.height = imgHight/2;
                }

                if (widthRatio >1){
                    layoutParams.width = (int) (imageWidth / widthRatio);
                }else{
                    layoutParams.width = imgWidth;
                }

                imageView.setLayoutParams(layoutParams);
                imageView.setImageDrawable(resource);
//                imageView.invalidate();
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                Exception exception = e;
                super.onLoadFailed(e, errorDrawable);
            }
        };

        Glide.with(context.getApplicationContext())
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(placeHolder)
                .error(errorImageId)
                .override(UdeskUtil.getScreenWidth(context), UdeskUtil.getScreenHeight(context))
                .into(target);
//                .into(imageView);
    }


    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.udesk_zoom_save) {
                new Thread() {
                    public void run() {
                        saveImage();
                    }

                }.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveImage() {
        if (uri == null) {
            return;
        }
        try {
//            File oldFile = UdeskUtil.getFileFromDiskCache(uri);
            File oldFile = Glide.with(this)
                    .load(uri)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
            if (oldFile == null) {
                String oldPath = uri.getPath();
                oldFile = new File(oldPath);
            }
            // 修改文件路径
            String newName = oldFile.getName();
            if (!newName.contains(".png")) {
                newName = newName + ".png";
            }
            final File folder = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            final File newFile = new File(folder, newName);
            // 拷贝，成功或者失败 都提示下
            if (copyFile(oldFile, newFile)) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(
                                UdeskZoomImageActivty.this,
                                getResources().getString(
                                        R.string.udesk_success_save_image) + folder.getAbsolutePath(),
                                Toast.LENGTH_LONG).show();
                        UdeskZoomImageActivty.this.finish();
                    }
                });

            } else {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(
                                UdeskZoomImageActivty.this,
                                getResources().getString(
                                        R.string.udesk_fail_save_image),
                                Toast.LENGTH_SHORT).show();
                        UdeskZoomImageActivty.this.finish();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    private boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            if (destFile.exists()) {
                destFile.delete();
            }
            FileOutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.flush();
                try {
                    out.getFD().sync();
                } catch (Exception e) {
                } finally {
                    out.close();
                    inputStream.close();
                }

            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
