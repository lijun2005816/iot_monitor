package com.smartgreen.monitor.iotmonitor;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class IotDevice {
    private String TAG = "IotDevice";
    private IotProduct Product;
    private String Name;
    private String Secret;
    private String TopicGet;
    private String TopicUpdate;

    IotDevice(IotProduct product, String deviceName, String deviceSecret) {
        Product = product;
        Name = deviceName;
        Secret = deviceSecret;
        TopicGet = String.format("/%s/%s/get", Product.getKey(), Name);
        TopicUpdate = String.format("/%s/%s/update", Product.getKey(), Name);
        if (Secret == null || Secret.trim().length() == 0) {
            try {
                Secret = initDeviceSecret();
            } catch (InvalidKeyException | NoSuchAlgorithmException | IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, this.toString());
    }

    public IotProduct getProduct() {
        return Product;
    }

    public String getName() {
        return Name;
    }

    public String getSecret() {
        return Secret;
    }

    public String getTopicGet() {
        return TopicGet;
    }

    public String getTopicUpdate() {
        return TopicUpdate;
    }

    public String toString() {
        return String.format("%s:[%s:%s]", Product.toString(), Name, Secret);
    }

    private String initDeviceSecret() throws InvalidKeyException, NoSuchAlgorithmException, IOException, JSONException {
        String url = "https://iot-auth.cn-shanghai.aliyuncs.com/auth/register/device";
        String random = "567345";
        String signMethod = "hmacsha1";
        String signStr = String.format("deviceName%sproductKey%srandom%s", Name, Product.getKey(), random);
        String sign = sign(signStr, Product.getSecret(), signMethod);
        String params = String.format("deviceName=%s&productKey=%s&random=%s&sign=%s&signMethod=%s", Name, Product.getKey(), random, sign, signMethod);
        String responses = httpSendPost(url, params);
        JSONObject json = new JSONObject(responses);
        String labelLevel1 = "data";
        String labelLevel2 = "deviceSecret";
        if (json.has(labelLevel1)) {
            JSONObject data = (JSONObject) json.get(labelLevel1);
            if (data.has(labelLevel2)) {
                Secret = data.get(labelLevel2).toString();
            }
        }
        return Secret;
    }

    private String sign(String content, String key, String signMethod) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), signMethod);
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);
        byte[] data = mac.doFinal(content.getBytes());
        return bytesToHexString(data);
    }

    private String bytesToHexString(byte[] bArray) {
        StringBuilder sb = new StringBuilder(bArray.length);
        String sTemp;
        for (byte aBArray : bArray) {
            sTemp = Integer.toHexString(0xFF & aBArray);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    private String httpSendPost(String url, String param) throws IOException {
        URL realUrl = new URL(url);
        URLConnection conn = realUrl.openConnection();
        conn.setRequestProperty("Accept", "text/xml,text/javascript,text/html,application/json");
        conn.setRequestProperty("POST", "/auth/register/device HTTP/1.1");
        conn.setRequestProperty("Host", "iot-auth.cn-shanghai.aliyuncs.com");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(param.length()));
        conn.setDoOutput(true);
        conn.setDoInput(true);
        PrintWriter out = new PrintWriter(conn.getOutputStream());
        out.print(param);
        out.flush();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            result.append(line);
        }
        out.close();
        in.close();
        return result.toString();
    }
}
