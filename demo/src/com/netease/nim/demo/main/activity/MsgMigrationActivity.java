package com.netease.nim.demo.main.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.netease.nim.demo.R;
import com.netease.nim.demo.session.extension.RTSAttachment;
import com.netease.nim.demo.session.extension.RedPacketAttachment;
import com.netease.nim.demo.session.extension.RedPacketOpenedAttachment;
import com.netease.nim.demo.session.extension.SnapChatAttachment;
import com.netease.nim.demo.session.extension.StickerAttachment;
import com.netease.nim.rtskit.common.dialog.EasyAlertDialog;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.migration.model.MigrationConstant;
import com.netease.nimlib.sdk.migration.processor.IMsgExportProcessor;
import com.netease.nimlib.sdk.migration.processor.IMsgImportProcessor;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class MsgMigrationActivity extends UI implements View.OnClickListener {

    private final static int EXPORT_ACTION = 1;//导出
    private final static int IMPORT_ACTION = 2;//导入
    private final static String CHAR_SET = "UTF-8";
    private final static String ALGORITHM = "AES";
    private final static int BUFFER_SIZE = 512;
    private final static String VECTOR = "0123456789012345";
    private final static int PROGRESS_INTERVAL = 10;

    private static final String TAG = "MsgMigrationActivity";

    private EasyAlertDialog easyAlertDialog;

    private View actionContainer;
    private View processContainer;
    private TextView tvActingHint;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private View resultContainer;
    private TextView tvResultHint;

    private int actionType = 0;


    // 消息导出返回的Future , 在导出途中可用于取消
    private AbortableFuture<Void> msgExportFuture;
    // 消息导出自定义处理器
    private MsgExportProcessor msgExportProcessor = new MsgExportProcessor();


    // 消息导入返回的Future , 在导入途中可用于取消
    private AbortableFuture<Void> msgImportFuture;
    // 消息导入自定义处理器
    private MsgImportProcessor msgImportProcessor = new MsgImportProcessor();


    private DismissOnClickListener clickDismissListener = new DismissOnClickListener();


    private MsgService msgService;

    private boolean paused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_migration);
        ToolBarOptions options = new NimToolBarOptions();
        setToolBar(R.id.toolbar, options);

        msgService = NIMClient.getService(MsgService.class);
        setupView();
    }

    private void setupView() {
        actionContainer = findView(R.id.ll_action_container);
        processContainer = findView(R.id.rl_process_container);
        tvActingHint = findView(R.id.tv_action_hint);
        progressBar = findView(R.id.progress_bar);
        tvProgress = findView(R.id.tv_cancel_and_progress);

        resultContainer = findView(R.id.ll_result_hint_container);
        tvResultHint = findView(R.id.tv_result_hint);

        findView(R.id.tv_msg_export).setOnClickListener(this);
        findView(R.id.tv_msg_import).setOnClickListener(this);
        findView(R.id.btn_back_to_list).setOnClickListener(this);
        tvProgress.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.tv_msg_export) {
            showExportDialog();
        } else if (id == R.id.tv_msg_import) {
            showImportDialog();
        } else if (id == R.id.tv_cancel_and_progress) {
            showCancelDialog();
        } else if (id == R.id.btn_back_to_list) {
            MainActivity.start(this);
            finish();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
    }

    @Override
    protected void onPause() {
        paused = true;
        super.onPause();
    }

    private void showExportDialog() {

        View.OnClickListener positiveListener = new DismissOnClickListener() {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                exportMsg();
            }
        };

        showAlterDialog("确定导出本地消息？",
                "本地消息将存至云端，会耗费较长时间",
                "返回", clickDismissListener,
                "继续导出", positiveListener);

    }

    private void showImportDialog() {

        View.OnClickListener positiveListener = new DismissOnClickListener() {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                importMsg();
            }
        };

        showAlterDialog("确定导入本地消息？",
                "此过程需要较长时间",
                "返回", clickDismissListener,
                "继续导入", positiveListener);

    }

    private void exportMsg() {
        actionType = EXPORT_ACTION;
        updateProgress(0, true);
        progressBar.setProgress(0);
        processContainer.setVisibility(View.VISIBLE);
        tvActingHint.setText("导出本地消息需要较长时间，请耐心等待");

        msgExportFuture = msgService.exportAllMessage(msgExportProcessor, true);

        msgExportFuture.setCallback(new RequestCallback() {
            @Override
            public void onSuccess(Object param) {
                showSuccessResult("本地消息导出成功，请在新设备上点击导入吧");
            }

            @Override
            public void onFailed(int code) {

                String msg = "导出失败 ， code = " + code;
                if (code == MigrationConstant.EXPORT_ERR_DB_EMPTY) {
                    msg = "导出失败 , 本地消息为空";
                } else if (code == MigrationConstant.EXPORT_ERR_LOCAL_FORMAT) {
                    msg = "导出失败 , 本地消息格式化失败";
                } else if (code == MigrationConstant.EXPORT_ERR_USER_CUSTOM_ZIP) {
                    msg = "导出失败 , 文件自定义压缩失败";
                } else if (code == MigrationConstant.EXPORT_ERR_USER_CUSTOM_ENCRYPT) {
                    msg = "导出失败 , 文件自定义加密过程失败";
                } else if (code == MigrationConstant.EXPORT_ERR_UPLOAD_FILE) {
                    msg = "导出失败 , 文件上传失败";
                }
                showFailedDialog(msg);
            }

            @Override
            public void onException(Throwable exception) {
                showFailedDialog("导出失败，发生异常 , e = " + exception.getMessage());
            }
        });
    }

    private void importMsg() {
        actionType = IMPORT_ACTION;
        updateProgress(0, true);
        progressBar.setProgress(0);
        processContainer.setVisibility(View.VISIBLE);
        tvActingHint.setText("导入本地消息需要较长时间，请耐心等待");

        msgImportFuture = msgService.importAllMessage(msgImportProcessor, true);

        msgImportFuture.setCallback(new RequestCallback() {
            @Override
            public void onSuccess(Object param) {
                showSuccessResult("本地消息导入成功");
                //更新最近会话（如果需要） ， 这里演示更新已经存在的会话
                queryRecent();
            }

            @Override
            public void onFailed(int code) {
                String msg = "导入失败 ， code = " + code;
                if (code == MigrationConstant.IMPORT_ERR_NO_BACKUP || code == MigrationConstant.IMPORT_ERR_RECORD_EMPTY) {
                    msg = "导入失败 ，未找到备份";
                } else if (code == MigrationConstant.IMPORT_ERR_DOWN_FILE) {
                    msg = "导入失败 ，文件下载失败";
                } else if (code == MigrationConstant.IMPORT_ERR_CUSTOM_DECRYPT) {
                    msg = "导入失败 ，文件自定义解密失败";
                } else if (code == MigrationConstant.IMPORT_ERR_CUSTOM_UNZIP) {
                    msg = "导入失败 ，文件自定义解压缩失败";
                } else if (code == MigrationConstant.IMPORT_ERR_FILE_FORMAT) {
                    msg = "导入失败 ，文件解析格式失败";
                } else if (code == MigrationConstant.IMPORT_ERR_PART_SUCCESS) {
                    msg = "导入失败 ，部分成功";
                }
                showFailedDialog(msg);
            }

            @Override
            public void onException(Throwable exception) {
                showFailedDialog("导入失败，发生异常 , e = " + exception.getMessage());
            }
        });
    }


    //查找之前已经存在的会话
    private void queryRecent() {
        msgService.queryRecentContacts().setCallback(new RequestCallback<List<RecentContact>>() {
            @Override
            public void onSuccess(List<RecentContact> param) {
                queryRecentLastMessage(param);
            }

            @Override
            public void onFailed(int code) {
                ToastHelper.showToast(MsgMigrationActivity.this, "查询最近会话失败 ，code = " + code);

            }

            @Override
            public void onException(Throwable exception) {
                ToastHelper.showToast(MsgMigrationActivity.this, "查询最近会话异常");
                exception.printStackTrace();
            }
        });
    }

    // 根据会话查询对应的最新的一条消息
    private void queryRecentLastMessage(List<RecentContact> recentContacts) {
        if (CommonUtil.isEmpty(recentContacts)) {
            return;
        }


        for (final RecentContact recentContact : recentContacts) {
            IMMessage anchor = MessageBuilder.createEmptyMessage(recentContact.getContactId(),
                    recentContact.getSessionType(),
                    Long.MAX_VALUE);

            msgService.queryMessageListEx(anchor, QueryDirectionEnum.QUERY_OLD, 1, false).setCallback(new RequestCallback<List<IMMessage>>() {
                @Override
                public void onSuccess(List<IMMessage> messageList) {
                    updateRecent(messageList, recentContact);
                }

                @Override
                public void onFailed(int code) {

                }

                @Override
                public void onException(Throwable exception) {

                }
            });
        }

    }

    //根据消息更新会话
    private void updateRecent(List<IMMessage> messageList, RecentContact recentContact) {
        if (CommonUtil.isEmpty(messageList)) {
            return;
        }
        IMMessage message = messageList.get(0);
        //没必要更新了
        if (recentContact.getTime() >= message.getTime()) {
            return;
        }
        msgService.updateRecentByMessage(message, true);

    }


    private void showSuccessResult(String msg) {
        actionType = 0;
        actionContainer.setVisibility(View.GONE);
        processContainer.setVisibility(View.GONE);
        tvResultHint.setText(msg);
        resultContainer.setVisibility(View.VISIBLE);
    }


    private void showFailedDialog(String msg) {
        final boolean isExport = actionType == EXPORT_ACTION;
        actionType = 0;
        View.OnClickListener negativeListener = new DismissOnClickListener() {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                processContainer.setVisibility(View.GONE);
            }
        };


        View.OnClickListener positiveListener = new DismissOnClickListener() {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                if (isExport) {
                    exportMsg();
                } else {
                    importMsg();
                }
            }
        };


        showAlterDialog(isExport ? "导出失败" : "导入失败",
                msg,
                "返回", negativeListener,
                isExport ? "重新导出" : "重新导入", positiveListener);

    }


    private void showCancelDialog() {
        final boolean isExport = actionType == EXPORT_ACTION;
        View.OnClickListener negativeListener = new DismissOnClickListener() {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                cancel(isExport ? msgExportFuture : msgImportFuture);
            }
        };

        showAlterDialog(isExport ? "确定取消导出？" : "确定取消导入？",
                null,
                isExport ? "取消导出" : "取消导入", negativeListener,
                isExport ? "继续导出" : "继续导入", clickDismissListener);
    }

    private void cancel(AbortableFuture abortableFuture) {
        actionType = 0;
        processContainer.setVisibility(View.GONE);
        if (abortableFuture != null) {
            abortableFuture.abort();
        }
    }


    private void dismissDialog() {
        if (easyAlertDialog != null) {
            easyAlertDialog.dismiss();
            easyAlertDialog = null;
        }
    }


    private void showAlterDialog(String title,
                                 String message,
                                 String negativeText, View.OnClickListener negativeListener,
                                 String positiveText, View.OnClickListener positiveListener) {

        if (paused) {
            return;
        }
        dismissDialog();
        easyAlertDialog = new EasyAlertDialog(this);
        easyAlertDialog.setTitle(title);
        easyAlertDialog.setCancelable(false);
        if (!TextUtils.isEmpty(message)) {
            easyAlertDialog.setMessage(message);
        }
        if (!TextUtils.isEmpty(negativeText)) {
            easyAlertDialog.addNegativeButton(negativeText, negativeListener);
        }

        if (!TextUtils.isEmpty(positiveText)) {
            easyAlertDialog.addPositiveButton(positiveText, positiveListener);
        }
        easyAlertDialog.show();
    }


    @Override
    protected void onDestroy() {
        dismissDialog();
        super.onDestroy();
    }


    private void updateProgress(final int progress, boolean atUI) {
        if (atUI) {
            final int finalProgress = progress < 100 ? progress : 100;
            progressBar.setProgress(finalProgress);
            tvProgress.setText(finalProgress + "%");
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateProgress(progress, true);
            }
        });
    }

    private class DismissOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            dismissDialog();
        }
    }


    // 消息导出自定义处理器
    private class MsgExportProcessor implements IMsgExportProcessor {

        private String password;

        MsgExportProcessor() {
            // 生成32 位的UUID密钥 , 256 bit
            password = StringUtil.get32UUID();
        }

        @Override
        public ArrayList<IMMessage> filterMsg(ArrayList<IMMessage> messages) {
            Iterator<IMMessage> iterator = messages.iterator();

            while (iterator.hasNext()) {
                IMMessage message = iterator.next();
                MsgAttachment attachment = message.getAttachment();
                if (attachment == null) {
                    continue;
                }

                //  过滤 白板、贴图、阅后即焚、红包消息。
                if (attachment instanceof RTSAttachment ||
                        attachment instanceof StickerAttachment ||
                        attachment instanceof SnapChatAttachment ||
                        attachment instanceof RedPacketAttachment ||
                        attachment instanceof RedPacketOpenedAttachment) {
                    iterator.remove();
                }
            }

            return messages;
        }

        @Override
        public String secretKey() {
            return password;
        }


        // 运行在后台线程
        @Override
        public File encrypt(File originFile) throws Exception {
            byte[] passwordKey = getPasswordBytes(password);
            if (passwordKey == null) {
                return originFile;
            }

            // 初始化ASE  Cipher , 偏移向量VECTOR 各端统一即可
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(passwordKey, ALGORITHM);
            byte[] ivVector = new byte[16];
            System.arraycopy(VECTOR.getBytes(CHAR_SET), 0, ivVector, 0, ivVector.length);
            IvParameterSpec ivSpec = new IvParameterSpec(ivVector);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);


            File outAESFile = new File(originFile.getParentFile(), originFile.getName() + "_aes");

            CipherInputStream cipherInput = new CipherInputStream(new FileInputStream(originFile), cipher);
            OutputStream output = new FileOutputStream(outAESFile);

            long totalLen = originFile.length();
            long aesLen = 0;
            int aesProgress = 0;

            // 目前buffer 只能是512 ， 写大了，底层也是512
            byte[] cache = new byte[BUFFER_SIZE];
            int readLen;

            while ((readLen = cipherInput.read(cache)) != -1) {
                output.write(cache, 0, readLen);
                aesLen += readLen;

                // 这里只是个近似进度，因为加密后的文件会大于原文件 ， 这里只以原文件大小去计算进度
                int tempProgress = (int) (aesLen * 100 / totalLen);

                // aesProgress进度变化 10 % 通知一次
                if (tempProgress > aesProgress + PROGRESS_INTERVAL || tempProgress >= 100) {
                    aesProgress = tempProgress;
                    //step3: 加密文件 10 %   60 = 50 (消息转换成文件) + 10 (压缩文件)
                    updateProgress(60 + aesProgress * 10 / 100, false);
                }
            }
            output.flush();
            output.close();
            cipherInput.close();

            return outAESFile;
        }


        // 运行在后台线程
        @Override
        public File zip(File originFile) throws Exception {
            // 尽量不要用 .zip
            File outZipFile = new File(originFile.getParentFile(), originFile.getName() + "_zip");
            long totalLen = originFile.length();
            long zipLen = 0;

            // 目前只有单个文件
            if (originFile.isFile()) {
                ZipOutputStream zipOutput = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outZipFile)));
                ZipEntry zipEntry = new ZipEntry(originFile.getName());
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(originFile));

                // 设置原文件大小 ， 要不然解压时就没法获取正确的长度
                zipEntry.setSize(totalLen);
                zipOutput.putNextEntry(zipEntry);

                int len;
                int zipProgress = 0;
                byte[] buffer = new byte[8192];
                while ((len = inputStream.read(buffer)) != -1) {
                    zipOutput.write(buffer, 0, len);
                    zipLen += len;
                    int tempProgress = (int) (zipLen * 100 / totalLen);

                    // zipProgress 进度变化 10 % 通知一次
                    if (tempProgress > zipProgress + PROGRESS_INTERVAL || tempProgress >= 100) {
                        zipProgress = tempProgress;
                        // step2: 压缩文件 10 %
                        // 50 (消息转换成文件)
                        updateProgress(50 + zipProgress * 10 / 100, false);
                    }

                }
                zipOutput.closeEntry();
                zipOutput.flush();
                zipOutput.close();
                inputStream.close();

            } else {
                //目前不会走到这
                return originFile;
            }

            return outZipFile;
        }

        // 运行在UI线程
        // 进度：用户可以自定义，这只是个推荐值
        // step1: 消息转换成文件 50 %
        // step2: 压缩文件 10 %
        // step3: 加密文件 10 %
        // step4: 上传文件 30 %
        @Override
        public void progressUpdate(int progress, int state) {

            if (state == MigrationConstant.EXPORT_PROGRESS_CONVERT_MSG_STATE) {
                // step1: 消息转换成文件 50 %
                updateProgress(progress * 50 / 100, true);
                return;
            }

            if (state == MigrationConstant.EXPORT_PROGRESS_UPLOAD_FILE_STATE) {
                //step4: 上传文件 30 %
                // 70 = 50 (消息转换成文件) + 10 (压缩文件) + 10 (加密文件)
                updateProgress(70 + progress * 30 / 100, true);
            }
        }
    }


    private class MsgImportProcessor implements IMsgImportProcessor {

        // 运行在后台线程
        @Override
        public File decrypt(File originFile, String secretKey) throws Exception {

            byte[] passwordBytes = getPasswordBytes(secretKey);
            if (passwordBytes == null) {
                return originFile;
            }

            // 初始化ASE  Cipher , 偏移向量VECTOR 各端统一即可
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(passwordBytes, ALGORITHM);
            byte[] ivVector = new byte[16];
            System.arraycopy(VECTOR.getBytes(CHAR_SET), 0, ivVector, 0, ivVector.length);
            IvParameterSpec ivSpec = new IvParameterSpec(ivVector);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            File outFile = new File(originFile.getParentFile(), originFile.getName() + "_decrypt");
            FileInputStream fileInput = new FileInputStream(originFile);
            CipherOutputStream cipherOutput = new CipherOutputStream(new FileOutputStream(outFile), cipher);
            byte[] cache = new byte[BUFFER_SIZE];
            int readLen;
            long totalLen = originFile.length();
            long processLen = 0;
            int decryptProgress = 0;
            while ((readLen = fileInput.read(cache)) != -1) {
                cipherOutput.write(cache, 0, readLen);
                processLen += readLen;
                int tempProgress = (int) (processLen * 100 / totalLen);
                // 进度变化 10 % 通知一次，也就是总进度变化 2 %
                if (tempProgress - decryptProgress > PROGRESS_INTERVAL || tempProgress >= 100) {
                    decryptProgress = tempProgress;
                    // step2: 解密文件  30 (下载文件)
                    updateProgress(10 + decryptProgress * 20 / 100, false);
                }
            }
            cipherOutput.flush();
            cipherOutput.close();
            fileInput.close();
            return outFile;
        }

        // 运行在后台线程
        @Override
        public File unzip(File originFile) throws Exception {

            ZipFile zipFile = new ZipFile(originFile);
            Enumeration enumeration = zipFile.entries();
            ZipEntry zipEntry;
            long originLen = -1;
            if (enumeration.hasMoreElements()) {
                zipEntry = (ZipEntry) enumeration.nextElement();
                originLen = zipEntry.getSize();
            }

            ZipInputStream zinInput = new ZipInputStream(new BufferedInputStream(new FileInputStream(originFile)));
            zipEntry = zinInput.getNextEntry();
            byte[] buffer = new byte[8192];
            File unzipFile = null;
            long processLen = 0;
            int unzipProgress = 0;
            // 目前只有一个文件
            if (zipEntry != null) {
                unzipFile = new File(originFile.getParentFile(), originFile.getName() + "_unzip");
                FileOutputStream unzipOut = new FileOutputStream(unzipFile);
                int len;
                while ((len = zinInput.read(buffer)) > 0) {
                    unzipOut.write(buffer, 0, len);
                    processLen += len;

                    //如果获取原始长度失败，直接跳过进度 (压缩时需要设置下原文件长度)
                    if (originLen <= 0) {
                        continue;
                    }
                    int tempProgress = (int) (processLen * 100 / originLen);
                    // 进度变化 10 % 通知一次，也就是总进度变化 2 %
                    if (tempProgress - unzipProgress > PROGRESS_INTERVAL || tempProgress >= 100) {
                        unzipProgress = tempProgress;
                        // step3: 解压缩文件  50 = 30 (下载文件) +20 (解密文件)
                        updateProgress(15 + unzipProgress * 5 / 100, false);
                    }

                }
                unzipOut.flush();
                unzipOut.close();
            }
            zinInput.close();

            return unzipFile;
        }


        // 运行在UI线程
        // 推荐进度：用户可以自定义，这只是个建议 。 如果简单点，用户的自定义的过程（解密/解压缩 文件）进度可以忽略
        // step1: 下载文件 10  %
        // step2: 解密文件 5 %
        // step3: 解压缩文件 5 %
        // step4: 解析文件 80 %
        @Override
        public void progressUpdate(int progress, int state) {

            // step1: 下载文件
            if (state == MigrationConstant.IMPORT_PROGRESS_DOWNLOAD_FILE_STATE) {
                updateProgress(progress * 10 / 100, true);
                return;
            }

            // step4: 解析文件
            if (state == MigrationConstant.IMPORT_PROGRESS_FILE_TO_MSG_STATE) {
                // 70 = 30 (下载文件) + 20 (解密文件) + 20(解压缩文件)
                int totalProgress = 20 + progress * 80 / 100;
                updateProgress(totalProgress, true);
            }

        }
    }


    private byte[] getPasswordBytes(String password) throws UnsupportedEncodingException {

        byte[] passwordKey = new byte[32];
        int passwordLen = password.getBytes(CHAR_SET).length;
        // 长度必须256 bit
        if (passwordLen != passwordKey.length) {
            Log.e(TAG, "password len is not 256 bit , password = " + password);
            return null;
        }
        System.arraycopy(password.getBytes(CHAR_SET), 0, passwordKey, 0, passwordLen);
        return passwordKey;
    }

    @Override
    public void onBackPressed() {
        if (actionType != 0) {
            ToastHelper.showToast(this, "正在处理中，请先取消任务再退出页面");
            return;
        }
        super.onBackPressed();
    }
}
