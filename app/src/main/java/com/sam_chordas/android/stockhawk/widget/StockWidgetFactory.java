package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by Rui on 2/08/16.
 */
public class StockWidgetFactory implements RemoteViewsService.RemoteViewsFactory{

    private Cursor cursor;
    private int appWidgetId;
    private Context mContext;
    private static final String TAG= "StockWidgetFactory";


    public StockWidgetFactory(Context context, Intent intent){
        Log.d(TAG, "constructor");
        cursor = null;
        mContext =context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "OnCreate");

        cursor= mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);


    }

    @Override
    public void onDataSetChanged() {
        Log.d(TAG, "OnDataSetChanged");
        if(cursor!=null){
            cursor.close();
        }

        cursor= mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestory");
        if(cursor!=null){
            cursor.close();
            cursor=null;
        }


    }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount");

        return cursor==null? 0 :cursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION || cursor == null || !cursor.moveToPosition(position)) {
            return null;
        }
        Log.d(TAG, "getViewAt "+position);

        if (cursor.moveToPosition(position)) {
            RemoteViews remoteView = new RemoteViews( mContext.getPackageName(), R.layout.stock_item_widget);
            remoteView.setTextViewText(R.id.widget_stock_symbol, cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)));
            remoteView.setTextViewText(R.id.widget_bid_price, cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE)));

            String changeString = cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE));
            remoteView.setTextViewText(R.id.widget_change, changeString);
            if (Float.parseFloat(changeString) >= 0) {
                remoteView.setTextColor(R.id.widget_change, Color.GREEN);
            } else {
                remoteView.setTextColor(R.id.widget_change, Color.RED);
            }
            Log.d(TAG, cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)));


            Intent fillInIntent = new Intent();


            fillInIntent.putExtra(QuoteColumns.SYMBOL, cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)));
            Log.d(TAG, "fillIntent"+fillInIntent.getStringExtra(QuoteColumns.SYMBOL));
            remoteView.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
            return remoteView;
            }
        return null;
    }

    @Override
    public RemoteViews getLoadingView() {

        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
