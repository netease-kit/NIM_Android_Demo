package com.faceunity;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.faceunity.auth.Auth;
import com.faceunity.ui.FaceULayout;
import com.faceunity.ui.adapter.EffectAndFilterSelectAdapter;
import com.faceunity.utils.NimSingleThreadExecutor;
import com.faceunity.utils.ThreadUtils;
import com.faceunity.wrapper.faceunity;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * Face Unity 封装接口
 * 开启FaceUnity的前置条件是已经从官方获取了授权，即官方为接入方提供的AuthPack.java文件。
 * 特别声明：由于使用FaceUnity是付费的，云信Demo不提供该授权文件，需要第三方开发者自行联系FaceUnity来获取。
 * 开发者接入只需要关心5个函数即可：
 * 1. 是否已经加入了授权文件: FaceU.hasAuthorized()
 * 2. 初始化FaceUnity并关联UI布局: FaceU.createAndAttach()，支持同步/异步加载
 * 3. FaceUnity的销毁: faceU.destroy()，一般在Activity#onDestory中调用
 * 4. 给视频帧添加人脸识别/道具/美颜效果: faceU.effect()， 支持I420和NV21两种帧图像格式
 * 5. 显示和隐藏UI布局切换：faceU.showOrHideLayout()，默认显示
 * <p>
 * Created by huangjun on 2017/7/13.
 */

public class FaceU {

    private static final String TAG = "FaceU";

    /// 上下文Activity
    private Context context;
    private FaceULayout faceULayout;

    /// 线程控制
    private static final String EFFECT_THREAD_NAME = "FU_EFFECT_T";
    private Handler handler;
    private final Object mHandlerLock = new Object();

    /// 道具/美颜滤镜参数
    private static String mEffectFileName = EffectAndFilterSelectAdapter.EFFECT_NAMES[EffectAndFilterSelectAdapter
            .EFFECT_DEFAULT]; // 道具
    private String mFilterName = EffectAndFilterSelectAdapter.FILTER_NAMES[EffectAndFilterSelectAdapter.FILTER_DEFAULT]; // 滤镜
    private float mFaceBeautyBlurLevel = 6.0f;
    private float mFaceBeautyColorLevel = 0.2f;
    private float mFaceBeautyCheckThin = 1.0f;
    private float mFaceBeautyRedLevel = 0.5f;
    private float mFaceBeautyEnlargeEye = 0.5f;
    private float mFaceShapeLevel = 0.5f;
    private int mFaceShape = 3;

    /// FU native底层控制
    private static int mFaceBeautyItem = 0;
    private static int mEffectItem = 0;
    private static int[] itemsArray = {mFaceBeautyItem, mEffectItem};
    private boolean effectChange = true; // 道具选项改变
    private boolean beautifyChange = true; // 美颜选项改变
    private int frame_id = 0;

    /// 人脸跟踪回调
    private int faceTrackingStatus = 0;
    private Callback callback;

    /// 视频帧宽高变化监控
    private int oldWidth = 0;
    private int oldHeight = 0;

    /// 异步请求响应
    public interface Response<T> {
        void onResult(T t);
    }

    public enum VIDEO_FRAME_FORMAT {
        I420,
        NV21
    }

    /**
     * **************************** 是否授权FaceUnity ****************************
     */

    public static boolean hasAuthorized() {
        return Auth.hasAuthFile();
    }

    /**
     * **************************** 初始化FaceUnity并关联UI布局 ****************************
     */

    public static FaceU createAndAttach(final Context context, final View faceUnityLayoutRoot) {
        FaceU faceU = create(context);
        FaceULayout.attach(context, faceU, faceUnityLayoutRoot);
        return faceU;
    }

    public static void createAndAttach(final Context context, final View faceUnityLayoutRoot, final Response<FaceU> cb) {
        NimSingleThreadExecutor.getInstance(context).execute(new NimSingleThreadExecutor.NimTask<FaceU>() {

            @Override
            public FaceU runInBackground() {
                return create(context);
            }

            @Override
            public void onCompleted(FaceU faceU) {
                if (faceU != null) {
                    FaceULayout.attach(context, faceU, faceUnityLayoutRoot); // should run on UI
                }

                if (cb != null) {
                    cb.onResult(faceU);
                }
            }
        });
    }

    private static FaceU create(final Context context) {
        final HandlerThread thread = new HandlerThread(EFFECT_THREAD_NAME);
        thread.start();
        final Handler handler = new Handler(thread.getLooper());

        return ThreadUtils.invokeAtFrontUninterruptibly(handler, new Callable<FaceU>() {
            @Override
            public FaceU call() {
                return new FaceU(context, handler);
            }
        });
    }

    private FaceU(Context context, Handler handler) {
        this.handler = handler;
        this.context = context;
        initFaceU();
    }

    private void initFaceU() {
        Log.i(TAG, "init faceU...");

        faceunity.fuCreateEGLContext();
        try {
            InputStream is = context.getAssets().open("v3.mp3");
            byte[] v3data = new byte[is.available()];
            is.read(v3data);
            is.close();
            faceunity.fuSetup(v3data, null, Auth.getFaceUnityAuthToken());
            faceunity.fuSetMaxFaces(4);
            Log.i(TAG, "fuSetup");

            is = context.getAssets().open("face_beautification.mp3");
            byte[] itemData = new byte[is.available()];
            is.read(itemData);
            is.close();
            mFaceBeautyItem = faceunity.fuCreateItemFromPackage(itemData);
            Log.i(TAG, "fuSetup mFaceBeautyItem=" + mFaceBeautyItem);
            itemsArray[0] = mFaceBeautyItem;
            frame_id = 0;

            Log.i(TAG, "init faceU done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ******************************** 释放FaceUnity ********************************
     */

    public void destroy() {
        releaseFaceU();
        mEffectFileName = EffectAndFilterSelectAdapter.EFFECT_NAMES[EffectAndFilterSelectAdapter.EFFECT_DEFAULT];
        synchronized (mHandlerLock) {
            handler.getLooper().quit();
            handler = null;
        }
    }

    private void releaseFaceU() {
        final CountDownLatch barrier = new CountDownLatch(1);
        final boolean didPost = maybePostOnEffectThread(new Runnable() {
            @Override
            public void run() {
                doReleaseFaceUNative();
                barrier.countDown();
            }
        });

        if (didPost) {
            ThreadUtils.awaitUninterruptibly(barrier);
        }
    }

    private void doReleaseFaceUNative() {
        Log.i(TAG, "release faceU native...");
        faceunity.fuDestroyAllItems();
        itemsArray[1] = mEffectItem = 0;
        itemsArray[0] = mFaceBeautyItem = 0;
        faceunity.fuDone();
        faceunity.fuReleaseEGLContext();
        faceunity.fuOnDeviceLost();
        frame_id = 0;
        effectChange = true;
        beautifyChange = true;
    }

    /**
     * ******************************** 道具/美颜效果(视频帧添加效果） ********************************
     */

    public boolean effect(final byte[] img, final int w, final int h, final VIDEO_FRAME_FORMAT format) {
        final CountDownLatch barrier = new CountDownLatch(1);
        final boolean didPost = maybePostOnEffectThread(new Runnable() {
            @Override
            public void run() {
                if (oldWidth == 0) {
                    oldWidth = w;
                    oldHeight = h;
                } else if ((oldWidth != w || oldHeight != h)) {
                    // 绘制数据帧的SurfaceView宽度高度变化了，必须重置
                    oldWidth = w;
                    oldHeight = h;
                    // reset!!!
                    doReleaseFaceUNative();
                    initFaceU();
                }
                faceTracking();
                fuItemSetParam();
                loadEffect();

                if (format == VIDEO_FRAME_FORMAT.I420) {
                    faceunity.fuRenderToI420Image(img, w, h, frame_id++, itemsArray);
                } else {
                    faceunity.fuRenderToNV21Image(img, w, h, frame_id++, itemsArray);
                }

                barrier.countDown();
            }
        });

        if (didPost) {
            ThreadUtils.awaitUninterruptibly(barrier);
            return true;
        }
        return false;
    }

    private void faceTracking() {
        final int isTracking = faceunity.fuIsTracking(); // 是否已识别人脸
        if (isTracking != faceTrackingStatus) {
            if (callback != null) {
                callback.faceTracking(isTracking);
            }
            faceTrackingStatus = isTracking;
        }
    }

    private void fuItemSetParam() {
        if (!beautifyChange) {
            return;
        }
        beautifyChange = false;
        faceunity.fuItemSetParam(mFaceBeautyItem, "color_level", mFaceBeautyColorLevel);
        faceunity.fuItemSetParam(mFaceBeautyItem, "blur_level", mFaceBeautyBlurLevel);
        faceunity.fuItemSetParam(mFaceBeautyItem, "filter_name", mFilterName);
        faceunity.fuItemSetParam(mFaceBeautyItem, "cheek_thinning", mFaceBeautyCheckThin);
        faceunity.fuItemSetParam(mFaceBeautyItem, "eye_enlarging", mFaceBeautyEnlargeEye);
        faceunity.fuItemSetParam(mFaceBeautyItem, "face_shape", mFaceShape);
        faceunity.fuItemSetParam(mFaceBeautyItem, "face_shape_level", mFaceShapeLevel);
        faceunity.fuItemSetParam(mFaceBeautyItem, "red_level", mFaceBeautyRedLevel);
    }

    private void loadEffect() {
        // 加载道具
        if (!effectChange) {
            return;
        }
        effectChange = false;
        try {
            int prev = itemsArray[1];
            if (mEffectFileName.equals("none")) {
                itemsArray[1] = mEffectItem = 0;
            } else {
                InputStream is = context.getAssets().open(mEffectFileName);
                byte[] itemData = new byte[is.available()];
                is.read(itemData);
                is.close();
                itemsArray[1] = mEffectItem = faceunity.fuCreateItemFromPackage(itemData);
                faceunity.fuItemSetParam(mEffectItem, "isAndroid", 1.0);
            }

            if (prev != 0) {
                faceunity.fuDestroyItem(prev);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean maybePostOnEffectThread(Runnable runnable) {
        return maybePostDelayedOnEffectThread(0, runnable);
    }

    private boolean maybePostDelayedOnEffectThread(int delayMs, Runnable runnable) {
        synchronized (mHandlerLock) {
            return handler != null && handler.postAtTime(runnable, this, SystemClock.uptimeMillis() + delayMs);
        }
    }

    /**
     * ********************************* 人脸跟踪回调 ******************************
     */

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void faceTracking(int trackStatus);
    }

    /**
     * ********************************* Face Unity UI 回调 ******************************
     */

    public void onEffectItemSelected(String effectItemName) {
        if (effectItemName.equals(mEffectFileName)) {
            return;
        }
        Log.i(TAG, "onEffectItemSelected=" + effectItemName);
        mEffectFileName = effectItemName;
        effectChange = true;
    }

    public void onFilterSelected(String filterName) {
        mFilterName = filterName;
        beautifyChange = true;
    }

    public void onBlurLevelSelected(int level) {
        mFaceBeautyBlurLevel = level * 1.0f;
        beautifyChange = true;
    }

    public void onColorLevelSelected(int progress, int max) {
        mFaceBeautyColorLevel = 1.0f * progress / max;
        beautifyChange = true;
    }

    public void onCheekThinSelected(int progress, int max) {
        mFaceBeautyCheckThin = 1.0f * progress / max;
        beautifyChange = true;
    }

    public void onEnlargeEyeSelected(int progress, int max) {
        mFaceBeautyEnlargeEye = 1.0f * progress / max;
        beautifyChange = true;
    }

    public void onFaceShapeSelected(int faceShape) {
        mFaceShape = faceShape;
        beautifyChange = true;
    }

    public void onFaceShapeLevelSelected(int progress, int max) {
        mFaceShapeLevel = (1.0f * progress) / max;
        beautifyChange = true;
    }

    public void onRedLevelSelected(int progress, int max) {
        mFaceBeautyRedLevel = 1.0f * progress / max;
        beautifyChange = true;
    }

    public void setFaceULayout(final FaceULayout layout) {
        this.faceULayout = layout;
    }

    /**
     * ********************************* Face Unity UI 显示隐藏 ******************************
     */
    public void showOrHideLayout() {
        faceULayout.showOrHideLayout();
    }
}
