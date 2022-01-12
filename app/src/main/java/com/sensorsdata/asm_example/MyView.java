package com.sensorsdata.asm_example;

import android.content.Context;
import android.os.Parcelable;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

public class MyView extends FrameLayout {
    public MyView(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
    }
}
