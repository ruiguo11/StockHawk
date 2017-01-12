package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Rui on 30/07/16.
 */
public class AppWidgetIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AppWidgetIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
