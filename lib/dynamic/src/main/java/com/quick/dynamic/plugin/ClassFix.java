package com.quick.dynamic.plugin;

import android.content.Context;
import android.os.Build;

import com.quick.dynamic.util.ArrayUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

public class ClassFix {

    public void fix(Context context, File apkFile) {
        try {
            ClassLoader classLoader = context.getClassLoader();
            combineClass(context, apkFile, classLoader);
            combineSo(context, apkFile, classLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void combineClass(Context context, File apkFile, ClassLoader hostClassLoader) throws Exception {
        Field pathListField = Class.forName("dalvik.system.BaseDexClassLoader").getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object pathList = pathListField.get(hostClassLoader);

        Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        Object pathDexElements = dexElementsField.get(pathList);

        Constructor constructor;
        if (Build.VERSION.SDK_INT >= 26) {
            constructor = Class.forName("dalvik.system.DexPathList$Element").getConstructor(DexFile.class, File.class);
        } else {
            constructor = Class.forName("dalvik.system.DexPathList$Element").getConstructors()[0];
        }

        String apkName = apkFile.getName().split("\\.")[0];
        File file = new File(getDir(context, apkName), apkName + ".dex");
        DexFile dexFile = DexFile.loadDex(apkFile.getAbsolutePath(), file.getAbsolutePath(), 0);

        Object element;
        switch (constructor.getParameterTypes().length) {
            case 3:
                if (constructor.getParameterTypes()[1].equals(ZipFile.class)) {
                    // Element(File apk, ZipFile zip, DexFile dex)
                    element = constructor.newInstance(apkFile, new ZipFile(apkFile), dexFile);
                } else {
                    // Element(File apk, File zip, DexFile dex)
                    element = constructor.newInstance(apkFile, apkFile, dexFile);
                }
                break;
            case 2:
                element = constructor.newInstance(dexFile, apkFile);
                break;
            case 4:
            default:
                element = constructor.newInstance(new File(""), false, apkFile, dexFile);
                break;
        }

        Object result = ArrayUtil.combineArrayItem(pathDexElements, element);
        dexElementsField.set(pathList, result);
    }

    private static final String[] CPU64_PREFIX = {"arm64", "x86_64", "mips64"};
    private static final String[] CPU32_PREFIX = {"armeabi-v7a", "armeabi", "x86"};

    private String combineSo(Context context, File apkFile, ClassLoader hostClassLoader) throws Exception {
        boolean is64 = false;
        for (String cpu : CPU64_PREFIX) {
            is64 = context.getApplicationInfo().nativeLibraryDir.contains(cpu);
            if (is64) {
                break;
            }
        }

        String apkName = apkFile.getName().split("\\.")[0];
        File pluginDir = getDir(context, apkName);

        String cpuArch = null;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(apkFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.isDirectory()) {
                    continue;
                }
                String zipEntryName = zipEntry.getName();
                if (zipEntryName.endsWith(".so")) {
                    if (cpuArch == null) {
                        String[] cpuList = is64 ? CPU64_PREFIX : CPU32_PREFIX;
                        for (String item : cpuList) {
                            if (zipEntryName.contains(item)) {
                                cpuArch = item;
                                break;
                            }
                        }
                    }

                    if (cpuArch == null) {
                        return pluginDir.getAbsolutePath();
                    }

                    if (!zipEntryName.contains(cpuArch)) {
                        continue;
                    }

                    InputStream inputStream = null;
                    FileOutputStream fileOutputStream = null;

                    try {
                        zipEntryName = zipEntryName.substring(zipEntryName.lastIndexOf("/") + 1);

                        inputStream = zipFile.getInputStream(zipEntry);
                        fileOutputStream = new FileOutputStream(new File(pluginDir, zipEntryName));

                        int len;
                        byte[] buffer = new byte[1024];
                        while ((len = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                        }

                        fileOutputStream.flush();
                        fileOutputStream.close();
                        inputStream.close();
                    } finally {
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                }
            }
        } finally {
            if (zipFile != null) {
                zipFile.close();
            }
        }

        Field pathListField = Class.forName("dalvik.system.BaseDexClassLoader")
                .getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object pathList = pathListField.get(hostClassLoader);

        Field nativeLibraryDirectoriesField = pathList.getClass().getDeclaredField("nativeLibraryDirectories");
        nativeLibraryDirectoriesField.setAccessible(true);

        if (Build.VERSION.SDK_INT >= 23) {
//            private final List<File> nativeLibraryDirectories;
            List<File> nativeLibraryDirectories = (List<File>) nativeLibraryDirectoriesField.get(pathList);
            nativeLibraryDirectories.add(pluginDir);

//            NativeLibraryElement[] nativeLibraryPathElements;
            Field nativeLibraryPathElementsField = pathList.getClass().getDeclaredField("nativeLibraryPathElements");
            nativeLibraryPathElementsField.setAccessible(true);
            Object nativeLibraryPathElements = nativeLibraryPathElementsField.get(pathList);

            Object pluginDexElement;
            if (Build.VERSION.SDK_INT >= 26) {
                Constructor<?> constructor = Class.forName("dalvik.system.DexPathList$NativeLibraryElement")
                        .getConstructor(File.class);
                constructor.setAccessible(true);
                pluginDexElement = constructor.newInstance(pluginDir);
            } else {
                //public Element(File dir, boolean isDirectory, File zip, DexFile dexFile)
                pluginDexElement = Class.forName("dalvik.system.DexPathList$Element")
                        .getConstructor(File.class, boolean.class, File.class, DexFile.class)
                        .newInstance(pluginDir, true, pluginDir, null);
            }

            Object newDexElements = ArrayUtil.combineArrayItem(nativeLibraryPathElements, pluginDexElement);
            nativeLibraryPathElementsField.set(pathList, newDexElements);
        } else if (Build.VERSION.SDK_INT >= 14) {
            Object nativeLibraryDirectories = nativeLibraryDirectoriesField.get(pathList);
            Object newDexElements = ArrayUtil.combineArrayItem(nativeLibraryDirectories, pluginDir);
            nativeLibraryDirectoriesField.set(pathList, newDexElements);
        } else {
            //不管了
        }

        return pluginDir.getAbsolutePath();
    }

    private File getDir(Context context, String pluginName) {
        String suffix = "apkPlugin";
        File pluginNativeDir;
        if (Build.VERSION.SDK_INT >= 24) {
            pluginNativeDir = new File(context.getDataDir(), suffix);
        } else {
            pluginNativeDir = context.getDir(suffix, Context.MODE_PRIVATE);
        }

        if (!pluginNativeDir.exists()) {
            pluginNativeDir.mkdir();
        }

        File result = new File(pluginNativeDir, pluginName);
        if (!result.exists()) {
            result.mkdir();
        }

        return result;
    }
}
