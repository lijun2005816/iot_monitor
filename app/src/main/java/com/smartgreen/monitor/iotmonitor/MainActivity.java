package com.smartgreen.monitor.iotmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private LineChartManager mLineChartManager;

    BroadcastReceiver broadcastReceiverMonitor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = Objects.requireNonNull(intent.getExtras()).getString("data");
            JSONObject json;
            try {
                json = new JSONObject(data);
                if (json.has("value")) {
                    mLineChartManager.addEntry(Float.valueOf(json.getString("value")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i(TAG, data);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // start monitorrvice
        Intent monitor = new Intent(this, MonitorService.class);
        startService(monitor);
        // register broadcast
        IntentFilter filterMonitor = new IntentFilter(MonitorService.action);
        registerReceiver(broadcastReceiverMonitor, filterMonitor);
        //keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //get linechart
        LineChart lineChart = findViewById(R.id.lineChart);
        mLineChartManager = new LineChartManager(lineChart);
    }

    public void onBtnZoomInClick(View view){
        mLineChartManager.ZoomView(0.5f);
    }

    public void onBtnZoomOutClick(View view){
        mLineChartManager.ZoomView(2f);
    }

    public void onBtnOriginClick(View view){
        mLineChartManager.OriginView();
    }

    public void onBtnExportClick(View view) {
        //IotTableStore.getRange("1527948032000000", "1528034432000000");
    }

    public void onBtnMoreClick(View view) {
    }
}
