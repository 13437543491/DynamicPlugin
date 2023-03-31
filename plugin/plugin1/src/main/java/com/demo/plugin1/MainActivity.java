package com.demo.plugin1;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, SecondActivity.class), 1000);
            }
        });

        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permissions, 1100);
                }
            }
        });

        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("com.demo.ad");
                intent.putExtra("name", "1111");

                Bundle bundle = new Bundle();
                bundle.putString("nameeee", "22222");
                intent.putExtras(bundle);

                sendBroadcast(intent);
            }
        });

        findViewById(R.id.btn5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DynamicBroadcastReceiver dynamicBroadcastReceiver = new DynamicBroadcastReceiver();

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("com.aaa.bbb");

                registerReceiver(dynamicBroadcastReceiver, intentFilter);
            }
        });

        findViewById(R.id.btn6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("com.aaa.bbb");
                intent.putExtra("name", "3333");

                Bundle bundle = new Bundle();
                bundle.putString("namebbb", "4444");
                intent.putExtras(bundle);

                sendBroadcast(intent);
            }
        });

        findViewById(R.id.btn7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyLocalService.class);
                intent.putExtra("name", "test");

                Bundle bundle = new Bundle();
                bundle.putString("name_bunglde", "bundle");
                intent.putExtras(bundle);

                startService(intent);
            }
        });

        findViewById(R.id.btn8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyLocalService.class);
                stopService(intent);
            }
        });

        findViewById(R.id.btn9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyLocalService.class);
                bindService(intent, connection, Service.BIND_AUTO_CREATE);
            }
        });

        findViewById(R.id.btn10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unbindService(connection);
            }
        });

    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("yangyangyang", "onServiceConnected==>" + name);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("yangyangyang", "onServiceDisconnected==>" + name);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String name = data.getStringExtra("name");
            Log.e("yangyangyang", "getStringExtra==>" + name);

            name = data.getExtras().getString("name");
            Log.e("yangyangyang", "getExtras==>" + name);
        }
        Toast.makeText(this, "requestCode==>" + requestCode + "resultCode==>" + resultCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("yangyangyang", "onRequestPermissionsResult==>" + requestCode);

        for (String item : permissions) {
            Log.e("yangyangyang", "permissions==>" + item);
        }

        for (int item : grantResults) {
            Log.e("yangyangyang", "grantResults==>" + item);
        }


    }
}