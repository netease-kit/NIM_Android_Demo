package com.netease.nim.demo.chatroom.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.ui.imageview.CircleImageView;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.nos.model.NosThumbParam;
import com.netease.nimlib.sdk.nos.util.NosThumbImageUtil;

public class ChatRoomImageView extends CircleImageView {

    public static final int DEFAULT_THUMB_SIZE = (int) NimUIKit.getContext().getResources()
                                                               .getDimension(
                                                                       R.dimen.avatar_max_size);

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

    /**
     * short to long?
     *
     * @param url
     */
    public void loadAvatarByUrl(String roomId, String url) {
        if (TextUtils.isEmpty(url)) {
            // avoid useless call
            loadAvatar(url, DEFAULT_THUMB_SIZE);
        } else {
            /*
             * 若使用网易云信云存储，这里可以设置下载图片的压缩尺寸，生成下载URL
             * 如果图片来源是非网易云信云存储，请不要使用NosThumbImageUtil
             */
            NIMClient.getService(NosService.class).getOriginUrlFromShortUrl(roomId, url)
                     .setCallback(new RequestCallbackWrapper<String>() {

                         @Override
                         public void onResult(int code, String result, Throwable exception) {
                             if (TextUtils.isEmpty(result)) {
                                 result = url;
                             }
                             final String thumbUrl = makeAvatarThumbNosUrl(result,
                                                                           DEFAULT_THUMB_SIZE);
                             loadAvatar(thumbUrl, DEFAULT_THUMB_SIZE);
                         }
                     });
        }
    }

    /**
     * 生成头像缩略图NOS URL地址（用作ImageLoader缓存的key）
     */
    private static String makeAvatarThumbNosUrl(final String url, final int thumbSize) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        return thumbSize > 0 ? NosThumbImageUtil.makeImageThumbUrl(url,
                                                                   NosThumbParam.ThumbType.Crop,
                                                                   thumbSize, thumbSize) : url;
    }

    private void loadAvatar(final String url, final int thumbSize) {
        Glide.with(getContext().getApplicationContext()).asBitmap().load(url).apply(
                new RequestOptions().centerCrop().placeholder(DEFAULT_AVATAR_RES_ID)
                                    .error(DEFAULT_AVATAR_RES_ID).override(thumbSize, thumbSize))
             .into(this);
    }
}
