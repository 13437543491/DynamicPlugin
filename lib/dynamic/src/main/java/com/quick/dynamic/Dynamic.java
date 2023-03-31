package com.quick.dynamic;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.webkit.WebView;

import com.quick.dynamic.plugin.PluginManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Dynamic {

    public static void onCreate(Context context) {
        PluginManager.getInstance().onCreate(context);
    }

    public static Application getApp() {
        return ActivityThread.currentApplication();
    }

    public static void installApk(File apkFile, OnInstallListener listener) {
        PluginManager.getInstance().install(apkFile, listener);
    }

    public static void startPluginLaunchActivity(Context context, String packageName) {
        PluginManager.getInstance().startLaunchActivity(context, packageName);
    }
}
