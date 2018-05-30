package com.smartgreen.monitor.iotmonitor;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceService extends Service {
    private String TAG = "DeviceService";
    private IotConnectManager Client;
    private int mDelayMillis = 2000;
    private Handler handlerRepeat = new Handler();
    private Runnable runnableRepeat = new Runnable() {
        @Override
        public void run() {
            JSONObject json = new JSONObject();
            try {
                json.put("value", Math.random());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Client.TopicSend(json.toString());
            handlerRepeat.postDelayed(this, mDelayMillis);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread() {
            @Override
            public void run() {
                SharedPreferences setting = getSharedPreferences("setting", 0);
                String mProductKey = setting.getString("productkey", "a12tTqAYnlp");
                String mProductSecret = setting.getString("productsecret", "aNBXcXkGuyFrn8aU");
                String mDeviceName = setting.getString("devicename", "device_000001");
                String mDeviceSecret = setting.getString("devicesecret", "");
                Log.i(TAG, String.format("%s:%s:%s:%s", mProductKey, mProductSecret, mDeviceName, mDeviceSecret));
                Client = new IotConnectManager(new IotDevice(mProductKey, mProductSecret, mDeviceName, mDeviceSecret), DeviceService.this);
                SharedPreferences.Editor editor = setting.edit();
                editor.putString("devicesecret", Client.getDeviceSecret()).apply();
                Log.i(TAG, String.format("%s:%s:%s:%s", mProductKey, mProductSecret, mDeviceName, mDeviceSecret));
                handlerRepeat.postDelayed(runnableRepeat, mDelayMillis);
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        handlerRepeat.removeCallbacks(runnableRepeat);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
