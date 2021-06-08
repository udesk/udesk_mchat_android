package cn.udesk.multimerchant;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.udesk.multimerchant.activity.UdeskZoomImageActivty;
import cn.udesk.multimerchant.imageloader.UdeskImage;
import cn.udesk.multimerchant.imageloader.UdeskImageLoader;
import cn.udesk.multimerchant.model.CustomerInfo;
import cn.udesk.multimerchant.model.InitMode;
import cn.udesk.multimerchant.core.UdeskLibConst;
import cn.udesk.multimerchant.core.bean.UpdateCustomerField;
import cn.udesk.multimerchant.provider.UdeskExternalCacheProvider;
import cn.udesk.multimerchant.provider.UdeskExternalFileProvider;
import cn.udesk.multimerchant.provider.UdeskExternalProvider;
import cn.udesk.multimerchant.provider.UdeskInternalCacheProvider;
import cn.udesk.multimerchant.provider.UdeskInternalFileProvider;
import cn.udesk.multimerchant.core.utils.UdeskUtils;


public class UdeskUtil {
    public static final String ImgFolderName = "UDeskIMg";
    public static final String AudioFolderName = "UDeskAudio";


    /**
     * 检查网络是否是GPRS连接
     */
    public static boolean isGpsNet(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

        if (gprs == NetworkInfo.State.CONNECTED || gprs == NetworkInfo.State.CONNECTING) {
            return true;
        }
        return false;

    }
    /**
     * 获取存储路径目录
     *
     * @param context
     * @param folderName
     * @return
     */
    public static String getDirectoryPath(Context context, String folderName) {
        String directoryPath = "";
        if (context == null) {
            return "";
        }
        try {
            if (UdeskUtils.checkSDcard() && context.getExternalFilesDir(UdeskConst.EXTERNAL_FOLDER) != null) {
                directoryPath = context.getExternalFilesDir(UdeskConst.EXTERNAL_FOLDER).getAbsolutePath() + File.separator + folderName;
            } else {
                directoryPath = context.getFilesDir() + File.separator + UdeskConst.EXTERNAL_FOLDER + File.separator + folderName;
            }
        } catch (Exception e) {
            directoryPath = context.getFilesDir() + File.separator + UdeskConst.EXTERNAL_FOLDER + File.separator + folderName;
        }
        File file = new File(directoryPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return directoryPath;
    }

    /**
     * 根据文件类型和路径 创建目录和判断文件是否存在
     *
     * @param context
     * @param fileType
     * @param url
     * @return
     */
    public static boolean fileIsExitByUrl(Context context, String fileType, String url) {
        try {
            if (TextUtils.isEmpty(url)) {
                return false;
            }
            String fileName = getFileName(context, url, fileType);
            String filepath = getDirectoryPath(context.getApplicationContext(), fileType) + File.separator + fileName;
            File file = new File(filepath);
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getFileName(Context context, String filePath, String type) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        try {
            String filename = getFileName(context, filePath);
            if (filePath.startsWith("http") || filePath.startsWith("https")) {
                return filename;
            }
            if (type.equals(UdeskConst.FileAudio) && !filename.contains(UdeskConst.AUDIO_SUF_WAV)) {
                filename = filename + UdeskConst.AUDIO_SUF_WAV;
            } else if (type.equals(UdeskConst.FileImg) && !filename.contains(UdeskConst.IMG_SUF)) {
                filename = filename + UdeskConst.IMG_SUF;
            } else if (type.equals(UdeskConst.FileVideo) && !filename.contains(UdeskConst.VIDEO_SUF)) {
                filename = filename + UdeskConst.VIDEO_SUF;
            }
            return filename;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getPathByUrl(Context context, String fileType, String url) {
        String fileName = getFileName(context, url, fileType);
        try {
            return getDirectoryPath(context.getApplicationContext(), fileType) + File.separator + fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public static File getFileByUrl(Context context, String fileType, String url) {
        String fileName = getFileName(context, url, fileType);
        try {
            String filepath = getDirectoryPath(context.getApplicationContext(), fileType) + File.separator + fileName;
            File file = new File(filepath);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File cameaFile(Context context) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            return getOutputMediaFile(context, "IMG_" + timeStamp + ".jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static long getFileSizeQ(Context context, String filePath) {
        long blockSize = 0L;
        try {
            if (UdeskUtil.isAndroidQ()) {
                AssetFileDescriptor assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(Uri.parse(getFilePathQ(context, filePath)), "r");
                if (assetFileDescriptor != null) {
                    blockSize = assetFileDescriptor.getLength();
                }
            } else {
                File file = new File(filePath);
                if (file.exists()) {
                    blockSize = getFileSize(file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return blockSize;
    }

    public static String saveBitmap(Context context, Bitmap b) {
        try {
            String path = getDirectoryPath(context, UdeskConst.FileImg);
            long dataTake = System.currentTimeMillis();
            String jpegName = path + File.separator + "picture_" + dataTake + ".jpg";
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            return jpegName;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    public static String saveBitmap(Context context, String url, Bitmap b) {
        String jpegName = getPathByUrl(context, UdeskConst.FileImg, url);
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            return jpegName;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    public static Bitmap getVideoThumbnail(String url) {
        Bitmap bitmap = null;
        // MediaMetadataRetriever 是android中定义好的一个类，提供了统一
        // 的接口，用于从输入的媒体文件中取得帧和元数据；
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(url, new HashMap());
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

public static Uri getOutputMediaFileUri(Context context, File file) {
    if (file == null) {
        return null;
    }
    try {
        if (Build.VERSION.SDK_INT >= 24) {
            if (context.getExternalFilesDir("") != null
                    && file.getAbsolutePath().contains(context.getExternalFilesDir("").getAbsolutePath())) {
                return UdeskExternalFileProvider.getUriForFile(context, getExternalFileProviderName(context), file);
            } else if (file.getAbsolutePath().contains(context.getExternalCacheDir().getAbsolutePath())) {
                return UdeskExternalCacheProvider.getUriForFile(context, getExternalCacheProviderName(context), file);
            } else if (file.getAbsolutePath().contains(context.getFilesDir().getAbsolutePath())) {
                return UdeskInternalFileProvider.getUriForFile(context, getInternalFileProviderName(context), file);
            } else if (file.getAbsolutePath().contains(context.getCacheDir().getAbsolutePath())) {
                return UdeskInternalCacheProvider.getUriForFile(context, getInternalCacheProviderName(context), file);
            } else if (file.getAbsolutePath().contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                return UdeskExternalProvider.getUriForFile(context, getExternalProviderName(context), file);
            }
        } else {
            return Uri.fromFile(file);
        }
    } catch (Exception e) {
        return null;
    }
    return null;
}


    public static String getExternalFileProviderName(Context context) {
        return context.getPackageName() + ".udesk_external_file_provider";
    }
    public static String getExternalCacheProviderName(Context context) {
        return context.getPackageName() + ".udesk_external_cache_provider";
    }

    public static String getInternalFileProviderName(Context context) {
        return context.getPackageName() + ".udesk_internal_file_provider";
    }

    public static String getInternalCacheProviderName(Context context) {
        return context.getPackageName() + ".udesk_internal_cache_provider";
    }

    public static String getExternalProviderName(Context context) {
        return context.getPackageName() + ".udesk_external_provider";
    }

    /**
     * 提供的Uri 解析出文件绝对路径
     *
     * @param uri
     * @return
     */
    public static String parseOwnUri(Uri uri, Context context, File cameraFile) {
        if (uri == null) {
            return "";
        }
        if (isAndroidQ()) {
            return uri.toString();
        }
        String path = "";
        try {
            if (TextUtils.equals(uri.getAuthority(), getExternalFileProviderName(context))) {
                if (cameraFile != null) {
                    return cameraFile.getAbsolutePath();
                } else {
                    path = new File(context.getExternalFilesDir(""), uri.getPath().replace("udesk_multimerchant_external/", "")).getAbsolutePath();
                }
            } else if (TextUtils.equals(uri.getAuthority(), getExternalCacheProviderName(context))) {
                if (cameraFile != null) {
                    return cameraFile.getAbsolutePath();
                } else {
                    path = new File(context.getExternalCacheDir(), uri.getPath().replace("udesk_multimerchant_external/", "")).getAbsolutePath();
                }
            } else if (TextUtils.equals(uri.getAuthority(), getInternalFileProviderName(context))) {
                if (cameraFile != null) {
                    return cameraFile.getAbsolutePath();
                } else {
                    path = new File(context.getFilesDir(), uri.getPath().replace("udesk_multimerchant_external/", "")).getAbsolutePath();
                }
            } else if (TextUtils.equals(uri.getAuthority(), getInternalCacheProviderName(context))) {
                if (cameraFile != null) {
                    return cameraFile.getAbsolutePath();
                } else {
                    path = new File(context.getCacheDir(), uri.getPath().replace("udesk_multimerchant_external/", "")).getAbsolutePath();
                }
            } else if (TextUtils.equals(uri.getAuthority(), getExternalProviderName(context))) {
                if (cameraFile != null) {
                    return cameraFile.getAbsolutePath();
                } else {
                    path = new File(Environment.getExternalStorageDirectory(), uri.getPath().replace("udesk_multimerchant_external/", "")).getAbsolutePath();
                }
            } else {
                path = uri.getEncodedPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    public static boolean isAndroidQ() {
        return Build.VERSION.SDK_INT >= 29;
    }

    public static void decodeFileAndContent(Context context, String path, BitmapFactory.Options options) {
        try {
            if (isAndroidQ()) {
                AssetFileDescriptor parcelFileDescriptor = context.getContentResolver().openAssetFileDescriptor(Uri.parse(getFilePathQ(context, path)), "r");
                if (parcelFileDescriptor != null) {
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
                    parcelFileDescriptor.close();
                }
            } else {
                BitmapFactory.decodeFile(path, options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 本地输出文件路径
     *
     * @param context
     * @param filePath
     * @return
     */
    public static String getFilePathQ(Context context, String filePath) {
        try {
            if (context == null) {
                return "";
            }
            if (!filePath.startsWith("http")
                    && !filePath.startsWith("https")
                    && !filePath.startsWith("content")
                    && isAndroidQ()) {
                File file;
                if (filePath.startsWith("file")) {
                    file = new File(new URI(filePath));
                } else {
                    file = new File(filePath);
                }
                if (context.getExternalFilesDir("") != null
                        && filePath.contains(context.getExternalFilesDir("").getAbsolutePath())) {
                    return UdeskExternalFileProvider.getUriForFile(context, getExternalFileProviderName(context), file).toString();
                } else if (file.getAbsolutePath().contains(context.getExternalCacheDir().getAbsolutePath())) {
                    return UdeskExternalCacheProvider.getUriForFile(context, getExternalCacheProviderName(context), file).toString();
                } else if (filePath.contains(context.getFilesDir().getAbsolutePath())) {
                    return UdeskInternalFileProvider.getUriForFile(context, getInternalFileProviderName(context), file).toString();
                } else if (filePath.contains(context.getCacheDir().getAbsolutePath())) {
                    return UdeskInternalCacheProvider.getUriForFile(context, getInternalCacheProviderName(context), file).toString();
                } else if (file.getAbsolutePath().contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                    return UdeskExternalProvider.getUriForFile(context, getExternalProviderName(context), file).toString();
                }
            }
        } catch (Exception e) {
            return "";
        }
        return filePath;
    }

    public static Uri getUriFromPath(Context context, String path) {
        try {
            if (isAndroidQ()) {
                return Uri.parse(getFilePathQ(context, path));
            } else {
                return getUriFromPathBelowQ(context, path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Uri getUriFromPathBelowQ(Context context, String path) {
        try {
            if (TextUtils.isEmpty(path)) {
                return null;
            }
            if (path.startsWith("http") || path.startsWith("https") || path.startsWith("file") || path.startsWith("content")) {
                return Uri.parse(path);
            } else {
                return Uri.fromFile(new File(path));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFileName(Context context, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        try {
            if ((isAndroidQ() && filePath.startsWith("content"))) {
                return getFileName(context, Uri.parse(filePath));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getName(filePath);
    }

    public static String getFileName(@NonNull Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        String filename = null;
        if (mimeType == null) {
            filename = getName(uri.toString());
        } else {
            Cursor returnCursor = context.getContentResolver().query(uri, null,
                    null, null, null);
            if (returnCursor != null) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                filename = returnCursor.getString(nameIndex);
                returnCursor.close();
            }
        }
        return filename;
    }

    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf('/');
        return filename.substring(index + 1);
    }
    public static String getPngName(String fileName) {
        try {
            int dotIndex = fileName.lastIndexOf(".");
            if (dotIndex < 0) {
                return fileName + ".png";
            } else {
                return fileName.substring(0, dotIndex) + ".png";
            }
        } catch (Exception e) {
        }
        return fileName;
    }

    public static File getOutputMediaFile(Context context, String mediaName) {
        File mediaFile = null;
        try {
            File mediaStorageDir = null;
            try {
                mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), ImgFolderName);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + mediaName);
        } catch (Exception e) {
            return null;
        }
        return mediaFile;
    }

    public static boolean isExitFileByPath(Context context, String path) {
        try {
            if (null == context) {
                return false;
            }
            if (isAndroidQ()) {
                ContentResolver cr = context.getContentResolver();
                AssetFileDescriptor afd = cr.openAssetFileDescriptor(Uri.parse(getFilePathQ(context, path)), "r");
                if (null == afd) {
                    return false;
                } else {
                    afd.close();
                    return true;
                }
            } else {
                File file = new File(path);
                return file.exists();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isExitFileByMsgIdAndUrl(String msgId, String url) {
        try {
            File file = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), buildFileName(msgId, url));
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static String getFileSizeByLoaclPath(Context context, String filePath) {
        try {
            long blockSize = 0L;
            if (UdeskUtil.isAndroidQ()) {
                AssetFileDescriptor assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(Uri.parse(getFilePathQ(context, filePath)), "r");
                if (assetFileDescriptor != null) {
                    blockSize = assetFileDescriptor.getLength();
                }
            } else {
                File file = new File(filePath);
                if (file.exists()) {
                    blockSize = getFileSize(file);
                }
            }
            return formetFileSize(blockSize);
        } catch (Exception e) {
            return "0B";
        }
    }

    public static String getFileSizeByMsgIdAndUrl(String msgId, String url) {
        File file = getLoaclpathByMsgIdAndUrl(msgId, url);
        if (file == null) {
            return "0B";
        } else {
            long blockSize = getFileSize(file);
            return formetFileSize(blockSize);
        }
    }

    public static long getFileSize(File file) {
        long size = 0;
        if (file == null) {
            return size;
        }
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                size = fis.available();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return size;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public static String formetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            DecimalFormat dfb = new DecimalFormat("#");
            fileSizeString = dfb.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        }

        return fileSizeString;
    }


    public static File getLoaclpathByMsgIdAndUrl(String msgId, String url) {
        try {
            File file = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), buildFileName(msgId, url));
            if (file.exists()) {
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String buildFileName(String msgId, String url) {
        return msgId + "_" + getFileName(url);
    }

    public static String getFileName(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public static boolean audiofileIsDown(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        File mediaStorageDir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_RINGTONES),
                AudioFolderName);
        if (!mediaStorageDir.exists()) {
            return false;
        }
        String filepath = mediaStorageDir.getPath() + File.separator + fileName;
        File file = new File(filepath);
        return file.exists();
    }


    public static String getDownAudioPath(Context context, String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        File mediaStorageDir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_RINGTONES),
                AudioFolderName);

        return mediaStorageDir.getPath() + File.separator + fileName;
    }


    public static String getOutputAudioPath(Context context) {
        return getOutputAudioPath(context, "audio_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+ UdeskConst.AUDIO_SUF_WAV);
    }


    public static File getOutputAudioFile(Context context, String mediaName) {
        String path = getOutputAudioPath(context, mediaName);
        if (TextUtils.isEmpty(path)) {
            return null;
        } else {
            return new File(path);
        }
    }

    public static String getOutputAudioPath(Context context, String mediaName) {
        try {
            File mediaStorageDir = new File(
                    context.getExternalFilesDir(Environment.DIRECTORY_RINGTONES),
                    AudioFolderName);

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }

            File noMediaFile = new File(mediaStorageDir, ".nomedia");
            if (!noMediaFile.exists()) {
                try {
                    noMediaFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return mediaStorageDir.getPath() + File.separator + mediaName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static int getDisplayWidthPixels(Activity activity) {
        DisplayMetrics dMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
        return dMetrics.widthPixels;
    }


    public static Date stringToDate(String strTime) {
        SimpleDateFormat formatter = null;
        Date date = null;
        if (strTime.contains("T") && strTime.contains("+")) {
            if (strTime.length() > 26) {
                formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            } else {
                formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            }
        } else if (strTime.contains("T")) {
            formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        } else {
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        try {
            date = formatter.parse(strTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    public static String formatLongTypeTimeToString(Context context, String createTime) {
        long time = stringToLong(createTime);
        return formatLongTypeTimeToString(context, time);
    }


    public static String formatLongTypeTimeToString(Context context, long time) {
        long OFFSET_DAY = 3600 * 24;
        String timeYes = context.getString(R.string.udesk_multimerchant_im_time_format_yday);
        String timeQt = context.getString(R.string.udesk_multimerchant_im_time_format_dby);
        String timeDate = "yyyy/MM/dd HH:mm:ss";
        Calendar calendar = Calendar.getInstance();
        StringBuilder build = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        // 解析需要转化时间
        calendar.setTimeInMillis(time);
        int year = calendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DAY_OF_YEAR);

        // 拼接 转化结果  现转换小时分秒
        build.append(" ").append(sdf.format(calendar.getTime()));// 先添加

        // 先解析当前时间。取出当前年，日 等信息
        calendar.setTimeInMillis(System.currentTimeMillis());
        int nowYear = calendar.get(Calendar.YEAR);
        int nowDay = calendar.get(Calendar.DAY_OF_YEAR);

        if (year != nowYear) {
            sdf.applyLocalizedPattern(timeDate);
            return sdf.format(time);

        } else if (day == nowDay) {// 这里是一年内的当天
            // 当天的话 就不用管了
        } else {// 一年内
            int dayOffset = (nowDay - day);// nowDay要大一些
            if (dayOffset == 0) {
            } else if (dayOffset == 1) {// 1表示差一天，即昨天
                return timeYes + build.toString();
            } else if (dayOffset == 2) {// 1表示差两天，即前天
                return timeQt + build.toString();
            } else {
                sdf.applyLocalizedPattern(timeDate);
                return sdf.format(time);
            }
        }

        return build.toString().trim();
    }


    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.endsWith("zh");
    }


    //预览大图
    public static void previewPhoto(Context context, Uri uri) {
        try {
            if (uri == null) {
                return;
            }
            Intent intent = new Intent(context,
                    UdeskZoomImageActivty.class);
            Bundle data = new Bundle();
            data.putParcelable("image_path", uri);
            intent.putExtras(data);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    public static int objectToInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Integer) {
            return (int) obj;
        }
        if (obj instanceof Double) {
            return Double.valueOf((Double) obj).intValue();
        }
        if (obj instanceof Float) {
            return Float.valueOf((Float) obj).intValue();
        }
        if (isNumeric(obj.toString())) {
            return toInt(obj.toString(), 0);
        }
        return 0;
    }

    //
    public static boolean isNumeric(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static String objectToString(Object obj) {
        if (obj == null) {
            return "";
        }
        String string = "";
        try {
            if (obj instanceof String) {
                string = (String) obj;
            } else if (obj instanceof Double) {
                string = String.valueOf(Double.valueOf((Double) obj).longValue());
            } else if (obj instanceof Float) {
                string = String.valueOf(Float.valueOf((Float) obj).intValue());
            } else {
                string = String.valueOf(obj);
            }
        } catch (Exception e) {
            string = "";
        }
        if (string.equals("null")) {
            string = "";
        }
        return string;
    }


    public static boolean objectToBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        return false;
    }

    public static boolean compare(String createtime1, String createtime2) {
        return stringToLong(createtime1) - stringToLong(createtime2) >= 0;

    }

    public static String parseEventTime(String strTime) {
        if (strTime == null) {
            return "";
        }
        if (strTime.length() <= 10) {
            return strTime;
        }
        long time = stringToLong(strTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdf.format(new Date(time));
    }

    public static String parseEventTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdf.format(new Date(time));
    }

    public static long stringToLong(String strTime) {
        Date date = stringToDate(strTime); // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            long currentTime = dateToLong(date); // date类型转成long类型
            return currentTime;
        }
    }

    public static String uRLEncoder(String urlString) {
        try {
            urlString = URLEncoder.encode(urlString, "utf-8").
                    replaceAll("\\+", "%20").
                    replaceAll("%3A", ":").
                    replaceAll("%2F", "/").
                    replaceAll("%3F", "?").
                    replaceAll("%26amp%3B", "&");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urlString;
    }

    //过滤掉字符串中的特殊字符
    public static String stringFilter(String str) {
        String regEx = "[/=]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    public static long dateToLong(Date date) {
        return date.getTime();
    }


    /**
     * 获取图像的宽高
     **/

    public static int[] getImageWH(String path) {
        int[] wh = {-1, -1};
        if (path == null) {
            return wh;
        }
        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                InputStream is = new FileInputStream(path);
                BitmapFactory.decodeStream(is, null, options);
                wh[0] = options.outWidth;
                wh[1] = options.outHeight;
            } catch (Exception e) {

            }
        }
        return wh;
    }


    public static double getRatioSize(int bitWidth, int bitHeight, int imageHeight, int imageWidth) {

        // 缩放比
        double ratio = 1.0;
        // 缩放比,由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        if (bitWidth >= bitHeight && bitWidth > imageWidth) {
            // 如果图片宽度比高度大,以宽度为基准
            ratio = (double) bitWidth / imageWidth;
        } else if (bitWidth < bitHeight && bitHeight > imageHeight) {
            // 如果图片高度比宽度大，以高度为基准
            ratio = (double) bitHeight / imageHeight;
        }
//        // 最小比率为1
//        if (ratio <= 1.0)
//            ratio = 1.0;
        return ratio;
    }

    public static int dip2px(Context context, int dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int getScreenWith(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }

    public static String getFilePath(Activity context, Uri uri) {
        String path = "";
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            Cursor cursor = null;
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    path = getNewPath(context, uri);
                } else {
                    String[] projection = {MediaStore.Images.Media.DATA};
                    cursor = context.getContentResolver().query(uri, projection, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    path = cursor.getString(column_index);
                }
            } catch (Exception e) {
                return uri.getPath();
            } finally {

                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }
        }
        if (path == null || TextUtils.isEmpty(path.trim())) {
            path = uri.getPath();
        }

        return path;
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }

        return null;
    }

    public static String getNewPath(final Context context, final Uri uri) {
        try {
            if (isAndroid_19()) {
                if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if (isAndroidQ()) {
                        if ("image".equals(type)) {
                            return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Long.valueOf(split[1])).toString();
                        } else if ("video".equals(type)) {
                            return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Long.valueOf(split[1])).toString();
                        } else if ("audio".equals(type)) {
                            return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(split[1])).toString();
                        }
                    }
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                } else {
                    if (isAndroidQ()) {
                        return uri.toString();
                    } else {
                        if (DocumentsContract.isDocumentUri(context, uri)) {
                            // ExternalStorageProvider
                            if (isExternalStorageDocument(uri)) {
                                final String docId = DocumentsContract.getDocumentId(uri);
                                final String[] split = docId.split(":");
                                final String type = split[0];

                                if ("primary".equalsIgnoreCase(type)) {
                                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                                }

                            }
                            // DownloadsProvider
                            else if (isDownloadsDocument(uri)) {
                                final String id = DocumentsContract.getDocumentId(uri);

                                if (id != null && id.startsWith("raw:")) {
                                    return id.substring(4);
                                }

                                String[] contentUriPrefixesToTry = new String[]{
                                        "content://downloads/public_downloads",
                                        "content://downloads/my_downloads"
                                };

                                for (String contentUriPrefix : contentUriPrefixesToTry) {
                                    Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
                                    try {
                                        String path = getDataColumn(context, contentUri, null, null);
                                        if (path != null && !path.equals("")) {
                                            return path;
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                                return getCopyFilePath(context, uri);
                            }
                        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
//                            return getDataColumn(context, uri, null, null);
                            String path = getDataColumn(context, uri, null, null);
                            if (path != null && !path.equals("")) {
                                return path;
                            }

                            // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                            return getCopyFilePath(context, uri);
                        }
                        // File
                        else if ("file".equalsIgnoreCase(uri.getScheme())) {
                            return uri.getPath();
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getCopyFilePath(Context context, Uri uri) {
        // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
        String fileName = getFileName(context, uri);
        File cacheDir = getDocumentCacheDir(context);
        File file = generateFileName(fileName, cacheDir);
        String destinationPath = "";
        if (file != null) {
            destinationPath = file.getAbsolutePath();
            saveFileFromUri(context, uri, destinationPath);
        }

        return destinationPath;
    }


    public static File getDocumentCacheDir(@NonNull Context context) {
        File dir = (context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    @Nullable
    public static File generateFileName(@Nullable String name, File directory) {
        if (name == null) {
            return null;
        }

        File file = new File(directory, name);

        if (file.exists()) {
            String fileName = name;
            String extension = "";
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex > 0) {
                fileName = name.substring(0, dotIndex);
                extension = name.substring(dotIndex);
            }

            int index = 0;

            while (file.exists()) {
                index++;
                name = fileName + '(' + index + ')' + extension;
                file = new File(directory, name);
            }
        }

        try {
            if (!file.createNewFile()) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        return file;
    }


    private static void saveFileFromUri(Context context, Uri uri, String destinationPath) {
        InputStream is = null;
        BufferedOutputStream bos = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            bos = new BufferedOutputStream(new FileOutputStream(destinationPath, false));
            byte[] buf = new byte[1024];
            is.read(buf);
            do {
                bos.write(buf);
            } while (is.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static boolean isAndroid_19() {
        return Build.VERSION.SDK_INT >= 19;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static String getDataColumnByName(Context context, Uri uri, String selection,
                                             String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public static File getExternalCacheDir(final Context context) {
        if (hasExternalCacheDir())
            return context.getExternalCacheDir();

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return createFile(Environment.getExternalStorageDirectory().getPath() + cacheDir, "");
    }

    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static File createFile(String folderPath, String fileName) {
        File destDir = new File(folderPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        return new File(folderPath, fileName);
    }

    public static String getMIMEType(File file) {

        String type = "*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        /* 获取文件的后缀名*/
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "") return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    public static String getMIMEType(Context context, Uri uri) {
        String mimeType = "*/*";
        try {
            mimeType = context.getContentResolver().getType(uri);
            if (mimeType != null) {
                return mimeType;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mimeType;
    }

    public static final String[][] MIME_MapTable = {
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
    };

    public final static int TYPE_IMAGE = 1;
    public final static int TYPE_SHORT_VIDEO = 2;
    public final static int TYPE_AUDIO = 3;

    public static int isPictureType(String pictureType) {
        if (TextUtils.isEmpty(pictureType)) {
            return TYPE_IMAGE;
        }
        switch (pictureType) {
            case "image/png":
            case "image/PNG":
            case "image/jpeg":
            case "image/JPEG":
            case "image/webp":
            case "image/WEBP":
            case "image/gif":
            case "image/bmp":
            case "image/GIF":
            case "imagex-ms-bmp":
                return TYPE_IMAGE;
            case "video/3gp":
            case "video/3gpp":
            case "video/3gpp2":
            case "video/avi":
            case "video/mp4":
            case "video/quicktime":
            case "video/x-msvideo":
            case "video/x-matroska":
            case "video/mpeg":
            case "video/webm":
            case "video/mp2ts":
                return TYPE_SHORT_VIDEO;
            case "audio/mpeg":
            case "audio/x-ms-wma":
            case "audio/x-wav":
            case "audio/amr":
            case "audio/wav":
            case "audio/aac":
            case "audio/mp4":
            case "audio/quicktime":
            case "audio/lamr":
            case "audio/3gpp":
                return TYPE_AUDIO;
        }
        return TYPE_IMAGE;
    }

    public static String getAuthToken(String userName, String password) {
        String basic = userName + ":" + password;
        basic = "Basic " + Base64.encodeToString(basic.getBytes(), Base64.NO_WRAP);
        return basic;
    }

    public static void modifyTextViewDrawable(TextView v, Drawable drawable, int index) {
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        //index 0:左 1：上 2：右 3：下
        if (index == 0) {
            v.setCompoundDrawables(drawable, null, null, null);
        } else if (index == 1) {
            v.setCompoundDrawables(null, drawable, null, null);
        } else if (index == 2) {
            v.setCompoundDrawables(null, null, drawable, null);
        } else {
            v.setCompoundDrawables(null, null, null, drawable);
        }
    }
    public static String timeParse(long duration) {
        String time = "";
        if (duration > 1000) {
            time = timeParseMinute(duration);
        } else {
            long minute = duration / 60000;
            long seconds = duration % 60000;
            long second = Math.round((float) seconds / 1000);
            if (minute < 10) {
                time += "0";
            }
            time += minute + ":";
            if (second < 10) {
                time += "0";
            }
            time += second;
        }
        return time;
    }

    private static SimpleDateFormat msFormat = new SimpleDateFormat("mm:ss");

    public static String timeParseMinute(long duration) {
        try {
            return msFormat.format(duration);
        } catch (Exception e) {
            e.printStackTrace();
            return "0:00";
        }
    }

    public static void loadInto(final Context context, final String imageUrl, int errorImageId, int placeHolder, final ImageView imageView) {

        UdeskImage.loadImage(context, imageView, getUriFromPath(context, imageUrl), placeHolder, errorImageId, imageView.getWidth(), imageView.getHeight(), null);
    }

    public static void loadIntoCustomerSize(final Context context, final String imageUrl, int errorImageId, int placeHolder, final ImageView imageView, int width, int height) {

        UdeskImage.loadImage(context, imageView, getUriFromPath(context, imageUrl), placeHolder, errorImageId, width, height, null);
    }
    private static void scaleImageView(Context context, ImageView imageView, boolean isfixScale, int reqWidth, int reqHeight) {
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        int imgWidth = dip2px(context, 140);
        if (isfixScale) {
            //固定宽度缩放
            double bitScalew = (double) reqWidth / imgWidth;
            layoutParams.height = (int) (reqHeight / bitScalew);
            layoutParams.width = (int) (reqWidth / bitScalew);
        } else {
            int imgHight = dip2px(context, 220);
            double bitScalew = getRatioSize(reqWidth, reqHeight, imgHight, imgWidth);
            if (bitScalew >= 1) {
                layoutParams.height = (int) (reqHeight / bitScalew);
                layoutParams.width = (int) (reqWidth / bitScalew);
            } else if (bitScalew >= 0.5) {
                layoutParams.height = reqHeight;
                layoutParams.width = reqWidth;
            } else {
                layoutParams.height = imgWidth / 2;
                layoutParams.width = imgWidth / 2;
            }
        }
        imageView.requestLayout();
    }

    /**
     * 自适应宽度加载图片。保持图片的长宽比例不变，通过修改imageView的高度来完全显示图片。
     */
    public static void loadIntoFitSize(final Context context, String imageUrl, int errorImageId, int placeHolder, final ImageView imageView,final boolean isfixScale) {
        try {
            UdeskImageLoader.UdeskDisplayImageListener udeskDisplayImageListener = new UdeskImageLoader.UdeskDisplayImageListener() {
                @Override
                public void onSuccess(View view, Uri uri, int width, int height) {
                    scaleImageView(context, imageView, isfixScale, width, height);
                }
            };
            UdeskImage.loadImage(context, imageView, getUriFromPath(context, imageUrl), placeHolder, errorImageId, getScreenWidth(context), getScreenHeight(context), udeskDisplayImageListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (Exception e) {
            bitmap = null;
        }
        return bitmap;
    }
    public static boolean isClassExists(String classFullName) {

        try {
            Class.forName(classFullName);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 本地缓存文件
     */
    public static void getFileFromDiskCache(Context context, String path, UdeskImageLoader.UdeskDownloadImageListener udeskDownloadImageListener) {
        try {
            UdeskImage.loadImageFile(context, getUriFromPath(context, path), udeskDownloadImageListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return
     */
    public static String MD5(byte[] btInput) {
        char hexDigits[] = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param s
     * @return
     */
    public static String MD5(String s) {
        return MD5(s.getBytes());
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getAppName(Context context) {
        String appName = "";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = null;
            info = manager.getPackageInfo(context.getPackageName(), 0);
            appName = info.applicationInfo.loadLabel(manager).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }

    public static String getPhoneModal() {
        try {
            return Build.MODEL;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
    public static String getPhoneVersion() {
        try {
            return Build.VERSION.RELEASE + ",  sdk version：" + UdeskLibConst.sdkversion;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static String getScaleScreen(Context context) {
        try {
            DisplayMetrics dm = new DisplayMetrics();
            android.view.Display display = ((WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            display.getMetrics(dm);
            return dm.heightPixels + "*" + dm.widthPixels;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static String getNetworkState(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            ConnectivityManager connectMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfoinfo = connectMgr.getActiveNetworkInfo();
            if (networkInfoinfo == null) {
                return "offline";
            } else {
                if (networkInfoinfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return "wifi";
                } else if (networkInfoinfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    int netWorkType = tm.getNetworkType();
                    if (netWorkType == TelephonyManager.NETWORK_TYPE_EVDO_A
                            || netWorkType == TelephonyManager.NETWORK_TYPE_HSDPA
                            || netWorkType == TelephonyManager.NETWORK_TYPE_UMTS
                            || netWorkType == TelephonyManager.NETWORK_TYPE_EVDO_0) {
                        return "3G";
                    } else if (netWorkType == TelephonyManager.NETWORK_TYPE_GPRS
                            || netWorkType == TelephonyManager.NETWORK_TYPE_EDGE
                            || netWorkType == TelephonyManager.NETWORK_TYPE_CDMA) {
                        return "2G";
                    } else if (netWorkType == TelephonyManager.NETWORK_TYPE_LTE) {
                        return "4G";
                    }else if (netWorkType == TelephonyManager.NETWORK_TYPE_NR) {
                        return "5G";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getCarrier(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getNetworkOperatorName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static UpdateCustomerField buildUpdateCustomerField(Context context, InitMode initMode, CustomerInfo customerInfo) {
        UpdateCustomerField updateCustomerField = new UpdateCustomerField();
        try {
            if (initMode != null && customerInfo != null) {
                updateCustomerField.setImUsername(objectToString(initMode.getIm_username()));
                updateCustomerField.setImUserpassword(objectToString(initMode.getIm_password()));
                updateCustomerField.setTenantId(objectToString(initMode.getTenantId()));
                updateCustomerField.setCustomerEuid(objectToString(initMode.getEuid()));
                updateCustomerField.setCustomerName(objectToString(initMode.getName()));
                updateCustomerField.setCarrier(getCarrier(context));
                updateCustomerField.setPhoneVersion(getPhoneVersion());
                updateCustomerField.setScaleScreen(getScaleScreen(context));
                updateCustomerField.setPhoneModal(getPhoneModal());
                updateCustomerField.setVersion(UdeskLibConst.sdkversion);
                updateCustomerField.setNetworkStatus(getNetworkState(context));
                updateCustomerField.setAppVersion(getVersionName(context));
                updateCustomerField.setSearchType(getAppName(context));
                updateCustomerField.setEmail(customerInfo.getEmail());
                updateCustomerField.setPhone(customerInfo.getCellphone());
                updateCustomerField.setTags(customerInfo.getTags());
                updateCustomerField.setDesc(customerInfo.getCustomerDescription());
                updateCustomerField.setOrg(customerInfo.getOrg());
                updateCustomerField.setFieldMap(customerInfo.getCustomField());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updateCustomerField;
    }
    public static String setCreateTime(){
        try {
            String strDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
            SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
            return sdf.format(new Date());
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static <T> void savePreferenceCache(Context context,String key, T navigatesChatCache){
        try {
            PreferenceHelper.write(context,UdeskLibConst.SharePreParams.Udesk_Sharepre_Name,key, PreferenceHelper.Object2String(navigatesChatCache));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static <T> T getPreferenceCache(Context context,String key){
        try {
            String cache = PreferenceHelper.readString(context, UdeskLibConst.SharePreParams.Udesk_Sharepre_Name,key);
            if (!TextUtils.isEmpty(cache)){
                return (T)PreferenceHelper.String2Object(cache);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
