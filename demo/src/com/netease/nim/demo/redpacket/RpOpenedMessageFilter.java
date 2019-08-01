package com.netease.nim.demo.redpacket;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.session.extension.RedPacketOpenedAttachment;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by hzchenkang on 2017/8/2.
 * 结合observeReceiveMessage、observeRecentContact 做一个应用层面的消息过滤，过滤掉与自己无关的拆红包消息
 * 前提是RpOpenedMessageFilter 必须先于其他ReceiveMessage、RecentContact 观察者之前注册
 * 该消息最好设置为不计未读数
 */
public class RpOpenedMessageFilter {

    private static Map<String, IMMessage> delete = new HashMap<>();

    private static Map<String, IMMessage> emptyCheck = new HashMap<>();

    private static Observer<List<IMMessage>> messageObserver = new Observer<List<IMMessage>>() {
        @Override
        public void onEvent(final List<IMMessage> imMessages) {
            if (imMessages != null) {
                Iterator<IMMessage> iterator = imMessages.iterator();
                while (iterator.hasNext()) {
                    final IMMessage imMessage = iterator.next();
                    if (shouldFilter(imMessage)) {
                        delete.put(imMessage.getUuid(), imMessage);
                        // 过滤掉，其他观察者不会再收到了
                        iterator.remove();
                    }
                }
            }
        }
    };

    private static Observer<List<RecentContact>> recentContactObserver = new Observer<List<RecentContact>>() {
        @Override
        public void onEvent(List<RecentContact> recentContacts) {
            if (recentContacts != null) {
                Iterator<RecentContact> iterator = recentContacts.iterator();
                while (iterator.hasNext()) {
                    RecentContact recentContact = iterator.next();

                    if (delete.containsKey(recentContact.getRecentMessageId())) {
                        // 等待删除该消息之后再通知该 recentContact
                        iterator.remove();
                    } else if (delete.isEmpty() && emptyCheck.containsKey(recentContact.getContactId())) {
                        // deleteChattingHistory 之后再次回调，判断是否 remove
                        if (recentContact.getRecentMessageId().isEmpty()) {
                            iterator.remove();
                        }
                    }
                }
            }

            if (!emptyCheck.isEmpty()) {
                emptyCheck.clear();
            } else if (!delete.isEmpty()) {
                Iterator<String> iterator = delete.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    emptyCheck.put(delete.get(key).getSessionId(), delete.get(key));
                    NIMClient.getService(MsgService.class).deleteChattingHistory(delete.get(key));
                }
                delete.clear();
            }
        }
    };

    private static boolean shouldFilter(IMMessage message) {
        if (message != null && message.getAttachment() instanceof RedPacketOpenedAttachment) {
            RedPacketOpenedAttachment attachment = (RedPacketOpenedAttachment) message.getAttachment();
            if (!attachment.belongTo(DemoCache.getAccount())) {
                return true;
            }
        }
        return false;
    }

    public static void startFilter() {
        NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(messageObserver, true);
        NIMClient.getService(MsgServiceObserve.class).observeRecentContact(recentContactObserver, true);
    }

    public static void stopFilter() {
        NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(messageObserver, false);
        NIMClient.getService(MsgServiceObserve.class).observeRecentContact(recentContactObserver, false);
    }
}
