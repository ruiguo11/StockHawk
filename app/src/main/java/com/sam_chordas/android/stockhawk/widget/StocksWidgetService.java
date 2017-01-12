package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

/**
 * Created by Rui on 2/08/16.
 */
public class StocksWidgetService extends RemoteViewsService {
    private static final String TAG= "StocksWidgetService";
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d(TAG, "onGetViewFactory");
        return new StockWidgetFactory(this.getApplicationContext(), intent);
        //return null;
    }
}
