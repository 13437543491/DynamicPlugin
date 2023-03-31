package com.demo.plugin1;

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

public class MyLocalService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("localService", "MyLocalService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("localService", "MyLocalService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("localService", "MyLocalService onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("localService", "MyLocalService is bind");
        return new Binder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("localService", "MyLocalService is unbind");
        return super.onUnbind(intent);
    }
}
