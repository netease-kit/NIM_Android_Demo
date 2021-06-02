package com.netease.yunxin.nertc.ui.team.model;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.netease.lava.nertc.sdk.video.NERtcVideoView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.nos.model.NosThumbParam;
import com.netease.nimlib.sdk.nos.util.NosThumbImageUtil;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.yunxin.nertc.model.ProfileManager;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.ui.R;
import com.netease.yunxin.nertc.ui.team.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.yunxin.nertc.ui.team.recyclerview.holder.BaseViewHolder;

import static android.view.View.GONE;

/**
 * Created by huangjun on 2017/5/4.
 */

public class TeamG2ItemViewHolder extends TeamAVChatItemViewHolderBase<TeamG2Item> {

    private static final int DEFAULT_AVATAR_THUMB_SIZE = (int) NERTCVideoCall.sharedInstance().getContext().getResources().getDimension(
            R.dimen.avatar_max_size);

    private ImageView avatarImage;

    private ImageView loadingImage;

    private NERtcVideoView surfaceView;

    private TextView nickNameText;

    private TextView stateText;

    private ProgressBar volumeBar;

    private ImageView ivMute;


    public TeamG2ItemViewHolder(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    protected void inflate(final BaseViewHolder holder) {
        avatarImage = holder.getView(R.id.avatar_image);
        loadingImage = holder.getView(R.id.loading_image);
        surfaceView = holder.getView(R.id.surface);
        nickNameText = holder.getView(R.id.nick_name_text);
        stateText = holder.getView(R.id.avchat_state_text);
        volumeBar = holder.getView(R.id.avchat_volume);
        ivMute = holder.getView(R.id.iv_voice_control);
    }

    protected void refresh(final TeamG2Item data) {
        if (data==null){
            return;
        }
        TeamMember teamMember = NIMClient.getService(TeamService.class).queryTeamMemberBlock(data.teamId, data.account);
        if (teamMember != null) {
            String name = TextUtils.isEmpty(teamMember.getTeamNick()) ? teamMember.getAccount() : teamMember.getTeamNick();
            nickNameText.setText(name);
        } else {
            nickNameText.setText(data.account);
        }

        loadAvatar(data);
        if (data.state == TeamG2Item.STATE.STATE_WAITING) {
            // 等待接听
            Glide.with(NERTCVideoCall.sharedInstance().getContext()).asGif().load(R.drawable.t_avchat_loading).into(loadingImage);
            loadingImage.setVisibility(View.VISIBLE);
            surfaceView.setVisibility(View.INVISIBLE);
            stateText.setVisibility(GONE);
            ivMute.setVisibility(GONE);
        } else if (data.state == TeamG2Item.STATE.STATE_PLAYING) {
            // 正在通话
            loadingImage.setVisibility(GONE);
            surfaceView.setVisibility(data.videoLive ? View.VISIBLE : View.INVISIBLE); // 有视频流才需要SurfaceView
            stateText.setVisibility(GONE);
            if (data.isSelf) {
                ivMute.setVisibility(GONE);
            } else {
                ivMute.setVisibility(View.VISIBLE);
                ivMute.setSelected(data.isMute);
            }
            if (data.account.equals(ProfileManager.getInstance().getUserModel().imAccid) && data.videoLive){
                //自己
                NERTCVideoCall.sharedInstance().setupLocalView(surfaceView);
            }else {
                NERTCVideoCall.sharedInstance().setupRemoteView(surfaceView,data.account);
            }
        } else if (data.state == TeamG2Item.STATE.STATE_END || data.state == TeamG2Item.STATE.STATE_HANGUP) {
            // 未接听/挂断
            loadingImage.setVisibility(GONE);
            surfaceView.setVisibility(GONE);
            stateText.setVisibility(View.VISIBLE);
            stateText.setText(data.state ==
                    TeamG2Item.STATE.STATE_HANGUP ? R.string.avchat_has_hangup : R.string.avchat_no_pick_up);
            ivMute.setVisibility(GONE);
        } else if(data.state == TeamG2Item.STATE.STATE_REJECTED){
            //已拒绝
            loadingImage.setVisibility(GONE);
            surfaceView.setVisibility(GONE);
            stateText.setVisibility(View.VISIBLE);
            stateText.setText(R.string.avchat_has_reject);
            ivMute.setVisibility(GONE);
        }
        updateVolume(data.volume);
    }

    private void loadAvatar(TeamG2Item data) {
        final UserInfo userInfo = NIMClient.getService(UserService.class).getUserInfo(data.account);
        final int defaultResId = R.drawable.t_avchat_avatar_default;
        changeUrlBeforeLoad(userInfo != null ? userInfo.getAvatar() : null, defaultResId, DEFAULT_AVATAR_THUMB_SIZE);
    }

    /**
     * 如果图片是上传到云信服务器，并且用户开启了文件安全功能，那么这里可能是短链，需要先换成源链才能下载。
     * 如果没有使用云信存储或没开启文件安全，那么不用这样做
     */
    private void changeUrlBeforeLoad(final String url, final int defaultResId, final int thumbSize) {
        if (TextUtils.isEmpty(url)) {
            // avoid useless call
            onLoad(url, defaultResId, thumbSize);
        } else {
            /*
             * 若使用网易云信云存储，这里可以设置下载图片的压缩尺寸，生成下载URL
             * 如果图片来源是非网易云信云存储，请不要使用NosThumbImageUtil
             */
            NIMClient.getService(NosService.class).getOriginUrlFromShortUrl(url).setCallback(
                    new RequestCallbackWrapper<String>() {

                        @Override
                        public void onResult(int code, String result, Throwable exception) {
                            if (TextUtils.isEmpty(result)) {
                                result = url;
                            }
                            onLoad(result, defaultResId, thumbSize);
                        }
                    });
        }
    }

    private void onLoad(String url, int defaultResId, int thumbSize) {
        final String thumbUrl = makeAvatarThumbNosUrl(url, DEFAULT_AVATAR_THUMB_SIZE);
        Glide.with(NERTCVideoCall.sharedInstance().getContext()).asBitmap().load(thumbUrl).apply(
                new RequestOptions().centerCrop().placeholder(defaultResId).error(defaultResId)
                                    .override(thumbSize, thumbSize)).into(avatarImage);
    }

    /**
     * 生成头像缩略图NOS URL地址（用作ImageLoader缓存的key）
     */
    private static String makeAvatarThumbNosUrl(final String url, final int thumbSize) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        return thumbSize > 0 ? NosThumbImageUtil.makeImageThumbUrl(url, NosThumbParam.ThumbType.Crop, thumbSize,
                                                                   thumbSize) : url;
    }

    public NERtcVideoView getSurfaceView() {
        return surfaceView;
    }

    public void updateVolume(int volume) {
        volumeBar.setProgress(volume);
    }
}
