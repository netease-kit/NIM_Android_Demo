package com.netease.yunxin.nertc.model;

import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.util.Objects;

/**
 * 业务用户数据
 */
public final class UserModel implements Serializable {
    public String nickname;//String  登录的手机号
    public String imAccid;//long  IM账号
    public String imToken;//String  IM令牌，重新生成的新令牌
    public String avatar;//String  头像地址

    public long g2Uid;//g2的UID

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserModel userModel = (UserModel) o;
        return TextUtils.equals(this.imAccid, userModel.imAccid);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hashCode(imAccid);
    }
}
