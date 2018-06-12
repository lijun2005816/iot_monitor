package com.smartgreen.monitor.iotmonitor;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class IotTableStore {
    private String TAG = "IotTableStore";
    private String endPoint = "https://ssc-huangshan.cn-shanghai.ots.aliyuncs.com/GetRange";
    private String accessId = "LTAIwGxz1QYtm14l";
    private String accessKey = "41HxEu5bnloXcm51NcrI0SNlIknKnH";
    private String instanceName = "ssc-huangshan";
    private String tableName = "data";
    private String primaryKeyDevice = "device";
    private String primaryKeyProduct = "product";
    private String primaryKeyId = "id";
    private String apiVersion = "2015-12-31";
    private String canonicalURI = "/GetRange";
    private String httpRequestMethod = "POST";

    public String getRange(String startPkValue, String endPkValue) {
        return "";
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

    private String MD5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        try {
            byte[] btInput = s.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String httpSendPost(String url, String param) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        URL realUrl = new URL(url);
        URLConnection conn = realUrl.openConnection();
        SimpleDateFormat format = new SimpleDateFormat("%Y-%m-%dT%H:%M:%S.000Z", Locale.CHINA);
        String t=format.format(new Date());
        Log.i(TAG, t);
        conn.setRequestProperty("x-ots-date", t);
        conn.setRequestProperty("x-ots-apiversion", apiVersion);
        conn.setRequestProperty("x-ots-accesskeyid", accessId);
        conn.setRequestProperty("x-ots-instancename", instanceName);
        String md5 = MD5(param);
        conn.setRequestProperty("x-ots-contentmd5", md5);
        conn.setRequestProperty("User-Agent", "aliyun-tablestore-sdk-android-java/1.8");
        //conn.setRequestProperty("x-ots-ststoken", "token");
        String CanonicalHeaders = String.format("%s:%s\n%s:%s\n%s:%s\n%s:%s\n%s:%s\n", "x-ots-accesskeyid", accessId, "x-ots-apiversion", apiVersion, "x-ots-contentmd5", md5, "x-ots-date", t, "x-ots-instancename", instanceName);
        String stringToSign = String.format("%s\n%s\n\n%s", canonicalURI, httpRequestMethod, CanonicalHeaders);
        String signature = sign(stringToSign, accessKey, "hmacsha1");
        conn.setRequestProperty("x-ots-signature", signature);
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
