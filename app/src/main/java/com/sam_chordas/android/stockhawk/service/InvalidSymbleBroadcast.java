package com.sam_chordas.android.stockhawk.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Rui on 27/07/16.
 */
public class InvalidSymbleBroadcast extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Invalid symble",Toast.LENGTH_LONG).show();
    }
}
