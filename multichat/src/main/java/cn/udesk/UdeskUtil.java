package cn.udesk;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.udesk.activity.UdeskZoomImageActivty;
import cn.udesk.provider.UdeskFileProvider;


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

    public static File cameaFile(Context context) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            return getOutputMediaFile(context, "IMG_" + timeStamp + ".jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Uri getOutputMediaFileUri(Context context, File cameaFile) {
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                return UdeskFileProvider.getUriForFile(context, getFileProviderName(context), cameaFile);
            } else {
                if (cameaFile != null) {
                    return Uri.fromFile(cameaFile);
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            return null;
        }

    }

    public final static String getFileProviderName(Context context) {
        return context.getPackageName() + ".fileprovider";
    }

    /**
     * 提供的Uri 解析出文件绝对路径
     *
     * @param uri
     * @return
     */
    public static String parseOwnUri(Uri uri, Context context, File cameraFile) {
        if (uri == null) return "";
        String path;
        if (TextUtils.equals(uri.getAuthority(), getFileProviderName(context))) {
            if (cameraFile != null) {
                return cameraFile.getAbsolutePath();
            } else {
                path = new File(Environment.getExternalStorageDirectory(), uri.getPath().replace("my_external/", "")).getAbsolutePath();
            }
        } else {
            path = uri.getEncodedPath();
        }
        return path;
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

    public static boolean isExitFileByPath(String path) {
        File file = new File(path);
        return file.exists();
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

    public static String getFileSizeByLoaclPath(String filePath) {
        try {
            File file = new File(filePath);
            if (file != null && file.exists()) {
                long blockSize = getFileSize(file);
                return formetFileSize(blockSize);
            }
        } catch (Exception e) {
            return "0B";
        }
        return "0B";
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
                + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
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
        String timeYes = context.getString(R.string.udesk_im_time_format_yday);
        String timeQt = context.getString(R.string.udesk_im_time_format_dby);
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

        return build.toString();
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
        if (obj instanceof String) {
            string = (String) obj;
        }
        try {
            string = String.valueOf(obj);
        } catch (Exception e) {
            e.printStackTrace();
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
        if (bitWidth > bitHeight && bitWidth > imageWidth) {
            // 如果图片宽度比高度大,以宽度为基准
            ratio = (double) bitWidth / imageWidth;
        } else if (bitWidth < bitHeight && bitHeight > imageHeight) {
            // 如果图片高度比宽度大，以高度为基准
            ratio = (double) bitHeight / imageHeight;
        }
        // 最小比率为1
        if (ratio <= 0)
            ratio = 1.0;
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
                    path = getPath(context, uri);
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
        if (TextUtils.isEmpty(path.trim())) {
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


    public static String getAuthToken(String userName, String password) {
        String basic = userName + ":" + password;
        basic = "Basic " + Base64.encodeToString(basic.getBytes(), Base64.NO_WRAP);
        return basic;
    }


    public static void loadInto(final Context context, final String imageUrl, int errorImageId, int placeHolder, final ImageView imageView) {

        Glide.with(context.getApplicationContext())
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(placeHolder)
                .error(errorImageId)
                .into(imageView);
    }

    /**
     * 自适应宽度加载图片。保持图片的长宽比例不变，通过修改imageView的高度来完全显示图片。
     */
    public static void loadIntoFitSize(final Context context, final String imageUrl, int errorImageId, int placeHolder, final ImageView imageView) {
        final int screenWidth = getScreenWidth(context.getApplicationContext());
        final int imgWidth = screenWidth / 3;
        final int imgHight = getScreenHeight(context) / 3;
        SimpleTarget<GlideDrawable> target = new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                if (resource == null || TextUtils.equals(imageUrl, (String) imageView.getTag())) {
                    return;
                }
                imageView.setTag(imageUrl);
                int imageWidth = resource.getIntrinsicWidth();
                int imageHeight = resource.getIntrinsicHeight();


                double bitScalew = getRatioSize(imageWidth, imageHeight, imgHight, imgWidth);
                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                Log.i("xxxxxxx", "screenWidth = " + screenWidth + "; imageWidth=" + imageWidth + "   ;imageHeight=" + imageHeight + ";imgWidth=" + imgWidth + ";=imgHight" + imgHight + "; bitScalew= " + bitScalew);
                layoutParams.height = (int) (imageHeight / bitScalew);
                layoutParams.width = (int) (imageWidth / bitScalew);
                imageView.setLayoutParams(layoutParams);
                imageView.setImageDrawable(resource);
//                imageView.invalidate();
            }
        };

        Glide.with(context.getApplicationContext())
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(placeHolder)
                .error(errorImageId)
                .override(getScreenWidth(context), getScreenHeight(context))
                .into(target);
//                .into(imageView);
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


}
