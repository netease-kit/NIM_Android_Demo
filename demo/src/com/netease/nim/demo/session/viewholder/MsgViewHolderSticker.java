package com.netease.nim.demo.session.viewholder;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.netease.nim.demo.R;
import com.netease.nim.uikit.session.emoji.StickerManager;
import com.netease.nim.demo.session.extension.StickerAttachment;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderThumbBase;

/**
 * Created by zhoujianghua on 2015/8/7.
 */
public class MsgViewHolderSticker extends MsgViewHolderBase {

    private ImageView baseView;

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_sticker;
    }

    @Override
    protected void inflateContentView() {
        baseView = findViewById(R.id.message_item_sticker_image);
        baseView.setMaxWidth(MsgViewHolderThumbBase.getImageMaxEdge());
    }

    @Override
    protected void bindContentView() {
        StickerAttachment attachment = (StickerAttachment) message.getAttachment();
        if (attachment == null) {
            return;
        }

        Bitmap bm = StickerManager.getInstance().getStickerBitmap(context, attachment.getCatalog(), attachment
                .getChartlet());
        if (bm != null) {
            baseView.setImageBitmap(bm);
        } else {
            baseView.setImageResource(R.drawable.default_img_failed);
        }
    }

    @Override
    protected int leftBackground() {
        return 0;
    }

    @Override
    protected int rightBackground() {
        return 0;
    }
}
