package com.stockhawk;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.stockhawk.model.QuoteColumns;
import com.stockhawk.model.QuoteProvider;

public class StockChartActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private LineChartView lineChartView;
    private static final int STOCKS_LOADER = 1;
    String currency = "";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deatils);
        lineChartView = (LineChartView) findViewById(R.id.linechart);
        Intent intent = getIntent();
        Bundle args = new Bundle();
        args.putString(Constants.SYMBOL, intent.getStringExtra(Constants.SYMBOL));

        currency = getIntent().getStringExtra(Constants.SYMBOL);
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
            drawChart(data);
        }else{
            Toast.makeText(this, getString(R.string.no_data_avaliable), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void drawChart(Cursor data) {
        LineSet lineSet = new LineSet();
        float minStockValue = Float.MAX_VALUE;
        float maxStockValue = Float.MIN_VALUE;

        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            String label = data.getString(data.getColumnIndexOrThrow(QuoteColumns.BIDPRICE));
            float stockValue = Float.parseFloat(label);

            lineSet.addPoint(label, stockValue);
            minStockValue = Math.min(minStockValue, stockValue);
            maxStockValue = Math.max(maxStockValue, stockValue);
        }

        lineSet.setColor(ContextCompat.getColor(StockChartActivity.this, R.color.colorIcons))
                .setFill(ContextCompat.getColor(StockChartActivity.this, R.color.colorChatBg))
                .setDotsColor(ContextCompat.getColor(StockChartActivity.this, R.color.colorPrimaryDark))
                .setThickness(3)
                .setDashed(new float[]{20f, 20f});


        lineChartView.setBorderSpacing(Tools.fromDpToPx(15))
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setLabelsColor(ContextCompat.getColor(StockChartActivity.this, R.color.colorIcons))
                .setXAxis(true)
                .setAxisColor(ContextCompat.getColor(StockChartActivity.this, R.color.colorSecondaryText))
                .setYAxis(true)
                .setAxisBorderValues(Math.round(Math.max(0f, minStockValue - 5f)), Math.round(maxStockValue + 5f))
                .addData(lineSet);

        if (lineSet.size() > 1)
            lineChartView.show(new Animation());
        else
            Toast.makeText(this, getString(R.string.no_data_avaliable), Toast.LENGTH_SHORT).show();
    }
}
