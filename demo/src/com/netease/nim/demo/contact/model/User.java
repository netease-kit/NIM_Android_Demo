package com.netease.nim.demo.contact.model;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;

public class User extends AbsContact implements UserInfoProvider.UserInfo {

    private static final int[] headIconResources = new int[]{
            R.drawable.avatar_def, R.drawable.head_icon_1, R.drawable.head_icon_2, R.drawable.head_icon_3,
            R.drawable.head_icon_4, R.drawable.head_icon_5, R.drawable.head_icon_6, R.drawable.head_icon_7,
            R.drawable.head_icon_8, R.drawable.head_icon_9, R.drawable.head_icon_10
    };

    private String account;

    private String name;

    private int icon;

    public User() {
        this.icon = 0;
    }

    public User(String account, String name, int icon) {
        this.account = account;
        this.name = name;
        this.icon = icon % headIconResources.length;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Bitmap getAvatar() {
//        Drawable drawable = DemoCache.getContext().getResources().getDrawable(headIconResources[icon]);
//        if (drawable instanceof BitmapDrawable) {
//            return ((BitmapDrawable)drawable).getBitmap();
//        } else {
//            return BitmapDecoder.decodeSampled(DemoCache.getContext().getResources(), headIconResources[icon], 1);
//        }

        return null;
    }

    @Override
    public int getAvatarResId() {
        return headIconResources[icon];
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o == this) {
            return true;
        }

        User other = (User) o;
        if (this.account.equals(other.getAccount()) && this.name.equals(other.getName()) &&
                this.icon == other.getIcon()) {
            return true;
        }

        return false;
    }

    @Override
    public String getContactId() {
        return getAccount();
    }

    @Override
    public int getContactType() {
        return IContact.Type.Buddy;
    }

    @Override
    public String getDisplayName() {
        return TextUtils.isEmpty(name) ? account : name;
    }
}
