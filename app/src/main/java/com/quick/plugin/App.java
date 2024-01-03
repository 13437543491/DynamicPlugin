package com.quick.plugin;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.quick.dynamic.Dynamic;
import com.quick.dynamic.plugin.PluginManager;

public class App extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Dynamic.onBase(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Dynamic.onCreate(this);
    }
}
