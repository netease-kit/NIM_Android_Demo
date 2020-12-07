package com.netease.nim.uikit.business.chatroom.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.model.chatroom.ChatRoomProvider;
import com.netease.nim.uikit.business.chatroom.helper.ChatRoomHelper;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.chatroom.constant.MemberType;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessageExtension;


/**
 * 聊天室成员姓名
 * Created by hzxuwen on 2016/1/20.
 */
public class ChatRoomViewHolderHelper {

    /**
     * 所有信息都从chat member 获取
     *
     * @param message
     * @return
     */
    public static String getNameText(ChatRoomMessage message) {
        ChatRoomMessageExtension extension = message.getChatRoomMessageExtension();
        if (extension != null && !TextUtils.isEmpty(extension.getSenderNick())) {
            return extension.getSenderNick();
        } else {
            ChatRoomProvider provider = NimUIKitImpl.getChatRoomProvider();
            ChatRoomMember member = provider.getChatRoomMember(message.getSessionId(),
                                                               message.getFromAccount());
            if (member == null) {
                // fetch
                provider.fetchMember(message.getSessionId(), message.getFromAccount(), null);
                return null;
            }
            return member.getNick();
        }
    }

    /**
     * 所有信息都从chat member 获取
     *
     * @param message
     * @return
     */
    public static String getAvatar(ChatRoomMessage message) {
        ChatRoomMessageExtension extension = message.getChatRoomMessageExtension();
        if (extension != null && !TextUtils.isEmpty(extension.getSenderAvatar())) {
            return extension.getSenderAvatar();
        } else {
            ChatRoomProvider provider = NimUIKitImpl.getChatRoomProvider();
            ChatRoomMember member = provider.getChatRoomMember(message.getSessionId(),
                                                               message.getFromAccount());
            if (member == null) {
                // fetch
                provider.fetchMember(message.getSessionId(), message.getFromAccount(), null);
                return null;
            }
            return member.getAvatar();
        }
    }

    public static void setStyleOfNameTextView(ChatRoomMessage message, TextView nameTextView,
                                              ImageView nameIconView) {
        nameTextView.setTextColor(
                NimUIKitImpl.getContext().getResources().getColor(R.color.color_black_ff999999));
        MemberType type = ChatRoomHelper.getMemberTypeByRemoteExt(message);
        if (type == MemberType.ADMIN) {
            nameIconView.setImageResource(R.drawable.nim_admin_icon);
            nameIconView.setVisibility(View.VISIBLE);
        } else if (type == MemberType.CREATOR) {
            nameIconView.setImageResource(R.drawable.nim_master_icon);
            nameIconView.setVisibility(View.VISIBLE);
        } else {
            nameIconView.setVisibility(View.GONE);
        }
    }
}
