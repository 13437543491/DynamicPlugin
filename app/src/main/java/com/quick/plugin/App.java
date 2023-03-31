package com.quick.plugin;

import android.app.Application;

import com.quick.dynamic.Dynamic;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Dynamic.onCreate(this);
    }
}
