package com.quick.dynamic.delegate;

import android.app.ActivityThread;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;

import com.quick.dynamic.plugin.PluginManager;
import com.quick.dynamic.util.Reflector;

import java.util.List;

public final class HandlerCallbackProxy implements Handler.Callback {

    private HandlerCallbackProxy() {

    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 100:
                replaceActivityInfo100(msg);
                break;
            case 159:
                replaceActivityInfo159(msg);
                break;
        }
        return false;
    }

    private void replaceActivityInfo100(Message message) {
        try {
            Object obj = message.obj;

            Intent intent = Reflector.with(obj).field("intent").get();
            replaceActivityInfo(intent, obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void replaceActivityInfo159(Message message) {
        try {
            Object obj = message.obj;

            List callbacks = Reflector.with(obj).field("mActivityCallbacks").get();
            if (callbacks == null) return;

            for (Object item : callbacks) {
                if (!item.getClass().getName().equals("android.app.servertransaction.LaunchActivityItem")) {
                    continue;
                }

                Intent intent = Reflector.with(item).field("mIntent").get();
                replaceActivityInfo(intent, item);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void replaceActivityInfo(Intent intent, Object obj) {
        String realName = InstrumentationProxy.getCategoryName(intent);
        if (realName != null) {
            ActivityInfo activityInfo = PluginManager.getInstance().getActivityInfo(realName);
            if (activityInfo == null) {
                throw new RuntimeException("realName==>" + realName);
            }
            try {
                if (obj.getClass().getName().equals("android.app.servertransaction.LaunchActivityItem")) {
                    Reflector.with(obj).field("mInfo").set(activityInfo);
                } else {
                    Reflector.with(obj).field("activityInfo").set(activityInfo);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void hook() {
        ActivityThread thread = ActivityThread.currentActivityThread();
        try {
            Handler handler = Reflector.with(thread).field("mH").get();
            Reflector.with(handler).field("mCallback").set(new HandlerCallbackProxy());
        } catch (Reflector.ReflectedException e) {
            throw new RuntimeException(e);
        }
    }
}
