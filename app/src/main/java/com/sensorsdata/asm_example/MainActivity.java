package com.sensorsdata.asm_example;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testLog();
                testString("", "");
            }
        });

        int c = Math.max(1, 2);
        System.out.println(c);

        TelephonyManager telephonyManager = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
        System.out.println(telephonyManager.getNetworkType());
    }

    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        int c = Math.max(1, 2);
        System.out.println(c);
        super.onRestoreInstanceState(savedInstanceState);
//        Object o = new Object();
//        String s = (String)o;
//        System.out.println(s);
    }

    public void testLog() {
        Log.d("TAG", "dsw");
        Log.i("TAG", "dsw");
        Log.v("TAG", "dsw");
        Log.w("TAG", "dsw");
        Log.e("TAG", "dsw");
    }

    public String testString(String a, String b) {
        int result = 5 / 0;
        return "HelloWorld";
    }

    public Object getObject() {
        return null;
    }

    public char getObjectChar() {
        return 0;
    }

    public short getObjectShort() {
        return 0;
    }

    public byte getObjectByte() {
        return 0;
    }

    public int getNetworkType() {
        return 0;
    }
}