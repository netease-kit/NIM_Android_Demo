package com.netease.nim.demo.session.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.nim.demo.R;
import com.netease.nim.demo.session.extension.SnapChatAttachment;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.imageview.BaseZoomableImageView;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;


/**
 * 查看阅后即焚消息原图
 */
public class WatchSnapChatPictureFragment extends TFragment {
    public static final String INTENT_EXTRA_IMAGE = "INTENT_EXTRA_IMAGE";

    private Handler handler;
    private IMMessage message;

    private View loadingLayout;
    private BaseZoomableImageView image;
    protected CustomAlertDialog alertDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        onParseIntent(getArguments());
    }

    private void onParseIntent(Bundle bundle) {
        this.message = (IMMessage) bundle.getSerializable(INTENT_EXTRA_IMAGE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nim_watch_snapchat_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews();
        registerObservers(true);
        requestOriImage();
    }

    private void findViews() {
        alertDialog = new CustomAlertDialog(getContext());
        loadingLayout = getView().findViewById(R.id.loading_layout);
        image = (BaseZoomableImageView) getView().findViewById(R.id.watch_image_view);

        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        registerObservers(false);
    }

    /**
     * ********************************* 下载 ****************************************
     */

    private void registerObservers(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeMsgStatus(statusObserver, register);
    }

    private Observer<IMMessage> statusObserver = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage msg) {
            if (!msg.isTheSame(message)) {
                return;
            }
            if (msg.getAttachStatus() == AttachStatusEnum.transferred && isOriginImageHasDownloaded(msg)) {
                onDownloadSuccess(msg);
            } else if (msg.getAttachStatus() == AttachStatusEnum.fail) {
                onDownloadFailed();
            }
        }
    };

    private void requestOriImage() {
        if (isOriginImageHasDownloaded(message)) {
            onDownloadSuccess(message);
            return;
        }

        // async download original image
        onDownloadStart(message);
        NIMClient.getService(MsgService.class).downloadAttachment(message, false);
    }

    private boolean isOriginImageHasDownloaded(final IMMessage message) {
        if (message.getAttachStatus() == AttachStatusEnum.transferred &&
                !TextUtils.isEmpty(((SnapChatAttachment) message.getAttachment()).getPath())) {
            return true;
        }

        return false;
    }

    /**
     * ******************************** 设置图片 *********************************
     */

    private void setThumbnail() {
        String path = ((SnapChatAttachment) message.getAttachment()).getThumbPath();
        if (!TextUtils.isEmpty(path)) {
            Bitmap bitmap = BitmapDecoder.decodeSampledForDisplay(path);
            bitmap = ImageUtil.rotateBitmapInNeeded(path, bitmap);
            if (bitmap != null) {
                image.setImageBitmap(bitmap);
                return;
            }
        }

        image.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnLoading()));
    }

    private void setImageView(final IMMessage msg) {
        String path = ((SnapChatAttachment) msg.getAttachment()).getPath();
        if (TextUtils.isEmpty(path)) {
            image.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnLoading()));
            return;
        }

        Bitmap bitmap = BitmapDecoder.decodeSampledForDisplay(path, false);
        bitmap = ImageUtil.rotateBitmapInNeeded(path, bitmap);
        if (bitmap == null) {
            ToastHelper.showToast(getContext(), R.string.picker_image_error);
            image.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnFailed()));
        } else {
            image.setImageBitmap(bitmap);
        }
    }

    private int getImageResOnLoading() {
        return R.drawable.nim_image_default;
    }

    private int getImageResOnFailed() {
        return R.drawable.nim_image_download_failed;
    }



    private void onDownloadStart(final IMMessage msg) {
        setThumbnail();
        if (TextUtils.isEmpty(((SnapChatAttachment) msg.getAttachment()).getPath())) {
            loadingLayout.setVisibility(View.VISIBLE);
        } else {
            loadingLayout.setVisibility(View.GONE);
        }

    }

    private void onDownloadSuccess(final IMMessage msg) {
        loadingLayout.setVisibility(View.GONE);
        handler.post(new Runnable() {

            @Override
            public void run() {
                setImageView(msg);
            }
        });
    }

    private void onDownloadFailed() {
        loadingLayout.setVisibility(View.GONE);
        image.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnFailed()));
        ToastHelper.showToast(getContext(), R.string.download_picture_fail);
    }
}
