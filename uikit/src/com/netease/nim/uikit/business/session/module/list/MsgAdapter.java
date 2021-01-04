package com.netease.nim.uikit.business.session.module.list;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.session.helper.MessageHelper;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderFactory;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by huangjun on 2016/12/21.
 */
public class MsgAdapter extends BaseMultiItemFetchLoadAdapter<IMMessage, BaseViewHolder> {

    private Map<Class<? extends MsgViewHolderBase>, Integer> holder2ViewType;

    private ViewHolderEventListener eventListener;
    private Map<String, Float> progresses; // 有文件传输，需要显示进度条的消息ID map
    private String messageId;
    private Container container;

    public MsgAdapter(RecyclerView recyclerView, List<IMMessage> data, Container container) {
        super(recyclerView, data);

        timedItems = new HashSet<>();
        progresses = new HashMap<>();

        // view type, view holder
        holder2ViewType = new HashMap<>();
        List<Class<? extends MsgViewHolderBase>> holders = MsgViewHolderFactory.getAllViewHolders();
        int viewType = 0;
        for (Class<? extends MsgViewHolderBase> holder : holders) {
            viewType++;
            addItemType(viewType, R.layout.nim_message_item, holder);
            holder2ViewType.put(holder, viewType);
        }

        this.container = container;
//        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onChanged() {
//                Log.e("TAG","AdapterData Change....");
//            }
//        });
    }

    @Override
    protected int getViewType(IMMessage message) {
        return holder2ViewType.get(MsgViewHolderFactory.getViewHolderByType(message));
    }

    @Override
    protected String getItemKey(IMMessage item) {
        return item.getUuid();
    }

    public void setEventListener(ViewHolderEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public ViewHolderEventListener getEventListener() {
        return eventListener;
    }

    public void deleteItem(IMMessage message, boolean isRelocateTime) {
        if (message == null) {
            return;
        }

        int index = 0;
        for (IMMessage item : getData()) {
            if (item.isTheSame(message)) {
                break;
            }
            ++index;
        }

        if (index < getDataSize()) {
            remove(index);
            if (isRelocateTime) {
                relocateShowTimeItemAfterDelete(message, index);
            }
//            notifyDataSetChanged(); // 可以不要！！！
        }
    }

    public void deleteItems(List<IMMessage> msgList, boolean isRelocateTime) {
        if (CommonUtil.isEmpty(msgList)) {
            return;
        }

        int index = 0;
        List<Integer> deleteIndexList = new ArrayList<>(msgList.size());
        Set<String> msgUuidSet = MessageHelper.getUuidSet(msgList);
        List<IMMessage> items = getData();
        for (IMMessage item : items) {
            if (msgUuidSet.contains(item.getUuid())) {
                deleteIndexList.add(index);
            }
            ++index;
        }

        if (!deleteIndexList.isEmpty()) {
            if (isRelocateTime) {
                IMMessage toDeleteMsg;
                for (int i = deleteIndexList.size() - 1; i >= 0; --i) {
                    index = deleteIndexList.get(i);
                    toDeleteMsg = items.get(index);
                    remove(index);
                    relocateShowTimeItemAfterDelete(toDeleteMsg, index);
                }
            }
//            notifyDataSetChanged(); // 可以不要！！！
        }
    }

    public void deleteItemsRange(long fromTime, long toTime, boolean isRelocateTime) {
        if (toTime <= 0 || fromTime >= toTime) {
            return;
        }

        List<IMMessage> items = getData();
        if (CommonUtil.isEmpty(items)) {
            return;
        }
        int index;
        IMMessage item;
        long itemTime;
        ListIterator<IMMessage> itemIterator = items.listIterator(items.size());
        while (itemIterator.hasPrevious()){
            try {
                index = itemIterator.previousIndex();
                item = itemIterator.previous();
                itemTime = item.getTime();

                if (itemTime < toTime && itemTime > fromTime) {
                    itemIterator.remove();
                    notifyItemRemoved(index);
                    onRemove(item);
                    if (isRelocateTime) {
                        relocateShowTimeItemAfterDelete(item, index);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public float getProgress(IMMessage message) {
        Float progress = progresses.get(message.getUuid());
        return progress == null ? 0 : progress;
    }

    public void putProgress(IMMessage message, float progress) {
        progresses.put(message.getUuid(), progress);
    }

    /**
     * *********************** 时间显示处理 ***********************
     */

    private Set<String> timedItems; // 需要显示消息时间的消息ID
    private IMMessage lastShowTimeItem; // 用于消息时间显示,判断和上条消息间的时间间隔

    public boolean needShowTime(IMMessage message) {
        return timedItems.contains(message.getUuid());
    }

    /**
     * 列表加入新消息时，更新时间显示
     */
    public void updateShowTimeItem(List<IMMessage> items, boolean fromStart, boolean update) {
        IMMessage anchor = fromStart ? null : lastShowTimeItem;
        for (IMMessage message : items) {
            if (setShowTimeFlag(message, anchor)) {
                anchor = message;
            }
        }

        if (update) {
            lastShowTimeItem = anchor;
        }
    }

    /**
     * 是否显示时间item
     */
    private boolean setShowTimeFlag(IMMessage message, IMMessage anchor) {
        boolean update = false;

        if (hideTimeAlways(message)) {
            setShowTime(message, false);
        } else {
            if (anchor == null) {
                setShowTime(message, true);
                update = true;
            } else {
                long time = anchor.getTime();
                long now = message.getTime();

                if (now - time == 0) {
                    // 消息撤回时使用
                    setShowTime(message, true);
                    lastShowTimeItem = message;
                    update = true;
                } else if (now - time < (NimUIKitImpl.getOptions().displayMsgTimeWithInterval)) {
                    setShowTime(message, false);
                } else {
                    setShowTime(message, true);
                    update = true;
                }
            }
        }

        return update;
    }

    private void setShowTime(IMMessage message, boolean show) {
        if (show) {
            timedItems.add(message.getUuid());
        } else {
            timedItems.remove(message.getUuid());
        }
    }

    private void relocateShowTimeItemAfterDelete(IMMessage messageItem, int index) {
        // 如果被删的项显示了时间，需要继承
        if (needShowTime(messageItem)) {
            setShowTime(messageItem, false);
            if (getDataSize() > 0) {
                IMMessage nextItem;
                if (index == getDataSize()) {
                    //删除的是最后一项
                    nextItem = getItem(index - 1);
                } else {
                    //删除的不是最后一项
                    nextItem = getItem(index);
                }

                // 增加其他不需要显示时间的消息类型判断
                if (hideTimeAlways(nextItem)) {
                    setShowTime(nextItem, false);
                    if (lastShowTimeItem != null && lastShowTimeItem != null
                            && lastShowTimeItem.isTheSame(messageItem)) {
                        lastShowTimeItem = null;
                        for (int i = getDataSize() - 1; i >= 0; i--) {
                            IMMessage item = getItem(i);
                            if (needShowTime(item)) {
                                lastShowTimeItem = item;
                                break;
                            }
                        }
                    }
                } else {
                    setShowTime(nextItem, true);
                    if (lastShowTimeItem == null
                            || (lastShowTimeItem != null && lastShowTimeItem.isTheSame(messageItem))) {
                        lastShowTimeItem = nextItem;
                    }
                }
            } else {
                lastShowTimeItem = null;
            }
        }
    }

    private boolean hideTimeAlways(IMMessage message) {
        if (message.getSessionType() == SessionTypeEnum.ChatRoom) {
            return true;
        }
        switch (message.getMsgType()) {
            case notification:
                return true;
            default:
                return false;
        }
    }

    public interface ViewHolderEventListener {
        // 长按事件响应处理
        boolean onViewHolderLongClick(View clickView, View viewHolderView, IMMessage item);

        // 发送失败或者多媒体文件下载失败指示按钮点击响应处理
        void onFailedBtnClick(IMMessage resendMessage);

        // viewholder footer按钮点击，如机器人继续会话
        void onFooterClick(IMMessage message);

        /**
         * 消息对应的复选框的状况变化时回调
         * 状态: true: 选中; false: 未被选中; null: 选则无效（复选框不可见，且状态重置为未被选中）
         *
         * @param index    消息在列表中的位置
         * @param newState 变化后的状态
         */
        void onCheckStateChanged(int index, Boolean newState);
    }

    /**
     * 为了在实现ViewHolderEventListener时只需要复写需要的部分
     */
    public static class BaseViewHolderEventListener implements ViewHolderEventListener {

        @Override
        public boolean onViewHolderLongClick(View clickView, View viewHolderView, IMMessage item) {
            return false;
        }

        @Override
        public void onFailedBtnClick(IMMessage resendMessage) {
        }

        @Override
        public void onFooterClick(IMMessage message) {
        }

        @Override
        public void onCheckStateChanged(int index, Boolean newState) {

        }
    }

    public void setUuid(String messageId) {
        this.messageId = messageId;
    }

    public String getUuid() {
        return messageId;
    }

    public Container getContainer() {
        return container;
    }
}
