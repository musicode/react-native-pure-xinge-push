package com.github.musicode.xingepush;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.XGPushTextMessage;

import me.leolin.shortcutbadger.ShortcutBadger;

public class RNTXingePushModule extends ReactContextBaseJavaModule {

    public static RNTXingePushModule instance;

    private final ReactApplicationContext reactContext;

    private int badge = 0;

    public RNTXingePushModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        instance = this;
    }

    @Override
    public String getName() {
        return "RNTXingePush";
    }

    @ReactMethod
    public void setDebug(boolean debug) {
        XGPushConfig.enableDebug(reactContext, debug);
    }

    @ReactMethod
    public void enableOtherPush(boolean enable) {
        XGPushConfig.enableOtherPush(reactContext, enable);
    }

    @ReactMethod
    public void setHuaweiDebug(boolean debug) {
        XGPushConfig.setHuaweiDebug(debug);
    }

    @ReactMethod
    public void setMiPush(String appId, String appKey) {
        XGPushConfig.setMiPushAppId(reactContext, appId);
        XGPushConfig.setMiPushAppKey(reactContext, appKey);
    }

    @ReactMethod
    public void setMZPush(String appId, String appKey) {
        XGPushConfig.setMzPushAppId(reactContext, appId);
        XGPushConfig.setMzPushAppKey(reactContext, appKey);
    }

    @ReactMethod
    public void start(int accessId, String accessKey) {

        XGPushConfig.setAccessId(reactContext, accessId);
        XGPushConfig.setAccessKey(reactContext, accessKey);

        XGPushManager.registerPush(reactContext, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                onStart(XGPushBaseReceiver.SUCCESS);
                onRegister(XGPushConfig.getToken(reactContext), XGPushBaseReceiver.SUCCESS);
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {
                onStart(errCode);
            }
        });

    }

    @ReactMethod
    public void stop() {
        XGPushManager.unregisterPush(reactContext, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                onStop(XGPushBaseReceiver.SUCCESS);
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {
                onStop(errCode);
            }
        });
    }

    @ReactMethod
    public void bindAccount(String account) {
        XGPushManager.bindAccount(reactContext, account, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                onBindAccount(XGPushBaseReceiver.SUCCESS);
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {
                onBindAccount(errCode);
            }
        });
    }

    @ReactMethod
    public void unbindAccount(String account) {
        XGPushManager.delAccount(reactContext, account, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                onUnbindAccount(XGPushBaseReceiver.SUCCESS);
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {
                onUnbindAccount(errCode);
            }
        });
    }

    @ReactMethod
    public void bindTag(String tag) {
        XGPushManager.setTag(reactContext, tag);
    }

    @ReactMethod
    public void unbindTag(String tag) {
        XGPushManager.deleteTag(reactContext, tag);
    }

    @ReactMethod
    public void setBadge(int badge) {
        this.badge = badge;
        ShortcutBadger.applyCount(reactContext, badge);
    }

    @ReactMethod
    public void getBadge(Promise promise) {
        WritableMap map = Arguments.createMap();
        map.putInt("badge", badge);
        promise.resolve(map);
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    private void onStart(int code) {
        WritableMap map = Arguments.createMap();
        map.putInt("error", code);
        sendEvent("start", map);
    }

    private void onStop(int code) {
        WritableMap map = Arguments.createMap();
        map.putInt("error", code);
        sendEvent("stop", map);
    }

    private void onRegister(String deviceToken, int code) {
        WritableMap map = Arguments.createMap();
        map.putString("deviceToken", deviceToken);
        map.putInt("error", code);
        sendEvent("register", map);
    }

    private void onBindAccount(int code) {
        WritableMap map = Arguments.createMap();
        map.putInt("error", code);
        sendEvent("bindAccount", map);
    }

    private void onUnbindAccount(int code) {
        WritableMap map = Arguments.createMap();
        map.putInt("error", code);
        sendEvent("unbindAccount", map);
    }

    public void onBindTag(int code) {
        WritableMap map = Arguments.createMap();
        map.putInt("error", code);
        sendEvent("bindTag", map);
    }

    public void onUnbindTag(int code) {
        WritableMap map = Arguments.createMap();
        map.putInt("error", code);
        sendEvent("unbindTag", map);
    }

    public void onMessage(XGPushTextMessage message) {

        String title = message.getTitle();
        String content = message.getContent();
        String customContent = message.getCustomContent();

        WritableMap map = Arguments.createMap();
        map.putString("title", title);
        map.putString("content", content);
        map.putString("customContent", customContent);
        sendEvent("message", map);

    }

    public void onNotifaction(XGPushClickedResult result) {

        String title = result.getTitle();
        String content = result.getContent();
        String customContent = result.getCustomContent();

        WritableMap map = Arguments.createMap();
        map.putString("title", title);
        map.putString("content", content);
        map.putString("customContent", customContent);

        long actionType = result.getActionType();
        if (actionType == XGPushClickedResult.NOTIFACTION_CLICKED_TYPE) {
            map.putBoolean("clicked", true);
        }
        else if (actionType == XGPushClickedResult.NOTIFACTION_DELETED_TYPE) {
            map.putBoolean("deleted", true);
        }

        sendEvent("notification", map);

    }
}
