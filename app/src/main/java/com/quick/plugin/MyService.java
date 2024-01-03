package com.quick.plugin;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.FileDescriptor;

public class MyService extends Service {

    private Handler mUI;

    @Override
    public void onCreate() {
        super.onCreate();
        mUI = new Handler(Looper.myLooper());
        mUI.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "MyLocalService is on!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUI.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "MyLocalService is off!", Toast.LENGTH_SHORT).show();
                mUI = null;
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        mUI.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "MyRemoteService is bind!", Toast.LENGTH_SHORT).show();
            }
        });
        return new DownloadBinder();
    }

    public class DownloadBinder extends Binder {
        public void startDownload() {
            Log.d("MyService", "开始下载");
            Toast.makeText(getApplicationContext(), "开始下载", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mUI.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "MyRemoteService is unbind!", Toast.LENGTH_SHORT).show();
            }
        });
        return super.onUnbind(intent);
    }
}
