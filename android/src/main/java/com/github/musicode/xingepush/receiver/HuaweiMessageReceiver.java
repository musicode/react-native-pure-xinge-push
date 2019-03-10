package com.github.musicode.xingepush.receiver;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.tencent.android.hwpush.HWPushMessageReceiver;

public class HuaweiMessageReceiver extends HWPushMessageReceiver {

    @Override
    public void onEvent(Context context, Event event, Bundle bundle) {
        super.onEvent(context, event, bundle);
        Log.d("xg_service", "!!! huawei  onEvent");
    }

    @Override
    public void onToken(Context context, String s, Bundle bundle) {
        super.onToken(context, s, bundle);
        Log.d("xg_service", "!!! huawei  onToken bundle " + s);
    }

    @Override
    public boolean onPushMsg(Context context, byte[] bytes, Bundle bundle) {
        Log.d("xg_service", "!!! huawei  onPushMsg bundle");
        return super.onPushMsg(context, bytes, bundle);
    }

    @Override
    public void onPushMsg(Context context, byte[] bytes, String s) {
        super.onPushMsg(context, bytes, s);
        Log.d("xg_service", "!!! huawei  onPushMsg " + s);
    }

    @Override
    public void onPushState(Context context, boolean b) {
        super.onPushState(context, b);
        Log.d("xg_service", "!!! huawei  onPushState");
    }

    @Override
    public void onToken(Context context, String s) {
        super.onToken(context, s);
        Log.d("xg_service", "!!! huawei  onToken " + s);
    }
}