package com.netease.nim.demo.location.helper;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.netease.nim.demo.location.model.NimLocation;
import com.netease.nim.uikit.common.framework.infra.TaskExecutor;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.test.MockTestService;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class NimLocationManager implements AMapLocationListener {

    public interface NimLocationListener {
        void onLocationChanged(NimLocation location);
    }

    private static final String TAG = "NimLocationManager";
    private Context mContext;
    private NimLocationListener mListener;
    private Criteria criteria;

    /**
     * msg handler
     */
    private static final int MSG_LOCATION_WITH_ADDRESS_OK = 1;
    private static final int MSG_LOCATION_POINT_OK = 2;
    private static final int MSG_LOCATION_ERROR = 3;

    private MsgHandler mMsgHandler = new MsgHandler();
    private TaskExecutor executor = new TaskExecutor(TAG, TaskExecutor.defaultConfig, true);

    /**
     * AMap location
     */
    private AMapLocationClient client;

    private Geocoder mGeocoder;

    public NimLocationManager(Context context, NimLocationListener oneShotListener) {
        mContext = context;
        mGeocoder = new Geocoder(mContext, Locale.getDefault());
        mListener = oneShotListener;
    }

    public static boolean isLocationEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria cri = new Criteria();
        cri.setAccuracy(Criteria.ACCURACY_COARSE);
        cri.setAltitudeRequired(false);
        cri.setBearingRequired(false);
        cri.setCostAllowed(false);
        String bestProvider = locationManager.getBestProvider(cri, true);
        return !TextUtils.isEmpty(bestProvider);
    }

    public Location getLastKnownLocation() {
        try {
            if (criteria == null) {
                criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setCostAllowed(false);
            }
            return client.getLastKnownLocation();
        } catch (Exception e) {
            LogUtil.i(TAG, "get last known location failed: " + e.toString());
        }
        return null;
    }

    public void request() {
        if (client == null) {
            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
            option.setInterval(30 * 1000);
            option.setHttpTimeOut(10 * 1000);
            client = new AMapLocationClient(mContext);
            client.setLocationOption(option);
            client.setLocationListener(this);
            client.startLocation();
        }
    }

    public void stop() {
        if (client != null) {
            client.unRegisterLocationListener(this);
            client.stopLocation();
            client.onDestroy();
        }
        mMsgHandler.removeCallbacksAndMessages(null);
        client = null;
    }

    @Override
    public void onLocationChanged(final AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    getAMapLocationAddress(aMapLocation);
                }
            });
        } else {
            onLocation(null, MSG_LOCATION_ERROR);
        }
    }

    private void onLocation(NimLocation location, int what) {
        Message msg = mMsgHandler.obtainMessage();
        msg.what = what;
        msg.obj = location;
        mMsgHandler.sendMessage(msg);
    }

    private class MsgHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOCATION_WITH_ADDRESS_OK:
                    if (mListener != null && msg.obj != null) {
                        if (msg.obj != null) {
                            NimLocation loc = (NimLocation) msg.obj;
                            loc.setStatus(NimLocation.Status.HAS_LOCATION_ADDRESS);

                            // 记录地址信息
                            loc.setFromLocation(true);

                            mListener.onLocationChanged(loc);
                        } else {
                            NimLocation loc = new NimLocation();
                            mListener.onLocationChanged(loc);
                        }
                    }
                    break;
                case MSG_LOCATION_POINT_OK:
                    if (mListener != null) {
                        if (msg.obj != null) {
                            NimLocation loc = (NimLocation) msg.obj;
                            loc.setStatus(NimLocation.Status.HAS_LOCATION);
                            mListener.onLocationChanged(loc);
                        } else {
                            NimLocation loc = new NimLocation();
                            mListener.onLocationChanged(loc);
                        }
                    }
                    break;
                case MSG_LOCATION_ERROR:
                    if (mListener != null) {
                        NimLocation loc = new NimLocation();
                        mListener.onLocationChanged(loc);
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    }

    private void getAMapLocationAddress(final AMapLocation loc) {
        if (TextUtils.isEmpty(loc.getAddress())) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    getLocationAddress(new NimLocation(loc, NimLocation.AMap_Location));
                }
            });
        } else {
            NimLocation location = new NimLocation(loc, NimLocation.AMap_Location);
            location.setAddrStr(loc.getAddress());
            location.setProvinceName(loc.getProvince());
            location.setCityName(loc.getCity());
            location.setCityCode(loc.getCityCode());
            location.setDistrictName(loc.getDistrict());
            location.setStreetName(loc.getStreet());
            location.setStreetCode(loc.getAdCode());

            onLocation(location, MSG_LOCATION_WITH_ADDRESS_OK);
        }

    }

    private boolean getLocationAddress(NimLocation location) {
        List<Address> list;
        boolean ret = false;
        try {
            list = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 2);
            if (list != null && list.size() > 0) {
                Address address = list.get(0);
                if (address != null) {
                    location.setCountryName(address.getCountryName());
                    location.setCountryCode(address.getCountryCode());
                    location.setProvinceName(address.getAdminArea());
                    location.setCityName(address.getLocality());
                    location.setDistrictName(address.getSubLocality());
                    location.setStreetName(address.getThoroughfare());
                    location.setFeatureName(address.getFeatureName());
                }
                ret = true;
            }
        } catch (IOException e) {
            LogUtil.e(TAG, e + "");
        }

        int what = ret ? MSG_LOCATION_WITH_ADDRESS_OK : MSG_LOCATION_POINT_OK;
        onLocation(location, what);

        return ret;
    }
}
