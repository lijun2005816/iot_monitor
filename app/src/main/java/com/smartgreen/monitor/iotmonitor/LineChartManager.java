package com.smartgreen.monitor.iotmonitor;

import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class LineChartManager {
    private String TAG = "LineChartManager";
    private LineChart mLineChart;
    private YAxis mLeftAxis;
    private YAxis mRightAxis;
    private XAxis mXAxis;
    private LineData mLineData;
    private LineDataSet mLineDataSet;
    private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
    private long mMaxTime = 60 * 1000;
    private long mCurTime;
    private long mStartTime;

    LineChartManager(LineChart lineChart) {
        mLineChart = lineChart;
        mLeftAxis = mLineChart.getAxisLeft();
        mRightAxis = mLineChart.getAxisRight();
        mXAxis = mLineChart.getXAxis();
        mStartTime = System.currentTimeMillis();
        mCurTime = mMaxTime;
        initLineChart();
        initLineData();
    }

    private void initLineChart() {
        mLineChart.setDrawBorders(true);
        mLeftAxis.setAxisMinimum(0f);
        mRightAxis.setAxisMinimum(0f);
        mXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        mXAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                long timeMil = Float.valueOf(value).longValue() + mStartTime;
                return df.format(timeMil);
            }
        });
    }

    private void initLineData() {
        mLineDataSet = new LineDataSet(null, "温度");
        mLineData = new LineData();
        mLineChart.setData(mLineData);
        mLineChart.invalidate();
    }

    public void addEntry(float data) {
        if (mLineDataSet.getEntryCount() == 0) {
            mLineData.addDataSet(mLineDataSet);
        }
        long x = System.currentTimeMillis() - mStartTime;
        Entry entry = new Entry(x, data);
        mLineData.addEntry(entry, 0);
        mLineData.notifyDataChanged();
        mLineChart.notifyDataSetChanged();
        mLineChart.setVisibleXRange(mCurTime, mCurTime);
        long midTime = x-mCurTime/2;
        mLineChart.moveViewToX(midTime);
    }

    public void ZoomView(float ratio) {
        mCurTime = (long)(mCurTime * ratio);
        mLineChart.setVisibleXRange(mCurTime, mCurTime);
        Log.i(TAG, String.format("view:%s", mCurTime));
    }

    public void OriginView() {
        mCurTime = mMaxTime;
        mLineChart.setVisibleXRange(mCurTime, mCurTime);
        Log.i(TAG, String.format("view:%s", mCurTime));
    }
}
