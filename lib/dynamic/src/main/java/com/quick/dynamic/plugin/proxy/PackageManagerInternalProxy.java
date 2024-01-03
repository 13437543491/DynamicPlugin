package com.quick.dynamic.plugin.proxy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;

import java.lang.reflect.Field;
import java.util.List;

public class PackageManagerInternalProxy extends PackageManagerInternal {

    private PackageManagerInternal mOrigin;

    public PackageManagerInternalProxy(PackageManagerInternal origin) {
        this.mOrigin = origin;
    }

    @Override
    public void setLocationPackagesProvider(PackagesProvider provider) {
        mOrigin.setLocationPackagesProvider(provider);
    }

    @Override
    public void setVoiceInteractionPackagesProvider(PackagesProvider provider) {
        mOrigin.setVoiceInteractionPackagesProvider(provider);
    }

    @Override
    public void setSmsAppPackagesProvider(PackagesProvider provider) {
        mOrigin.setSmsAppPackagesProvider(provider);
    }

    @Override
    public void setDialerAppPackagesProvider(PackagesProvider provider) {
        mOrigin.setDialerAppPackagesProvider(provider);
    }

    @Override
    public void setSimCallManagerPackagesProvider(PackagesProvider provider) {
        mOrigin.setSimCallManagerPackagesProvider(provider);
    }

    @Override
    public void setUseOpenWifiAppPackagesProvider(PackagesProvider provider) {
        mOrigin.setUseOpenWifiAppPackagesProvider(provider);
    }

    @Override
    public void setSyncAdapterPackagesprovider(SyncAdapterPackagesProvider provider) {
        mOrigin.setSyncAdapterPackagesprovider(provider);
    }

    @Override
    public void grantDefaultPermissionsToDefaultSmsApp(String packageName, int userId) {
        mOrigin.grantDefaultPermissionsToDefaultSmsApp(packageName, userId);
    }

    @Override
    public void grantDefaultPermissionsToDefaultDialerApp(String packageName, int userId) {
        mOrigin.grantDefaultPermissionsToDefaultDialerApp(packageName, userId);
    }

    @Override
    public void grantDefaultPermissionsToDefaultSimCallManager(String packageName, int userId) {
        mOrigin.grantDefaultPermissionsToDefaultSimCallManager(packageName, userId);
    }

    @Override
    public void grantDefaultPermissionsToDefaultUseOpenWifiApp(String packageName, int userId) {
        mOrigin.grantDefaultPermissionsToDefaultUseOpenWifiApp(packageName, userId);
    }

    @Override
    public void setKeepUninstalledPackages(List<String> packageList) {
        mOrigin.setKeepUninstalledPackages(packageList);
    }

    @Override
    public boolean isPermissionsReviewRequired(String packageName, int userId) {
        return mOrigin.isPermissionsReviewRequired(packageName, userId);
    }

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags, int filterCallingUid, int userId) {
        return mOrigin.getPackageInfo(packageName, flags, filterCallingUid, userId);
    }

    @Override
    public Bundle getSuspendedPackageLauncherExtras(String packageName, int userId) {
        return mOrigin.getSuspendedPackageLauncherExtras(packageName, userId);
    }

    @Override
    public boolean isPackageSuspended(String packageName, int userId) {
        return mOrigin.isPackageSuspended(packageName, userId);
    }

    @Override
    public String getSuspendingPackage(String suspendedPackage, int userId) {
        return mOrigin.getSuspendingPackage(suspendedPackage, userId);
    }

    @Override
    public String getSuspendedDialogMessage(String suspendedPackage, int userId) {
        return mOrigin.getSuspendedDialogMessage(suspendedPackage, userId);
    }

    @Override
    public int getPackageUid(String packageName, int flags, int userId) {
        return mOrigin.getPackageUid(packageName, flags, userId);
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags, int filterCallingUid, int userId) {
        return mOrigin.getApplicationInfo(packageName, flags, filterCallingUid, userId);
    }

    @Override
    public ActivityInfo getActivityInfo(ComponentName component, int flags, int filterCallingUid, int userId) {
        return mOrigin.getActivityInfo(component, flags, filterCallingUid, userId);
    }

    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags, int filterCallingUid, int userId) {
        return mOrigin.queryIntentActivities(intent, flags, filterCallingUid, userId);
    }

    @Override
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags, int callingUid, int userId) {
        return mOrigin.queryIntentServices(intent, flags, callingUid, userId);
    }

    @Override
    public ComponentName getHomeActivitiesAsUser(List<ResolveInfo> allHomeCandidates, int userId) {
        return mOrigin.getHomeActivitiesAsUser(allHomeCandidates, userId);
    }

    @Override
    public ComponentName getDefaultHomeActivity(int userId) {
        return mOrigin.getDefaultHomeActivity(userId);
    }

    @Override
    public void setDeviceAndProfileOwnerPackages(int deviceOwnerUserId, String deviceOwner, SparseArray<String> profileOwners) {
        mOrigin.setDeviceAndProfileOwnerPackages(deviceOwnerUserId, deviceOwner, profileOwners);
    }

    @Override
    public boolean isPackageDataProtected(int userId, String packageName) {
        return mOrigin.isPackageDataProtected(userId, packageName);
    }

    @Override
    public boolean isPackageStateProtected(String packageName, int userId) {
        return mOrigin.isPackageStateProtected(packageName, userId);
    }

    @Override
    public boolean isPackageEphemeral(int userId, String packageName) {
        return mOrigin.isPackageEphemeral(userId, packageName);
    }

    @Override
    public boolean wasPackageEverLaunched(String packageName, int userId) {
        return mOrigin.wasPackageEverLaunched(packageName, userId);
    }

    @Override
    public void grantRuntimePermission(String packageName, String name, int userId, boolean overridePolicy) {
        mOrigin.grantRuntimePermission(packageName, name, userId, overridePolicy);
    }

    @Override
    public void revokeRuntimePermission(String packageName, String name, int userId, boolean overridePolicy) {
        mOrigin.revokeRuntimePermission(packageName, name, userId, overridePolicy);
    }

    @Override
    public String getNameForUid(int uid) {
        return mOrigin.getNameForUid(uid);
    }

    @Override
    public void requestInstantAppResolutionPhaseTwo(Object responseObj, Intent origIntent, String resolvedType, String callingPackage, Bundle verificationBundle, int userId) {
        mOrigin.requestInstantAppResolutionPhaseTwo(responseObj, origIntent, resolvedType, callingPackage, verificationBundle, userId);
    }

    @Override
    public void grantEphemeralAccess(int userId, Intent intent, int targetAppId, int ephemeralAppId) {
        mOrigin.grantEphemeralAccess(userId, intent, targetAppId, ephemeralAppId);
    }

    @Override
    public boolean isInstantAppInstallerComponent(ComponentName component) {
        return mOrigin.isInstantAppInstallerComponent(component);
    }

    @Override
    public void pruneInstantApps() {
        mOrigin.pruneInstantApps();
    }

    @Override
    public String getSetupWizardPackageName() {
        return mOrigin.getSetupWizardPackageName();
    }

    @Override
    public void setExternalSourcesPolicy(ExternalSourcesPolicy policy) {
        mOrigin.setExternalSourcesPolicy(policy);
    }

    @Override
    public boolean isPackagePersistent(String packageName) {
        return mOrigin.isPackagePersistent(packageName);
    }

    @Override
    public boolean isLegacySystemApp(PackageParser.Package pkg) {
        return mOrigin.isLegacySystemApp(pkg);
    }

    @Override
    public List<PackageInfo> getOverlayPackages(int userId) {
        return mOrigin.getOverlayPackages(userId);
    }

    @Override
    public List<String> getTargetPackageNames(int userId) {
        return mOrigin.getTargetPackageNames(userId);
    }

    @Override
    public boolean setEnabledOverlayPackages(int userId, String targetPackageName, List<String> overlayPackageNames) {
        return mOrigin.setEnabledOverlayPackages(userId, targetPackageName, overlayPackageNames);
    }

    @Override
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId, boolean resolveForStart, int filterCallingUid) {
        return mOrigin.resolveIntent(intent, resolvedType, flags, userId, resolveForStart, filterCallingUid);
    }

    @Override
    public ResolveInfo resolveService(Intent intent, String resolvedType, int flags, int userId, int callingUid) {
        Log.e("yangyangyang", "resolveService==>" + intent);
        return mOrigin.resolveService(intent, resolvedType, flags, userId, callingUid);
    }

    @Override
    public ProviderInfo resolveContentProvider(String name, int flags, int userId) {
        return mOrigin.resolveContentProvider(name, flags, userId);
    }

    @Override
    public void addIsolatedUid(int isolatedUid, int ownerUid) {
        mOrigin.addIsolatedUid(isolatedUid, ownerUid);
    }

    @Override
    public void removeIsolatedUid(int isolatedUid) {
        mOrigin.removeIsolatedUid(isolatedUid);
    }

    @Override
    public int getUidTargetSdkVersion(int uid) {
        return mOrigin.getUidTargetSdkVersion(uid);
    }

    @Override
    public int getPackageTargetSdkVersion(String packageName) {
        return mOrigin.getPackageTargetSdkVersion(packageName);
    }

    @Override
    public boolean canAccessInstantApps(int callingUid, int userId) {
        return mOrigin.canAccessInstantApps(callingUid, userId);
    }

    @Override
    public boolean canAccessComponent(int callingUid, ComponentName component, int userId) {
        return mOrigin.canAccessComponent(callingUid, component, userId);
    }

    @Override
    public boolean hasInstantApplicationMetadata(String packageName, int userId) {
        return mOrigin.hasInstantApplicationMetadata(packageName, userId);
    }

    @Override
    public void notifyPackageUse(String packageName, int reason) {
        mOrigin.notifyPackageUse(packageName, reason);
    }

    @Override
    public PackageParser.Package getPackage(String packageName) {
        return mOrigin.getPackage(packageName);
    }

    @Override
    public Object getPackageList(PackageListObserver observer) {
        return mOrigin.getPackageList(observer);
    }

    @Override
    public void removePackageListObserver(PackageListObserver observer) {
        mOrigin.removePackageListObserver(observer);
    }

    @Override
    public PackageParser.Package getDisabledPackage(String packageName) {
        return mOrigin.getDisabledPackage(packageName);
    }

    @Override
    public boolean isResolveActivityComponent(ComponentInfo component) {
        return mOrigin.isResolveActivityComponent(component);
    }

    @Override
    public String getKnownPackageName(int knownPackage, int userId) {
        return mOrigin.getKnownPackageName(knownPackage, userId);
    }

    @Override
    public boolean isInstantApp(String packageName, int userId) {
        return mOrigin.isInstantApp(packageName, userId);
    }

    @Override
    public String getInstantAppPackageName(int uid) {
        return mOrigin.getInstantAppPackageName(uid);
    }

    @Override
    public boolean filterAppAccess(PackageParser.Package pkg, int callingUid, int userId) {
        return mOrigin.filterAppAccess(pkg, callingUid, userId);
    }

    @Override
    public int getPermissionFlagsTEMP(String permName, String packageName, int userId) {
        return mOrigin.getPermissionFlagsTEMP(permName, packageName, userId);
    }

    @Override
    public void updatePermissionFlagsTEMP(String permName, String packageName, int flagMask, int flagValues, int userId) {
        mOrigin.updatePermissionFlagsTEMP(permName, packageName, flagMask, flagValues, userId);
    }

    @Override
    public boolean isDataRestoreSafe(byte[] restoringFromSigHash, String packageName) {
        return mOrigin.isDataRestoreSafe(restoringFromSigHash, packageName);
    }

    @Override
    public boolean isDataRestoreSafe(Signature restoringFromSig, String packageName) {
        return mOrigin.isDataRestoreSafe(restoringFromSig, packageName);
    }

    @Override
    public boolean hasSignatureCapability(int serverUid, int clientUid, int capability) {
        return mOrigin.hasSignatureCapability(serverUid, clientUid, capability);
    }

    public static void install(Context context) {
        try {
            Class<?> localServicesClass = Class.forName("com.android.server.LocalServices");

            Field sLocalServiceObjectsField = localServicesClass.getDeclaredField("sLocalServiceObjects");
            sLocalServiceObjectsField.setAccessible(true);
            ArrayMap<Class<?>, Object> sLocalServiceObjects = (ArrayMap<Class<?>, Object>) sLocalServiceObjectsField.get(null);

            Log.e("yangyangyang", "ccccc==>" + sLocalServiceObjects.size());


//            for (Class c:sLocalServiceObjects.keySet()){
//            }
//
//            Object origin = sLocalServiceObjects.remove(PackageManagerInternal.class);
//            sLocalServiceObjects.put(PackageManagerInternal.class, new PackageManagerInternalProxy((PackageManagerInternal) origin));

//            mPackageManagerInt
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
