package com.sensorsdata.asm_example;

import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.github.beansoft.android.crashutil.ThreadDumpBuilder;

public class TelephonyManagerProxy {
    public static boolean enableCollect = false;

    public static int getNetworkType(TelephonyManager telephonyManager) {
        System.out.println(ThreadDumpBuilder.buildCurrentThreadStackTrace());
        return (enableCollect) ? telephonyManager.getNetworkType() : 1024;
    }
}
