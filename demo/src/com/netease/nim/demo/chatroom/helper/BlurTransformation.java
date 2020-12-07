package com.netease.nim.demo.chatroom.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

import java.security.MessageDigest;

import androidx.annotation.NonNull;

/**
 *
 */
public final class BlurTransformation implements Transformation<Bitmap> {

    private static final String ID = "com.netease.nim.demo.chatroom.helper.BlurTransformation";

    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    BitmapPool bitmapPool;

    private int radius;

    public BlurTransformation(Context context, int radius) {
        this.bitmapPool = Glide.get(context).getBitmapPool();
        this.radius = radius;
    }

    @NonNull
    @Override
    public Resource<Bitmap> transform(@NonNull Context context, @NonNull Resource<Bitmap> resource,
                                      int outWidth, int outHeight) {
        Bitmap source = resource.get();
        try {
            // make thumb
            Bitmap bitmap = ThumbnailUtils.extractThumbnail(source, (int) (outWidth * 0.3f),
                                                            (int) (outHeight * 0.3f));
            // make blur
            return BitmapResource.obtain(ImageBlur.fastBlur(bitmap, radius), bitmapPool);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return BitmapResource.obtain(source, bitmapPool);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BlurTransformation;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}
