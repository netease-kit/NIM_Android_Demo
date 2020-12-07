package com.netease.nim.demo.ysf.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.qiyukf.unicorn.ysfkit.unicorn.api.ImageLoaderListener;
import com.qiyukf.unicorn.ysfkit.unicorn.api.UnicornImageLoader;

/**
 * Created by zhoujianghua on 2016/12/27.
 */

public class GlideImageLoader implements UnicornImageLoader {
    private Context context;

    public GlideImageLoader(Context context) {
        this.context = context.getApplicationContext();
    }

    @Nullable
    @Override
    public Bitmap loadImageSync(String uri, int width, int height) {
        return null;
    }

    @Override
    public void loadImage(String uri, int width, int height, final ImageLoaderListener listener) {
        if (width <= 0) {
            width = Integer.MIN_VALUE;
        }

        if (height <= 0) {
            height = Integer.MIN_VALUE;
        }

        Glide.with(context).asBitmap().load(uri).into(new CustomTarget<Bitmap>(width, height) {

            @Override
            public void onLoadStarted(@Nullable Drawable placeholder) {

            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {

            }

            @Override
            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                if (listener != null) {
                    listener.onLoadComplete(resource);
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }
}
