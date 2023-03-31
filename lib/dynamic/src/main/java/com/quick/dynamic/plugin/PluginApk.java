package com.quick.dynamic.plugin;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;

import java.io.File;
import java.util.ArrayList;

public class PluginApk {
    public File apkFile;
    public String packageName;
    public ApplicationInfo applicationInfo;
    public ArrayList<PackageParser.Activity> activities;
    public ArrayList<PackageParser.Activity> receivers;
    public ArrayList<PackageParser.Service> services;
}
