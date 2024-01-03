package com.quick.dynamic;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.quick.dynamic.plugin.PluginManager;
import com.quick.dynamic.plugin.proxy.ActivityManagerServiceProxy;
import com.quick.dynamic.plugin.proxy.PackageManagerInternalProxy;
import com.quick.dynamic.util.Reflection;

import java.io.File;


public class Dynamic {

    public static Context sContext;

    public static void onCreate(Context context) {
        sContext = context;
        PluginManager.getInstance().onCreate(context);
    }

    public static Application getApp() {
        return ActivityThread.currentApplication();
    }

    public static void installApk(File apkFile) {
//        ActivityManagerServiceProxy.test();
//        PackageManagerInternalProxy.install(null);
        PluginManager.getInstance().installApk(apkFile);
    }

    public static void onBase(Context base) {
        Reflection.unseal(base);
    }
}
