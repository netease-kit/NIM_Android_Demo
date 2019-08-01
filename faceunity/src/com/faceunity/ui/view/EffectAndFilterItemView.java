package com.faceunity.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.faceunity.R;
import com.faceunity.ui.adapter.EffectAndFilterSelectAdapter;

/**
 * Created by lirui on 2017/1/20.
 */

public class EffectAndFilterItemView extends LinearLayout {
    private ImageView mItemIcon;
    private TextView mItemText;

    private int mItemType; //effect or filter

    public EffectAndFilterItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EffectAndFilterItemView(Context context, int itemType) {
        super(context);
        this.mItemType = itemType;
        init(context);
    }

    private void init(Context context) {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
        View viewRoot = LayoutInflater.from(context).inflate(R.layout.effect_and_filter_item_view, this, true);
        mItemIcon = (ImageView) viewRoot.findViewById(R.id.item_icon);
        mItemText = (TextView) viewRoot.findViewById(R.id.item_text);
        if (mItemType == EffectAndFilterSelectAdapter.VIEW_TYPE_FILTER) {
            mItemText.setVisibility(VISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setUnselectedBackground() {
        if (mItemType == EffectAndFilterSelectAdapter.VIEW_TYPE_EFFECT) {
            mItemIcon.setBackground(getResources().getDrawable(R.drawable.effect_item_circle_unselected));
        } else {
            mItemIcon.setBackgroundColor(Color.parseColor("#00000000"));
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setSelectedBackground() {
        if (mItemType == EffectAndFilterSelectAdapter.VIEW_TYPE_EFFECT) {
            mItemIcon.setBackground(getResources().getDrawable(R.drawable.effect_item_circle_selected));
        } else {
            mItemIcon.setBackground(getResources().getDrawable(R.drawable.effect_item_square_selected));
        }
    }

    public void setItemIcon(int resourceId) {
        mItemIcon.setImageDrawable(getResources().getDrawable(resourceId));
    }

    public void setItemText(String text) {
        mItemText.setText(text);
    }
}
