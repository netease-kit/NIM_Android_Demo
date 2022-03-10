/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.TitleBarLayoutBinding;

public class TitleBarView extends FrameLayout {

    private TitleBarLayoutBinding viewBinding;

    public TitleBarView(@NonNull Context context) {
        super(context);
        initView(null);
    }

    public TitleBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public TitleBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(AttributeSet attrs){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        viewBinding = TitleBarLayoutBinding.inflate(layoutInflater,this,true);
        if (attrs != null){
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TitleBarView);
            String title = array.getString(R.styleable.TitleBarView_head_title);
            viewBinding.titleBarTitleTv.setText(title);

            int titleColor = array.getInt(R.styleable.TitleBarView_head_title_color, R.color.title_color);
            viewBinding.titleBarTitleTv.setTextColor(titleColor);

            int visible = array.getInt(R.styleable.TitleBarView_head_img_visible,GONE);
            viewBinding.titleBarHeadImg.setVisibility(visible);

            int src = array.getInt(R.styleable.TitleBarView_head_img_src, R.mipmap.ic_title_bar_yunxin);
            viewBinding.titleBarHeadImg.setImageResource(src);
        }
    }

    public void setTitle(String title){
        viewBinding.titleBarTitleTv.setText(title);
    }

    public void setTitleColor(int color){
        viewBinding.titleBarTitleTv.setTextColor(color);
    }

    public void setHeadImageVisible(int visible){
        viewBinding.titleBarHeadImg.setVisibility(visible);
    }

    public void setHeadImageDrawable(Drawable drawable){
        viewBinding.titleBarHeadImg.setImageDrawable(drawable);
    }

    public ImageView getHeadImageView(){
        return viewBinding.titleBarHeadImg;
    }

    public TextView getTitleTextView(){
        return viewBinding.titleBarTitleTv;
    }

    public ImageView getRightImageView(){
        return viewBinding.titleBarMoreImg;
    }

    public ImageView getMiddleImageView(){
        return viewBinding.titleBarSearchImg;
    }

    public void setMiddleImageClick(OnClickListener listener){
        viewBinding.titleBarSearchImg.setOnClickListener(listener);
    }

    public void setMiddleImageRes(Drawable drawable){
        viewBinding.titleBarSearchImg.setImageDrawable(drawable);
    }

    public void showMiddleImageView(boolean show){
        viewBinding.titleBarSearchImg.setVisibility(show?VISIBLE:GONE);
    }

    public void setMoreImageClick(OnClickListener listener){
        viewBinding.titleBarMoreImg.setOnClickListener(listener);
    }

    public void setMoreImageRes(Drawable drawable){
        viewBinding.titleBarMoreImg.setImageDrawable(drawable);
    }

    public void showMoreImageView(boolean show){
        viewBinding.titleBarMoreImg.setVisibility(show?VISIBLE:GONE);
    }

}
