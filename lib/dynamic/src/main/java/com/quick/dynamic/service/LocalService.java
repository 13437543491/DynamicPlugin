package com.quick.dynamic.service;

import android.app.ActivityThread;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.quick.dynamic.delegate.ActivityManagerProxy;
import com.quick.dynamic.util.Reflector;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalService extends Service {
    private static final ArrayMap<String, ServiceInfo> sBuffer = new ArrayMap<>();
    private static final ArrayMap<String, AtomicInteger> sServiceCounters = new ArrayMap<>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            parserIntent(intent);
        }

        return START_STICKY;
    }

    private void parserIntent(Intent intent) {
        String command = intent.getStringExtra(ActivityManagerProxy.SERVICE_COMMAND);
        if (TextUtils.isEmpty(command)) {
            return;
        }

        if (ActivityManagerProxy.START_SERVICE.equals(command)) {
            startRealService(intent);
        } else if (ActivityManagerProxy.STOP_SERVICE.equals(command)) {
            stopRealService(intent);
        } else if (ActivityManagerProxy.BIND_SERVICE.equals(command)) {
            bindService(intent);
        } else if (ActivityManagerProxy.UNBIND_SERVICE.equals(command)) {
            unbindService(intent);
        }
    }

    private synchronized void startRealService(Intent intent) {
        String realClassName = intent.getStringExtra(ActivityManagerProxy.SERVICE_REAL_NAME);
        if (TextUtils.isEmpty(realClassName)) {
            throw new RuntimeException("realClassName==>" + realClassName);
        }

        Intent target = intent.getParcelableExtra(ActivityManagerProxy.SERVICE_TARGET_INTENT);

        ServiceInfo serviceInfo = sBuffer.get(realClassName);
        if (serviceInfo == null) {
            serviceInfo = createService(realClassName);
        }
        serviceInfo.isStart = true;

        Service service = serviceInfo.service;
        if (!sServiceCounters.containsKey(realClassName)) {
            sServiceCounters.put(realClassName, new AtomicInteger(0));
        }

        service.onStartCommand(target, 0, sServiceCounters.get(realClassName).getAndIncrement());
    }

    private synchronized void stopRealService(Intent intent) {
        String realClassName = intent.getStringExtra(ActivityManagerProxy.SERVICE_REAL_NAME);
        if (TextUtils.isEmpty(realClassName)) {
            throw new RuntimeException("realClassName==>" + realClassName);
        }

        ServiceInfo serviceInfo = sBuffer.get(realClassName);
        if (serviceInfo == null) {
            return;
        }
        serviceInfo.isStart = false;

        if (serviceInfo.isDie()) {
            sBuffer.remove(realClassName);
            serviceInfo.service.onDestroy();
        }
    }

    private void bindService(Intent intent) {
        String realClassName = intent.getStringExtra(ActivityManagerProxy.SERVICE_REAL_NAME);
        if (TextUtils.isEmpty(realClassName)) {
            throw new RuntimeException("realClassName==>" + realClassName);
        }

        Intent target = intent.getParcelableExtra(ActivityManagerProxy.SERVICE_TARGET_INTENT);

        ServiceInfo serviceInfo = sBuffer.get(realClassName);
        if (serviceInfo == null) {
            serviceInfo = createService(realClassName);
        } else {
            return;
        }

        serviceInfo.isBind = true;

        Service service = serviceInfo.service;
        IBinder iBinder = service.onBind(target);
        IBinder serviceConnection = intent.getExtras().getBinder(ActivityManagerProxy.SERVICE_CONNECTION);

        try {
            ComponentName componentName = target.getComponent();

            if (Build.VERSION.SDK_INT >= 26) {
                Reflector.with(serviceConnection)
                        .method("connected", ComponentName.class, IBinder.class, boolean.class)
                        .call(componentName, iBinder, false);
            } else {
                Reflector.with(serviceConnection)
                        .method("connected", ComponentName.class, IBinder.class)
                        .call(componentName, iBinder);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void unbindService(Intent intent) {
        String realClassName = intent.getStringExtra(ActivityManagerProxy.SERVICE_REAL_NAME);
        if (TextUtils.isEmpty(realClassName)) {
            throw new RuntimeException("realClassName==>" + realClassName);
        }

        Intent target = intent.getParcelableExtra(ActivityManagerProxy.SERVICE_TARGET_INTENT);

        ServiceInfo serviceInfo = sBuffer.get(realClassName);
        if (serviceInfo == null) {
            return;
        }
        serviceInfo.isBind = false;

        Service service = serviceInfo.service;
        service.onUnbind(target);

        if (serviceInfo.isDie()) {
            sBuffer.remove(realClassName);
            service.onDestroy();
        }
    }

    private ServiceInfo createService(String realClassName) {
        ServiceInfo serviceInfo = sBuffer.get(realClassName);
        if (serviceInfo == null) {
            try {
                Service service = (Service) Class.forName(realClassName).newInstance();

                Context context = ActivityThread.currentApplication();

                Method attach = service.getClass().getMethod("attach",
                        Context.class, ActivityThread.class, String.class, IBinder.class, Application.class, Object.class);
                attach.invoke(service, context, ActivityThread.currentActivityThread(), realClassName, null, context, ActivityManagerProxy.IActivityManagerProxy);
                service.onCreate();

                serviceInfo = new ServiceInfo(service);
                sBuffer.put(realClassName, serviceInfo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return serviceInfo;
    }

    private static class ServiceInfo {
        Service service;
        boolean isStart;
        boolean isBind;

        public ServiceInfo(Service service) {
            this.service = service;
        }

        public boolean isDie() {
            return !isStart && !isBind;
        }
    }
}

