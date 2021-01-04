package com.netease.nim.demo.session.adapter;

import android.app.Activity;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.main.helper.MessageHelper;
import com.netease.nim.uikit.business.session.activity.WatchMultiRetweetPictureActivity;
import com.netease.nim.uikit.business.session.emoji.MoonUtil;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.business.session.module.list.MsgAdapter;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderAudio;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderFactory;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderPicture;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MultiRetweetAdapter extends RecyclerView.Adapter<MultiRetweetAdapter.ViewHolder> {
    private List<IMMessage> mItems;
    private Activity mContext;
    private RecyclerView mRecyclerView;

    public MultiRetweetAdapter(RecyclerView recyclerView, List<IMMessage> items, Activity context) {
        mItems = items;
        mContext = context;
        mRecyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.nim_multi_retweet_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IMMessage msg = mItems.get(position);
        MsgViewHolderBase subViewHolder = null;
        try {
            Class<? extends MsgViewHolderBase> viewHolerClazz = MsgViewHolderFactory.getViewHolderByType(msg);
            Constructor vidwHolderConstructor = viewHolerClazz.getDeclaredConstructors()[0]; // 第一个显式的构造函数
            vidwHolderConstructor.setAccessible(true);

            MsgAdapter subAdapter = new MsgAdapter(mRecyclerView, mItems, new Container(mContext, null, null, null));
            subViewHolder = (MsgViewHolderBase) vidwHolderConstructor.newInstance(new Object[]{subAdapter});
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.setViews(msg, mContext, showDate(position), subViewHolder);
    }

    /**
     * 判断是否需要显示日期
     * 如果跨越一天，则要显示
     *
     * @param position 项所在的位置
     * @return true: 显示; false: 不显示
     */
    private boolean showDate(final int position) {
        if (position < 0) {
            return false;
        }
        if (position == 0) {
            return true;
        }
        IMMessage message = mItems.get(position);
        IMMessage lastMessage = mItems.get(position - 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
        Date msgDate = new Date(message.getTime());
        Date lastDate = new Date(lastMessage.getTime());
        return !dateFormat.format(msgDate).equals(dateFormat.format(lastDate));
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        /** 发送方名称 */
        private TextView mSessionNameTV;
        /** 头像 */
        private HeadImageView mAvatarHIV;
        /** 消息内容 */
        private TextView mDetailsTV;
        /** 图片、视频的简略展示 */
        private ImageView mDetailsIV;
        /** 消息时间 */
        private TextView mTimeTV;
        /** 日期标记 */
        private TextView mDateTV;

        private FrameLayout mContentContainer;

        private MsgViewHolderBase mContentViewHolder;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void setViews(IMMessage msg, Activity context, boolean showDate, MsgViewHolderBase contentViewHolder) {
            mContentViewHolder = contentViewHolder;
            findViews();
            initViews(msg, context, showDate);
        }

        private void findViews() {
            mAvatarHIV = itemView.findViewById(R.id.message_item_portrait_left);
            mSessionNameTV = itemView.findViewById(R.id.tv_session_name);
            mDetailsTV = itemView.findViewById(R.id.tv_details);
            mDetailsIV = itemView.findViewById(R.id.img_details);
            mTimeTV = itemView.findViewById(R.id.tv_time);
            mDateTV = itemView.findViewById(R.id.tv_date);
            mContentContainer = itemView.findViewById(R.id.fl_content_container);
        }

        private void initViews(IMMessage msg, Activity context, boolean showDate) {
            //头像
            mAvatarHIV.loadBuddyAvatar(msg);

            //会话名称
            String senderName = MessageHelper.getStoredNameFromSessionId(msg.getFromAccount(), SessionTypeEnum.P2P);
            mSessionNameTV.setText(senderName == null ? msg.getFromAccount() : senderName);

            //消息时间和日期 HH:mm
            long time = msg.getTime();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            Date date = new Date(time);

            mTimeTV.setText(timeFormat.format(date));
            mDateTV.setText(dateFormat.format(date));
            mDateTV.setVisibility(showDate ? View.VISIBLE : View.GONE);

            //初始化消息内容
            initContent(msg, context);

            //设置点击事件
            itemView.setOnClickListener((v) -> {
                if (mContentViewHolder == null) {
                    return;
                }

                if (mContentViewHolder instanceof MsgViewHolderPicture && msg.getAttachment() instanceof ImageAttachment){
                    WatchMultiRetweetPictureActivity.start(context, (ImageAttachment) msg.getAttachment());
                    return;
                }
                mContentViewHolder.onItemClick();
            });

        }

        private void initContent(IMMessage msg, Activity context) {
            //读取ViewHolder失败
            if (mContentViewHolder == null) {
                //缺省方式加载资源
                initContentInSimple(msg, context);
                return;
            }

            mDetailsTV.setVisibility(View.GONE);
            mDetailsIV.setVisibility(View.GONE);

            try {
                int subViewId = mContentViewHolder.getContentResId();
                mContentContainer.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(context);
                inflater.inflate(subViewId, mContentContainer, true);
                mContentViewHolder.initParameter(itemView, context, msg, getLayoutPosition());
                mContentViewHolder.inflateContentView();
                mContentViewHolder.bindContentView();
                if (mContentViewHolder instanceof MsgViewHolderAudio) {
                    NIMClient.getService(MsgService.class).downloadAttachment(msg, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void initContentInSimple(IMMessage msg, Activity context) {
            mContentContainer.removeAllViews();
            MsgTypeEnum msgType = msg.getMsgType();
            //消息概要
            //文本
            if (MsgTypeEnum.text.equals(msgType)) {
                mDetailsTV.setVisibility(View.VISIBLE);
                mDetailsIV.setVisibility(View.GONE);
                MoonUtil.identifyFaceExpression(DemoCache.getContext(), mDetailsTV, MessageHelper.getContent(msg), ImageSpan.ALIGN_BOTTOM);
            }
            //图片
            else if (MsgTypeEnum.image.equals(msgType)) {
                mDetailsTV.setVisibility(View.GONE);
                mDetailsIV.setVisibility(View.VISIBLE);
                ImageAttachment attachment = (ImageAttachment) msg.getAttachment();
                Glide.with(context).load(attachment.getUrl()).into(mDetailsIV);
            }
            //视频
            else if (MsgTypeEnum.video.equals(msgType)) {
                mDetailsTV.setVisibility(View.GONE);
                mDetailsIV.setVisibility(View.VISIBLE);
                VideoAttachment attachment = (VideoAttachment) msg.getAttachment();
                Glide.with(context).load(attachment.getThumbUrl()).into(mDetailsIV);
            } else {
                mDetailsTV.setVisibility(View.VISIBLE);
                mDetailsIV.setVisibility(View.GONE);

                mDetailsTV.setText(MessageHelper.getContent(msg));
            }
            return;
        }
    }
}
