package com.netease.nim.demo.file.browser;

import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.demo.R;
import com.netease.nim.demo.file.browser.FileBrowserAdapter.FileManagerItem;
import com.netease.nim.uikit.common.adapter.TViewHolder;

import java.io.File;

/**
 * Created by hzxuwen on 2015/4/17.
 */
public class FileBrowserViewHolder extends TViewHolder {
    private ImageView fileImage;
    private TextView fileName;
    private FileManagerItem fileItem;


    @Override
    protected int getResId() {
        return R.layout.file_browser_list_item;
    }

    @Override
    protected void inflate() {
        fileImage = view.findViewById(R.id.file_image);
        fileName = view.findViewById(R.id.file_name);
    }

    @Override
    protected void refresh(Object item) {
        fileItem = (FileManagerItem) item;

        File f = new File(fileItem.getPath());
        if (fileItem.getName().equals("@1")) {
            fileName.setText("/返回根目录");
            fileImage.setImageResource(R.drawable.directory);
        } else if (fileItem.getName().equals("@2")) {
            fileName.setText("..返回上一级目录");
            fileImage.setImageResource(R.drawable.directory);
        } else {
            fileName.setText(fileItem.getName());
            if (f.isDirectory()) {
                fileImage.setImageResource(R.drawable.directory);
            } else if (f.isFile()) {
                fileImage.setImageResource(R.drawable.file);
            }
        }

    }
}
