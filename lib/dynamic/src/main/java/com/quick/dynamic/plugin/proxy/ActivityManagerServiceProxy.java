package com.quick.dynamic.plugin.proxy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.util.Singleton;

import com.quick.dynamic.plugin.PluginManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ActivityManagerServiceProxy implements InvocationHandler {

    static {
        System.loadLibrary("myapplication");
    }

    public static native void test();

    public static String abc = "111";

    public static Context sContext;

    public static void test2() {
        Log.e("yangyangyang", "myPid3==>" + android.os.Process.myPid());
//
//        Log.e("yangyangyang", "abc==>" + abc);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.quick.plugin", "com.quick.plugin.MainActivity"));
        sContext.startActivity(intent);
    }

    private Object mOrigin;

    public ActivityManagerServiceProxy(Object origin) {
        this.mOrigin = origin;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("startActivity".equals(method.getName())) {
            startActivity(args);
        }
        return method.invoke(mOrigin, args);
    }

    private void startActivity(Object[] args) {
        Intent intent = null;
        for (Object item : args) {
            if (item instanceof Intent) {
                intent = (Intent) item;
                break;
            }
        }

        if (intent == null) {
            return;
        }

        ActivityThreadInstrumentationProxy.decorIntent(intent);
    }

    public static void install(Context context) {
        sContext = context;
        try {
            Singleton<?> singleton;
            if (Build.VERSION.SDK_INT >= 26) {
                Field IActivityManagerSingletonField = Class.forName("android.app.ActivityManager")
                        .getDeclaredField("IActivityManagerSingleton");
                IActivityManagerSingletonField.setAccessible(true);
                singleton = (Singleton<?>) IActivityManagerSingletonField.get(null);
            } else {
                Field gDefaultField = Class.forName("android.app.ActivityManagerNative")
                        .getDeclaredField("gDefault");
                gDefaultField.setAccessible(true);
                singleton = (Singleton<?>) gDefaultField.get(null);
            }

            Field mInstanceField = Class.forName("android.util.Singleton")
                    .getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            Object mInstance = mInstanceField.get(singleton);

            Class iActivityManagerClass = Class.forName("android.app.IActivityManager");
            Object amsProxy = Proxy.newProxyInstance(context.getClassLoader(),
                    new Class[]{iActivityManagerClass},
                    new ActivityManagerServiceProxy(mInstance));
            mInstanceField.set(singleton, amsProxy);
        } catch (Exception e) {
            Log.e("yangyangyang", "eee==>" + e.toString());
            e.printStackTrace();
        }
    }
}
