package cn.udesk.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
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
import java.io.OutputStream;

import cn.udesk.LoaderTask;
import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;


public class UdeskZoomImageActivty extends UdeskBaseActivity implements
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
            loadInto(getApplicationContext(), uri.toString(), R.drawable.udesk_defualt_failure, R.drawable.udesk_defalut_image_loading, zoomImageView);
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
        final int imgWidth = screenWidth;
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

                if (heightRatio >= 1) {
                    layoutParams.height = (int) (imageHeight / heightRatio);
                } else {
                    layoutParams.height = imgHight / 2;
                }

                if (widthRatio > 1) {
                    layoutParams.width = (int) (imageWidth / widthRatio);
                } else {
                    layoutParams.width = imgWidth;
                }

                imageView.setLayoutParams(layoutParams);
                imageView.setImageDrawable(resource);
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
                LoaderTask.getThreadPoolExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        saveImageQ();

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveImageQ() {
        if (uri == null) {
            showFail();
            return;
        }
        try {
            File oldFile = Glide.with(this)
                    .load(uri)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
            if (UdeskUtil.isAndroidQ()) {
                Uri cacheFileUri = null;
                if (oldFile == null) {
                    if ("content".equalsIgnoreCase(uri.getScheme())) {
                        cacheFileUri = uri;
                    } else {
                        showFail();
                        return;
                    }
                } else {
                    cacheFileUri = UdeskUtil.getOutputMediaFileUri(this, oldFile);
                }
                if (cacheFileUri == null) {
                    showFail();
                    return;
                }
                String newName = cacheFileUri.getPath().substring(cacheFileUri.getPath().lastIndexOf("/") + 1);
                if (!newName.contains(".png")) {
                    newName = newName + ".png";
                }
                if (copyFileQ(this.getApplicationContext(), newName, cacheFileUri)) {
                    showSuccess();
                } else {
                    showFail();
                }
            } else {
                // 修改文件路径
                if (oldFile == null) {
                    if ("file".equalsIgnoreCase(uri.getScheme())) {
                        String oldPath = uri.getPath();
                        oldFile = new File(oldPath);
                    } else {
                        showFail();
                        return;
                    }
                }
                String newName = oldFile.getName();
                if (!newName.contains(".png")) {
                    newName = newName + ".png";
                }
                final File folder = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                final File newFile = new File(folder, newName);
                // 拷贝，成功或者失败 都提示下
                if (copyFile(oldFile, newFile)) {
                    showSuccess();
                } else {
                    showFail();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showFail();
        }

    }

    private void showSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(
                        UdeskZoomImageActivty.this,
                        getResources().getString(
                                R.string.udesk_success_save_image),
                        Toast.LENGTH_LONG).show();
                UdeskZoomImageActivty.this.finish();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private boolean copyFileQ(Context context, String fileName, Uri uri) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DISPLAY_NAME + "=?", new String[]{fileName});
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
            Uri insert = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (insert == null) {
                return false;
            }
            InputStream inputStream = contentResolver.openInputStream(uri);
            OutputStream outputStream = contentResolver.openOutputStream(insert);
            return copyToFileQ(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean copyToFileQ(InputStream inputStream, OutputStream outputStream) {
        try {
            if (inputStream == null || outputStream == null) {
                return false;
            }
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                return false;
            } finally {
                try {
                    outputStream.flush();
                    inputStream.close();
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void showFail() {
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
