package com.netease.nim.demo.main.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.netease.nim.demo.R;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.common.util.sys.ActionBarUtil;

/**
 * Created by hzxuwen on 2015/8/18.
 */
public class EditNicknameActivity extends TActionBarActivity {
    public static final int RES_CODE = 1;
    private ClearableEditTextWithIcon nickEdit;

    public static final void startActivityForResult(Context context, int reqCode) {
        Intent intent = new Intent();
        intent.setClass(context, EditNicknameActivity.class);
        ((Activity) context).startActivityForResult(intent, reqCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_nick_activity);
        setTitle(R.string.edit_nick);
        findViews();
        initActionbar();
    }

    private void findViews() {
        nickEdit = findView(R.id.nick_edit);
        nickEdit.setDeleteImage(R.drawable.nim_grey_delete_icon);
    }

    private void initActionbar() {
        ActionBarUtil.addRightClickableTextViewOnActionBar(this, R.string.done, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(nickEdit.getText().toString())) {
                    Toast.makeText(EditNicknameActivity.this, R.string.not_allow_empty, Toast.LENGTH_SHORT).show();
                } else {
                    editNick();
                }
            }
        });
    }

    private void editNick() {

    }
}
