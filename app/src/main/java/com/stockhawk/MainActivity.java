package com.stockhawk;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.stockhawk.Utils.Utils;
import com.stockhawk.adapter.QuoteCursorAdapter;
import com.stockhawk.adapter.RecyclerViewItemClickListener;
import com.stockhawk.adapter.SimpleItemTouchHelperCallback;
import com.stockhawk.model.QuoteColumns;
import com.stockhawk.model.QuoteProvider;
import com.stockhawk.service.StockIntentService;
import com.stockhawk.service.StockTaskService;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private CharSequence mTitle;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Context mContext;
    private Cursor mCursor;
    boolean isConnected;
    private DataChangedReciever dataReciever;
    private boolean isFirstTime = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = MainActivity.this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        if(savedInstanceState == null){
            isFirstTime = true;
        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.stocks_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        mCursorAdapter = new QuoteCursorAdapter(this, null);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        //TODO:
                        // do something on item click
                        Intent intent = new Intent(MainActivity.this, StockChartActivity.class);
                        intent.putExtra(Constants.SYMBOL, mCursorAdapter.getSymbol(position));
                        startActivity(intent);
                    }
                }));
        recyclerView.setAdapter(mCursorAdapter);
        findViewById(R.id.fab).setOnClickListener(fabClickListener);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        mTitle = getTitle();
        if (isConnected) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = getString(R.string.periodic);

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerDataReceiver();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    private void registerDataReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.stockhawk.datanotfound");
        dataReciever = new DataChangedReciever();
        registerReceiver(dataReciever, filter);
    }

    @Override
    protected void onDestroy() {
        if (dataReciever != null) {
            unregisterReceiver(dataReciever);
        }
        super.onDestroy();
    }

    private View.OnClickListener fabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final SearchDialogFragment dialogFragment = new SearchDialogFragment();
            dialogFragment.setOkListener(new SearchDialogFragment.OkListener() {
                @Override
                public void onOk(String text) {
                    dialogFragment.dismiss();
                    showProgressDialog();
                    searchForStock(text);
                }
            });
            dialogFragment.show(getFragmentManager(), "search_symbol");
        }
    };


    private void searchForStock(String text) {
        Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                new String[]{text.toString()}, null);
        if (c.getCount() != 0) {
            Toast toast =
                    Toast.makeText(MainActivity.this, getString(R.string.stock_is_already_saved),
                            Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
            toast.show();
            return;
        } else {
            // Add the stock to DB
            mServiceIntent.putExtra(Constants.TAG, Constants.ADD);
            mServiceIntent.putExtra(Constants.SYMBOL, text.toString());
            startService(mServiceIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void networkToast() {
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            // this is for changing stock changes from percent value to dollar value
            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        hideProgressDialog();
        mCursorAdapter.swapCursor(data);
        mCursor = data;
        if(isFirstTime){
            mServiceIntent = new Intent(this, StockIntentService.class);
            StringBuilder builder = new StringBuilder();
            if (mCursor != null && mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                while (mCursor.moveToNext()) {
                    String symbol = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL));
                    if (builder.toString().length() > 0) {
                        builder.append(",\"" + symbol + "\"");
                    } else {
                        builder.append("\"" + symbol + "\"");
                    }
                }
            }
            mServiceIntent.putExtra(Constants.SYMBOL, builder.toString());
            mServiceIntent.putExtra(Constants.TAG, Constants.INIT);
            if (isConnected) {
                startService(mServiceIntent);
            } else {
                networkToast();
            }
            isFirstTime = false;
        }
        Log.i("finished", data.toString());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        hideProgressDialog();
        Log.i("finished", "reset");
        mCursorAdapter.swapCursor(null);
    }

    public class DataChangedReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            hideProgressDialog();
            Toast.makeText(context, getString(R.string.no_stock_details_found), Toast.LENGTH_SHORT).show();
        }
    }
}
