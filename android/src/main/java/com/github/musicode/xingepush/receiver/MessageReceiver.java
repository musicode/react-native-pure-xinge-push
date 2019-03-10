package com.github.musicode.xingepush.receiver;

import android.content.Context;

import com.github.musicode.xingepush.RNTXingePushModule;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

public class MessageReceiver extends XGPushBaseReceiver {

    @Override
    public void onRegisterResult(Context context, int code, XGPushRegisterResult result) {

    }

    @Override
    public void onUnregisterResult(Context context, int code) {

    }

    @Override
    public void onSetTagResult(Context context, int code, String tagName) {
        if (RNTXingePushModule.instance == null) {
            return;
        }
        RNTXingePushModule.instance.onBindTag(code);
    }

    @Override
    public void onDeleteTagResult(Context context, int code, String tagName) {
        if (RNTXingePushModule.instance == null) {
            return;
        }
        RNTXingePushModule.instance.onUnbindTag(code);
    }

    @Override
    public void onTextMessage(Context context, XGPushTextMessage message) {
        if (RNTXingePushModule.instance == null) {
            return;
        }
        RNTXingePushModule.instance.onMessage(message);
    }

    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult result) {
        if (RNTXingePushModule.instance == null || result == null) {
            return;
        }
        RNTXingePushModule.instance.onNotifaction(result);
    }

    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult result) {

        if (RNTXingePushModule.instance == null || result == null) {
            return;
        }
        
    }
}
