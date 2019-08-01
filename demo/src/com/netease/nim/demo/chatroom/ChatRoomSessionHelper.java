package com.netease.nim.demo.chatroom;

import com.netease.nim.demo.chatroom.viewholder.ChatRoomMsgViewHolderGuess;
import com.netease.nim.demo.session.action.GuessAction;
import com.netease.nim.demo.session.extension.GuessAttachment;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.chatroom.ChatRoomSessionCustomization;

import java.util.ArrayList;

/**
 * UIKit自定义聊天室消息界面用法展示类
 * <p>
 * Created by huangjun on 2017/9/18.
 */

public class ChatRoomSessionHelper {

    public static void init() {
        registerViewHolders();
        NimUIKit.setCommonChatRoomSessionCustomization(getChatRoomSessionCustomization());
    }

    private static void registerViewHolders() {
        NimUIKit.registerChatRoomMsgItemViewHolder(GuessAttachment.class, ChatRoomMsgViewHolderGuess.class);
    }

    private static ChatRoomSessionCustomization getChatRoomSessionCustomization() {
        ArrayList<BaseAction> actions = new ArrayList<>();
        actions.add(new GuessAction());
        ChatRoomSessionCustomization customization = new ChatRoomSessionCustomization();
        customization.actions = actions;
        return customization;
    }
}
