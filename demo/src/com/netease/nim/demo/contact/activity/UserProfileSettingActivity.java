package com.netease.nim.demo.contact.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nim.demo.R;
import com.netease.nim.demo.contact.constant.UserConstant;
import com.netease.nim.demo.contact.helper.UserUpdateHelper;
import com.netease.nim.demo.main.model.Extras;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.business.session.actions.PickImageAction;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.media.imagepicker.Constants;
import com.netease.nim.uikit.common.media.imagepicker.ImagePickerLauncher;
import com.netease.nim.uikit.common.media.model.GLImage;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by hzxuwen on 2015/9/14.
 */
public class UserProfileSettingActivity extends UI implements View.OnClickListener {
    private final String TAG = UserProfileSettingActivity.class.getSimpleName();

    // constant
    private static final int PICK_AVATAR_REQUEST = 0x0E;
    private static final int AVATAR_TIME_OUT = 30000;

    private String account;

    // view
    private HeadImageView userHead;
    private RelativeLayout nickLayout;
    private RelativeLayout genderLayout;
    private RelativeLayout birthLayout;
    private RelativeLayout phoneLayout;
    private RelativeLayout emailLayout;
    private RelativeLayout signatureLayout;

    private TextView nickText;
    private TextView genderText;
    private TextView birthText;
    private TextView phoneText;
    private TextView emailText;
    private TextView signatureText;

    // data
    AbortableFuture<String> uploadAvatarFuture;
    private NimUserInfo userInfo;

    public static void start(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, UserProfileSettingActivity.class);
        intent.putExtra(Extras.EXTRA_ACCOUNT, account);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_set_activity);

        ToolBarOptions options = new NimToolBarOptions();
        options.titleId = R.string.user_information;
        setToolBar(R.id.toolbar, options);

        account = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
        findViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserInfo();
    }

    private void findViews() {
        userHead = findView(R.id.user_head);
        nickLayout = findView(R.id.nick_layout);
        genderLayout = findView(R.id.gender_layout);
        birthLayout = findView(R.id.birth_layout);
        phoneLayout = findView(R.id.phone_layout);
        emailLayout = findView(R.id.email_layout);
        signatureLayout = findView(R.id.signature_layout);

        ((TextView) nickLayout.findViewById(R.id.attribute)).setText(R.string.nickname);
        ((TextView) genderLayout.findViewById(R.id.attribute)).setText(R.string.gender);
        ((TextView) birthLayout.findViewById(R.id.attribute)).setText(R.string.birthday);
        ((TextView) phoneLayout.findViewById(R.id.attribute)).setText(R.string.phone);
        ((TextView) emailLayout.findViewById(R.id.attribute)).setText(R.string.email);
        ((TextView) signatureLayout.findViewById(R.id.attribute)).setText(R.string.signature);

        nickText = (TextView) nickLayout.findViewById(R.id.value);
        genderText = (TextView) genderLayout.findViewById(R.id.value);
        birthText = (TextView) birthLayout.findViewById(R.id.value);
        phoneText = (TextView) phoneLayout.findViewById(R.id.value);
        emailText = (TextView) emailLayout.findViewById(R.id.value);
        signatureText = (TextView) signatureLayout.findViewById(R.id.value);

        findViewById(R.id.head_layout).setOnClickListener(this);
        nickLayout.setOnClickListener(this);
        genderLayout.setOnClickListener(this);
        birthLayout.setOnClickListener(this);
        phoneLayout.setOnClickListener(this);
        emailLayout.setOnClickListener(this);
        signatureLayout.setOnClickListener(this);
    }

    private void getUserInfo() {
        userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(account);
        if (userInfo == null) {
            NimUIKit.getUserInfoProvider().getUserInfoAsync(account, new SimpleCallback<NimUserInfo>() {

                @Override
                public void onResult(boolean success, NimUserInfo result, int code) {
                    if (success) {
                        userInfo = result;
                        updateUI();
                    } else {
                        ToastHelper.showToast(UserProfileSettingActivity.this, "getUserInfoFromRemote failed:" + code);
                    }
                }
            });
        } else {
            updateUI();
        }
    }

    private void updateUI() {
        userHead.loadBuddyAvatar(account);
        nickText.setText(userInfo.getName());
        if (userInfo.getGenderEnum() != null) {
            if (userInfo.getGenderEnum() == GenderEnum.MALE) {
                genderText.setText("男");
            } else if (userInfo.getGenderEnum() == GenderEnum.FEMALE) {
                genderText.setText("女");
            } else {
                genderText.setText("其他");
            }
        }
        if (userInfo.getBirthday() != null) {
            birthText.setText(userInfo.getBirthday());
        }
        if (userInfo.getMobile() != null) {
            phoneText.setText(userInfo.getMobile());
        }
        if (userInfo.getEmail() != null) {
            emailText.setText(userInfo.getEmail());
        }
        if (userInfo.getSignature() != null) {
            signatureText.setText(userInfo.getSignature());
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_layout:
                ImagePickerLauncher.pickImage(UserProfileSettingActivity.this, PICK_AVATAR_REQUEST, R.string.set_head_image);
                break;
            case R.id.nick_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_NICKNAME,
                        nickText.getText().toString());
                break;
            case R.id.gender_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_GENDER,
                        String.valueOf(userInfo.getGenderEnum().getValue()));
                break;
            case R.id.birth_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_BIRTH,
                        birthText.getText().toString());
                break;
            case R.id.phone_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_PHONE,
                        phoneText.getText().toString());
                break;
            case R.id.email_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_EMAIL,
                        emailText.getText().toString());
                break;
            case R.id.signature_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_SIGNATURE,
                        signatureText.getText().toString());
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_AVATAR_REQUEST) {
            onPicked(data);
        }
    }

    private void onPicked(Intent data) {
        if (data == null) {
            return;
        }
        ArrayList<GLImage> images = (ArrayList<GLImage>) data.getSerializableExtra(Constants.EXTRA_RESULT_ITEMS);
        if (images == null || images.isEmpty()) {
            return;
        }
        GLImage image = images.get(0);
        updateAvatar(image.getPath());
    }

    /**
     * 更新头像
     */
    private void updateAvatar(final String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        File file = new File(path);
        if (file == null) {
            return;
        }

        DialogMaker.showProgressDialog(this, null, null, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelUpload(R.string.user_info_update_cancel);
            }
        }).setCanceledOnTouchOutside(true);

        LogUtil.i(TAG, "start upload avatar, local file path=" + file.getAbsolutePath());
        new Handler().postDelayed(outimeTask, AVATAR_TIME_OUT);
        uploadAvatarFuture = NIMClient.getService(NosService.class).upload(file, PickImageAction.MIME_JPEG);
        uploadAvatarFuture.setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int code, String url, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS && !TextUtils.isEmpty(url)) {
                    LogUtil.i(TAG, "upload avatar success, url =" + url);

                    UserUpdateHelper.update(UserInfoFieldEnum.AVATAR, url, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void result, Throwable exception) {
                            if (code == ResponseCode.RES_SUCCESS) {
                                ToastHelper.showToast(UserProfileSettingActivity.this, R.string.head_update_success);
                                onUpdateDone();
                            } else {
                                ToastHelper.showToast(UserProfileSettingActivity.this, R.string.head_update_failed);
                            }
                        }
                    }); // 更新资料
                } else {
                    ToastHelper.showToast(UserProfileSettingActivity.this, R.string.user_info_update_failed);
                    onUpdateDone();
                }
            }
        });
    }

    private void cancelUpload(int resId) {
        if (uploadAvatarFuture != null) {
            uploadAvatarFuture.abort();
            ToastHelper.showToast(UserProfileSettingActivity.this, resId);
            onUpdateDone();
        }
    }

    private Runnable outimeTask = new Runnable() {
        @Override
        public void run() {
            cancelUpload(R.string.user_info_update_failed);
        }
    };

    private void onUpdateDone() {
        uploadAvatarFuture = null;
        DialogMaker.dismissProgressDialog();
        getUserInfo();
    }
}
