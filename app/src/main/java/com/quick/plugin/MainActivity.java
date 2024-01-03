package com.quick.plugin;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.quick.dynamic.Dynamic;
import com.quick.dynamic.plugin.proxy.PackageManagerInternalProxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity {

    private String mPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("yangyangyang", "myPid==>" + android.os.Process.myPid());
    }

    public void installPlugin(View view) {
        File pluginDirPath = new File(getFilesDir(), "plugin");
        File tempFile = new File(pluginDirPath, "plugin.apk");
        if (tempFile.exists()) {
            tempFile.delete();
        }

        try {
            copyFileWithStream(getAssets().open("plugin.apk"), tempFile.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Dynamic.installApk(tempFile);
    }

    public void startPlugin(View view) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.demo.plugin1", "com.demo.plugin1.MainActivity"));
        startActivity(intent);
    }

    public void startMyService(View view) {
        startService(new Intent(this, MyService.class));
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("yangyangyang", "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("yangyangyang", "onServiceDisconnected");
        }
    };

    private boolean copyFileWithStream(InputStream inputStream, String targetFilePath) {
        try {
            File file = new File(targetFilePath);

            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] tempByte = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(tempByte)) != -1) {
                fileOutputStream.write(tempByte, 0, length);
            }
            inputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}