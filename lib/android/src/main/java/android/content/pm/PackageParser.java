package android.content.pm;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.util.DisplayMetrics;

import java.io.File;
import java.util.ArrayList;

public class PackageParser {

    public final static int PARSE_MUST_BE_APK = 1 << 2;

    public PackageParser() {

    }

    public PackageParser(String archiveSourcePath) {
    }

    public Package parsePackage(File sourceFile, String destCodePath, DisplayMetrics metrics, int flags) {
        throw new RuntimeException();
    }

    public Package parsePackage(File packageFile, int flags) {
        throw new RuntimeException();
    }

    public boolean collectCertificates(Package pkg, int flags) {
        return false;
    }

    public final static class Package {
        public final ApplicationInfo applicationInfo = new ApplicationInfo();
        public final ArrayList<Activity> activities = new ArrayList<Activity>(0);
        public final ArrayList<Activity> receivers = new ArrayList<Activity>(0);
        public final ArrayList<Service> services = new ArrayList<Service>(0);
    }

    public final static class Activity extends Component<ActivityIntentInfo> {
        public ActivityInfo info;

        public Activity(Package _owner) {
            super(_owner);
        }
    }

    public final static class Service extends Component<ServiceIntentInfo> {
        public final ServiceInfo info;

        public Service(Package _owner) {
            super(_owner);
            throw new RuntimeException();
        }
    }

    public final static class ServiceIntentInfo extends IntentInfo {

    }

    public static class Component<II extends IntentInfo> {
        public final ArrayList<II> intents;

        public Component(Package _owner) {
            intents = null;
        }

        public ComponentName getComponentName() {
            throw new RuntimeException();
        }
    }

    public final static class ActivityIntentInfo extends IntentInfo {

    }

    public static class IntentInfo extends IntentFilter {

    }
}
