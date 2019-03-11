package com.github.musicode.xingepush;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import me.leolin.shortcutbadger.ShortcutBadger;

public class RNTXingePushModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {

    private final ReactApplicationContext reactContext;

    private int badge = 0;

    public RNTXingePushModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
        reactContext.addLifecycleEventListener(this);
        registerReceivers();
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
    public void setXiaomi(String appId, String appKey) {
        XGPushConfig.setMiPushAppId(reactContext, appId);
        XGPushConfig.setMiPushAppKey(reactContext, appKey);
    }

    @ReactMethod
    public void setMeizu(String appId, String appKey) {
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

    private void onBindTag(int code) {
        WritableMap map = Arguments.createMap();
        map.putInt("error", code);
        sendEvent("bindTag", map);
    }

    private void onUnbindTag(int code) {
        WritableMap map = Arguments.createMap();
        map.putInt("error", code);
        sendEvent("unbindTag", map);
    }

    private void onMessage(Intent intent) {

        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String customContent = intent.getStringExtra("customContent");

        WritableMap map = Arguments.createMap();
        map.putString("title", title);
        map.putString("content", content);

        addCustomContent(map, customContent);

        sendEvent("message", map);

    }

    private void onNotifaction(Intent intent) {

        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String customContent = intent.getStringExtra("customContent");
        boolean clicked = intent.getBooleanExtra("clicked", false);
        boolean deleted = intent.getBooleanExtra("deleted", false);
        boolean showed = intent.getBooleanExtra("showed", false);

        WritableMap map = Arguments.createMap();
        map.putBoolean("clicked", clicked);
        map.putBoolean("deleted", deleted);
        map.putBoolean("showed", showed);

        WritableMap body = Arguments.createMap();
        body.putString("title", title);
        body.putString("content", content);
        map.putMap("body", body);

        addCustomContent(map, customContent);

        sendEvent("notification", map);

    }

    private void addCustomContent(WritableMap map, String customContent) {
        if (customContent == null || customContent.isEmpty()) {
            return;
        }
        try {
            JSONObject json = new JSONObject(customContent);
            WritableMap body = Arguments.createMap();

            Iterator<String> iterator = json.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                // 貌似信鸽只支持字符串
                body.putString(key, json.getString(key));
            }
            map.putMap("custom_content", body);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onHostResume() {
        XGPushManager.onActivityStarted(getCurrentActivity());
    }

    @Override
    public void onHostPause() {
        XGPushManager.onActivityStoped(getCurrentActivity());
    }

    @Override
    public void onHostDestroy() {

    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onNewIntent(Intent intent) {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            // 后台运行时点击通知会调用
            activity.setIntent(intent);
        }
    }

    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.EVENT_BIND_TAG);
        intentFilter.addAction(Constant.EVENT_UNBIND_TAG);
        intentFilter.addAction(Constant.EVENT_MESSAGE);
        intentFilter.addAction(Constant.EVENT_NOTIFICATION);

        reactContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()){
                    case Constant.EVENT_BIND_TAG:
                        onBindTag(intent.getIntExtra("code", -1));
                        break;
                    case Constant.EVENT_UNBIND_TAG:
                        onUnbindTag(intent.getIntExtra("code", -1));
                        break;
                    case Constant.EVENT_MESSAGE:
                        onMessage(intent);
                        break;
                    case Constant.EVENT_NOTIFICATION:
                        onNotifaction(intent);
                        break;
                    default:
                        break;
                }

            }
        }, intentFilter);
    }

}
