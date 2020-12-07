package com.netease.nim.uikit.business.session.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;

/**
 * 查看合并消息中的图片
 */
public class WatchMultiRetweetPictureActivity extends UI {

    /** 图片地址 */
    @Nullable
    private ImageAttachment attachment;

    /** 展示图 */
    private ImageView detailsIV;

    public static void start(Activity activity, ImageAttachment attachment) {
        Intent intent = new Intent(activity, WatchMultiRetweetPictureActivity.class);
        intent.putExtra(Extras.EXTRA_DATA, attachment);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_multi_retweet_picture);
        ToolBarOptions options = new NimToolBarOptions();
        options.titleString = getString(R.string.picture);
        options.navigateId = R.drawable.nim_actionbar_dark_back_icon;
        setToolBar(R.id.toolbar, options);
        getExtra();
        initViews();
    }

    private void getExtra() {
        Intent intent = getIntent();
        attachment = (ImageAttachment) intent.getSerializableExtra(Extras.EXTRA_DATA);
    }

    private void initViews() {
        detailsIV = findViewById(R.id.img_details);
        if (attachment == null) {
            setImageViewWithPath("");
        } else if (!TextUtils.isEmpty(attachment.getPath())) {
            setImageViewWithPath(attachment.getPath());
        } else if (!TextUtils.isEmpty(attachment.getThumbPath())) {
            setImageViewWithPath(attachment.getThumbPath());
        } else if (!TextUtils.isEmpty(attachment.getUrl())) {
            loadImageViewWithUri(attachment.getUrl());
        } else if (!TextUtils.isEmpty(attachment.getThumbUrl())) {
            loadImageViewWithUri(attachment.getThumbUrl());
        } else {
            setEmptyImageView();
        }
    }

    private void setImageViewWithPath(String path) {
        if (TextUtils.isEmpty(path)) {
            setEmptyImageView();
            return;
        }

        Bitmap bitmap = BitmapDecoder.decodeSampledForDisplay(path, false);
        bitmap = ImageUtil.rotateBitmapInNeeded(path, bitmap);
        if (bitmap == null) {
            ToastHelper.showToastLong(this, R.string.picker_image_error);
            detailsIV.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(R.drawable.nim_image_default));
        } else {
            detailsIV.setImageBitmap(bitmap);
        }
    }

    private void loadImageViewWithUri(String url) {
        if (TextUtils.isEmpty(url)) {
            setEmptyImageView();
            return;
        }
        Glide.with(this).load(url).into(detailsIV);
    }

    private void setEmptyImageView() {
        detailsIV.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(R.drawable.nim_image_default));
    }
}
