package com.quick.dynamic.plugin.proxy;

import android.app.ActivityThread;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;

import com.quick.dynamic.plugin.PluginManager;
import com.quick.dynamic.util.ArrayUtil;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ActivityThreadHandlerProxy implements Handler.Callback {

    private static final int LAUNCH_ACTIVITY = 100;
    private static final int EXECUTE_TRANSACTION = 159;

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case LAUNCH_ACTIVITY:
                handleStartActivity(msg.obj);
                break;
            case EXECUTE_TRANSACTION:
                handleStartActivityAndroidP(msg.obj);
                break;
        }
        return false;
    }

    private void handleStartActivity(Object obj) {
        if (Build.VERSION.SDK_INT >= 19) {
            handleAndroid23(obj);
        }
    }

    private void handleStartActivityAndroidP(Object obj) {
        if (Build.VERSION.SDK_INT >= 28) {
            handleAndroid28(obj);
        }
    }

    private void handleAndroid23(Object obj) {
        try {
            Field intentField = obj.getClass().getDeclaredField("intent");
            intentField.setAccessible(true);
            Intent intent = (Intent) intentField.get(obj);
            if (!ActivityThreadInstrumentationProxy.isPluginIntent(intent)) {
                return;
            }

            String[] pluginApkPaths = PluginManager.getInstance().getAllPluginApkPath();

            if (Build.VERSION.SDK_INT > 19) {
                Field activityInfoField = obj.getClass().getDeclaredField("activityInfo");
                activityInfoField.setAccessible(true);
                ActivityInfo activityInfo = (ActivityInfo) activityInfoField.get(obj);
                ApplicationInfo applicationInfo = activityInfo.applicationInfo;

                Field mPackagesField = ActivityThread.class.getDeclaredField("mPackages");
                mPackagesField.setAccessible(true);
                ArrayMap arrayMap = (ArrayMap) mPackagesField.get(ActivityThread.currentActivityThread());

                WeakReference weakReference = (WeakReference) arrayMap.get(applicationInfo.packageName);
                Object loadedApk = weakReference.get();
                Field mSplitAppDirsField = loadedApk.getClass().getDeclaredField("mSplitResDirs");
                mSplitAppDirsField.setAccessible(true);
                Object mSplitResDirs = mSplitAppDirsField.get(loadedApk);

                if (mSplitResDirs == null) {
                    mSplitAppDirsField.set(loadedApk, pluginApkPaths);
                } else {
                    mSplitAppDirsField.set(loadedApk, ArrayUtil.combineArray(mSplitResDirs, pluginApkPaths));
                }
            }

            if (Build.VERSION.SDK_INT <= 23) {
                Method getInstanceMethod = Class.forName("android.app.ResourcesManager").getDeclaredMethod("getInstance");
                getInstanceMethod.setAccessible(true);
                Object resourcesManagerInstance = getInstanceMethod.invoke(null);

                Field mActiveResourcesField = Class.forName("android.app.ResourcesManager").getDeclaredField("mActiveResources");
                mActiveResourcesField.setAccessible(true);
                ArrayMap<?, WeakReference<Resources>> mActiveResources = (ArrayMap) mActiveResourcesField.get(resourcesManagerInstance);

                String originApkPath = null;
                if (Build.VERSION.SDK_INT <= 19) {
                    Field activityInfoField = obj.getClass().getDeclaredField("activityInfo");
                    activityInfoField.setAccessible(true);
                    ActivityInfo activityInfo = (ActivityInfo) activityInfoField.get(obj);

                    originApkPath = activityInfo.applicationInfo.sourceDir;
                }

                for (WeakReference<Resources> item : mActiveResources.values()) {
                    Resources resources = item.get();
                    if (resources == null) {
                        continue;
                    }

                    AssetManager assetManager = resources.getAssets();

                    if (Build.VERSION.SDK_INT <= 19) {
                        Method destroyMethod = AssetManager.class.getDeclaredMethod("destroy");
                        destroyMethod.setAccessible(true);
                        destroyMethod.invoke(assetManager);

                        Method initMethod = AssetManager.class.getDeclaredMethod("init");
                        initMethod.setAccessible(true);
                        initMethod.invoke(assetManager);

                        Field mStringBlocksField = AssetManager.class.getDeclaredField("mStringBlocks");
                        mStringBlocksField.setAccessible(true);
                        mStringBlocksField.set(assetManager, null);
                    }

                    Method addAssetPathMethod = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
                    addAssetPathMethod.setAccessible(true);
                    if (originApkPath != null) {
                        addAssetPathMethod.invoke(assetManager, originApkPath);
                    }
                    for (String apk : pluginApkPaths) {
                        addAssetPathMethod.invoke(assetManager, apk);
                    }

                    if (Build.VERSION.SDK_INT <= 19) {
                        Method ensureStringBlocksMethod = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
                        ensureStringBlocksMethod.setAccessible(true);
                        ensureStringBlocksMethod.invoke(assetManager);

                        resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAndroid28(Object obj) {
        try {
            Field mActivityCallbacksField = obj.getClass().getDeclaredField("mActivityCallbacks");
            mActivityCallbacksField.setAccessible(true);
            List list = (List) mActivityCallbacksField.get(obj);

            Class<?> launcherActivityItemClass = Class.forName("android.app.servertransaction.LaunchActivityItem");

            for (Object item : list) {
                if (item.getClass() == launcherActivityItemClass) {
                    Field mIntentField = launcherActivityItemClass.getDeclaredField("mIntent");
                    mIntentField.setAccessible(true);
                    Intent intent = (Intent) mIntentField.get(item);
                    if (!ActivityThreadInstrumentationProxy.isPluginIntent(intent)) {
                        return;
                    }

                    Field mInfoField = launcherActivityItemClass.getDeclaredField("mInfo");
                    mInfoField.setAccessible(true);
                    ActivityInfo activityInfo = (ActivityInfo) mInfoField.get(item);
                    ApplicationInfo applicationInfo = activityInfo.applicationInfo;

                    Field mPackagesField = ActivityThread.class.getDeclaredField("mPackages");
                    mPackagesField.setAccessible(true);
                    ArrayMap arrayMap = (ArrayMap) mPackagesField.get(ActivityThread.currentActivityThread());

                    WeakReference weakReference = (WeakReference) arrayMap.get(applicationInfo.packageName);
                    Object loadedApk = weakReference.get();
                    Field mSplitAppDirsField = loadedApk.getClass().getDeclaredField("mSplitResDirs");
                    mSplitAppDirsField.setAccessible(true);
                    Object mSplitResDirs = mSplitAppDirsField.get(loadedApk);

                    String[] pluginApkPaths = PluginManager.getInstance().getAllPluginApkPath();
                    if (mSplitResDirs == null) {
                        mSplitAppDirsField.set(loadedApk, pluginApkPaths);
                    } else {
                        mSplitAppDirsField.set(loadedApk, ArrayUtil.combineArray(mSplitResDirs, pluginApkPaths));
                    }

                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void install(Context context) {
        ActivityThread activityThread = ActivityThread.currentActivityThread();

        try {
            Field mHField = activityThread.getClass().getDeclaredField("mH");
            mHField.setAccessible(true);
            Handler handler = (Handler) mHField.get(activityThread);

            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            mCallbackField.set(handler, new ActivityThreadHandlerProxy());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
