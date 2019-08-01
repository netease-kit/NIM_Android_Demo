package com.netease.nim.demo.chatroom.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.ui.imageview.CircleImageView;
import com.netease.nim.uikit.api.NimUIKit;

public class ChatRoomImageView extends CircleImageView {

    public static final int DEFAULT_THUMB_SIZE = (int) NimUIKit.getContext().getResources().getDimension(R.dimen.avatar_max_size);
    private static final int DEFAULT_AVATAR_RES_ID = R.drawable.nim_avatar_default;

    public ChatRoomImageView(Context context) {
        super(context);
    }

    public ChatRoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatRoomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void loadAvatarByUrl(String url) {
        loadAvatar(url, DEFAULT_THUMB_SIZE);
    }

    private void loadAvatar(final String url, final int thumbSize) {
        Glide.with(getContext().getApplicationContext())
                .asBitmap()
                .load(url)
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(DEFAULT_AVATAR_RES_ID)
                        .error(DEFAULT_AVATAR_RES_ID)
                        .override(thumbSize, thumbSize))
                .into(this);
    }
}
