package com.smartgreen.monitor.iotmonitor;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private LineChartManager mLineChartManager;

    BroadcastReceiver mBroadcastReceiverMonitor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String data = Objects.requireNonNull(intent.getExtras()).getString("data");
                JSONObject json = new JSONObject(data);
                List<Float> value = new ArrayList<>();
                int mChNum = 8;
                for (int i = 0; i < mChNum; i++) {
                    if (json.has(String.format("CH%s", i + 1))) {
                        Float temp = Float.valueOf(json.getString(String.format("CH%s", i + 1)));
                        value.add(temp);
                    } else {
                        value.add(0f);
                    }
                }
                mLineChartManager.addEntry(value);
                Log.i(TAG, data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.i(TAG, "true flag");
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }
            }
        }
        Intent mMonitor = new Intent(this, MonitorService.class);
        startService(mMonitor);
        IntentFilter filterMonitor = new IntentFilter(MonitorService.action);
        registerReceiver(mBroadcastReceiverMonitor, filterMonitor);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        LineChart lineChart = findViewById(R.id.lineChart);
        mLineChartManager = new LineChartManager(lineChart);
    }

    public void onBtnZoomInClick(View view){
        mLineChartManager.ZoomView(0.5f);
        Log.i(TAG, "zoom in 0.5 ratio");
    }

    public void onBtnZoomOutClick(View view){
        mLineChartManager.ZoomView(2f);
        Log.i(TAG, "zoom out 2 ratio");
    }

    public void onBtnOriginClick(View view){
        mLineChartManager.OriginView();
        Log.i(TAG, "zoom to origin view");
    }

    public void onBtnExportClick(View view) {
        try {
            String filePath = String.format("%s/iotmonitor", Environment.getExternalStorageDirectory().getPath());
            File dir = new File(filePath);
            if (! dir.exists()) {
                if (! dir.mkdir() ) {
                    Log.i(TAG, "make dir failed");
                    return;
                }
            }
            String fileName = String.format("/export_%s.csv", System.currentTimeMillis());
            File file = new File(filePath + fileName);
            if (! file.exists()) {
                if (! file.createNewFile() ) {
                    Log.i(TAG, "file create failed");
                    return;
                }
                Log.i(TAG, filePath + fileName);
            }
            FileOutputStream fos= new FileOutputStream(file);
            OutputStreamWriter osw=new OutputStreamWriter(fos,"UTF-8");
            osw.write(mLineChartManager.ExportData());
            osw.flush();
            fos.flush();
            fos.close();
            osw.close();
            Toast.makeText(this, "export finished", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "export error", Toast.LENGTH_LONG).show();
        }
    }

    public void onBtnMoreClick(View view) {
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mBroadcastReceiverMonitor);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        String filePath = String.format("%s/iotmonitor", Environment.getExternalStorageDirectory().getPath());
                        File dir = new File(filePath);
                        if (!dir.exists()) {
                            Log.d(TAG, "create result:" + dir.mkdirs());
                        }
                    }
                    break;
                }
        }
    }
}
