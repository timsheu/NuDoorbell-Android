package com.nuvoton.socketmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BGBCReceiver extends BroadcastReceiver {
    private final String TAG = "BGBCReceiver";
    public BGBCReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d(TAG, "onReceive: Service stops!");
        context.startService(new Intent(context, BGBCReceiverService.class));
    }
}
