package com.demo.plugin1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StaticBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String name = intent.getStringExtra("name");
        Log.e("yangyangyang", "action==>" + action);
        Log.e("yangyangyang", "getStringExtra==>" + name);

        String extraName = intent.getExtras().getString("nameeee");
        Log.e("yangyangyang", "getExtras==>" + extraName);
    }
}
