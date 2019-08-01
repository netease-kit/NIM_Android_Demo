package com.faceunity.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.faceunity.R;
import com.faceunity.ui.view.EffectAndFilterItemView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by lirui on 2017/1/20.
 */

public class EffectAndFilterSelectAdapter extends RecyclerView.Adapter<EffectAndFilterSelectAdapter.ItemViewHolder> {

    // 道具名称
    public static final String[] EFFECT_NAMES = {"none", "tiara.mp3", "item0208.mp3",
            "YellowEar.mp3", "PrincessCrown.mp3", "Mood.mp3", "Deer.mp3", "BeagleDog.mp3", "item0501.mp3",
            "ColorCrown.mp3", "item0210.mp3", "HappyRabbi.mp3", "item0204.mp3", "hartshorn.mp3"};

    // 道具图片
    private static final int[] EFFECT_ITEM_RES_ARRAY = {
            R.mipmap.ic_delete_all, R.mipmap.tiara, R.mipmap.item0208, R.mipmap.yellowear,
            R.mipmap.princesscrown, R.mipmap.mood, R.mipmap.deer, R.mipmap.beagledog, R.mipmap.item0501,
            R.mipmap.colorcrown, R.mipmap.item0210, R.mipmap.happyrabbi, R.mipmap.item0204, R.mipmap.hartshorn
    };

    // 滤镜名称
    public final static String[] FILTER_NAMES = {"nature", "delta", "electric", "slowlived", "tokyo", "warm"};

    // 滤镜图片
    private static final int[] FILTER_ITEM_RES_ARRAY = {
            R.mipmap.nature, R.mipmap.delta, R.mipmap.electric, R.mipmap.slowlived, R.mipmap.tokyo, R.mipmap.warm
    };

    // RecyclerView
    private RecyclerView mRecyclerView;
    private int mRecyclerViewType;
    public static final int VIEW_TYPE_EFFECT = 0;
    public static final int VIEW_TYPE_FILTER = 1;
    public static final int EFFECT_DEFAULT = 1; // 道具默认值
    public static final int FILTER_DEFAULT = 0; // 滤镜默认值

    // 点击事件
    private ArrayList<Boolean> mItemsClickStateList;
    private int mLastClickPosition = -1;
    private OnItemSelectedListener mOnItemSelectedListener;

    public EffectAndFilterSelectAdapter(RecyclerView recyclerView, int recyclerViewType) {
        mRecyclerView = recyclerView;
        mRecyclerViewType = recyclerViewType;

        mItemsClickStateList = new ArrayList<>();
        initItemsClickState();
    }

    @Override
    public int getItemCount() {
        return mRecyclerViewType == VIEW_TYPE_EFFECT ? EFFECT_ITEM_RES_ARRAY.length : FILTER_ITEM_RES_ARRAY.length;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(new EffectAndFilterItemView(parent.getContext(), mRecyclerViewType));
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {
        if (mItemsClickStateList.get(position) == null || !mItemsClickStateList.get(position)) {
            holder.mItemView.setUnselectedBackground();
        } else {
            holder.mItemView.setSelectedBackground();
        }

        if (mRecyclerViewType == VIEW_TYPE_EFFECT) {
            holder.mItemView.setItemIcon(EFFECT_ITEM_RES_ARRAY[position % EFFECT_ITEM_RES_ARRAY.length]);
        } else {
            holder.mItemView.setItemIcon(FILTER_ITEM_RES_ARRAY[position % FILTER_ITEM_RES_ARRAY.length]);
            holder.mItemView.setItemText(FILTER_NAMES[position % FILTER_ITEM_RES_ARRAY.length].toUpperCase());
        }

        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastClickPosition != position) {
                    ItemViewHolder lastItemViewHolder = (ItemViewHolder) mRecyclerView.findViewHolderForAdapterPosition(mLastClickPosition);
                    if (lastItemViewHolder != null) {
                        lastItemViewHolder.mItemView.setUnselectedBackground();
                    }
                    mItemsClickStateList.set(mLastClickPosition, false);
                }

                holder.mItemView.setSelectedBackground();
                setClickPosition(position);
            }
        });
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        EffectAndFilterItemView mItemView;

        ItemViewHolder(View itemView) {
            super(itemView);
            mItemView = (EffectAndFilterItemView) itemView;
        }
    }

    private void initItemsClickState() {
        if (mItemsClickStateList == null) {
            return;
        }
        mItemsClickStateList.clear();
        if (mRecyclerViewType == VIEW_TYPE_EFFECT) {
            mItemsClickStateList.addAll(Arrays.asList(new Boolean[EFFECT_ITEM_RES_ARRAY.length]));
            setClickPosition(EFFECT_DEFAULT);
        } else {
            mItemsClickStateList.addAll(Arrays.asList(new Boolean[FILTER_ITEM_RES_ARRAY.length]));
            setClickPosition(FILTER_DEFAULT);
        }
    }

    private void setClickPosition(int position) {
        if (position < 0) {
            return;
        }
        mItemsClickStateList.set(position, true);
        mLastClickPosition = position;
        if (mOnItemSelectedListener != null) {
            mOnItemSelectedListener.onItemSelected(position);
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int itemPosition);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.mOnItemSelectedListener = onItemSelectedListener;
    }
}
