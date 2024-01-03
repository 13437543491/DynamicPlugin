package com.quick.dynamic.plugin.proxy;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageParser;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.quick.dynamic.plugin.PluginManager;

public class ActivityStackManager {

    private static ActivityStackManager mInstance;

    public synchronized static ActivityStackManager getInstance() {
        if (mInstance == null) {
            mInstance = new ActivityStackManager();
        }
        return mInstance;
    }

    private int mSingleTopIndex = 0;
    private final ArrayMap<String, String> mSingleTopBuffer = new ArrayMap<>();

    private int mSingleTaskIndex = 0;
    private final ArrayMap<String, String> mSingleTaskBuffer = new ArrayMap<>();

    private int mSingleInstanceIndex = 0;
    private final ArrayMap<String, String> mSingleInstanceBuffer = new ArrayMap<>();

    private String mTopClassName;

    private ActivityStackManager() {

    }

    public void onActivityCreate(String className) {
        this.mTopClassName = className;
    }

    public String getProxyActivity(Intent intent) {
        PackageParser.Activity activity = PluginManager.getInstance().getPackageActivity(intent);
        if (activity == null) {
            return null;
        }

        ActivityInfo activityInfo = activity.info;
        switch (activityInfo.launchMode) {
            case ActivityInfo.LAUNCH_MULTIPLE: {
                return getStandardActivity(activity);
            }
            case ActivityInfo.LAUNCH_SINGLE_TOP: {
                return getSingleTopActivity(activity);
            }
            case ActivityInfo.LAUNCH_SINGLE_TASK: {
                return getSingleTaskActivity(activity);
            }
            case ActivityInfo.LAUNCH_SINGLE_INSTANCE: {
                return getSingleInstanceActivity(activity);
            }
            default:
                return null;
        }
    }

    private String getStandardActivity(PackageParser.Activity activity) {
        return "com.quick.dynamic.plugin.A";
    }

    private String getSingleTopActivity(PackageParser.Activity activity) {
        String className = activity.info.name;
        String bufferName = mSingleTopBuffer.get(className);
        if (!TextUtils.isEmpty(bufferName) && className.equals(mTopClassName)) {
            return bufferName;
        }

        String result = "com.quick.dynamic.plugin.B" + mSingleTopIndex;
        mSingleTopIndex++;
        if (mSingleTopIndex > 4) {
            mSingleTopIndex = 0;
        }
        mSingleTopBuffer.put(className, result);
        return result;
    }

    private String getSingleTaskActivity(PackageParser.Activity activity) {
        String className = activity.info.name;
        String bufferName = mSingleTaskBuffer.get(className);
        if (!TextUtils.isEmpty(bufferName)) {
            return bufferName;
        }

        String result = "com.quick.dynamic.plugin.C" + mSingleTaskIndex;
        mSingleTaskIndex++;
        if (mSingleTaskIndex > 4) {
            mSingleTaskIndex = 0;
        }
        mSingleTaskBuffer.put(className, result);
        return result;
    }

    private String getSingleInstanceActivity(PackageParser.Activity activity) {
        String className = activity.info.name;
        String bufferName = mSingleInstanceBuffer.get(className);
        if (!TextUtils.isEmpty(bufferName)) {
            return bufferName;
        }

        String result = "com.quick.dynamic.plugin.D" + mSingleInstanceIndex;
        mSingleInstanceIndex++;
        if (mSingleInstanceIndex > 4) {
            mSingleInstanceIndex = 0;
        }
        mSingleInstanceBuffer.put(className, result);
        return result;
    }

    public void onActivityDestroy(Intent intent) {
        PackageParser.Activity activity = PluginManager.getInstance().getPackageActivity(intent);
        if (activity == null) {
            return;
        }

        ActivityInfo activityInfo = activity.info;
        switch (activityInfo.launchMode) {
            case ActivityInfo.LAUNCH_SINGLE_TOP:
                mSingleTopBuffer.remove(activityInfo.name);
                break;
            case ActivityInfo.LAUNCH_SINGLE_TASK:
                mSingleTaskBuffer.remove(activityInfo.name);
                break;
            case ActivityInfo.LAUNCH_SINGLE_INSTANCE:
                mSingleInstanceBuffer.remove(activityInfo.name);
                break;
        }
    }
}
