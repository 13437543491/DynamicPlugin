package com.quick.dynamic.delegate;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageParser;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Window;

import com.quick.dynamic.plugin.PluginManager;
import com.quick.dynamic.util.Reflector;

import java.lang.reflect.Field;
import java.util.Set;

public final class InstrumentationProxy extends Instrumentation {

    private final Instrumentation mBase;

    private InstrumentationProxy(Instrumentation instrumentation) {
        this.mBase = instrumentation;
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        replaceIntent(intent);

        try {
            return Reflector.with(mBase)
                    .method("execStartActivity", Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class, int.class, Bundle.class)
                    .call(who, contextThread, token, target, intent, requestCode, options);
        } catch (Reflector.ReflectedException e) {
            throw new RuntimeException(e);
        }
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode) {
        replaceIntent(intent);

        try {
            return Reflector.with(mBase)
                    .method("execStartActivity", Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class, int.class)
                    .call(who, contextThread, token, target, intent, requestCode);
        } catch (Reflector.ReflectedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String realName = getCategoryName(intent);
        if (realName != null) {
            className = realName;
        }

        return mBase.newActivity(cl, className, intent);
    }

    @Override
    public void callActivityOnCreate(Activity activity, android.os.Bundle icicle) {
        String realName = getCategoryName(activity.getIntent());
        if (realName != null) {
            AssetManager newAssetManager = activity.getApplication().getAssets();
            Resources resources = activity.getResources();

            try {
                Field mResourcesImpl = Resources.class.getDeclaredField("mResourcesImpl");
                mResourcesImpl.setAccessible(true);
                Object resourceImpl = mResourcesImpl.get(resources);
                Field implAssets = resourceImpl.getClass().getDeclaredField("mAssets");
                implAssets.setAccessible(true);
                implAssets.set(resourceImpl, newAssetManager);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ActivityInfo activityInfo = PluginManager.getInstance().getActivityInfo(activity.getClass().getName());
            try {
                Reflector.with(activity).field("mActivityInfo").set(activityInfo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            Window window = activity.getWindow();
            window.setSoftInputMode(activityInfo.softInputMode);
            activity.setRequestedOrientation(activityInfo.screenOrientation);
        }

        mBase.callActivityOnCreate(activity, icicle);
    }

    @Override
    public void callActivityOnSaveInstanceState(Activity activity, android.os.Bundle outState) {
        mBase.callActivityOnSaveInstanceState(activity, outState);
    }

    @Override
    public void callActivityOnRestoreInstanceState(Activity activity, android.os.Bundle savedInstanceState) {
        mBase.callActivityOnRestoreInstanceState(activity, savedInstanceState);
    }

    @Override
    public void callActivityOnStop(Activity activity) {
        mBase.callActivityOnStop(activity);
    }

    @Override
    public void callActivityOnDestroy(Activity activity) {
        mBase.callActivityOnDestroy(activity);
    }

    private void replaceIntent(Intent intent) {
        ComponentName component = intent.getComponent();
        PackageParser.Activity activity = null;
        if (component == null) {
            component = intent.resolveActivity(ActivityThread.currentApplication().getPackageManager());

            if (component != null) {
                return;
            }

            activity = PluginManager.getInstance().resolveActivity(intent);
            if (activity == null) {
                return;
            }
        } else {
            activity = PluginManager.getInstance().resolveActivity(component);
        }

        String activityClassName = component.getClassName();
        intent.addCategory(PLUGIN_ACTIVITY_PREFIX + activityClassName);

        String stubClazz = PluginManager.getInstance().dispatchStubActivity(activity);
        intent.setComponent(new ComponentName(ActivityThread.currentApplication(), stubClazz));
    }

    public static void hook() {
        ActivityThread thread = ActivityThread.currentActivityThread();
        try {
            Instrumentation baseInstrumentation = Reflector.with(thread).field("mInstrumentation").get();
            Reflector.with(thread).field("mInstrumentation").set(new InstrumentationProxy(baseInstrumentation));
        } catch (Reflector.ReflectedException e) {
            throw new RuntimeException(e);
        }
    }

    private static final char PLUGIN_ACTIVITY_PREFIX = '@';

    public static String getCategoryName(Intent intent) {
        Set<String> categories = intent.getCategories();
        if (categories != null) {
            for (String category : categories) {
                if (category.charAt(0) == PLUGIN_ACTIVITY_PREFIX) {
                    return category.substring(1);
                }
            }
        }
        return null;
    }
}
