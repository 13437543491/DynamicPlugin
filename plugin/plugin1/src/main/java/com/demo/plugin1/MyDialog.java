package com.demo.plugin1;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

public class MyDialog extends AlertDialog {
    protected MyDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_my);
    }
}
