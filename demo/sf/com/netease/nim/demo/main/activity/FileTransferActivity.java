package com.netease.nim.demo.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.netease.nim.demo.R;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.nos.NosServiceObserve;
import com.netease.nimlib.sdk.nos.model.NosThumbParam;
import com.netease.nimlib.sdk.nos.model.NosTransferInfo;
import com.netease.nimlib.sdk.nos.model.NosTransferProgress;

import java.io.File;
import java.util.UUID;

/**
 * 文件传输示例
 * Created by huangjun on 2015/4/14.
 */
public class FileTransferActivity extends UI {

    private static final String TAG = "FileTransferActivity";

    private Button uploadBtn;
    private Button downloadBtn;
    private TextView uploadStatusText;
    private TextView downloadStatusText;

    private String uploadLocalPath;
    private String downloadUrl;

    public static void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, FileTransferActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.file_transfer_layout);

        findViews();

        uploadLocalPath = "/storage/sdcard0/com.netease.nim.demo/nim/image/ffbf871cd3c6e6c98049b856579856fa.JPG";
        downloadUrl = "http://nimtest.nos.netease.com/02754725-5a6a-47a1-ba72-ed5c0dca2f2f";

        registerObserver(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        registerObserver(false);
    }

    private void findViews() {
        uploadBtn = (Button) findViewById(R.id.upload_btn);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadBtn.setEnabled(false);
                uploadFile(new File(uploadLocalPath), null);
            }
        });
        downloadBtn = (Button) findViewById(R.id.download_btn);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile(downloadUrl);
            }
        });
        uploadStatusText = (TextView) findViewById(R.id.upload_status);
        downloadStatusText = (TextView) findViewById(R.id.download_status);
    }

    private void registerObserver(boolean register) {
        NIMClient.getService(NosServiceObserve.class).observeNosTransferProgress(fileTransProgressObserver,
                register);
        NIMClient.getService(NosServiceObserve.class).observeNosTransferStatus(fileTransStatusObserver, register);
    }


    // 上传文件到云
    private void uploadFile(File file, String mimeType) {
        NIMClient.getService(NosService.class).upload(file, mimeType).setCallback(new RequestCallback<String>() {
            @Override
            public void onSuccess(String url) {
                Log.i(TAG, "upload file success, url = " + url);
                uploadBtn.setEnabled(true);
            }

            @Override
            public void onFailed(int code) {
                Log.i(TAG, "upload file failed, code = " + code);
                uploadBtn.setEnabled(true);
            }

            @Override
            public void onException(Throwable exception) {
                uploadBtn.setEnabled(true);
            }
        });
    }

    // 从云上下载文件
    private void downloadFile(final String url) {
        downloadBtn.setEnabled(false);
        final String savePath = StorageUtil.getWritePath(UUID.randomUUID().toString(), StorageType.TYPE_FILE);
        NosThumbParam param = new NosThumbParam();
        param.width = 400;
        param.height = 300;
        NIMClient.getService(NosService.class).download(url, param, savePath).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                Log.i(TAG, "download file success, url = " + url + ", savePath = " + savePath);
                downloadBtn.setEnabled(true);
            }

            @Override
            public void onFailed(int code) {
                Log.i(TAG, "download file failed, code = " + code);
                downloadBtn.setEnabled(false);
            }

            @Override
            public void onException(Throwable exception) {
                downloadBtn.setEnabled(false);
            }
        });
    }

    Observer<NosTransferProgress> fileTransProgressObserver = new Observer<NosTransferProgress>() {
        @Override
        public void onEvent(NosTransferProgress progress) {
            int percent = (int) ((float) progress.getTransferred() / (float) progress.getTotal());
            String tip = String.format("%d%%", percent);
            if (progress.getKey().equals(uploadLocalPath)) {
                uploadStatusText.setText(tip);
            } else {
                downloadStatusText.setText(tip);
            }

            Log.i(TAG, "key: " + progress.getKey() + " progress: " + progress.getTransferred() + "/" + progress
                    .getTotal());
        }
    };

    Observer<NosTransferInfo> fileTransStatusObserver = new Observer<NosTransferInfo>() {
        @Override
        public void onEvent(NosTransferInfo fileTransferInfo) {
            if (fileTransferInfo.getTransferType() == NosTransferInfo.TransferType.UPLOAD) {
                uploadStatusText.setText(fileTransferInfo.getStatus().toString());
            } else {
                downloadStatusText.setText(fileTransferInfo.getStatus().toString());
            }

            Log.i(TAG, "key: " + fileTransferInfo.getKey() + ", status = " + fileTransferInfo.getStatus());
        }
    };
}
