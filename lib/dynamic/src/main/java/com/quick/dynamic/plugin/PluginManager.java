package com.quick.dynamic.plugin;

import android.app.ActivityThread;
import android.app.ResourcesManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageParser;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.Log;
import android.webkit.WebView;

import com.quick.dynamic.OnInstallListener;
import com.quick.dynamic.delegate.ActivityManagerProxy;
import com.quick.dynamic.delegate.HandlerCallbackProxy;
import com.quick.dynamic.delegate.InstrumentationProxy;
import com.quick.dynamic.util.Reflector;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

public class PluginManager implements Runnable {

    private static final String PACKAGE_NAME = PluginManager.class.getPackage().getName();
    private static final String STUB_ACTIVITY_STANDARD = PACKAGE_NAME + ".A";
    private static final String STUB_ACTIVITY_STANDARD_TRANSLUCENT = STUB_ACTIVITY_STANDARD + '1';
    private static final PluginManager sInstance = new PluginManager();

    public static PluginManager getInstance() {
        return sInstance;
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final Map<String, PluginApk> mApkList = new HashMap<>();

    private final LinkedBlockingQueue<ParserTask> mTaskList = new LinkedBlockingQueue<>();

    private PluginManager() {
        new Thread(this).start();
    }

    public void install(File apkFile, final OnInstallListener listener) {
        ParserTask task = new ParserTask(apkFile, mApkList, new OnParserListener() {
            @Override
            public void onFinish(final PluginApk pluginApk) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFinish(pluginApk.packageName);
                    }
                });
            }
        });
        mTaskList.add(task);
    }

    public void startLaunchActivity(Context context, String packageName) {
        if (!mApkList.containsKey(packageName)) {
            throw new RuntimeException("插件未安装");
        }

        for (PluginApk apk : mApkList.values()) {
            for (PackageParser.Activity activity : apk.activities) {
                for (IntentFilter intentFilter : activity.intents) {
                    if (intentFilter.hasCategory(Intent.CATEGORY_LAUNCHER) && intentFilter.hasAction(Intent.ACTION_MAIN)) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(context, activity.info.name));
                        context.startActivity(intent);
                        return;
                    }
                }
            }
        }

        throw new RuntimeException("获取不到启动的activity");
    }

    @Override
    public void run() {
        while (true) {
            try {
                mTaskList.take().run();
            } catch (InterruptedException e) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
        }
    }

    public PackageParser.Activity resolveActivity(Intent intent) {
        ComponentName componentName = intent.getComponent();
        for (PluginApk apk : mApkList.values()) {
            for (PackageParser.Activity activity : apk.activities) {
                if (componentName != null) {
                    if (activity.getComponentName().getClassName().equals(componentName.getClassName())) {
                        return activity;
                    }
                    continue;
                }

                for (IntentFilter intentFilter : activity.intents) {
                    if (intentFilter.hasCategory(Intent.CATEGORY_DEFAULT) && intentFilter.hasAction(intent.getAction())) {
                        return activity;
                    }
                }
            }
        }

        return null;
    }

    public PackageParser.Activity resolveActivity(ComponentName componentName) {
        for (PluginApk apk : mApkList.values()) {
            for (PackageParser.Activity activity : apk.activities) {
                if (activity.getComponentName().getClassName().equals(componentName.getClassName())) {
                    return activity;
                }
            }
        }
        return null;
    }

    public PackageParser.Service resolveService(Intent intent) {
        ComponentName targetComponentName = intent.getComponent();

        for (PluginApk apk : mApkList.values()) {
            for (PackageParser.Service service : apk.services) {
                if (targetComponentName != null) {
                    if (service.getComponentName().getClassName().equals(targetComponentName.getClassName())) {
                        return service;
                    }
                    continue;
                }

                for (IntentFilter intentFilter : service.intents) {
                    if (intentFilter.hasAction(intent.getAction())) {
                        return service;
                    }
                }
            }
        }
        return null;
    }

    public String dispatchStubActivity(PackageParser.Activity activity) {
        ActivityInfo activityInfo = activity.info;

        Resources.Theme theme = ActivityThread.currentApplication().getResources().newTheme();
        theme.applyStyle(activityInfo.getThemeResource(), true);
        TypedArray sa = theme.obtainStyledAttributes(
                new int[]{android.R.attr.windowIsTranslucent});
        boolean windowIsTranslucent = sa.getBoolean(0, false);
        sa.recycle();

        switch (activityInfo.launchMode) {
            case ActivityInfo.LAUNCH_MULTIPLE: {
                return windowIsTranslucent ? STUB_ACTIVITY_STANDARD_TRANSLUCENT : STUB_ACTIVITY_STANDARD;
            }
            case ActivityInfo.LAUNCH_SINGLE_TOP: {

                break;
            }
            case ActivityInfo.LAUNCH_SINGLE_TASK: {

                break;
            }
            case ActivityInfo.LAUNCH_SINGLE_INSTANCE: {

                break;
            }

            default:
                break;
        }

        return "";
    }

    public ActivityInfo getActivityInfo(String name) {
        for (PluginApk apk : mApkList.values()) {
            for (PackageParser.Activity activity : apk.activities) {
                if (activity.getComponentName().getClassName().equals(name)) {
                    return activity.info;
                }
            }
        }

        return null;
    }

    public void onCreate(Context context) {
        new WebView(context);
        InstrumentationProxy.hook();
        HandlerCallbackProxy.hook();
        ActivityManagerProxy.hook();
    }

    private static final class ParserTask implements Runnable {
        private final File mApkFile;
        private final Map<String, PluginApk> mApkList;
        private final OnParserListener mListener;
        private final Context mContext = ActivityThread.currentApplication();

        ParserTask(File apkFile, Map<String, PluginApk> apkList, OnParserListener listener) {
            this.mApkFile = apkFile;
            this.mApkList = apkList;
            this.mListener = listener;
        }

        @Override
        public void run() {
            parserPackage();
        }

        private void parserPackage() {
            PackageParser parser;
            PackageParser.Package pkg;
            int flags = PackageParser.PARSE_MUST_BE_APK;
            if (Build.VERSION.SDK_INT > 19) {
                parser = new PackageParser();
                pkg = parser.parsePackage(mApkFile, flags);
            } else {
                parser = new PackageParser(mApkFile.getAbsolutePath());
                pkg = parser.parsePackage(mApkFile, mApkFile.getAbsolutePath(), mContext.getResources().getDisplayMetrics(), flags);
            }

            PluginApk pluginApk = new PluginApk();
            pluginApk.apkFile = mApkFile;
            pluginApk.packageName = pkg.applicationInfo.packageName;
            pluginApk.applicationInfo = pkg.applicationInfo;
            pluginApk.activities = pkg.activities;
            pluginApk.receivers = pkg.receivers;
            pluginApk.services = pkg.services;

            for (PackageParser.Activity activity : pluginApk.activities) {
                ActivityInfo activityInfo = activity.info;
                activityInfo.applicationInfo.sourceDir = mApkFile.getAbsolutePath();
                activityInfo.applicationInfo.packageName = mContext.getPackageName();
                activityInfo.packageName = mContext.getPackageName();
            }

            for (PackageParser.Activity receiver : pluginApk.receivers) {
                ActivityInfo activityInfo = receiver.info;
                activityInfo.applicationInfo.sourceDir = mApkFile.getAbsolutePath();
                activityInfo.applicationInfo.packageName = mContext.getPackageName();
                activityInfo.packageName = mContext.getPackageName();
            }

            for (PackageParser.Service service : pluginApk.services) {
                service.info.applicationInfo.sourceDir = mApkFile.getAbsolutePath();
                service.info.packageName = mContext.getPackageName();
                service.info.applicationInfo.packageName = mContext.getPackageName();
            }

            mApkList.put(pluginApk.packageName, pluginApk);
            mergerClass();
            mergerResources();
            try {
                new SoFix().fix(mContext, mApkFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<PackageParser.Activity> receivers = pluginApk.receivers;
            if (receivers != null) {
                for (PackageParser.Activity receiver : receivers) {
                    if (receiver.intents == null) {
                        continue;
                    }

                    try {
                        BroadcastReceiver br = (BroadcastReceiver) Class.forName(receiver.getComponentName().getClassName()).newInstance();
                        for (PackageParser.ActivityIntentInfo aii : receiver.intents) {
                            mContext.registerReceiver(br, aii);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            mListener.onFinish(pluginApk);
        }

        private void mergerResources() {
            AssetManager assetManager;
            try {
                assetManager = AssetManager.class.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            String sourceDir = mContext.getApplicationInfo().sourceDir;

            try {
                if (Build.VERSION.SDK_INT <= 19) {
                    Reflector.with(assetManager).method("destroy").call();
                    Reflector.with(assetManager).method("init").call();
                    Reflector.with(assetManager).field("mStringBlocks").set(null);
                    Reflector.with(assetManager).method("addAssetPath", String.class).call(sourceDir);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (PluginApk item : mApkList.values()) {
                try {
                    Reflector.with(assetManager).method("addAssetPath", String.class).call(item.apkFile.getAbsolutePath());
                    if (Build.VERSION.SDK_INT < 28) {
                        Reflector.with(assetManager).method("ensureStringBlocks").call();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            Collection<WeakReference<Resources>> references = null;
            try {
                if (Build.VERSION.SDK_INT >= 19) {
                    ResourcesManager resourcesManager = ResourcesManager.getInstance();
                    try {
                        ArrayMap<?, WeakReference<Resources>> arrayMap = Reflector.with(resourcesManager).field("mActiveResources").get();
                        references = arrayMap.values();
                    } catch (Reflector.ReflectedException e) {
                        references = Reflector.with(resourcesManager).field("mResourceReferences").get();
                    }
                } else {
                    ActivityThread activityThread = ActivityThread.currentActivityThread();
                    HashMap<?, WeakReference<Resources>> map = Reflector.with(activityThread).field("mActiveResources").get();
                    references = map.values();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (references != null) {
                WeakReference[] referenceArrays = new WeakReference[references.size()];
                references.toArray(referenceArrays);

                for (WeakReference item : referenceArrays) {
                    Resources resources = (Resources) item.get();
                    if (resources == null) continue;

                    try {
                        Reflector.on(Resources.class).field("mAssets").set(resources, assetManager);
                    } catch (Exception e) {
                        try {
                            Object resourceImpl = Reflector.with(resources).field("mResourcesImpl").get();
                            Reflector.with(resourceImpl).field("mAssets").set(assetManager);
                        } catch (Exception ignore) {
                            e.printStackTrace();
                        }
                    }

                    resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
                }
            }
        }

        private void mergerClass() {
            try {
                List<Object> elementList = new ArrayList<>();
                for (PluginApk item : mApkList.values()) {
                    Constructor constructor;
                    if (Build.VERSION.SDK_INT >= 26) {
                        constructor = Class.forName("dalvik.system.DexPathList$Element").getConstructor(DexFile.class, File.class);
                    } else {
                        constructor = Class.forName("dalvik.system.DexPathList$Element").getConstructors()[0];
                    }

                    File dexOutput = new File(mContext.getDir("fix", Context.MODE_PRIVATE), item.apkFile.getName() + ".dex");
                    DexFile dexFile = DexFile.loadDex(item.apkFile.getAbsolutePath(), dexOutput.getAbsolutePath(), 0);

                    Object element;
                    switch (constructor.getParameterTypes().length) {
                        case 3:
                            if (constructor.getParameterTypes()[1].equals(ZipFile.class)) {
                                // Element(File apk, ZipFile zip, DexFile dex)
                                element = constructor.newInstance(item.apkFile, new ZipFile(item.apkFile), dexFile);
                            } else {
                                // Element(File apk, File zip, DexFile dex)
                                element = constructor.newInstance(item.apkFile, item.apkFile, dexFile);
                            }
                            break;
                        case 2:
                            element = constructor.newInstance(dexFile, item.apkFile);
                            break;
                        case 4:
                        default:
                            element = constructor.newInstance(new File(""), false, item.apkFile, dexFile);
                            break;
                    }
                    elementList.add(element);
                }

                ClassLoader baseClassLoader = mContext.getClassLoader();
                Object pathObj = Reflector.with(baseClassLoader).field("pathList").get();
                Object pathDexElements = Reflector.with(pathObj).field("dexElements").get();

                int i = Array.getLength(pathDexElements);
                int j = i + elementList.size();
                Object result = Array.newInstance(pathDexElements.getClass().getComponentType(), j);

                for (int k = 0; k < j; ++k) {
                    if (k < i) {
                        Array.set(result, k, Array.get(pathDexElements, k));
                    } else {
                        Array.set(result, k, elementList.get(k - i));
                    }
                }

                Field field = pathObj.getClass().getDeclaredField("dexElements");
                field.setAccessible(true);
                field.set(pathObj, result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private interface OnParserListener {
        void onFinish(PluginApk pluginApk);
    }
}
