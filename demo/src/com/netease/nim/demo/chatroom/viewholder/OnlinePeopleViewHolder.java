package com.netease.nim.demo.chatroom.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.demo.R;
import com.netease.nim.demo.chatroom.widget.ChatRoomImageView;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nimlib.sdk.chatroom.constant.MemberType;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;

/**
 * Created by hzxuwen on 2015/12/18.
 */
public class OnlinePeopleViewHolder extends TViewHolder {
    private static final String TAG = OnlinePeopleViewHolder.class.getSimpleName();
    private ChatRoomImageView userHeadImage;
    private TextView nameText;
    private ChatRoomMember chatRoomMember;
    private ImageView identityImage;

    @Override
    protected int getResId() {
        return R.layout.online_people_item;
    }

    @Override
    protected void inflate() {
        identityImage = findView(R.id.identity_image);
        userHeadImage = findView(R.id.user_head);
        nameText = findView(R.id.user_name);
    }

    @Override
    protected void refresh(Object item) {
        chatRoomMember = (ChatRoomMember) item;
        if (chatRoomMember.getMemberType() == MemberType.CREATOR) {
            identityImage.setVisibility(View.VISIBLE);
            identityImage.setImageDrawable(context.getResources().getDrawable(R.drawable.master_icon));
        } else if (chatRoomMember.getMemberType() == MemberType.ADMIN) {
            identityImage.setVisibility(View.VISIBLE);
            identityImage.setImageDrawable(context.getResources().getDrawable(R.drawable.admin_icon));
        } else {
            identityImage.setVisibility(View.GONE);
        }
        userHeadImage.loadAvatarByUrl(chatRoomMember.getAvatar());
        nameText.setText(TextUtils.isEmpty(chatRoomMember.getNick()) ? "" : chatRoomMember.getNick());
    }

}
