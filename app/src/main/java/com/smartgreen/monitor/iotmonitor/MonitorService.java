package com.smartgreen.monitor.iotmonitor;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aliyun.alink.linksdk.channel.core.persistent.event.IOnPushListener;

public class MonitorService extends Service {
    private String TAG = "MonitorService";
    public static String action = "monitor.broadcast.action";
    private IotConnectManager Client;
    private IOnPushListener onPushListener = new IOnPushListener() {
        public void onCommand(String topic, String data) {
            Intent intent = new Intent(action);
            intent.putExtra("data", data);
            sendBroadcast(intent);
            Log.i(TAG, String.format("message [%s:%s] arrived", topic, data));
        }

        public boolean shouldHandle(String topic) {
            return true;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread() {
            @Override
            public void run() {
                SharedPreferences setting = getSharedPreferences("setting", 0);
                String mProductKey = setting.getString("productkeymonitor", "a12tTqAYnlp");
                String mProductSecret = setting.getString("productsecretmonitor", "aNBXcXkGuyFrn8aU");
                String mDeviceName = setting.getString("devicenamemonitor", "device_000000");
                String mDeviceSecret = setting.getString("devicesecretmonitor", "");
                Log.i(TAG, String.format("%s:%s:%s:%s", mProductKey, mProductSecret, mDeviceName, mDeviceSecret));
                Client = new IotConnectManager(new IotDevice(mProductKey, mProductSecret, mDeviceName, mDeviceSecret), MonitorService.this, onPushListener);
                SharedPreferences.Editor editor = setting.edit();
                editor.putString("devicesecretmonitor", Client.getDeviceSecret()).apply();
                Log.i(TAG, String.format("%s:%s:%s:%s", mProductKey, mProductSecret, mDeviceName, Client.getDeviceSecret()));
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
