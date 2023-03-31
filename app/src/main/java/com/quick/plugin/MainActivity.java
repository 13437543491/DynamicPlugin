package com.quick.plugin;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.quick.dynamic.Dynamic;
import com.quick.dynamic.OnInstallListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

    private String mPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File tempFile = new File("/storage/emulated/0/2222.apk");
        Log.e("yangyangyang", "exists==>" + tempFile.exists());

        Dynamic.installApk(tempFile, new OnInstallListener() {
            @Override
            public void onFinish(String packageName) {
                mPackageName = packageName;
                Toast.makeText(MainActivity.this, "插件加载成功==>" + packageName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void startPlugin(View view) {
        Dynamic.startPluginLaunchActivity(this, mPackageName);
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