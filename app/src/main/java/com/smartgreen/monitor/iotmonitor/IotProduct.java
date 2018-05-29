package com.smartgreen.monitor.iotmonitor;

import android.util.Log;

public class IotProduct {
    private String TAG = "IotProduct";
    private String Key;
    private String Secret;

    IotProduct(String productkey, String productsecret) {
        Key = productkey;
        Secret = productsecret;
        Log.i(TAG, this.toString());
    }

    public String getSecret() {
        return Secret;
    }

    public void setSecret(String secret) {
        this.Secret = secret;
    }

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        this.Key = key;
    }

    public String toString() {
        return String.format("[%s:%s]", Key, Secret);
    }
}
