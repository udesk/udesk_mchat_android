package cn.udesk.config;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.module.GlideModule;

import cn.udesk.UdeskConst;

/**
 * author : ${揭军平}
 * time   : 2018/01/02
 * desc   :
 * version: 1.0
 */

public class UdeskGlideModule implements GlideModule {

    int diskSize = 1024 * 1024 * 300;


    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
        //设置磁盘缓存 DCard/Android/data/应用包名/cache/udeskcache
        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, UdeskConst.EXTERNAL_CACHE_FOLDER, diskSize)); //sd卡中
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
