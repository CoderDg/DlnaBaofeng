
package com.charon.dmc.service;

import org.cybergarage.upnp.ControlPoint;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;

import com.charon.dmc.DMCApplication;
import com.charon.dmc.engine.SearchThread;
import com.charon.dmc.util.LogUtil;

/**
 * The service to search the DLNA Device in background all the time.
 * 
 * @author CharonChui
 */
public class DLNAService extends Service {
    private static final String TAG = "DLNAService";

    private ControlPoint mControlPoint;

    private SearchThread mSearchThread;

    private WifiStateReceiver mWifiStateReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        mControlPoint = new ControlPoint();
        DMCApplication.getInstance().setControlPoint(mControlPoint);
        mSearchThread = new SearchThread(mControlPoint);
        registerWifiStateReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stop
        stopThread();
        unregisterWifiStateReceiver();
    }

    /**
     * Called by the system every time a client explicitly starts the service by
     * calling android.content.Context.startService, providing the arguments it
     * supplied and a unique integer token representing the start request. Do
     * not call this method directly.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startThread();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Make the thread start to search devices.
     */
    private void startThread() {
        if (mSearchThread != null) {
            LogUtil.d(TAG, "thread is not null");
            mSearchThread.setSearcTimes(0);
        } else {
            LogUtil.d(TAG, "thread is null, create a new thread");
            mSearchThread = new SearchThread(mControlPoint);
        }

        if (mSearchThread.isAlive()) {
            LogUtil.d(TAG, "thread is alive");
            mSearchThread.awake();
        } else {
            LogUtil.d(TAG, "start the thread");
            mSearchThread.start();
        }
    }

    private void stopThread() {
        if (mSearchThread != null) {
            mSearchThread.stopThread();
            mControlPoint.stop();
            mSearchThread = null;
            mControlPoint = null;
            LogUtil.w(TAG, "stop dlna service");
        }
    }

    private void registerWifiStateReceiver() {
        if (mWifiStateReceiver == null) {
            mWifiStateReceiver = new WifiStateReceiver();
            registerReceiver(mWifiStateReceiver, new IntentFilter(
                    ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    private void unregisterWifiStateReceiver() {
        if (mWifiStateReceiver != null) {
            unregisterReceiver(mWifiStateReceiver);
            mWifiStateReceiver = null;
        }
    }

    private class WifiStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            Bundle bundle = intent.getExtras();
            int statusInt = bundle.getInt("wifi_state");
            switch (statusInt) {
                case WifiManager.WIFI_STATE_UNKNOWN:
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    LogUtil.e(TAG, "wifi enable");
                    startThread();
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    LogUtil.e(TAG, "wifi disabled");
                    break;
                default:
                    break;
            }
        }
    }

}
