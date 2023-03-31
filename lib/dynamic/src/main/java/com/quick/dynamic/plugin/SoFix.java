package com.quick.dynamic.plugin;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Process;

import com.quick.dynamic.util.Reflector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

public class SoFix {

    public void fix(Context app, String apkPath) throws Exception {
        String cpuArch = "arm64-v8a";
        if (!isART64(app.getClassLoader())) {
            if (Build.CPU_ABI.toLowerCase().contains("arm")) {
                cpuArch = "armeabi";
            } else {
                cpuArch = "x86";
            }
        }

        String suffix = "pluginSo";
        File pluginNativeDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pluginNativeDir = new File(app.getDataDir(), suffix);
        } else {
            pluginNativeDir = app.getDir(suffix, Context.MODE_PRIVATE);
        }

        if (!pluginNativeDir.exists()) {
            pluginNativeDir.mkdir();
        }

        unzipSo(apkPath, cpuArch, pluginNativeDir.getAbsolutePath());
        mergeSo(app.getClassLoader(), pluginNativeDir);
    }

    private boolean isART64(ClassLoader classLoader) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Process.is64Bit();
        }

        try {
            Method method = ClassLoader.class.getDeclaredMethod("findLibrary", String.class);
            Object object = method.invoke(classLoader, "art");
            if (object != null) {
                return ((String) object).contains("lib64");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void unzipSo(String apkPath, String cpuArch, String pluginNativeDir) {
        try {
            ZipFile zipFile = new ZipFile(apkPath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.isDirectory()) {
                    continue;
                }
                String zipEntryName = zipEntry.getName();
                if (zipEntryName.endsWith(".so")) {
                    if (zipEntryName.contains("x86_64") && !cpuArch.equals("x86_64")) {
                        continue;
                    }

                    if (!zipEntryName.contains(cpuArch)) {
                        continue;
                    }

                    InputStream inputStream = null;
                    FileOutputStream fileOutputStream = null;

                    try {
                        zipEntryName = zipEntryName.substring(zipEntryName.lastIndexOf("/") + 1);

                        inputStream = zipFile.getInputStream(zipEntry);
                        fileOutputStream = new FileOutputStream(new File(pluginNativeDir, zipEntryName));

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mergeSo(ClassLoader hostClassLoader, File pluginNativeDir) throws Exception {
        Object pathList = Reflector.on("dalvik.system.BaseDexClassLoader").field("pathList").get(hostClassLoader);

        if (Build.VERSION.SDK_INT >= 26) {
//            private final List<File> nativeLibraryDirectories;
            List<File> nativeLibraryDirectories = Reflector.on(pathList.getClass()).field("nativeLibraryDirectories").get(pathList);
            nativeLibraryDirectories.add(pluginNativeDir);

//            NativeLibraryElement[] nativeLibraryPathElements;
            Object nativeLibraryPathElements = Reflector.on(pathList.getClass()).field("nativeLibraryPathElements").get(pathList);

            Object pluginNativeLibraryElement = makeNativeLibraryElement(pluginNativeDir);

            Object newDexElements = combineArray(nativeLibraryPathElements, new Object[]{pluginNativeLibraryElement}, false);
            Reflector.on(pathList.getClass()).field("nativeLibraryPathElements").set(pathList, newDexElements);
        } else if (Build.VERSION.SDK_INT >= 23) {
            List<File> nativeLibraryDirectories = Reflector.on(pathList.getClass()).field("nativeLibraryDirectories").get(pathList);
            nativeLibraryDirectories.add(pluginNativeDir);

            Object nativeLibraryPathElements = Reflector.on(pathList.getClass()).field("nativeLibraryPathElements").get(pathList);

            Object pluginDexElement = makeDexElement(pluginNativeDir);

            Object newDexElements = combineArray(nativeLibraryPathElements, new Object[]{pluginDexElement}, false);
            Reflector.on(pathList.getClass()).field("nativeLibraryPathElements").set(pathList, newDexElements);
        } else if (Build.VERSION.SDK_INT >= 14) {
            Object nativeLibraryDirectories = Reflector.on(pathList.getClass()).field("nativeLibraryDirectories").get(pathList);

            Object newDexElements = combineArray(nativeLibraryDirectories, new File[]{pluginNativeDir}, false);
            Reflector.on(pathList.getClass()).field("nativeLibraryDirectories").set(pathList, newDexElements);
        } else {

        }
    }

    private Object makeNativeLibraryElement(File pluginNativeDir) throws Exception {
        return Reflector.on("dalvik.system.DexPathList$NativeLibraryElement")
                .constructor(File.class)
                .newInstance(pluginNativeDir);
    }

    private Object makeDexElement(File pluginNativeDir) throws Exception {
        if (Build.VERSION.SDK_INT >= 26) {
            //public Element(File path)
            return Reflector.on("dalvik.system.DexPathList$Element")
                    .constructor(File.class)
                    .newInstance(pluginNativeDir);
        } else {
            //public Element(File dir, boolean isDirectory, File zip, DexFile dexFile)
            return Reflector.on("dalvik.system.DexPathList$Element")
                    .constructor(File.class, boolean.class, File.class, DexFile.class)
                    .newInstance(pluginNativeDir, true, pluginNativeDir, null);
        }
    }

    private static Object combineArray(Object target, Object extra, boolean isInsertHead) {
        int i = Array.getLength(target);
        int j = Array.getLength(extra);
        int k = i + j;

        Object result = Array.newInstance(target.getClass().getComponentType(), k);

        if (isInsertHead) {
            System.arraycopy(extra, 0, result, 0, j);
            System.arraycopy(target, 0, result, j, i);
        } else {
            System.arraycopy(target, 0, result, 0, i);
            System.arraycopy(extra, 0, result, i, j);
        }

        return result;
    }
}

















