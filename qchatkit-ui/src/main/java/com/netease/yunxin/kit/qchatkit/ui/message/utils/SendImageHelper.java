package com.netease.yunxin.kit.qchatkit.ui.message.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.netease.yunxin.kit.qchatkit.ui.utils.FileUtils;

import java.io.File;

public class SendImageHelper {
    public interface Callback {
        void sendImage(String filePath, boolean isOrig);
    }

    public static class SendImageTask extends AsyncTask<Void,Void,String>{

        private Callback callback;
        private Uri originUri;
        private Context context;

        public SendImageTask(Context context,Uri uri,Callback callback){
            this.originUri = uri;
            this.callback = callback;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            File tempFile = FileUtils.getTempFile(context,originUri.getPath());
            FileUtils.copy(context,originUri,tempFile.getPath());
            return tempFile.getPath();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (callback != null){
                callback.sendImage(s,false);
            }
        }
    }
}
