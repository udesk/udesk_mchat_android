package cn.udesk.multimerchant.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.udesk.multimerchant.LoaderTask;
import cn.udesk.multimerchant.R;
import cn.udesk.multimerchant.UdeskConst;
import cn.udesk.multimerchant.UdeskUtil;
import cn.udesk.multimerchant.imageloader.UdeskImageLoader;
import cn.udesk.multimerchant.core.utils.UdeskUtils;


public class UdeskZoomImageActivty extends UdeskBaseActivity implements
        OnClickListener {

    private PhotoView zoomImageView;
    private View saveIdBtn;
    private Uri uri;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        try {
            setContentView(R.layout.udesk_multimerchant_zoom_imageview);
            zoomImageView = (PhotoView) findViewById(R.id.udesk_zoom_imageview);
            Bundle bundle = getIntent().getExtras();
            uri = bundle.getParcelable("image_path");
            UdeskUtil.loadInto(getApplicationContext(), uri.toString(), R.drawable.udesk_multimerchant_defualt_failure, R.drawable.udesk_multimerchant_defalut_image_loading, zoomImageView);
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


    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.udesk_zoom_save) {
                saveImageQ();
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
            UdeskUtil.getFileFromDiskCache(UdeskZoomImageActivty.this.getApplicationContext(), uri.toString(), new UdeskImageLoader.UdeskDownloadImageListener() {
                @Override
                public void onSuccess(final Uri uri, final Bitmap bitmap) {
                    LoaderTask.getThreadPoolExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            File file = UdeskUtil.getFileByUrl(UdeskZoomImageActivty.this, UdeskConst.FileImg, uri.toString());
                            if (saveBitmapFile(uri, bitmap, file)) {
                                if (UdeskUtil.isAndroidQ()) {
                                    Uri cacheFileUri;
                                    if (file == null) {
                                        if ("content".equalsIgnoreCase(uri.getScheme())) {
                                            cacheFileUri = uri;
                                        } else {
                                            showFail();
                                            return;
                                        }
                                    } else {
                                        cacheFileUri = UdeskUtil.getOutputMediaFileUri(UdeskZoomImageActivty.this, file);
                                    }
                                    if (cacheFileUri == null) {
                                        showFail();
                                        return;
                                    }
                                    String newName = UdeskUtil.getFileName(UdeskZoomImageActivty.this, cacheFileUri);
                                    newName = UdeskUtil.getPngName(newName);
                                    if (copyFileQ(UdeskZoomImageActivty.this, newName, cacheFileUri)) {
                                        showSuccess(null);
                                    } else {
                                        showFail();
                                    }
                                } else {
                                    File cacheFile = file;
                                    // 修改文件路径
                                    if (cacheFile == null) {
                                        if ("file".equalsIgnoreCase(uri.getScheme())) {
                                            String oldPath = uri.getPath();
                                            cacheFile = new File(oldPath);
                                        } else {
                                            showFail();
                                            return;
                                        }
                                    }
                                    String newName = cacheFile.getName();
                                    final File folder = Environment
                                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                                    File newFile = new File(folder, newName);
                                    // 拷贝，成功或者失败 都提示下
                                    if (copyFile(cacheFile, newFile)) {
                                        showSuccess(newFile);
                                    } else {
                                        showFail();
                                    }
                                }
                            } else {
                                showFail();
                            }

                        }
                    });

                }

                @Override
                public void onFailed(Uri uri) {
                    showFail();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private boolean saveBitmapFile(Uri uri, Bitmap bitmap, File file) {
        BufferedOutputStream bos = null;
        try {
            if (file == null) {
                return false;
            }
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
        } catch (Exception e) {
            return false;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showSuccess(final File newFile) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (newFile != null) {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(newFile);
                    intent.setData(uri);
                    UdeskZoomImageActivty.this.sendBroadcast(intent);
                }
                UdeskUtils.showToast(getApplicationContext(), getString(R.string.udesk_multimerchant_success_save_image));
                UdeskZoomImageActivty.this.finish();
            }
        });
    }


    private void showSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(
                        UdeskZoomImageActivty.this,
                        getResources().getString(
                                R.string.udesk_multimerchant_success_save_image),
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
                                R.string.udesk_multimerchant_fail_save_image),
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
