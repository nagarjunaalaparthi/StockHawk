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
import com.db.chart.view.animation.Animation;
import android.widget.Toast;

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
    public static final String TAG_STOCK_SYMBOL = "STOCK_SYMBOL";
    private static final int STOCKS_LOADER = 1;
    String currency = "";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deatils);
        mLineSet = new LineSet();
        lineChartView = (LineChartView) findViewById(R.id.linechart);
        Intent intent = getIntent();
        Bundle args = new Bundle();
        args.putString("symbol", intent.getStringExtra("symbol"));

        currency = getIntent().getStringExtra("symbol");
        getLoaderManager().initLoader(STOCKS_LOADER, null, this);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case STOCKS_LOADER:
                return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                                QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                        QuoteColumns.SYMBOL + " = ?",
                        new String[]{currency},
                        null);
        }

        return null;
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data!=null && data.getCount() != 0) {
            renderChart(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

//    private void fillLineSet(){
//        mCursor.moveToFirst();
//        for (int i = 0; i < mCursor.getCount(); i++){
//            float price = Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
//            mLineSet.addPoint("test " + i, price);
//            mCursor.moveToNext();
//        }
//        mLineSet.setColor(ContextCompat.getColor(StockChartActivity.this,R.color.red))
//                .setDotsStrokeThickness(Tools.fromDpToPx(2))
//                .setDotsStrokeColor(ContextCompat.getColor(StockChartActivity.this,R.color.green))
//                .setDotsColor(ContextCompat.getColor(StockChartActivity.this,R.color.colorPrimaryLight));
//        lineChartView.addData(mLineSet);
//        lineChartView.show();
//    }

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
    public void renderChart(Cursor data) {
        LineSet lineSet = new LineSet();
        float minimumPrice = Float.MAX_VALUE;
        float maximumPrice = Float.MIN_VALUE;

        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            String label = data.getString(data.getColumnIndexOrThrow(QuoteColumns.BIDPRICE));
            float price = Float.parseFloat(label);

            lineSet.addPoint(label, price);
            minimumPrice = Math.min(minimumPrice, price);
            maximumPrice = Math.max(maximumPrice, price);
        }

        lineSet.setColor(ContextCompat.getColor(StockChartActivity.this, R.color.colorIcons))
                .setFill(ContextCompat.getColor(StockChartActivity.this, R.color.colorPrimaryDark))
                .setDotsColor(ContextCompat.getColor(StockChartActivity.this, R.color.colorIcons))
                .setThickness(4)
                .setDashed(new float[]{15f, 15f});


        lineChartView.setBorderSpacing(Tools.fromDpToPx(15))
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setLabelsColor(ContextCompat.getColor(StockChartActivity.this, R.color.colorDivider))
                .setXAxis(false)
                .setYAxis(false)
                .setAxisBorderValues(Math.round(Math.max(0f, minimumPrice - 5f)), Math.round(maximumPrice + 5f))
                .addData(lineSet);

        Animation anim = new Animation();

        if (lineSet.size() > 1)
            lineChartView.show(anim);
        else
            Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show();
    }
}
