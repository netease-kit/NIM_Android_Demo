package com.netease.nim.avchatkit.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.avchatkit.AVChatKit;
import com.netease.nim.avchatkit.R;
import com.netease.nim.avchatkit.common.imageview.HeadImageView;
import com.netease.nim.avchatkit.common.permission.BaseMPermission;
import com.netease.nim.avchatkit.common.util.ScreenUtil;
import com.netease.nim.avchatkit.common.widgets.ToggleListener;
import com.netease.nim.avchatkit.common.widgets.ToggleState;
import com.netease.nim.avchatkit.common.widgets.ToggleView;
import com.netease.nim.avchatkit.constant.AVChatExitCode;
import com.netease.nim.avchatkit.controll.AVChatController;
import com.netease.nim.avchatkit.module.AVChatControllerCallback;
import com.netease.nim.avchatkit.module.AVSwitchListener;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.video.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.video.AVChatSurfaceViewRenderer;
import com.netease.nrtc.video.render.IVideoRender;

import java.util.List;

/**
 * 视频界面变化及点击事件
 * Created by winnie on 2017/12/11.
 */

public class AVChatVideoUI implements View.OnClickListener, ToggleListener {

    // constant
    private static final int PEER_CLOSE_CAMERA = 0;
    private static final int LOCAL_CLOSE_CAMERA = 1;
    private static final int AUDIO_TO_VIDEO_WAIT = 2;
    private static final int TOUCH_SLOP = 10;
    private static final String TAG = AVChatVideoUI.class.getSimpleName();

    private final String[] BASIC_PERMISSIONS = new String[]{Manifest.permission.CAMERA,};

    /**
     * surface view
     */
    private LinearLayout largeSizePreviewLayout;
    private FrameLayout smallSizePreviewFrameLayout;
    private LinearLayout smallSizePreviewLayout;
    private ImageView smallSizePreviewCoverImg;//stands for peer or local close camera
    private TextView largeSizePreviewCoverLayout;//stands for peer or local close camera
    private View touchLayout;

    /**
     * video view
     */
    //顶部控制按钮
    private View topRoot;
    private View switchAudio;
    private Chronometer time;
    //中间控制按钮
    private View middleRoot;
    private HeadImageView headImg;
    private TextView nickNameTV;
    private TextView notifyTV;
    private View refuse_receive;
    private TextView refuseTV;
    private TextView receiveTV;
    //底部控制按钮
    private View bottomRoot;
    ToggleView switchCameraToggle;
    ToggleView closeCameraToggle;
    ToggleView muteToggle;
    ImageView recordToggle;
    ImageView hangUpImg;
    //face unity
    private View faceUnityRoot;
    //摄像头权限提示显示
    private View permissionRoot;
    //record
    private View recordView;
    private View recordTip;
    private View recordWarning;

    //render
    private AVChatSurfaceViewRenderer smallRender;
    private AVChatSurfaceViewRenderer largeRender;

    // state
    private boolean surfaceInit = false;
    private boolean videoInit = false;
    private boolean shouldEnableToggle = false;
    public boolean canSwitchCamera = false;
    private boolean isInSwitch = false;
    private boolean isPeerVideoOff = false;
    private boolean isLocalVideoOff = false;
    private boolean localPreviewInSmallSize = true;
    private boolean isRecordWarning = false;
    private boolean isInReceiveing = false;

    // data
    private TouchZoneCallback touchZoneCallback;
    private AVChatData avChatData;
    private String account;
    private String displayName;

    private int topRootHeight = 0;
    private int bottomRootHeight = 0;

    private String largeAccount; // 显示在大图像的用户id
    private String smallAccount; // 显示在小图像的用户id

    // move
    private int lastX, lastY;
    private int inX, inY;
    private Rect paddingRect;

    private Context context;
    private View root;
    private AVChatController avChatController;
    private AVSwitchListener avSwitchListener;
    private boolean isReleasedVideo = false;

    // touch zone
    public interface TouchZoneCallback {
        void onTouch();
    }

    public AVChatVideoUI(Context context, View root, AVChatData avChatData, String displayName,
                         AVChatController avChatController, TouchZoneCallback touchZoneCallback,
                         AVSwitchListener avSwitchListener) {
        this.context = context;
        this.root = root;
        this.avChatData = avChatData;
        this.displayName = displayName;
        this.avChatController = avChatController;
        this.touchZoneCallback = touchZoneCallback;
        this.avSwitchListener = avSwitchListener;
        this.smallRender = new AVChatSurfaceViewRenderer(context);
        this.largeRender = new AVChatSurfaceViewRenderer(context);
    }

    /**
     * ********************** surface 初始化 **********************
     */

    private void findSurfaceView() {
        if (surfaceInit) {
            return;
        }
        View surfaceView = root.findViewById(R.id.avchat_surface_layout);
        if (surfaceView != null) {
            touchLayout = surfaceView.findViewById(R.id.touch_zone);
            touchLayout.setOnTouchListener(touchListener);

            smallSizePreviewFrameLayout = surfaceView.findViewById(R.id.small_size_preview_layout);
            smallSizePreviewLayout = surfaceView.findViewById(R.id.small_size_preview);
            smallSizePreviewCoverImg = surfaceView.findViewById(R.id.smallSizePreviewCoverImg);
            smallSizePreviewFrameLayout.setOnTouchListener(smallPreviewTouchListener);

            largeSizePreviewLayout = surfaceView.findViewById(R.id.large_size_preview);
            largeSizePreviewCoverLayout = surfaceView.findViewById(R.id.notificationLayout);

            surfaceInit = true;
        }
    }


    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP && touchZoneCallback != null) {
                touchZoneCallback.onTouch();
            }

            return true;
        }
    };

    private View.OnTouchListener smallPreviewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = x;
                    lastY = y;
                    int[] p = new int[2];
                    smallSizePreviewFrameLayout.getLocationOnScreen(p);
                    inX = x - p[0];
                    inY = y - p[1];

                    break;
                case MotionEvent.ACTION_MOVE:
                    final int diff = Math.max(Math.abs(lastX - x), Math.abs(lastY - y));
                    if (diff < TOUCH_SLOP)
                        break;

                    if (paddingRect == null) {
                        paddingRect = new Rect(ScreenUtil.dip2px(10), ScreenUtil.dip2px(20), ScreenUtil.dip2px(10),
                                ScreenUtil.dip2px(70));
                    }

                    int destX, destY;
                    if (x - inX <= paddingRect.left) {
                        destX = paddingRect.left;
                    } else if (x - inX + v.getWidth() >= ScreenUtil.screenWidth - paddingRect.right) {
                        destX = ScreenUtil.screenWidth - v.getWidth() - paddingRect.right;
                    } else {
                        destX = x - inX;
                    }

                    if (y - inY <= paddingRect.top) {
                        destY = paddingRect.top;
                    } else if (y - inY + v.getHeight() >= ScreenUtil.screenHeight - paddingRect.bottom) {
                        destY = ScreenUtil.screenHeight - v.getHeight() - paddingRect.bottom;
                    } else {
                        destY = y - inY;
                    }

                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
                    params.gravity = Gravity.NO_GRAVITY;
                    params.leftMargin = destX;
                    params.topMargin = destY;
                    v.setLayoutParams(params);

                    break;
                case MotionEvent.ACTION_UP:
                    if (Math.max(Math.abs(lastX - x), Math.abs(lastY - y)) <= 5) {
                        if (largeAccount == null || smallAccount == null) {
                            return true;
                        }
                        String temp;
                        switchRender(smallAccount, largeAccount);
                        temp = largeAccount;
                        largeAccount = smallAccount;
                        smallAccount = temp;
                        switchAndSetLayout();
                    }

                    break;
            }

            return true;
        }
    };

    private IVideoRender remoteRender;
    private IVideoRender localRender;

    // 大小图像显示切换
    private void switchRender(String user1, String user2) {
        String remoteId = TextUtils.equals(user1, AVChatKit.getAccount()) ? user2 : user1;

        if (remoteRender == null && localRender == null) {
            localRender = smallRender;
            remoteRender = largeRender;
        }

        //交换
        IVideoRender render = localRender;
        localRender = remoteRender;
        remoteRender = render;


        //断开SDK视频绘制画布
        AVChatManager.getInstance().setupLocalVideoRender(null, false, 0);
        AVChatManager.getInstance().setupRemoteVideoRender(remoteId, null, false, 0);

        //重新关联上画布
        AVChatManager.getInstance().setupLocalVideoRender(localRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        AVChatManager.getInstance().setupRemoteVideoRender(remoteId, remoteRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);

    }

    /**
     * ************************** video 初始化 ***********************
     */
    private void findVideoViews() {
        if (videoInit)
            return;
        View videoRoot = root.findViewById(R.id.avchat_video_layout);
        topRoot = videoRoot.findViewById(R.id.avchat_video_top_control);
        switchAudio = topRoot.findViewById(R.id.avchat_video_switch_audio);
        switchAudio.setOnClickListener(this);
        time = topRoot.findViewById(R.id.avchat_video_time);

        middleRoot = videoRoot.findViewById(R.id.avchat_video_middle_control);
        headImg = middleRoot.findViewById(R.id.avchat_video_head);
        nickNameTV = middleRoot.findViewById(R.id.avchat_video_nickname);
        notifyTV = middleRoot.findViewById(R.id.avchat_video_notify);

        refuse_receive = middleRoot.findViewById(R.id.avchat_video_refuse_receive);
        refuseTV = refuse_receive.findViewById(R.id.refuse);
        receiveTV = refuse_receive.findViewById(R.id.receive);
        refuseTV.setOnClickListener(this);
        receiveTV.setOnClickListener(this);

        recordView = videoRoot.findViewById(R.id.avchat_record_layout);
        recordTip = recordView.findViewById(R.id.avchat_record_tip);
        recordWarning = recordView.findViewById(R.id.avchat_record_warning);

        bottomRoot = videoRoot.findViewById(R.id.avchat_video_bottom_control);
        faceUnityRoot = videoRoot.findViewById(R.id.avchat_video_face_unity);

        switchCameraToggle = new ToggleView(bottomRoot.findViewById(R.id.avchat_switch_camera), ToggleState.DISABLE, this);
        closeCameraToggle = new ToggleView(bottomRoot.findViewById(R.id.avchat_close_camera), ToggleState.DISABLE, this);
        muteToggle = new ToggleView(bottomRoot.findViewById(R.id.avchat_video_mute), ToggleState.DISABLE, this);
        recordToggle = bottomRoot.findViewById(R.id.avchat_video_record);
        recordToggle.setEnabled(false);
        recordToggle.setOnClickListener(this);
        hangUpImg = bottomRoot.findViewById(R.id.avchat_video_logout);
        hangUpImg.setOnClickListener(this);

        permissionRoot = videoRoot.findViewById(R.id.avchat_video_permission_control);
        videoInit = true;
    }

    public void onResume() {
        surfaceViewFixBefore43(smallSizePreviewLayout, largeSizePreviewLayout);
    }

    public void onDestroy() {
        if (time != null) {
            time.stop();
        }
    }

    /**
     * ********************** 视频流程 **********************
     */

    public void showIncomingCall(AVChatData avChatData) {
        this.avChatData = avChatData;
        this.account = avChatData.getAccount();

        findSurfaceView();
        findVideoViews();

        showProfile();//对方的详细信息
        showNotify(R.string.avchat_video_call_request);
        setRefuseReceive(true);
        receiveTV.setText(R.string.avchat_pickup);
        setTopRoot(false);
        setMiddleRoot(true);
        setBottomRoot(false);
        setFaceUnityRoot(false);
    }

    public void doOutgoingCall(String account) {
        this.account = account;

        findSurfaceView();
        findVideoViews();

        showProfile();//对方的详细信息
        showNotify(R.string.avchat_wait_recieve);
        setRefuseReceive(false);
        shouldEnableToggle = true;
        enableCameraToggle();   //使用音视频预览时这里可以开启切换摄像头按钮
        setTopRoot(false);
        setMiddleRoot(true);
        setBottomRoot(true);
        setFaceUnityRoot(true);

        avChatController.doCalling(account, AVChatType.VIDEO, new AVChatControllerCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData data) {
                avChatData = data;
                avChatController.setAvChatData(data);
                List<String> deniedPermissions = BaseMPermission.getDeniedPermissions((Activity) context, BASIC_PERMISSIONS);
                if (deniedPermissions != null && !deniedPermissions.isEmpty()) {
                    showNoneCameraPermissionView(true);
                    return;
                }
                canSwitchCamera = true;
                initLargeSurfaceView(AVChatKit.getAccount());
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                closeSession();
            }
        });
    }

    public void showVideoInitLayout() {
        findSurfaceView();
        findVideoViews();

        isInSwitch = false;
        enableToggle();
        setTime(true);
        setTopRoot(true);
        setMiddleRoot(false);
        setBottomRoot(true);
        setFaceUnityRoot(true);
        showNoneCameraPermissionView(false);
    }

    // 小图像surface view 初始化
    public void initSmallSurfaceView(String account) {
        smallAccount = account;
        smallSizePreviewFrameLayout.setVisibility(View.VISIBLE);

        // 设置画布，加入到自己的布局中，用于呈现视频图像
        AVChatManager.getInstance().setupLocalVideoRender(null, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        AVChatManager.getInstance().setupLocalVideoRender(smallRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        addIntoSmallSizePreviewLayout(smallRender);

        smallSizePreviewFrameLayout.bringToFront();
        localRender = smallRender;
        localPreviewInSmallSize = true;
    }

    private void addIntoSmallSizePreviewLayout(SurfaceView surfaceView) {
        smallSizePreviewCoverImg.setVisibility(View.GONE);
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }
        smallSizePreviewLayout.removeAllViews();
        smallSizePreviewLayout.addView(surfaceView);
        surfaceView.setZOrderMediaOverlay(true);
        smallSizePreviewLayout.setVisibility(View.VISIBLE);
    }


    // 大图像surface view 初始化
    public void initLargeSurfaceView(String account) {
        // 设置画布，加入到自己的布局中，用于呈现视频图像
        // account 要显示视频的用户帐号
        largeAccount = account;
        if (AVChatKit.getAccount().equals(account)) {
            AVChatManager.getInstance().setupLocalVideoRender(largeRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        } else {
            AVChatManager.getInstance().setupRemoteVideoRender(account, largeRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        }
        addIntoLargeSizePreviewLayout(largeRender);
        remoteRender = largeRender;
    }

    private void addIntoLargeSizePreviewLayout(SurfaceView surfaceView) {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }
        largeSizePreviewLayout.removeAllViews();
        largeSizePreviewLayout.addView(surfaceView);
        surfaceView.setZOrderMediaOverlay(false);
        largeSizePreviewCoverLayout.setVisibility(View.GONE);
    }

    /**
     * ******************* 音视频切换 *******************
     */

    public void onVideoToAudio() {
        isReleasedVideo = true;
        smallSizePreviewFrameLayout.setVisibility(View.INVISIBLE);
    }

    public void onAudioToVideo() {
        findVideoViews();
        findSurfaceView();

        showNotificationLayout(AUDIO_TO_VIDEO_WAIT);

        isInSwitch = true;
        setTime(true);
        setTopRoot(true);
        setMiddleRoot(false);
        setBottomRoot(true);
        setFaceUnityRoot(true);

        showRecordView(avChatController.isRecording(), isRecordWarning);
    }

    public void onAudioToVideoAgree(String largeAccount) {
        showVideoInitLayout();
        account = largeAccount;

        muteToggle.toggle(AVChatManager.getInstance().isLocalAudioMuted() ? ToggleState.ON : ToggleState.OFF);
        closeCameraToggle.toggle(ToggleState.OFF);
        switchCameraToggle.off(false);
        recordToggle.setEnabled(true);
        recordToggle.setSelected(avChatController.isRecording());

        //打开视频
        isReleasedVideo = false;
        smallRender = new AVChatSurfaceViewRenderer(context);
        largeRender = new AVChatSurfaceViewRenderer(context);

        //打开视频
        AVChatManager.getInstance().enableVideo();
        AVChatManager.getInstance().startVideoPreview();

        initSmallSurfaceView(AVChatKit.getAccount());
        // 是否在发送视频 即摄像头是否开启
        if (AVChatManager.getInstance().isLocalVideoMuted()) {
            AVChatManager.getInstance().muteLocalVideo(false);
            localVideoOn();
        }

        initLargeSurfaceView(largeAccount);
        showRecordView(avChatController.isRecording(), isRecordWarning);
    }

    /********************** 界面显示 **********************************/

    // 显示个人信息
    private void showProfile() {
        headImg.loadBuddyAvatar(account);
        nickNameTV.setText(displayName);
    }

    // 显示通知
    private void showNotify(int resId) {
        notifyTV.setText(resId);
        notifyTV.setVisibility(View.VISIBLE);
    }

    private void setRefuseReceive(boolean visible) {
        refuse_receive.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setTopRoot(boolean visible) {
        topRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (topRootHeight == 0) {
            Rect rect = new Rect();
            topRoot.getGlobalVisibleRect(rect);
            topRootHeight = rect.bottom;
        }
    }

    private void setMiddleRoot(boolean visible) {
        middleRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setBottomRoot(boolean visible) {
        bottomRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (bottomRootHeight == 0) {
            bottomRootHeight = bottomRoot.getHeight();
        }
    }

    private void setFaceUnityRoot(boolean visible) {
        if (faceUnityRoot == null) {
            return;
        }

        faceUnityRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    // 底部控制开关可用
    private void enableToggle() {
        if (shouldEnableToggle) {
            if (canSwitchCamera && AVChatCameraCapturer.hasMultipleCameras()) {
                switchCameraToggle.enable();
            }
            closeCameraToggle.enable();
            muteToggle.enable();
            recordToggle.setEnabled(true);
            shouldEnableToggle = false;
        }
    }

    private void setTime(boolean visible) {
        time.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            time.setBase(avChatController.getTimeBase());
            time.start();
        }
    }

    public void showNoneCameraPermissionView(boolean show) {
        permissionRoot.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void enableCameraToggle() {
        if (shouldEnableToggle) {
            if (canSwitchCamera && AVChatCameraCapturer.hasMultipleCameras())
                switchCameraToggle.enable();
        }
    }

    // 摄像头切换时，布局显隐
    private void switchAndSetLayout() {
        localPreviewInSmallSize = !localPreviewInSmallSize;
        largeSizePreviewCoverLayout.setVisibility(View.GONE);
        smallSizePreviewCoverImg.setVisibility(View.GONE);
        if (isPeerVideoOff) {
            peerVideoOff();
        }
        if (isLocalVideoOff) {
            localVideoOff();
        }
    }

    /**
     * ******************** 点击事件 **********************
     */

    @Override
    public void toggleOn(View v) {
        onClick(v);
    }

    @Override
    public void toggleOff(View v) {
        onClick(v);
    }

    @Override
    public void toggleDisable(View v) {

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.refuse) {
            doRefuseCall();
        } else if (i == R.id.receive) {
            if (isInReceiveing || avChatController.isCallEstablish.get()) {
                Toast.makeText(context, R.string.avchat_in_switch, Toast.LENGTH_SHORT).show();
            } else {
                doReceiveCall();
            }
        } else if (i == R.id.avchat_video_logout) {
            doHangUp();
        } else if (i == R.id.avchat_video_mute) {
            avChatController.toggleMute();
        } else if (i == R.id.avchat_switch_camera) {
            avChatController.switchCamera();
        } else if (i == R.id.avchat_close_camera) {
            closeCamera();
        } else if (i == R.id.avchat_video_record) {
            doToggleRecord();
        } else if (i == R.id.avchat_video_switch_audio) {
            if (isInSwitch) {
                Toast.makeText(context, R.string.avchat_in_switch, Toast.LENGTH_SHORT).show();
            } else {
                avChatController.switchVideoToAudio(avSwitchListener);
            }

        }
    }

    // 拒绝来电
    private void doRefuseCall() {
        avChatController.hangUp(AVChatExitCode.HANGUP);
        closeSession();
    }

    private void doReceiveCall() {
        isInReceiveing = true;
        showNotify(R.string.avchat_connecting);
        shouldEnableToggle = true;
        avChatController.receive(AVChatType.VIDEO, new AVChatControllerCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                isInReceiveing = false;
                canSwitchCamera = true;
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                isInReceiveing = false;
                closeSession();
            }
        });
    }

    private void doHangUp() {
        releaseVideo();
        avChatController.hangUp(AVChatExitCode.HANGUP);
        closeSession();
    }


    public void releaseVideo() {
        if (isReleasedVideo) {
            return;
        }
        isReleasedVideo = true;
        AVChatManager.getInstance().stopVideoPreview();
        AVChatManager.getInstance().disableVideo();
    }

    /**
     * ********************** 开关摄像头 **********************
     */

    private void closeCamera() {
        if (!AVChatManager.getInstance().isLocalVideoMuted()) {
            // 关闭摄像头
            AVChatManager.getInstance().muteLocalVideo(true);
            localVideoOff();
        } else {
            // 打开摄像头
            AVChatManager.getInstance().muteLocalVideo(false);
            localVideoOn();
        }
    }


    // 对方打开了摄像头
    private void localVideoOn() {
        isLocalVideoOff = false;
        if (localPreviewInSmallSize) {
            smallSizePreviewCoverImg.setVisibility(View.GONE);
        } else {
            largeSizePreviewCoverLayout.setVisibility(View.GONE);
        }
    }

    // 本地关闭了摄像头
    private void localVideoOff() {
        isLocalVideoOff = true;
        if (localPreviewInSmallSize)
            closeSmallSizePreview();
        else
            showNotificationLayout(LOCAL_CLOSE_CAMERA);
    }

    // 对方关闭了摄像头
    public void peerVideoOff() {
        isPeerVideoOff = true;
        if (localPreviewInSmallSize) { //local preview in small size layout, then peer preview should in large size layout
            showNotificationLayout(PEER_CLOSE_CAMERA);
        } else {  // peer preview in small size layout
            closeSmallSizePreview();
        }
    }

    // 对方打开了摄像头
    public void peerVideoOn() {
        isPeerVideoOff = false;
        if (localPreviewInSmallSize) {
            largeSizePreviewCoverLayout.setVisibility(View.GONE);
        } else {
            smallSizePreviewCoverImg.setVisibility(View.GONE);
        }
    }

    // 关闭小窗口
    private void closeSmallSizePreview() {
        smallSizePreviewCoverImg.setVisibility(View.VISIBLE);
    }

    // 界面提示
    private void showNotificationLayout(int closeType) {
        if (largeSizePreviewCoverLayout == null) {
            return;
        }
        TextView textView = largeSizePreviewCoverLayout;
        switch (closeType) {
            case PEER_CLOSE_CAMERA:
                textView.setText(R.string.avchat_peer_close_camera);
                break;
            case LOCAL_CLOSE_CAMERA:
                textView.setText(R.string.avchat_local_close_camera);
                break;
            case AUDIO_TO_VIDEO_WAIT:
                textView.setText(R.string.avchat_audio_to_video_wait);
                break;
            default:
                return;
        }
        largeSizePreviewCoverLayout.setVisibility(View.VISIBLE);
    }


    /**
     * ******************** 录制 ***************************
     */

    private void doToggleRecord() {
        avChatController.toggleRecord(AVChatType.VIDEO.getValue(), account, new AVChatController.RecordCallback() {
            @Override
            public void onRecordUpdate(boolean isRecording) {
                showRecordView(isRecording, isRecordWarning);
            }
        });
    }

    public void showRecordView(boolean show, boolean warning) {
        if (show) {
            recordToggle.setEnabled(true);
            recordToggle.setSelected(true);
            recordView.setVisibility(View.VISIBLE);
            recordTip.setVisibility(View.VISIBLE);
            if (warning) {
                recordWarning.setVisibility(View.VISIBLE);
            } else {
                recordWarning.setVisibility(View.GONE);
            }
        } else {
            recordToggle.setSelected(false);
            recordView.setVisibility(View.INVISIBLE);
            recordTip.setVisibility(View.INVISIBLE);
            recordWarning.setVisibility(View.GONE);
        }
    }

    public void showRecordWarning() {
        isRecordWarning = true;
        showRecordView(avChatController.isRecording(), isRecordWarning);
    }

    public void resetRecordTip() {
        isRecordWarning = false;
        avChatController.setRecording(false);
        showRecordView(false, isRecordWarning);
    }

    private void closeSession() {
        ((Activity) context).finish();
    }

    public AVChatData getAvChatData() {
        return avChatData;
    }

    private void surfaceViewFixBefore43(ViewGroup front, ViewGroup back) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (back.getChildCount() > 0) {
                View child = back.getChildAt(0);
                back.removeView(child);
                back.addView(child);
            }

            if (front.getChildCount() > 0) {
                View child = front.getChildAt(0);
                front.removeView(child);
                front.addView(child);
            }
        }
    }

}
