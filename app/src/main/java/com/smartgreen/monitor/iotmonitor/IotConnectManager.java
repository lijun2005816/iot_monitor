package com.smartgreen.monitor.iotmonitor;

import android.app.Service;
import android.util.Log;

import com.aliyun.alink.linksdk.channel.core.base.AError;
import com.aliyun.alink.linksdk.channel.core.base.ARequest;
import com.aliyun.alink.linksdk.channel.core.base.AResponse;
import com.aliyun.alink.linksdk.channel.core.base.IOnCallListener;
import com.aliyun.alink.linksdk.channel.core.persistent.IOnSubscribeListener;
import com.aliyun.alink.linksdk.channel.core.persistent.PersistentInitParams;
import com.aliyun.alink.linksdk.channel.core.persistent.PersistentNet;
import com.aliyun.alink.linksdk.channel.core.persistent.event.IConnectionStateListener;
import com.aliyun.alink.linksdk.channel.core.persistent.event.IOnPushListener;
import com.aliyun.alink.linksdk.channel.core.persistent.event.PersistentEventDispatcher;
import com.aliyun.alink.linksdk.channel.core.persistent.mqtt.MqttInitParams;
import com.aliyun.alink.linksdk.channel.core.persistent.mqtt.request.MqttPublishRequest;

public class IotConnectManager {
    private String TAG = "IotConnectManager";
    private IotDevice Device;
    private IOnPushListener onPushListener = new IOnPushListener() {
        public void onCommand(String topic, String data) {
            Log.i(TAG, String.format("push message success [%s:%s]", topic, data));
        }

        public boolean shouldHandle(String topic) {
            return true;
        }
    };
    private IOnCallListener onCallListener = new IOnCallListener() {
        @Override
        public void onSuccess(ARequest request, AResponse response) {
            Log.i(TAG, String.format("send message success [%s:%s]", request.toString(), response.toString()));
        }

        @Override
        public void onFailed(ARequest request, AError error) {
            Log.i(TAG, String.format("send message failed [%s:%s]", request.toString(), error.toString()));
        }

        @Override
        public boolean needUISafety() {
            return false;
        }
    };
    private IOnSubscribeListener onSubscribeListener = new IOnSubscribeListener() {
        @Override
        public void onSuccess(String s) {
            Log.i(TAG, String.format("subscribe success [%s]", s));
        }

        @Override
        public void onFailed(String s, AError aError) {
            Log.i(TAG, String.format("subscribe failed for [%s:%s]", s, aError.toString()));
        }

        @Override
        public boolean needUISafety() {
            return false;
        }
    };
    private IConnectionStateListener connectionStateListener = new IConnectionStateListener() {
        @Override
        public void onConnectFail(String msg) {
            Log.i(TAG, String.format("connect to aliyun failed for [%s]", msg));
        }

        @Override
        public void onConnected() {
            String topic = Device.getTopicGet();
            PersistentNet.getInstance().subscribe(topic, onSubscribeListener);
            PersistentEventDispatcher.getInstance().registerOnPushListener(onPushListener, true);
            Log.i(TAG, "connect to aliyun success");
        }

        @Override
        public void onDisconnect() {
            String topic = Device.getTopicGet();
            PersistentNet.getInstance().unSubscribe(topic, onSubscribeListener);
            PersistentEventDispatcher.getInstance().unregisterOnPushListener(onPushListener);
            PersistentEventDispatcher.getInstance().unregisterOnTunnelStateListener(connectionStateListener);
            Log.i(TAG, "disconnect from aliyun");
        }
    };

    IotConnectManager(IotDevice device, Service service) {
        Device = device;
        MqttInitParams initParams = new MqttInitParams(Device.getProductKey(), Device.getDeviceName(), Device.getDeviceSecret());
        PersistentNet.getInstance().init(service, initParams);
        PersistentInitParams init = new PersistentInitParams();
        PersistentEventDispatcher.getInstance().registerOnTunnelStateListener(connectionStateListener, true);
    }

    IotConnectManager(IotDevice device, Service service, IOnPushListener push) {
        Device = device;
        onPushListener = push;
        MqttInitParams initParams = new MqttInitParams(Device.getProductKey(), Device.getDeviceName(), Device.getDeviceSecret());
        PersistentNet.getInstance().init(service, initParams);
        PersistentInitParams init = new PersistentInitParams();
        PersistentEventDispatcher.getInstance().registerOnTunnelStateListener(connectionStateListener, true);
    }

    public String getDeviceSecret() {
        return Device.getDeviceSecret();
    }

    public void TopicSend(String payload) {
        MqttPublishRequest publishRequest = new MqttPublishRequest();
        publishRequest.isRPC = false;
        publishRequest.topic = Device.getTopicUpdate();
        publishRequest.payloadObj = payload;
        PersistentNet.getInstance().asyncSend(publishRequest, onCallListener);
    }
}
