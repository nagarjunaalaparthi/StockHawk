package com.stockhawk;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.stockhawk.model.QuoteColumns;
import com.stockhawk.model.QuoteProvider;

import java.util.ArrayList;
import java.util.Collections;

public class StockChartActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int CURSOR_LOADER_ID = 0;
    private Cursor mCursor;
    private LineChartView lineChartView;
    private LineSet mLineSet;
    int maxRange,minRange,step;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deatils);
        mLineSet = new LineSet();
        lineChartView = (LineChartView) findViewById(R.id.linechart);
        initLineChart();
        Intent intent = getIntent();
        Bundle args = new Bundle();
        args.putString("symbol", intent.getStringExtra("symbol"));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, args, this);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns.BIDPRICE},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{args.getString("symbol")},
                null);
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        findRange(mCursor);
        fillLineSet();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void fillLineSet(){
        mCursor.moveToFirst();
        for (int i = 0; i < mCursor.getCount(); i++){
            float price = Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
            mLineSet.addPoint("test " + i, price);
            mCursor.moveToNext();
        }
        mLineSet.setColor(ContextCompat.getColor(StockChartActivity.this,R.color.red))
                .setDotsStrokeThickness(Tools.fromDpToPx(2))
                .setDotsStrokeColor(ContextCompat.getColor(StockChartActivity.this,R.color.green))
                .setDotsColor(ContextCompat.getColor(StockChartActivity.this,R.color.colorPrimaryLight));
        lineChartView.addData(mLineSet);
        lineChartView.show();
    }

    private void initLineChart() {
        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.GRAY);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(1f));
        lineChartView.setBorderSpacing(1)
                .setAxisBorderValues(minRange-100, maxRange+100, 50)
                .setXLabels(AxisController.LabelPosition.OUTSIDE)
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setLabelsColor(ContextCompat.getColor(StockChartActivity.this,R.color.colorPrimary))
                .setXAxis(false)
                .setYAxis(false)
                .setBorderSpacing(Tools.fromDpToPx(5))
                .setGrid(ChartView.GridType.HORIZONTAL, gridPaint);
    }
    public void findRange(Cursor mCursor)
    {
        ArrayList<Float> mArrayList = new ArrayList<Float>();
        for(mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            // The Cursor is now set to the right position
            mArrayList.add(Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE))));
        }
        maxRange = Math.round(Collections.max(mArrayList));
        minRange = Math.round(Collections.min(mArrayList));
        if(minRange>100)
            minRange = minRange-100;
//        if(maxRange-minRange>10)
//            step = Math.round((maxRange*1.0f - minRange*1.0f)/10);
//        if(step==0)
//            step=10;
    }
}
