package com.quick.dynamic.plugin.proxy;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import com.quick.dynamic.plugin.PluginManager;
import java.lang.reflect.Field;

public class ActivityThreadInstrumentationProxy extends Instrumentation {

    private static final String PLUGIN_ACTIVITY_FLAG = "isPlugin";
    private static final String PLUGIN_PACKAGE_NAME = "pluginPkg";
    private static final String PLUGIN_ACTIVITY_CLASS = "pluginClass";

    @Override
    public Activity newActivity(Class<?> clazz, Context context, IBinder token, Application application, Intent intent, ActivityInfo info, CharSequence title, Activity parent, String id, Object lastNonConfigurationInstance) throws IllegalAccessException, InstantiationException {
        return super.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance);
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return super.newApplication(cl, className, context);
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (intent.getBooleanExtra(PLUGIN_ACTIVITY_FLAG, false)) {
            className = intent.getStringExtra(PLUGIN_ACTIVITY_CLASS);
        }
        return super.newActivity(cl, className, intent);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        ActivityStackManager.getInstance().onActivityCreate(activity.getClass().getName());
        super.callActivityOnCreate(activity, icicle);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        super.callActivityOnCreate(activity, icicle, persistentState);
    }

    @Override
    public void callActivityOnDestroy(Activity activity) {
        ActivityStackManager.getInstance().onActivityDestroy(activity.getIntent());
        super.callActivityOnDestroy(activity);
    }

    public static void decorIntent(Intent intent) {
        if (PluginManager.getInstance().isPluginActivity(intent)) {
            String pluginActivity = ActivityStackManager.getInstance().getProxyActivity(intent);

            ComponentName oldComponentName = intent.getComponent();
            ComponentName newComponentName =
                    new ComponentName("com.quick.plugin", pluginActivity);
            intent.setComponent(newComponentName);
            intent.putExtra(PLUGIN_ACTIVITY_FLAG, true);
            intent.putExtra(PLUGIN_PACKAGE_NAME, oldComponentName.getPackageName());
            intent.putExtra(PLUGIN_ACTIVITY_CLASS, oldComponentName.getClassName());
        }
    }

    public static boolean isPluginIntent(Intent intent) {
        return intent.getBooleanExtra(PLUGIN_ACTIVITY_FLAG, false);
    }

    public static String getPluginPackageName(Intent intent) {
        return intent.getStringExtra(PLUGIN_PACKAGE_NAME);
    }

    public static String getPluginActivityClass(Intent intent) {
        return intent.getStringExtra(PLUGIN_ACTIVITY_CLASS);
    }

    public static void install(Context context) {
        ActivityThread activityThread = ActivityThread.currentActivityThread();

        try {
            Field mInstrumentationField = activityThread.getClass().getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
            mInstrumentationField.set(activityThread, new ActivityThreadInstrumentationProxy());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
