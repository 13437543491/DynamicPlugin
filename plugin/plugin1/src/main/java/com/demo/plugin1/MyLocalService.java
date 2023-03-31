package com.demo.plugin1;

import android.app.Service;
import android.content.Intent;
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
        mUI.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "MyLocalService onStartCommand!", Toast.LENGTH_SHORT).show();
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUI.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "MyLocalService is onDestroy!", Toast.LENGTH_SHORT).show();
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
        return new IBinder() {
            @Override
            public String getInterfaceDescriptor() throws RemoteException {
                return null;
            }

            @Override
            public boolean pingBinder() {
                return false;
            }

            @Override
            public boolean isBinderAlive() {
                return false;
            }

            @Override
            public IInterface queryLocalInterface(String descriptor) {
                return null;
            }

            @Override
            public void dump(FileDescriptor fd, String[] args) throws RemoteException {

            }

            @Override
            public void dumpAsync(FileDescriptor fd, String[] args) throws RemoteException {

            }

            @Override
            public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                return false;
            }

            @Override
            public void linkToDeath(DeathRecipient recipient, int flags) throws RemoteException {

            }

            @Override
            public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
                return false;
            }
        };
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
