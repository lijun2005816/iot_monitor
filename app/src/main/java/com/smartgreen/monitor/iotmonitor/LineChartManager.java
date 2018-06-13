package com.smartgreen.monitor.iotmonitor;

import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LineChartManager {
    private String TAG = "LineChartManager";
    private LineChart mLineChart;
    private YAxis mLeftAxis;
    private YAxis mRightAxis;
    private XAxis mXAxis;
    private LineData mLineData;
    private List<ILineDataSet> mLineDataSets = new ArrayList<>();
    private int mChNum = 8;
    private SimpleDateFormat mDf = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
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
        Description des = new Description();
        des.setText("");
        mLineChart.setDescription(des);
        Legend legend = mLineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        mLeftAxis.setAxisMinimum(0f);
        mRightAxis.setAxisMinimum(0f);
        mXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        int mXDiv = 7;
        mXAxis.setLabelCount(mXDiv, true);
        mXAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                long timeMil = Float.valueOf(value).longValue() + mStartTime;
                return mDf.format(timeMil);
            }
        });
    }

    private void initLineData() {
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.MAGENTA);
        colors.add(Color.YELLOW);
        colors.add(Color.GREEN);
        colors.add(Color.CYAN);
        colors.add(Color.BLUE);
        colors.add(Color.BLACK);
        colors.add(Color.GRAY);
        for (int i = 0; i < mChNum; i++) {
            LineDataSet mLineDataSet = new LineDataSet(null, String.format("CH%s", i + 1));
            mLineDataSet.setColor(colors.get(i));
            mLineDataSet.setCircleColor(colors.get(i));
            mLineDataSet.setHighLightColor(colors.get(i));
            mLineDataSets.add(mLineDataSet);
        }
        mLineData = new LineData();
        mLineChart.setData(mLineData);
        mLineChart.invalidate();
    }

    public void addEntry(List<Float> data) {
        if (mLineDataSets.get(0).getEntryCount() == 0) {
            mLineData = new LineData(mLineDataSets);
            mLineChart.setData(mLineData);
        }
        if (data.size() < mChNum) {
            Log.i(TAG, "channel number error");
            return;
        }
        for (int i = 0; i < mChNum; i ++) {
            long x = System.currentTimeMillis() - mStartTime;
            Entry entry = new Entry(x, data.get(i));
            mLineData.addEntry(entry, i);
            String label = String.format("CH%s:%s", i + 1, data.get(i));
            mLineData.getDataSetByIndex(i).setLabel(label);
            mLineData.notifyDataChanged();
            mLineChart.notifyDataSetChanged();
            mLineChart.setVisibleXRangeMaximum(mCurTime);
            long midTime = x-mCurTime/2;
            mLineChart.moveViewToX(midTime);
        }
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

    public String ExportData() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        StringBuilder sb = new StringBuilder();
        sb.append("Time");
        for (int i = 0; i < mChNum; i ++) {
            String label = String.format(",CH%s", i + 1);
            sb.append(label);
        }
        sb.append("\n");
        for (int i = 0; i < mLineDataSets.get(0).getEntryCount(); i ++) {
            float x = mLineDataSets.get(0).getEntryForIndex(i).getX();
            long timeMil = Float.valueOf(x).longValue() + mStartTime;
            sb.append(df.format(timeMil));
            for (int j = 0; j < mChNum; j ++) {
                float y = mLineDataSets.get(j).getEntryForIndex(i).getY();
                sb.append(",");
                sb.append(y);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
