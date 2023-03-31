package com.quick.dynamic.delegate;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageParser;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Singleton;

import com.quick.dynamic.plugin.PluginManager;
import com.quick.dynamic.service.LocalService;
import com.quick.dynamic.util.Reflector;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ActivityManagerProxy implements InvocationHandler {
    public static final String START_SERVICE = "startService";
    public static final String BIND_SERVICE = "bindService";
    public static final String STOP_SERVICE = "stopService";
    public static final String UNBIND_SERVICE = "unbindService";
    public static final String SERVICE_COMMAND = "service_command";
    public static final String SERVICE_REAL_NAME = "service_real_name";
    public static final String SERVICE_TARGET_INTENT = "target_intent";
    public static final String SERVICE_CONNECTION = "service_connection";

    public static Object IActivityManagerProxy;

    private final Object mOrigin;
    private final ArrayMap<IBinder, ServiceCache> mServiceCache = new ArrayMap<>();

    ActivityManagerProxy(Object origin) {
        this.mOrigin = origin;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("startService".equals(method.getName())) {
            return startService(proxy, method, args);
        } else if ("stopService".equals(method.getName())) {
            return stopService(proxy, method, args);
        } else if ("bindService".equals(method.getName()) || "bindIsolatedService".equals(method.getName())) {
            return bindService(proxy, method, args);
        } else if ("unbindService".equals(method.getName())) {
            return unbindService(proxy, method, args);
        }

        return method.invoke(mOrigin, args);
    }

    private Object startService(Object proxy, Method method, Object[] args) throws Throwable {
        Intent target = null;

        for (Object item : args) {
            if (item instanceof Intent) {
                target = (Intent) item;
                break;
            }
        }

        if (target != null) {
            PackageParser.Service service = PluginManager.getInstance().resolveService(target);
            if (service != null) {
                String processName = service.info.processName;
                boolean isLocal = TextUtils.isEmpty(processName) || service.info.packageName.equals(processName);

                Context context = ActivityThread.currentApplication();
                if (isLocal) {
                    Intent intent = new Intent(context, LocalService.class);
                    intent.putExtra(SERVICE_TARGET_INTENT, target);
                    intent.putExtra(SERVICE_COMMAND, START_SERVICE);
                    intent.putExtra(SERVICE_REAL_NAME, service.info.name);
                    args[1] = intent;
                }
            }
        }

        return method.invoke(mOrigin, args);
    }

    private Object stopService(Object proxy, Method method, Object[] args) throws Throwable {
        Intent target = null;

        for (Object item : args) {
            if (item instanceof Intent) {
                target = (Intent) item;
                break;
            }
        }

        if (target != null) {
            PackageParser.Service service = PluginManager.getInstance().resolveService(target);
            if (service != null) {
                String processName = service.info.processName;
                boolean isLocal = TextUtils.isEmpty(processName) || service.info.packageName.equals(processName);

                Context context = ActivityThread.currentApplication();
                if (isLocal) {
                    Intent intent = new Intent(context, LocalService.class);
                    intent.putExtra(SERVICE_TARGET_INTENT, target);
                    intent.putExtra(SERVICE_COMMAND, STOP_SERVICE);
                    intent.putExtra(SERVICE_REAL_NAME, service.info.name);
                    context.startService(intent);
                    return 1;
                }
            }
        }

        return method.invoke(mOrigin, args);
    }

    private Object bindService(Object proxy, Method method, Object[] args) throws Throwable {
        Intent target = null;

        for (Object item : args) {
            if (item instanceof Intent) {
                target = (Intent) item;
                break;
            }
        }

        if (target != null) {
            PackageParser.Service service = PluginManager.getInstance().resolveService(target);
            if (service != null) {
                IBinder IServiceConnection = null;
                for (Object item : args) {
                    if (item != null && item.getClass().getName().equals("android.app.LoadedApk$ServiceDispatcher$InnerConnection")) {
                        IServiceConnection = (IBinder) item;
                        break;
                    }
                }

                if (IServiceConnection == null) {
                    throw new RuntimeException();
                }

                String processName = service.info.processName;
                boolean isLocal = TextUtils.isEmpty(processName) || service.info.packageName.equals(processName);

                Context context = ActivityThread.currentApplication();
                if (isLocal) {
                    Intent intent = new Intent(context, LocalService.class);
                    intent.putExtra(SERVICE_TARGET_INTENT, target);
                    intent.putExtra(SERVICE_COMMAND, BIND_SERVICE);
                    intent.putExtra(SERVICE_REAL_NAME, service.info.name);

                    Bundle bundle = new Bundle();
                    bundle.putBinder(SERVICE_CONNECTION, IServiceConnection);
                    intent.putExtras(bundle);

                    context.startService(intent);

                    ServiceCache cache = new ServiceCache();
                    cache.targetIntent = target;
                    cache.realName = service.info.name;
                    mServiceCache.put(IServiceConnection, cache);
                    return 1;
                }
            }
        }

        return method.invoke(mOrigin, args);
    }

    private Object unbindService(Object proxy, Method method, Object[] args) throws Throwable {
        IBinder iServiceConnection = (IBinder) args[0];
        ServiceCache serviceCache = mServiceCache.remove(iServiceConnection);
        if (serviceCache == null) {
            return method.invoke(mOrigin, args);
        }

        Context context = ActivityThread.currentApplication();

        Intent intent = new Intent(context, LocalService.class);
        intent.putExtra(SERVICE_COMMAND, UNBIND_SERVICE);
        intent.putExtra(SERVICE_TARGET_INTENT, serviceCache.targetIntent);
        intent.putExtra(SERVICE_REAL_NAME, serviceCache.realName);

        Bundle bundle = new Bundle();
        bundle.putBinder(SERVICE_CONNECTION, iServiceConnection);
        intent.putExtras(bundle);

        context.startService(intent);
        return true;
    }

    private static class ServiceCache {
        Intent targetIntent;
        String realName;
    }

    public static void hook() {
        Context mContext = ActivityThread.currentApplication();

        try {
            Singleton<Object> gDefault;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gDefault = Reflector.on(ActivityManager.class).field("IActivityManagerSingleton").get();
            } else {
                gDefault = Reflector.on(ActivityManagerNative.class).field("gDefault").get();
            }

            Object origin = gDefault.get();

            Class IActivityManagerClz = Class.forName("android.app.IActivityManager");
            IActivityManagerProxy = Proxy.newProxyInstance(mContext.getClassLoader(), new Class[]{IActivityManagerClz}, new ActivityManagerProxy(origin));
            Reflector.with(gDefault).field("mInstance").set(IActivityManagerProxy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
