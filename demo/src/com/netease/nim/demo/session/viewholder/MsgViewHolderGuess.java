package com.netease.nim.demo.session.viewholder;

import android.widget.ImageView;

import com.netease.nim.demo.R;
import com.netease.nim.demo.session.extension.GuessAttachment;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;

/**
 * Created by hzliuxuanlin on 17/9/15.
 */

public class MsgViewHolderGuess extends MsgViewHolderBase {

    private GuessAttachment guessAttachment;
    private ImageView imageView;

    public MsgViewHolderGuess(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.rock_paper_scissors;
    }

    @Override
    protected void inflateContentView() {
        imageView = (ImageView) view.findViewById(R.id.rock_paper_scissors_text);
    }

    @Override
    protected void bindContentView() {
        if (message.getAttachment() == null) {
            return;
        }
        guessAttachment = (GuessAttachment) message.getAttachment();
        switch (guessAttachment.getValue().getDesc()) {
            case "石头":
                imageView.setImageResource(R.drawable.message_view_rock);
                break;
            case "剪刀":
                imageView.setImageResource(R.drawable.message_view_scissors);
                break;
            case "布":
                imageView.setImageResource(R.drawable.message_view_paper);
                break;
            default:
                break;
        }

    }
}
