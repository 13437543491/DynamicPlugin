package android.content.pm;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.util.DisplayMetrics;

import java.io.File;
import java.util.ArrayList;

public class PackageParser {

    public PackageParser() {

    }

    public PackageParser(String archiveSourcePath) {

    }

    public Package parsePackage(File packageFile, int flags) {
        throw new RuntimeException();
    }

    public Package parsePackage(File sourceFile, String destCodePath, DisplayMetrics metrics, int flags) {
        throw new RuntimeException();
    }

    public final static class Package {
        public String packageName;
        public final ArrayList<Activity> activities = new ArrayList<Activity>(0);
        public final ArrayList<Activity> receivers = new ArrayList<Activity>(0);

        public ApplicationInfo applicationInfo = new ApplicationInfo();
    }

    public final static class Activity extends Component<ActivityIntentInfo> {
        public ActivityInfo info;
    }

    public static class Component<II extends IntentInfo> {
        public ArrayList<II> intents;
        ComponentName componentName;

        public ComponentName getComponentName() {
            return componentName;
        }
    }

    public final static class ActivityIntentInfo extends IntentInfo {

    }

    public static abstract class IntentInfo extends IntentFilter {

    }
}
