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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private LineChart mLineChart;
    private LineDataSet mLineDataSet;
    private LineData mLineData;
    private int mMaxNum = 25;
    BroadcastReceiver broadcastReceiverMonitor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = Objects.requireNonNull(intent.getExtras()).getString("data");
            JSONObject json;
            try {
                json = new JSONObject(data);
                if (json.has("value")) {
                    addEntry(Float.valueOf(json.getString("value")));
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
        // start deviceservice
        //Intent device = new Intent(this, DeviceService.class);
        //startService(device);
        // register broadcast
        IntentFilter filterMonitor = new IntentFilter(MonitorService.action);
        registerReceiver(broadcastReceiverMonitor, filterMonitor);
        //keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //get linechart
        mLineChart = findViewById(R.id.lineChart);
        initLineChart();
    }

    private void initLineChart() {
        mLineChart.setDrawBorders(true);
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < mMaxNum; i++) {
            entries.add(new Entry(i, (float) (Math.random())));
        }
        mLineDataSet = new LineDataSet(entries, "温度");
        mLineData = new LineData(mLineDataSet);
        mLineChart.setData(mLineData);
    }

    public void onBtnRefreshClick(View view) {
        addEntry((float) (Math.random()));
    }

    private void addEntry(float data) {
        Entry entry = new Entry(mLineDataSet.getEntryCount(), data);
        mLineData.addEntry(entry, 0);
        mLineData.notifyDataChanged();
        mLineChart.notifyDataSetChanged();
        mLineChart.setVisibleXRangeMaximum(mMaxNum);
        mLineChart.moveViewToX(mLineData.getEntryCount() - 5);
    }
}
