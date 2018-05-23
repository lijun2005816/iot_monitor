package com.smartgreen.monitor.iotmonitor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LineChart mLineChart;
    private LineDataSet mLineDataSet;
    private LineData mLineData;
    private int mMaxNum = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            entries.add(new Entry(i, (float) (Math.random()) * 80));
        }
        mLineDataSet = new LineDataSet(entries, "温度");
        mLineData = new LineData(mLineDataSet);
        mLineChart.setData(mLineData);
    }

    public void onBtnRefreshClick(View view) {
        Entry entry = new Entry(mLineDataSet.getEntryCount(), (float) (Math.random()) * 80);
        mLineData.addEntry(entry, 0);
        mLineData.notifyDataChanged();
        mLineChart.notifyDataSetChanged();
        mLineChart.setVisibleXRangeMaximum(mMaxNum);
        mLineChart.moveViewToX(mLineData.getEntryCount() - 5);
    }
}
