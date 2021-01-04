package com.netease.nim.demo.main.helper;

import android.text.TextUtils;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.session.extension.MultiRetweetAttachment;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.CreateMessageCallback;
import com.netease.nim.uikit.business.session.actions.PickImageAction;
import com.netease.nim.uikit.business.team.helper.SuperTeamHelper;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.util.log.sdk.wrapper.NimLog;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SystemMessageStatus;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.superteam.SuperTeam;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by huangjun on 2015/4/9.
 */
public class MessageHelper {
    public static String TAG = "MessageHelper";

    public static String getName(String account, SessionTypeEnum sessionType) {
        if (sessionType == SessionTypeEnum.P2P) {
            return UserInfoHelper.getUserDisplayName(account);
        } else if (sessionType == SessionTypeEnum.Team) {
            return TeamHelper.getTeamName(account);
        } else if (sessionType == SessionTypeEnum.SUPER_TEAM) {
            return SuperTeamHelper.getTeamName(account);
        }
        return account;
    }

    public static String getVerifyNotificationText(SystemMessage message) {
        StringBuilder sb = new StringBuilder();
        String fromAccount = UserInfoHelper.getUserDisplayNameEx(message.getFromAccount(), "你");
        String teamName;
        switch (message.getType()) {
            case TeamInvite:
            case DeclineTeamInvite:
            case ApplyJoinTeam:
            case RejectTeamApply:
            case AddFriend:
                Team team = NimUIKit.getTeamProvider().getTeamById(message.getTargetId());
                if (team == null && message.getAttachObject() instanceof Team) {
                    team = (Team) message.getAttachObject();
                }
                teamName = team == null ? message.getTargetId() : team.getName();
                break;
            case SuperTeamInvite:
            case SuperTeamInviteReject:
            case SuperTeamApply:
            case SuperTeamApplyReject:
                SuperTeam superTeam = NimUIKit.getSuperTeamProvider().getTeamById(message.getTargetId());
                if (superTeam == null && message.getAttachObject() instanceof SuperTeam) {
                    superTeam = (SuperTeam) message.getAttachObject();
                }
                teamName = superTeam == null ? message.getTargetId() : superTeam.getName();
                break;
            default:
                teamName = message.getTargetId();
        }

        if (message.getType() == SystemMessageType.TeamInvite || message.getType() == SystemMessageType.SuperTeamInvite) {
            sb.append("邀请").append("你").append("加入群 ").append(teamName);
        } else if (message.getType() == SystemMessageType.DeclineTeamInvite || message.getType() == SystemMessageType.SuperTeamInviteReject) {
            sb.append(fromAccount).append("拒绝了群 ").append(teamName).append(" 邀请");
        } else if (message.getType() == SystemMessageType.ApplyJoinTeam || message.getType() == SystemMessageType.SuperTeamApply) {
            sb.append("申请加入群 ").append(teamName);
        } else if (message.getType() == SystemMessageType.RejectTeamApply || message.getType() == SystemMessageType.SuperTeamApplyReject) {
            sb.append(fromAccount).append("拒绝了你加入群 ").append(teamName).append("的申请");
        } else if (message.getType() == SystemMessageType.AddFriend) {
            AddFriendNotify attachData = (AddFriendNotify) message.getAttachObject();
            if (attachData != null) {
                if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_DIRECT) {
                    sb.append("已添加你为好友");
                } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_AGREE_ADD_FRIEND) {
                    sb.append("通过了你的好友请求");
                } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_REJECT_ADD_FRIEND) {
                    sb.append("拒绝了你的好友请求");
                } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST) {
                    sb.append("请求添加好友" + (TextUtils.isEmpty(message.getContent()) ? "" : "：" + message.getContent()));
                }
            }
        }

        return sb.toString();
    }

    /**
     * 是否验证消息需要处理（需要有同意拒绝的操作栏）
     */
    public static boolean isVerifyMessageNeedDeal(SystemMessage message) {
        final SystemMessageType msgType = message.getType();
        if (msgType == SystemMessageType.AddFriend) {
            if (message.getAttachObject() != null) {
                AddFriendNotify attachData = (AddFriendNotify) message.getAttachObject();
                if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_DIRECT ||
                        attachData.getEvent() == AddFriendNotify.Event.RECV_AGREE_ADD_FRIEND ||
                        attachData.getEvent() == AddFriendNotify.Event.RECV_REJECT_ADD_FRIEND) {
                    return false; // 对方直接加你为好友，对方通过你的好友请求，对方拒绝你的好友请求
                } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST) {
                    return true; // 好友验证请求
                }
            }
            return false;
        } else if (msgType == SystemMessageType.TeamInvite ||
                msgType == SystemMessageType.ApplyJoinTeam ||
                msgType == SystemMessageType.SuperTeamApply ||
                msgType == SystemMessageType.SuperTeamInvite
        ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否含有附言，含有附言同时满足两个条件-: 1. 消息的content字段不为空; 2. 消息类型对应的请求可以输入附言
     *
     * @return 有: true; 没有: false
     */
    public static boolean hasPostscript(SystemMessage message) {
        final SystemMessageType messageType = message.getType();
        return !TextUtils.isEmpty(message.getContent()) && (SystemMessageType.SuperTeamApply.equals(messageType) ||
                SystemMessageType.SuperTeamApplyReject.equals(messageType) ||
        SystemMessageType.SuperTeamInvite.equals(messageType) ||
                SystemMessageType.SuperTeamInviteReject.equals(messageType));
    }

    public static String getVerifyNotificationDealResult(SystemMessage message) {
        if (message.getStatus() == SystemMessageStatus.passed) {
            return "已同意";
        } else if (message.getStatus() == SystemMessageStatus.declined) {
            return "已拒绝";
        } else if (message.getStatus() == SystemMessageStatus.ignored) {
            return "已忽略";
        } else if (message.getStatus() == SystemMessageStatus.expired) {
            return "已过期";
        } else {
            return "未处理";
        }
    }

    /**
     * 合并发送消息，并通过ActivityResult回传合并后的消息 (this->上层Activity->MessageListPanelEx)；最后由接收方发送
     */
    public static void createMultiRetweet(final List<IMMessage> toBeSent, final boolean shouldEncrypt, final CreateMessageCallback callback) {
        if (toBeSent.isEmpty()) {
            return;
        }

        //将多条消息合并成文件
        //现在是明文字节码，加密后存储密文字节码
        final byte[] fileBytes = MessageBuilder.createForwardMessageListFileDetail(toBeSent).getBytes();
        final byte[] key;
        final byte[] encryptedFileBytes;
        if (shouldEncrypt) {
            //RC4加密
            key = genRC4Key();
            encryptedFileBytes = encryptByRC4(fileBytes, key);
        } else {
            key = new byte[0];
            encryptedFileBytes = fileBytes;
        }
        //encryptedFileBytes是否是已加密字节码
        final boolean isEncrypted = encryptedFileBytes != fileBytes;
        if (isEncrypted != shouldEncrypt) {
            NimLog.d(TAG, "failed to encrypt file with RC4");
        }
        //将字节码的16进制String类型写入文件
        final String fileName = TAG + System.currentTimeMillis();
        final File file = new File(StorageUtil.getDirectoryByDirType(StorageType.TYPE_FILE), fileName);
        try {
            if (file.exists() || file.createNewFile()) {
                OutputStream outputStream = new FileOutputStream(file, false);
                outputStream.write(encryptedFileBytes);
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将文件上传到Nos，如果成功，将得到该文件的下载链接
        NIMClient.getService(NosService.class).upload(file, PickImageAction.MIME_JPEG).setCallback(new RequestCallback<String>() {

            @Override
            public void onSuccess(String url) {
                NimLog.d(TAG, "NosService.upload/onSuccess, url=" + url);
                file.delete();
                if (TextUtils.isEmpty(url)) {
                    return;
                }

                final IMMessage firstMsg = toBeSent.get(0);
                final IMMessage secondMsg = toBeSent.size() > 1 ? toBeSent.get(1) : null;
                //第一条消息的展示内容
                String firstContent = getContent(firstMsg);
                //会话类型
                final SessionTypeEnum sessionType = firstMsg.getSessionType();

                //标题的sessionID部分
                final String sessionId = firstMsg.getSessionId();
                String sessionName = null;
                switch (sessionType) {
                    case P2P:
                        sessionName = getStoredNameFromSessionId(NimUIKit.getAccount(), SessionTypeEnum.P2P);
                        break;
                    case Team:
                    case SUPER_TEAM:
                        sessionName = getStoredNameFromSessionId(sessionId, sessionType);
                        break;
                    default:
                        break;
                }
                if (sessionName == null) {
                    sessionName = sessionId;
                }


                String nick1 = getStoredNameFromSessionId(firstMsg.getFromAccount(), SessionTypeEnum.P2P);
                String nick2 = null;
                if (secondMsg != null) {
                    nick2 = getStoredNameFromSessionId(secondMsg.getFromAccount(), SessionTypeEnum.P2P);
                }

                //创建附件
                MultiRetweetAttachment attachment = new MultiRetweetAttachment(
                        sessionId, sessionName, url, MD5.getMD5(encryptedFileBytes), false, isEncrypted,
                        new String(key), nick1, firstContent, nick2, getContent(secondMsg)
                );
                String pushContent = DemoCache.getContext().getString(R.string.msg_type_multi_retweet);
                //创建MultiRetweet类型自定义信息
                IMMessage packedMsg = MessageBuilder.createCustomMessage(firstMsg.getSessionId(), sessionType, pushContent, attachment);
                packedMsg.setPushContent(pushContent);
                callback.onFinished(packedMsg);
            }

            @Override
            public void onFailed(int code) {
                NimLog.d(TAG, "NosService.upload/onFailed, code=" + code);
                file.delete();
                callback.onFailed(code);
            }

            @Override
            public void onException(Throwable exception) {
                NimLog.d(TAG, "NosService.upload/onException, exception=" + exception.getMessage());
                file.delete();
                callback.onException(exception);
            }
        });
    }

    /**
     * 根据消息类型标记和自定义消息的子类型标记判断一条消息是否是合并消息
     *
     * @param msg 待验证的消息
     * @return true: 是合并消息; false: 不是合并消息
     */
    public static boolean isMultiRetweet(IMMessage msg) {
        return msg != null && msg.getMsgType() == MsgTypeEnum.custom && msg.getAttachment() instanceof MultiRetweetAttachment;
    }

    /**
     * 获取消息的简略提示 {@link MsgTypeEnum}
     * txt: 显示content
     * 其他：有pushContent，则显示；否则查看是否有content，如果还没有，则显示"[" + MsgTypeEnum.getSendMessageTip() + "]"
     *
     * @param msg 消息体
     * @return 提示文本
     */
    public static String getContent(IMMessage msg) {
        if (msg == null) {
            return "";
        }
        MsgTypeEnum type = msg.getMsgType();
        if (type == MsgTypeEnum.text) {
            return msg.getContent();
        } else {
            String content = msg.getPushContent();
            if (!TextUtils.isEmpty(content)) {
                return content;
            }
            content = msg.getContent();
            if (!TextUtils.isEmpty(content)) {
                return content;
            }
            content = "[" + type.getSendMessageTip() + "]";
            return content;
        }
    }

    /**
     * 通过id和type，从本地存储中查询对应的群名或用户名
     *
     * @param id          群或用户的id
     * @param sessionType 会话类型
     * @return id对应的昵称
     */
    public static String getStoredNameFromSessionId(final String id, final SessionTypeEnum sessionType) {
        switch (sessionType) {
            case P2P:
                //读取对方用户名称
                NimUserInfo userInfo = NIMClient.getService(UserService.class).getUserInfo(id);
                if (userInfo == null) {
                    return null;
                }
                return userInfo.getName();
            case Team:
                //获取群信息
                Team team = NimUIKit.getTeamProvider().getTeamById(id);
                if (team == null) {
                    return null;
                }
                return team.getName();
            case SUPER_TEAM:
                //获取群信息
                SuperTeam superTeam = NimUIKit.getSuperTeamProvider().getTeamById(id);
                if (superTeam == null) {
                    return null;
                }
                return superTeam.getName();
            default:
                return null;
        }
    }

    /**
     * 生成可用于RC4加解密的秘钥
     *
     * @return 秘钥
     */
    public static byte[] genRC4Key() {
        byte[] selectionList = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int keyLen = 16;
        SecureRandom random = new SecureRandom(SecureRandom.getSeed(32));
        byte[] key = new byte[keyLen];
        random.nextBytes(key);
        for (int i = 0; i < key.length; ++i) {
            key[i] = selectionList[Math.abs(key[i] % selectionList.length)];
        }
        return key;
    }

    /**
     * RC4加密
     *
     * @param src 原始内容
     * @param key 秘钥
     * @return 加密后内容
     */
    public static byte[] encryptByRC4(byte[] src, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("RC4");
            cipher.init(Cipher.ENCRYPT_MODE, new RC4SecretKey(key));
            return cipher.doFinal(src);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException |
                InvalidKeyException |
                BadPaddingException |
                IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return src;
    }

    /**
     * RC4解密
     *
     * @param src 密文
     * @param key 秘钥
     * @return 解密后内容
     */
    public static byte[] decryptByRC4(byte[] src, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("RC4");
            cipher.init(Cipher.DECRYPT_MODE, new RC4SecretKey(key));
            return cipher.doFinal(src);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException |
                InvalidKeyException |
                BadPaddingException |
                IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class RC4SecretKey implements SecretKey {
        private SecretKeySpec spec;

        private RC4SecretKey(byte[] key) {
            this.spec = new SecretKeySpec(key, "RC4");
        }

        @Override
        public String getAlgorithm() {
            return this.spec.getAlgorithm();
        }

        @Override
        public String getFormat() {
            return spec.getFormat();
        }

        @Override
        public byte[] getEncoded() {
            return spec.getEncoded();
        }
    }
}
