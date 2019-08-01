package com.faceunity.ui;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.faceunity.FaceU;
import com.faceunity.R;
import com.faceunity.ui.adapter.EffectAndFilterSelectAdapter;
import com.faceunity.utils.ScreenUtil;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;


/**
 * Face Unity UI布局适配器及事件响应封装，绑定布局文件 faceu_layout.xml
 * <p>
 * Created by huangjun on 2017/7/13.
 */

public class FaceULayout implements View.OnClickListener {
    private final String TAG = "FaceULayout";

    /// 上下文
    private final Context context;
    private final View root;
    private final FaceU faceU;

    // all buttons
    private Button mChooseEffectBtn;
    private Button mChooseFilterBtn;
    private Button mChooseBlurLevelBtn;
    private Button mChooseColorLevelBtn;
    private Button mChooseFaceShapeBtn;
    private Button mChooseRedLevelBtn;

    /// 道具
    private RecyclerView mEffectRecyclerView;

    /// 滤镜
    private RecyclerView mFilterRecyclerView;

    /// 磨皮
    private LinearLayout mBlurLevelSelect;
    private TextView[] mBlurLevels;
    private final int[] BLUR_LEVEL_TV_ID = {R.id.blur_level0, R.id.blur_level1, R.id.blur_level2, R.id.blur_level3, R.id
            .blur_level4, R.id.blur_level5, R.id.blur_level6};

    // 美白
    private LinearLayout mColorLevelSelect;

    // 红润
    private LinearLayout mRedLevelSelect;

    // 美型
    private LinearLayout mFaceShapeSelect;
    private TextView mFaceShape0Nvshen;
    private TextView mFaceShape1Wanghong;
    private TextView mFaceShape2Ziran;
    private TextView mFaceShape3Default;

    public static void attach(Context context, FaceU faceU, View root) {
        new FaceULayout(context, faceU, root);
    }

    private FaceULayout(Context context, FaceU faceU, View root) {
        this.context = context.getApplicationContext();
        this.root = root;
        this.faceU = faceU;
        this.faceU.setFaceULayout(this);

        // init
        findViews();
        setViewsListener();
        ScreenUtil.init(context);
    }

    private void findViews() {
        // all buttons
        mChooseEffectBtn = (Button) root.findViewById(R.id.btn_choose_effect);
        mChooseFilterBtn = (Button) root.findViewById(R.id.btn_choose_filter);
        mChooseBlurLevelBtn = (Button) root.findViewById(R.id.btn_choose_blur_level);
        mChooseColorLevelBtn = (Button) root.findViewById(R.id.btn_choose_color_level);
        mChooseRedLevelBtn = (Button) root.findViewById(R.id.btn_choose_red_level);
        mChooseFaceShapeBtn = (Button) root.findViewById(R.id.btn_choose_face_shape);

        // 道具选择
        mEffectRecyclerView = (RecyclerView) root.findViewById(R.id.effect_recycle_view);
        mEffectRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        EffectAndFilterSelectAdapter mEffectRecyclerAdapter = new EffectAndFilterSelectAdapter(mEffectRecyclerView, EffectAndFilterSelectAdapter
                .VIEW_TYPE_EFFECT);
        mEffectRecyclerAdapter.setOnItemSelectedListener(new EffectAndFilterSelectAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int itemPosition) {
                Log.d(TAG, "effect item selected " + itemPosition);
                faceU.onEffectItemSelected(EffectAndFilterSelectAdapter.EFFECT_NAMES[itemPosition]); // cb
            }
        });
        mEffectRecyclerView.setAdapter(mEffectRecyclerAdapter);

        // 滤镜选额
        mFilterRecyclerView = (RecyclerView) root.findViewById(R.id.filter_recycle_view);
        mFilterRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        EffectAndFilterSelectAdapter mFilterRecyclerAdapter = new EffectAndFilterSelectAdapter(mFilterRecyclerView, EffectAndFilterSelectAdapter
                .VIEW_TYPE_FILTER);
        mFilterRecyclerAdapter.setOnItemSelectedListener(new EffectAndFilterSelectAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int itemPosition) {
                Log.d(TAG, "filter item selected " + itemPosition);
                faceU.onFilterSelected(EffectAndFilterSelectAdapter.FILTER_NAMES[itemPosition]); // cb
            }
        });
        mFilterRecyclerView.setAdapter(mFilterRecyclerAdapter);

        // 磨皮选择
        mBlurLevelSelect = (LinearLayout) root.findViewById(R.id.blur_level_select_block);
        mBlurLevels = new TextView[BLUR_LEVEL_TV_ID.length];
        for (int i = 0; i < BLUR_LEVEL_TV_ID.length; i++) {
            final int level = i;
            mBlurLevels[i] = (TextView) root.findViewById(BLUR_LEVEL_TV_ID[i]);
            mBlurLevels[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setBlurLevelTextBackground(mBlurLevels[level]);
                    faceU.onBlurLevelSelected(level); // cb
                }
            });
        }

        // 美白选择
        mColorLevelSelect = (LinearLayout) root.findViewById(R.id.color_level_select_block);

        // 红润选择
        mRedLevelSelect = (LinearLayout) root.findViewById(R.id.red_level_select_block);

        // 美型选择
        mFaceShapeSelect = (LinearLayout) root.findViewById(R.id.face_shape_select_block);
        mFaceShape0Nvshen = (TextView) root.findViewById(R.id.face_shape_0_nvshen);
        mFaceShape1Wanghong = (TextView) root.findViewById(R.id.face_shape_1_wanghong);
        mFaceShape2Ziran = (TextView) root.findViewById(R.id.face_shape_2_ziran);
        mFaceShape3Default = (TextView) root.findViewById(R.id.face_shape_3_default);
    }

    private void setViewsListener() {
        // all buttons
        mChooseEffectBtn.setOnClickListener(this);
        mChooseFilterBtn.setOnClickListener(this);
        mChooseBlurLevelBtn.setOnClickListener(this);
        mChooseColorLevelBtn.setOnClickListener(this);
        mChooseRedLevelBtn.setOnClickListener(this);
        mChooseFaceShapeBtn.setOnClickListener(this);

        // 美型
        mFaceShape0Nvshen.setOnClickListener(this);
        mFaceShape1Wanghong.setOnClickListener(this);
        mFaceShape2Ziran.setOnClickListener(this);
        mFaceShape3Default.setOnClickListener(this);

        // 美白
        DiscreteSeekBar colorLevelSeekBar = (DiscreteSeekBar) root.findViewById(R.id.color_level_seekbar);
        colorLevelSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                faceU.onColorLevelSelected(value, 100); // cb
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        // 红润
        DiscreteSeekBar redLevelShapeLevelSeekBar = (DiscreteSeekBar) root.findViewById(R.id.red_level_seekbar);
        redLevelShapeLevelSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                faceU.onRedLevelSelected(value, 100); // cb
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        // 美型-瘦脸
        DiscreteSeekBar cheekThinSeekBar = (DiscreteSeekBar) root.findViewById(R.id.cheekthin_level_seekbar);
        cheekThinSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                faceU.onCheekThinSelected(value, 100); // cb
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        // 美型-大眼
        DiscreteSeekBar enlargeEyeSeekBar = (DiscreteSeekBar) root.findViewById(R.id.enlarge_eye_level_seekbar);
        enlargeEyeSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                faceU.onEnlargeEyeSelected(value, 100); // cb
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        // 美颜-脸型程度
        DiscreteSeekBar faceShapeLevelSeekBar = (DiscreteSeekBar) root.findViewById(R.id.face_shape_seekbar);
        faceShapeLevelSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                faceU.onFaceShapeLevelSelected(value, 100); // cb
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_choose_effect) {
            setEffectFilterBeautyChooseBtnTextColor(mChooseEffectBtn);
            setEffectFilterBeautyChooseBlock(mEffectRecyclerView);
        } else if (i == R.id.btn_choose_filter) {
            setEffectFilterBeautyChooseBtnTextColor(mChooseFilterBtn);
            setEffectFilterBeautyChooseBlock(mFilterRecyclerView);
        } else if (i == R.id.btn_choose_blur_level) {
            setEffectFilterBeautyChooseBtnTextColor(mChooseBlurLevelBtn);
            setEffectFilterBeautyChooseBlock(mBlurLevelSelect);
        } else if (i == R.id.btn_choose_color_level) {
            setEffectFilterBeautyChooseBtnTextColor(mChooseColorLevelBtn);
            setEffectFilterBeautyChooseBlock(mColorLevelSelect);
        } else if (i == R.id.btn_choose_red_level) {
            setEffectFilterBeautyChooseBtnTextColor(mChooseRedLevelBtn);
            setEffectFilterBeautyChooseBlock(mRedLevelSelect);
        } else if (i == R.id.btn_choose_face_shape) {
            setEffectFilterBeautyChooseBtnTextColor(mChooseFaceShapeBtn);
            setEffectFilterBeautyChooseBlock(mFaceShapeSelect);
        } else if (i == R.id.face_shape_0_nvshen) {
            setFaceShapeBackground(mFaceShape0Nvshen);
            faceU.onFaceShapeSelected(0); // 脸型-女神
        } else if (i == R.id.face_shape_1_wanghong) {
            setFaceShapeBackground(mFaceShape1Wanghong);
            faceU.onFaceShapeSelected(1); // 脸型-网红
        } else if (i == R.id.face_shape_2_ziran) {
            setFaceShapeBackground(mFaceShape2Ziran);
            faceU.onFaceShapeSelected(2); // 脸型-自然
        } else if (i == R.id.face_shape_3_default) {
            setFaceShapeBackground(mFaceShape3Default);
            faceU.onFaceShapeSelected(3); // 脸型默认
        }
    }

    public void showOrHideLayout() {
        final View v1 = root.findViewById(R.id.face_u_control);
        if (v1.getAlpha() > 0.0f && v1.getAlpha() < 1.0f) {
            return; // 正在动画，点击无效
        }

        final View v2 = root.findViewById(R.id.effect_beauty_select);
        final ObjectAnimator anim1;
        final ObjectAnimator anim2;
        final int duration = 500;

        if (v1.getAlpha() == 1.0f) {
            anim1 = ObjectAnimator.ofFloat(v1, "alpha", 1.0f, 0.0f).setDuration(duration);
            anim2 = ObjectAnimator.ofFloat(v2, "alpha", 1.0f, 0.0f).setDuration(duration);
        } else {
            anim1 = ObjectAnimator.ofFloat(v1, "alpha", 0.0f, 1.0f).setDuration(duration);
            anim2 = ObjectAnimator.ofFloat(v2, "alpha", 0.0f, 1.0f).setDuration(duration);
        }
        anim1.start();
        anim2.start();
    }

    private void setEffectFilterBeautyChooseBlock(View v) {
        mEffectRecyclerView.setVisibility(View.INVISIBLE);
        mFilterRecyclerView.setVisibility(View.INVISIBLE);
        mFaceShapeSelect.setVisibility(View.INVISIBLE);
        mBlurLevelSelect.setVisibility(View.INVISIBLE);
        mColorLevelSelect.setVisibility(View.INVISIBLE);
        mRedLevelSelect.setVisibility(View.INVISIBLE);
        v.setVisibility(View.VISIBLE);
    }

    private void setEffectFilterBeautyChooseBtnTextColor(Button selectedBtn) {
        mChooseEffectBtn.setTextColor(context.getResources().getColor(R.color.colorWhite));
        mChooseColorLevelBtn.setTextColor(context.getResources().getColor(R.color.colorWhite));
        mChooseBlurLevelBtn.setTextColor(context.getResources().getColor(R.color.colorWhite));
        mChooseFilterBtn.setTextColor(context.getResources().getColor(R.color.colorWhite));
        mChooseFaceShapeBtn.setTextColor(context.getResources().getColor(R.color.colorWhite));
        mChooseRedLevelBtn.setTextColor(context.getResources().getColor(R.color.white));
        selectedBtn.setTextColor(context.getResources().getColor(R.color.faceunityYellow));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setBlurLevelTextBackground(TextView tv) {
        mBlurLevels[0].setBackground(context.getResources().getDrawable(R.drawable.zero_blur_level_item_unselected));
        for (int i = 1; i < BLUR_LEVEL_TV_ID.length; i++) {
            mBlurLevels[i].setBackground(context.getResources().getDrawable(R.drawable.blur_level_item_unselected));
        }
        if (tv == mBlurLevels[0]) {
            tv.setBackground(context.getResources().getDrawable(R.drawable.zero_blur_level_item_selected));
        } else {
            tv.setBackground(context.getResources().getDrawable(R.drawable.blur_level_item_selected));
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setFaceShapeBackground(TextView tv) {
        mFaceShape0Nvshen.setBackground(context.getResources().getDrawable(R.color.unselect_gray));
        mFaceShape1Wanghong.setBackground(context.getResources().getDrawable(R.color.unselect_gray));
        mFaceShape2Ziran.setBackground(context.getResources().getDrawable(R.color.unselect_gray));
        mFaceShape3Default.setBackground(context.getResources().getDrawable(R.color.unselect_gray));
        tv.setBackground(context.getResources().getDrawable(R.color.faceunityYellow));
    }
}
