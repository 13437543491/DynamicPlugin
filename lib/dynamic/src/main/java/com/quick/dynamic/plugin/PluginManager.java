package com.quick.dynamic.plugin;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.os.Build;
import android.util.ArrayMap;
import android.util.DisplayMetrics;

import com.quick.dynamic.plugin.proxy.ActivityManagerServiceProxy;
import com.quick.dynamic.plugin.proxy.ActivityThreadHandlerProxy;
import com.quick.dynamic.plugin.proxy.ActivityThreadInstrumentationProxy;
import com.quick.dynamic.plugin.proxy.PackageManagerInternalProxy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PluginManager {

    private static PluginManager sInstance;

    public synchronized static PluginManager getInstance() {
        if (sInstance == null) {
            sInstance = new PluginManager();
        }
        return sInstance;
    }

    private PluginManager() {

    }

    private boolean mIsInit;
    private Context mHostContext;
    private ClassFix mClassFix;
    private ArrayMap<String, PluginItem> mPluginBuffer;

    public void onCreate(Context context) {
        if (mIsInit) {
            return;
        }
//        check();
        this.mHostContext = context;
        this.mClassFix = new ClassFix();
        this.mPluginBuffer = new ArrayMap<>();

        ActivityManagerServiceProxy.install(context);
        ActivityThreadInstrumentationProxy.install(context);
        ActivityThreadHandlerProxy.install(context);
        PackageManagerInternalProxy.install(context);

        mIsInit = true;
    }

    public void installApk(File apkFile) {
        this.mClassFix.fix(mHostContext, apkFile);

        PackageParser packageParser;
        PackageParser.Package pp;

        if (Build.VERSION.SDK_INT > 19) {
            packageParser = new PackageParser();
            pp = packageParser.parsePackage(apkFile, 0);
        } else {
            String scanPath = apkFile.getPath();
            DisplayMetrics metrics = mHostContext.getResources().getDisplayMetrics();
            packageParser = new PackageParser(scanPath);
            pp = packageParser.parsePackage(apkFile, scanPath, metrics, 0);
        }

        for (PackageParser.Activity activity : pp.receivers) {
            try {
                BroadcastReceiver receiver = (BroadcastReceiver) Class.forName(activity.info.name).newInstance();
                for (PackageParser.IntentInfo intentInfo : activity.intents) {
                    mHostContext.registerReceiver(receiver, intentInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        mPluginBuffer.put(pp.packageName, new PluginItem(apkFile, pp));
    }

    public boolean isPluginActivity(Intent intent) {
        PluginItem pluginItem = mPluginBuffer.get(intent.getPackage());
        if (pluginItem != null) {
            List<PluginItem> list = new ArrayList<>();
            list.add(pluginItem);
            return getPackageActivity(list, intent) != null;
        } else {
            return getPackageActivity(mPluginBuffer.values(), intent) != null;
        }
    }

    public PackageParser.Activity getPackageActivity(Intent intent) {
        return getPackageActivity(mPluginBuffer.values(), intent);
    }

    public PackageParser.Activity getPackageActivity(Collection<PluginItem> items, Intent intent) {
        ContentResolver contentResolver = mHostContext.getContentResolver();
        ComponentName componentName = intent.getComponent();
        for (PluginItem item : items) {
            List<PackageParser.Activity> activities = item.mPackage.activities;
            for (PackageParser.Activity activity : activities) {
                if (componentName != null) {
                    if (componentName.equals(activity.getComponentName())) {
                        return activity;
                    }
                    if (componentName.getClassName().equals(activity.getComponentName().getClassName())) {
                        return activity;
                    }
                } else {
                    for (PackageParser.ActivityIntentInfo intentInfo : activity.intents) {
                        if (intentInfo.match(contentResolver, intent, true, "plugin") > 0) {
                            return activity;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void check() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (StackTraceElement item : elements) {
            if (item.getClassName().equals("") && item.getClassName().equals("")) {
                return;
            }
        }
        throw new RuntimeException("请在Application的onCreate方法调用");
    }

    public File[] getAllPluginApkFile() {
        if (mPluginBuffer.isEmpty()) {
            return null;
        }

        Collection<PluginItem> collection = mPluginBuffer.values();
        File[] result = new File[collection.size()];
        int index = 0;
        for (PluginItem item : collection) {
            result[index] = item.mApkFile;
            index++;
        }
        return result;
    }

    public String[] getAllPluginApkPath() {
        File[] files = getAllPluginApkFile();
        if (files == null) {
            return null;
        }

        String[] paths = new String[files.length];
        for (int i = 0; i < paths.length; i++) {
            paths[i] = files[i].getAbsolutePath();
        }
        return paths;
    }

    public ApplicationInfo getApplicationInfo(String pkg) {
        if (mPluginBuffer.isEmpty()) {
            return null;
        }

        PluginItem item = mPluginBuffer.get(pkg);
        if (item == null) {
            return null;
        }
        return item.mPackage.applicationInfo;
    }

    private static class PluginItem {
        public File mApkFile;
        public PackageParser.Package mPackage;

        public PluginItem(File apkFile, PackageParser.Package pp) {
            this.mApkFile = apkFile;
            this.mPackage = pp;
        }
    }
}
